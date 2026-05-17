#!/usr/bin/env bash
# Forwards the MCP bridge port from a connected Android device to localhost
# so desktop agents (Claude Desktop, Cursor, …) can reach it over WebSocket.
set -euo pipefail

PORT="${ANYFN_PORT:-5174}"

if ! command -v adb >/dev/null 2>&1; then
  echo "ERROR: adb not found. Install Android platform-tools."
  exit 1
fi

if ! adb get-state >/dev/null 2>&1; then
  echo "ERROR: no device authorised. Connect a phone via USB with developer mode on."
  exit 1
fi

adb forward tcp:"$PORT" tcp:"$PORT" >/dev/null
echo "==> forwarded device:$PORT → localhost:$PORT"
echo "    ws://localhost:$PORT/ws is now the MCP endpoint."
