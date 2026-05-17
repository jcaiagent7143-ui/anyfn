# Grant permissions

anyfn needs three permissions to work. The onboarding flow walks you through them; this page is the reference.

## 1. Accessibility (required)

**Why anyfn needs it:** reading the running app's UI tree so the LLM can describe it, and to detect popups during invocation.

**What anyfn does with it:**
- Reads the live `AccessibilityNodeInfo` tree from foreground windows.
- Sends compacted UI trees to the LLM you chose (Claude or Gemini Nano), only during a scan.

**What anyfn does NOT do with it:**
- No background reads when no scan or invocation is in flight.
- No keystroke logging — typing is done via UI Automator, not by recording key events.
- No telemetry. UI snapshots are never sent anywhere except the LLM provider you configured.

**Granting:** Settings → Accessibility → Installed apps → **anyfn** → toggle on.

## 2. Notifications (optional, recommended)

**Why:** the MCP bridge runs as a foreground service and posts a low-importance notification so Android doesn't kill it.

**Granting:** Android prompts on first bridge start. Pick "Allow".

## 3. QUERY_ALL_PACKAGES (declared at install time)

**Why:** enumerating installed apps. Android requires this to be declared and the user is informed of it at install time.

We declare this with `tools:ignore="QueryAllPackagesPermission"` because it is core to the product — without it, anyfn can't discover apps to scan. If this is a dealbreaker for you, don't install.

## Revoking

You can revoke any of these at any time from Android Settings. anyfn will surface a clear error and offer to re-prompt.
