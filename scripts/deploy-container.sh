#!/usr/bin/env bash
# Replaces the running portal container with a fresh one from <image> and rolls back automatically
# if the new container does not become healthy.
#
#   scripts/deploy-container.sh <image> [name] [host-port]
#
# Env: OFFICER_PASSWORD (required), APP_ENVIRONMENT (default production), DOCKER_NETWORK (default ccrp-ci)
set -euo pipefail

IMAGE=${1:?usage: deploy-container.sh <image> [name] [host-port]}
NAME=${2:-ccrp-prod}
PORT=${3:-8085}
ENVIRONMENT=${APP_ENVIRONMENT:-production}
NETWORK=${DOCKER_NETWORK:-ccrp-ci}
VOLUME="${NAME}-data"
: "${OFFICER_PASSWORD:?OFFICER_PASSWORD must be set}"

log() { printf '==> %s\n' "$*"; }

start() {
  docker run -d --name "$NAME" \
    --restart unless-stopped \
    --network "$NETWORK" \
    -p "$PORT:8080" \
    -v "$VOLUME:/var/lib/ccrp" \
    -e APP_ENVIRONMENT="$ENVIRONMENT" \
    -e APP_OFFICER_PASSWORD="$OFFICER_PASSWORD" \
    --label "ccrp.deployed-by=${BUILD_TAG:-manual}" \
    "$1" >/dev/null
}

healthy() {
  for _ in $(seq 1 60); do
    case "$(docker inspect --format '{{.State.Health.Status}}' "$NAME" 2>/dev/null)" in
      healthy) return 0 ;;
      unhealthy) return 1 ;;
    esac
    sleep 2
  done
  return 1
}

PREVIOUS=$(docker inspect --format '{{.Config.Image}}' "$NAME" 2>/dev/null || true)
log "Deploying $IMAGE as $NAME on :$PORT (previous: ${PREVIOUS:-none})"

docker pull -q "$IMAGE" >/dev/null 2>&1 || log "pull skipped (image is local)"
docker rm -f "$NAME" >/dev/null 2>&1 || true
start "$IMAGE"

if healthy; then
  log "Healthy: $(docker inspect --format '{{.Config.Image}} · started {{.State.StartedAt}}' "$NAME")"
  exit 0
fi

log "New container is NOT healthy, last log lines:"
docker logs --tail 40 "$NAME" || true
if [[ -n $PREVIOUS ]]; then
  log "Rolling back to $PREVIOUS"
  docker rm -f "$NAME" >/dev/null
  start "$PREVIOUS"
  healthy && log "Rollback healthy: $PREVIOUS" || log "Rollback also unhealthy: manual action required"
fi
exit 1
