#!/usr/bin/env bash
# Starts the CI/CD toolchain. Generates ci/.env with random secrets on first run.
set -euo pipefail
cd "$(dirname "$0")/.."

ENV_FILE=ci/.env
rand() { head -c 24 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 20; }

if [[ ! -f $ENV_FILE ]]; then
  umask 077
  cat > "$ENV_FILE" <<ENV
JENKINS_ADMIN_ID=admin
JENKINS_ADMIN_PASSWORD=$(rand)
TOMCAT_DEPLOYER_PASSWORD=$(rand)
APP_OFFICER_PASSWORD=Officer@$(rand | head -c 8)
CCRP_REPO_URL=https://github.com/tanishqbuilds/citizen-certificate-portal.git
GITHUB_TOKEN=${GITHUB_TOKEN:-none}
DOCKER_GID=$(stat -c %g /var/run/docker.sock)
ENV
  echo "Created $ENV_FILE with fresh secrets."
fi

docker compose -f ci/docker-compose.yml --env-file "$ENV_FILE" up -d --build "$@"

echo "Waiting for Jenkins…"
for _ in $(seq 1 90); do
  if curl -fsS -o /dev/null http://localhost:8090/login; then break; fi
  sleep 2
done
docker compose -f ci/docker-compose.yml --env-file "$ENV_FILE" ps
cat <<INFO

  Jenkins   http://localhost:8090   (user: admin, password: see JENKINS_ADMIN_PASSWORD in $ENV_FILE)
  Tomcat    http://localhost:8888   (staging: /certportal-staging, production: /certportal-production)
  Selenium  http://localhost:4444/ui  ·  live browser: http://localhost:7900 (password: secret)
INFO
