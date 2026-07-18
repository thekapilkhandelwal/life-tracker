#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

export PORT="${PORT:-8080}"
export DEMO_LOGIN_ENABLED="${DEMO_LOGIN_ENABLED:-true}"
export COOKIE_SECURE="${COOKIE_SECURE:-false}"
export COOKIE_SAME_SITE="${COOKIE_SAME_SITE:-Lax}"
export FRONTEND_URL="${FRONTEND_URL:-http://localhost:5173}"

echo "Starting API on :$PORT (Mongo at localhost:27017 required)"
(cd "$ROOT/backend" && mvn -q spring-boot:run) &
API_PID=$!

cleanup() {
  kill "$API_PID" 2>/dev/null || true
}
trap cleanup EXIT

sleep 8
echo "Starting frontend on :5173"
(cd "$ROOT/frontend" && npm run dev)
