#!/usr/bin/env bash
# Builds a release APK suitable for attaching to a GitHub Release.
# Requires android/keystore.properties to exist with valid signing config.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/android"

if [ ! -f keystore.properties ]; then
  echo "ERROR: android/keystore.properties not found. See docs/getting-started/build-from-source.md."
  exit 1
fi

./gradlew :app:assembleRelease --no-daemon
APK=$(ls -t app/build/outputs/apk/release/*.apk | head -n 1)
SHA=$(shasum -a 256 "$APK" | awk '{print $1}')

echo
echo "==> Built $APK"
echo "    SHA-256: $SHA"
echo "    Size:    $(du -h "$APK" | cut -f1)"
