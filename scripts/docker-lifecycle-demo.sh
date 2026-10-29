#!/usr/bin/env bash
# Walks through the complete container lifecycle of the portal image and records every command.
#   scripts/docker-lifecycle-demo.sh [log-file]
# Prerequisite: ./mvnw -DskipTests package  (the image packages target/*.war)
set -uo pipefail
cd "$(dirname "$0")/.."

LOG=${1:-target/docker-lifecycle.log}
VERSION=$(./mvnw -q help:evaluate -Dexpression=project.version -DforceStdout)
REVISION=$(git rev-parse --short HEAD)
IMAGE=getdocs-ccrp
NAME=ccrp-demo
PORT=8086
VOLUME=ccrp-demo-data
PASSWORD=Demo@$(head -c 6 /dev/urandom | base64 | tr -dc 'A-Za-z0-9')

mkdir -p "$(dirname "$LOG")"; : > "$LOG"
step() { printf '\n## %s\n' "$*" | tee -a "$LOG"; }
run()  { printf '$ %s\n' "$*" | tee -a "$LOG"; eval "$@" 2>&1 | tee -a "$LOG"; return "${PIPESTATUS[0]}"; }
wait_healthy() {
  for _ in $(seq 1 60); do
    s=$(docker inspect --format '{{.State.Health.Status}}' "$NAME" 2>/dev/null)
    [[ $s == healthy ]] && { echo "health: $s" | tee -a "$LOG"; return 0; }
    sleep 2
  done
  echo "health: ${s:-unknown} (timed out)" | tee -a "$LOG"; return 1
}

docker rm -f "$NAME" >/dev/null 2>&1; docker volume rm "$VOLUME" >/dev/null 2>&1

step "0. Toolchain"
run "docker version --format 'client {{.Client.Version}} · server {{.Server.Version}}'"
run "ls -lh target/citizen-certificate-portal-*.war"

step "1. Build the image (multi-stage, layered, non-root)"
run "docker build --build-arg VERSION=$VERSION --build-arg REVISION=$REVISION --build-arg CREATED=$(date -u +%Y-%m-%dT%H:%M:%SZ) -t $IMAGE:$VERSION ." | grep -E '^\$|#[0-9]+ \[|naming to|DONE [0-9.]+s$' | tail -25

step "2. Tag"
run "docker tag $IMAGE:$VERSION $IMAGE:$REVISION"
run "docker tag $IMAGE:$VERSION $IMAGE:latest"
run "docker images $IMAGE --format 'table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.Size}}'"

step "3. Inspect the image"
run "docker image inspect $IMAGE:$VERSION --format 'user={{.Config.User}} ports={{json .Config.ExposedPorts}} volumes={{json .Config.Volumes}} entrypoint={{json .Config.Entrypoint}}'"
run "docker image inspect $IMAGE:$VERSION --format '{{range \$k, \$v := .Config.Labels}}{{\$k}}={{\$v}}{{println}}{{end}}' | grep opencontainers"
run "docker history $IMAGE:$VERSION --format 'table {{.Size}}\t{{.CreatedBy}}' | head -14 | cut -c1-120"

step "4. Run: port mapping 8086→8080, named volume, env configuration, restart policy"
run "docker volume create $VOLUME"
run "docker run -d --name $NAME -p $PORT:8080 -e APP_OFFICER_PASSWORD=$PASSWORD -e APP_ENVIRONMENT=docker -v $VOLUME:/var/lib/ccrp --restart unless-stopped $IMAGE:$VERSION"
wait_healthy
run "docker ps --filter name=$NAME --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'"

step "5. Use the running container"
run "curl -s http://localhost:$PORT/actuator/health"
run "curl -s http://localhost:$PORT/actuator/info | jq -c '{env: .app.environment, version: .build.version, commit: .git.commit.id}'"
# create a real request so persistence can be demonstrated later
jar=$(mktemp); csrf=$(curl -s -c "$jar" "http://localhost:$PORT/apply" | sed -n 's/.*name="_csrf" value="\([^"]*\)".*/\1/p' | head -1)
location=$(curl -s -b "$jar" -o /dev/null -w '%{redirect_url}' -X POST "http://localhost:$PORT/apply" \
  -F _csrf="$csrf" -F certificateType=BIRTH -F applicantName="Meera Iyer" -F dateOfBirth=2001-11-03 \
  -F email=meera.iyer@example.com -F phone=9123456780 -F nationalId="9876 5432 1012" \
  -F address="12, Lake View Apartments, Ring Road" -F district=Chennai -F pinCode=600004 \
  -F purpose="School admission for the new academic year." -F declaration=true \
  -F "document=@src/test/resources/e2e/documents/income-proof.pdf;type=application/pdf")
TRACKING=${location##*/}; rm -f "$jar"
echo "submitted application → $TRACKING" | tee -a "$LOG"

step "6. Logs, exec, stats"
run "docker logs --tail 8 $NAME | cut -c1-160"
run "docker exec $NAME id"
run "docker exec $NAME sh -c 'ls -la /var/lib/ccrp && du -sh /var/lib/ccrp/*'"
run "docker stats --no-stream --format 'table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}' $NAME"

step "7. Stop (SIGTERM → graceful shutdown), start, restart"
run "docker stop $NAME"
run "docker ps -a --filter name=$NAME --format 'table {{.Names}}\t{{.Status}}'"
run "docker logs --tail 4 $NAME | cut -c1-160"
run "docker start $NAME"
wait_healthy
run "docker restart $NAME"
wait_healthy
run "docker inspect --format 'restarts={{.RestartCount}} started={{.State.StartedAt}} health={{.State.Health.Status}}' $NAME"

step "8. Remove the container, then recreate it from the same image and volume: data survives"
run "docker rm -f $NAME"
run "docker ps -a --filter name=$NAME --format '{{.Names}}' | wc -l"
run "docker run -d --name $NAME -p $PORT:8080 -e APP_OFFICER_PASSWORD=$PASSWORD -v $VOLUME:/var/lib/ccrp $IMAGE:$VERSION"
wait_healthy
run "curl -s -X POST http://localhost:$PORT/api/v1/status -H 'Content-Type: application/json' -d '{\"trackingId\":\"$TRACKING\",\"email\":\"meera.iyer@example.com\"}' | jq -c '{trackingId, status, certificateType}'"

step "9. Clean up"
run "docker rm -f $NAME"
run "docker volume rm $VOLUME"
run "docker rmi $IMAGE:$REVISION"
run "docker images $IMAGE --format 'table {{.Repository}}\t{{.Tag}}\t{{.Size}}'"
echo; echo "Lifecycle log written to $LOG"
