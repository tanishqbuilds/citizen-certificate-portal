#!/usr/bin/env bash
# Creates a fresh, clean target node for Ansible (or recreates it with --recreate).
#   scripts/ansible-node.sh [--recreate]
# SSH: localhost:2222 (user ansible, key infra/ansible/.ssh/id_ed25519) · App: http://localhost:8095
set -euo pipefail
cd "$(dirname "$0")/.."
NAME=ccrp-node-1
KEY=infra/ansible/.ssh/id_ed25519

if [[ ! -f $KEY ]]; then
  mkdir -p "$(dirname "$KEY")"; chmod 700 "$(dirname "$KEY")"
  ssh-keygen -q -t ed25519 -N '' -C 'ccrp-ansible' -f "$KEY"
fi
cp "$KEY.pub" infra/ansible/node/authorized_keys

if [[ ${1:-} == --recreate ]]; then docker rm -f "$NAME" >/dev/null 2>&1 || true; fi
if docker inspect "$NAME" >/dev/null 2>&1; then echo "$NAME already exists"; exit 0; fi

docker build -q -t sanad/ansible-node:ubuntu-24.04 infra/ansible/node >/dev/null
# systemd inside Docker needs a writable cgroup and tmpfs mounts; --privileged keeps this lab node simple.
docker run -d --name "$NAME" --hostname "$NAME" --privileged --cgroupns=host \
  -v /sys/fs/cgroup:/sys/fs/cgroup:rw --tmpfs /run --tmpfs /run/lock \
  -p 2222:22 -p 8095:8080 \
  sanad/ansible-node:ubuntu-24.04 >/dev/null
for _ in $(seq 1 30); do
  ssh -q -o BatchMode=yes -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i "$KEY" -p 2222 ansible@127.0.0.1 true && break
  sleep 1
done
echo "$NAME is up: ssh -i $KEY -p 2222 ansible@127.0.0.1"
