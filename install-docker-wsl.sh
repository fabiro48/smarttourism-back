#!/bin/bash
set -e

echo "=== Adding Docker repository ==="
. /etc/os-release
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $VERSION_CODENAME stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

echo "=== Updating apt ==="
apt-get update -qq

echo "=== Installing Docker Engine ==="
apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-buildx-plugin

echo "=== Starting Docker daemon ==="
service docker start || true

echo "=== Verifying Docker ==="
docker --version
docker info | grep -E "Server Version|Containers"

echo "=== Exposing Docker socket on TCP 2376 ==="
# Expose docker socket via socat on port 2376 (different from Docker Desktop's 2375)
apt-get install -y -qq socat
nohup socat TCP-LISTEN:2376,fork,reuseaddr UNIX-CONNECT:/var/run/docker.sock &
echo "socat PID: $!"

echo "=== Done ==="
