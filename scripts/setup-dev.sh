#!/usr/bin/env bash
# Sets up the development environment for working on anyfn.
# Run once after cloning. Idempotent.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> anyfn dev setup"

if ! command -v java >/dev/null 2>&1; then
  echo "  ✘ JDK not found. Install JDK 17 (Temurin recommended)."
  exit 1
fi

JAVA_MAJOR=$(java -version 2>&1 | awk -F[\".] '/version/ { print $2 }')
if [ "$JAVA_MAJOR" != "17" ]; then
  echo "  ⚠ JDK $JAVA_MAJOR detected — anyfn needs JDK 17."
fi

if [ -z "${ANDROID_HOME:-}" ] && [ ! -f android/local.properties ]; then
  echo "  ⚠ ANDROID_HOME not set and android/local.properties missing."
  echo "    Set ANDROID_HOME or create android/local.properties with 'sdk.dir=/path/to/sdk'."
fi

# Install a pre-commit hook that runs ktlint on staged Kotlin files.
HOOK=".git/hooks/pre-commit"
if [ -d .git ]; then
  cat > "$HOOK" <<'EOF'
#!/usr/bin/env bash
set -e
STAGED=$(git diff --cached --name-only --diff-filter=ACM | grep -E '\.(kt|kts)$' || true)
if [ -z "$STAGED" ]; then exit 0; fi
echo "Running ktlint on staged Kotlin files..."
(cd android && ./gradlew ktlintCheck -q)
EOF
  chmod +x "$HOOK"
  echo "  ✓ pre-commit hook installed"
fi

echo "==> done"
