#!/usr/bin/env bash
# Post-deployment smoke test: health, version, environment and the public pages.
#   scripts/smoke-test.sh <base-url> [expected-version] [expected-environment]
set -euo pipefail
BASE=${1:?usage: smoke-test.sh <base-url> [expected-version] [expected-environment]}
EXPECTED_VERSION=${2:-}
EXPECTED_ENV=${3:-}

fail() { echo "FAIL: $*" >&2; exit 1; }
row()  { printf '  %-40s %s\n' "$1" "$2"; }

health=$(curl -fsS "$BASE/actuator/health") || fail "health endpoint unreachable"
[[ $health == *'"status":"UP"'* ]] || fail "health is not UP: $health"
row "GET /actuator/health" "UP"

info=$(curl -fsS "$BASE/actuator/info") || fail "info endpoint unreachable"
version=$(jq -r '.build.version' <<<"$info")
environment=$(jq -r '.app.environment' <<<"$info")
commit=$(jq -r '.git.commit.id // "n/a"' <<<"$info")
row "GET /actuator/info" "version=$version environment=$environment commit=$commit"
[[ -z $EXPECTED_VERSION || $version == "$EXPECTED_VERSION" ]] || fail "expected version $EXPECTED_VERSION, got $version"
[[ -z $EXPECTED_ENV || $environment == "$EXPECTED_ENV" ]] || fail "expected environment $EXPECTED_ENV, got $environment"

home=$(curl -fsS "$BASE/") || fail "landing page unreachable"
[[ $home == *"without the queue"* ]] || fail "landing page content missing"
row "GET /" "200, content OK"

apply=$(curl -fsS "$BASE/apply") || fail "apply page unreachable"
[[ $apply == *'data-testid="apply-form"'* ]] || fail "apply form missing"
row "GET /apply" "200, form renders"

code=$(curl -s -o /dev/null -w '%{http_code}' "$BASE/officer")
[[ $code == 302 ]] || fail "/officer should redirect to login, got $code"
row "GET /officer (anonymous)" "302 → sign-in"

echo "Smoke test passed for $BASE"
