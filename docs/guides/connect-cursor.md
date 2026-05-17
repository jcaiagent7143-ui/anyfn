# Connect Cursor

Cursor speaks MCP natively — no helper binary needed.

## Steps

1. **Start the bridge on the phone** — anyfn → **Bridge** → **Start**.
2. **Forward over USB**: `adb forward tcp:5174 tcp:5174`.
3. In Cursor: **Settings → Features → MCP → Add new MCP server**.
4. Configure:
   - **Name:** `anyfn`
   - **Type:** `websocket`
   - **URL:** `ws://localhost:5174/ws`
5. Save. The status pill should turn green.

## Using it

In any Cursor chat with Agent mode on, the anyfn tools appear under your usual tool toggles. Ask:

> Search TikTok for 'sunset hiking', save the top 3 to my notes app.

Cursor will plan a sequence of anyfn calls and execute them. You'll see the phone drive both apps.

## LAN mode (no USB)

If you don't want a cable:

1. In anyfn → Settings, enable **LAN connections** and set a **Shared secret**.
2. Find the phone's LAN IP: anyfn → Bridge → tap the endpoint to reveal `0.0.0.0` mode.
3. In Cursor, use `ws://<phone-ip>:5174/ws` and add a header `x-anyfn-secret: <your-secret>` (Cursor MCP config supports custom headers in `headers`).

LAN mode requires the phone and laptop on the same Wi-Fi.
