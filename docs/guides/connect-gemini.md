# Connect Gemini on Android 16

If your phone is on Android 16 with AICore, anyfn registers itself as an `AppFunctionService` and Gemini discovers it like any first-party action — no extra wiring.

## Verify

1. Run a scan on the phone first; nothing to publish if the registry is empty.
2. Open anyfn → **Bridge** → look for **Android 16 AppFunctions**.
3. The label should read `published N functions`.
4. Open Google's Gemini app on the phone. Ask:

> "What can anyfn do?"

Gemini will list the tools registered by anyfn.

## What if it says "not supported"?

- Android < 16: AppFunctions doesn't exist. Use the MCP transport instead.
- Android 16 without AICore: some OEMs strip AICore. anyfn falls back to MCP — point your local agent at the bridge.
- The API is in private preview as of 2026-05; some devices receive it via Play Services updates first. Re-check after a Gemini app update.

## Limitations vs MCP

- **Authentication:** Gemini calls anyfn on-device; we trust the OS's caller identity. There is no shared-secret layer like LAN mode.
- **Streaming:** AppFunctions responses are single-shot. The MCP bridge supports SSE for progressive output; AppFunctions doesn't.
- **Confirmations:** destructive functions return the same `NeedsConfirmation` shape; Gemini surfaces it as a prompt to the user before re-calling.
