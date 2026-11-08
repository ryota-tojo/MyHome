#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
docker compose up -d postgres
docker compose build web
docker compose up -d --force-recreate web
docker compose ps
