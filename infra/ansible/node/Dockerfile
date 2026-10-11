# A *clean* Ubuntu 24.04 target node for Ansible: systemd as PID 1, SSH and Python only.
# Everything the portal needs (Java, user, folders, config, service, release) is installed by Ansible.
# Stand-in for a VM; run with scripts/ansible-node.sh (needs --privileged for systemd in Docker).
FROM ubuntu:24.04

ENV container=docker DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
 && apt-get install -y --no-install-recommends systemd systemd-sysv openssh-server python3 sudo ca-certificates \
 && rm -rf /var/lib/apt/lists/* \
 # trim units that make no sense in a container
 && rm -f /lib/systemd/system/multi-user.target.wants/* /etc/systemd/system/*.wants/* \
          /lib/systemd/system/local-fs.target.wants/* /lib/systemd/system/sockets.target.wants/*udev* \
          /lib/systemd/system/sockets.target.wants/*initctl* /lib/systemd/system/sysinit.target.wants/systemd-tmpfiles-setup* \
 && systemctl enable ssh \
 # Ansible control account: key-only SSH, passwordless sudo
 && useradd --create-home --shell /bin/bash ansible \
 && echo 'ansible ALL=(ALL) NOPASSWD:ALL' > /etc/sudoers.d/ansible && chmod 0440 /etc/sudoers.d/ansible \
 && mkdir -p /home/ansible/.ssh && chmod 700 /home/ansible/.ssh \
 && sed -i 's/^#\?PasswordAuthentication.*/PasswordAuthentication no/; s/^#\?PermitRootLogin.*/PermitRootLogin no/' /etc/ssh/sshd_config

COPY authorized_keys /home/ansible/.ssh/authorized_keys
RUN chown -R ansible:ansible /home/ansible/.ssh && chmod 600 /home/ansible/.ssh/authorized_keys

STOPSIGNAL SIGRTMIN+3
EXPOSE 22 8080
CMD ["/sbin/init"]
