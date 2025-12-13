#!/usr/bin/env bash
set -euo pipefail

TEAM_NAME=localtest
HOST_PORT=9001
APP_JWT_SECRET=$(openssl rand -hex 64)

export TEAM_NAME
export HOST_PORT
export APP_JWT_SECRET

docker compose -f docker-compose.prod.yml down --remove-orphans || true
docker compose -f docker-compose.prod.yml build 
docker compose -f docker-compose.prod.yml up -d

docker logs -f "${TEAM_NAME}-app"
