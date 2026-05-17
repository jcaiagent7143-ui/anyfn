#!/usr/bin/env bash
# Renders release notes for a tag by extracting the matching section from
# CHANGELOG.md. Used by the release.yml workflow.
set -euo pipefail

TAG="${1:?usage: generate-release-notes.sh <tag>}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CHANGELOG="$ROOT/CHANGELOG.md"
VERSION="${TAG#v}"

awk -v ver="$VERSION" '
  $0 ~ "^## \\[" ver "\\]" { capture=1; print; next }
  capture && $0 ~ "^## " { exit }
  capture { print }
' "$CHANGELOG"
