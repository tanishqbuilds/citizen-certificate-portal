#!/usr/bin/env bash
# Runs ansible-playbook from infra/ansible with the project's configuration.
#   scripts/ansible.sh site.yml [extra ansible-playbook args]
#   scripts/ansible.sh site.yml --check --diff
# Ansible comes from $ANSIBLE_VENV (default ~/.local/share/ccrp-ansible-venv), else from PATH.
set -euo pipefail
cd "$(dirname "$0")/../infra/ansible"
VENV=${ANSIBLE_VENV:-$HOME/.local/share/ccrp-ansible-venv}
PLAYBOOK=$VENV/bin/ansible-playbook; [[ -x $PLAYBOOK ]] || PLAYBOOK=ansible-playbook
[[ -f .vault-pass ]] || { echo "infra/ansible/.vault-pass is missing (see docs/09-configuration-management.md)" >&2; exit 2; }
export ANSIBLE_CONFIG=$PWD/ansible.cfg ANSIBLE_FORCE_COLOR=${ANSIBLE_FORCE_COLOR:-0}
# Ansible needs blocking stdio; </dev/null avoids issues when run from CI or non-interactive shells.
exec "$PLAYBOOK" "$@" </dev/null
