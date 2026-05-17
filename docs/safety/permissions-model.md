# Permissions model

anyfn is a power tool. This page explains exactly what it can do and why.

## The threat model

anyfn lives behind your Android lock screen. Anyone who can unlock your phone, plug it into a laptop, and run `adb forward` can drive every function anyfn knows about. That is the same threat as "anyone who can unlock your phone can use your phone." We do not provide a stronger guarantee than that.

If your threat model includes adversaries with physical device access, **do not install anyfn** on a device that holds anything those adversaries should not see.

## What permissions anyfn has

- **AccessibilityService** — read the foreground app's UI tree, dispatch gestures. Granted explicitly via Settings. Revocable.
- **QUERY_ALL_PACKAGES** — list installed apps. Granted at install time. Visible in Android's app info.
- **FOREGROUND_SERVICE / FOREGROUND_SERVICE_SPECIAL_USE** — keep the MCP bridge alive. Visible in the notification shade as an ongoing notification.
- **INTERNET** — call the LLM provider you chose for inference. Used only at scan time.
- **POST_NOTIFICATIONS** — show the bridge status.

## What permissions anyfn does NOT request

- Microphone, camera, location, contacts, calendar — none.
- `RECORD_AUDIO`, `READ_PHONE_STATE`, `READ_SMS`, `READ_CALL_LOG` — none.
- `MANAGE_EXTERNAL_STORAGE` — none.
- Any "system" permissions that require root or OEM signing — none.

If a future release needs a new permission, the changelog will say so loudly.

## Data anyfn writes to disk

- Function registry → `data/data/dev.anyfn/databases/anyfn.db` (private)
- Settings + API key → `data/data/dev.anyfn/shared_prefs/` (private)
- Debug-mode UI dumps → `/sdcard/Android/data/dev.anyfn/files/dumps/` (only when debug mode is on)
- Foreground notification text — visible to anyone who can see the lock screen

## Data anyfn sends over the network

- During a scan, with the Anthropic backend: compacted UI tree JSON + your prompt + your API key → `api.anthropic.com`. Anthropic's logging policy applies.
- During a scan, with the Gemini Nano backend: nothing leaves the device.
- During invocation: nothing leaves the device, ever.
- The MCP bridge accepts connections on `127.0.0.1:5174` by default; LAN mode broadens this but requires a shared secret.

## What anyfn refuses to do

- Type passwords. The Invoker has no path for entering credential-shaped strings into known-credential fields.
- Bypass `FLAG_SECURE`. Banking apps, password managers, and 2FA prompts are invisible to anyfn by design.
- Run when the screen is off or locked. We don't wake the device or unlock it.
- Phone home. Zero analytics, zero crash reporters, zero feature-flag services.

See [what-anyfn-cannot-do.md](what-anyfn-cannot-do.md) for the full incompatibility list.
