# Connect Claude Desktop

The fastest way to drive anyfn from an AI agent on your laptop.

## 1. Start the bridge on the phone

Open anyfn → **Bridge** → **Start**. The screen shows `ws://127.0.0.1:5174/ws`.

## 2. Forward the port over USB

Plug the phone in, then on your Mac:

```bash
adb forward tcp:5174 tcp:5174
```

## 3. Wire Claude Desktop

Edit `~/Library/Application Support/Claude/claude_desktop_config.json` (Windows: `%APPDATA%\Claude\claude_desktop_config.json`):

```json
{
  "mcpServers": {
    "anyfn": {
      "command": "wscat",
      "args": ["-c", "ws://localhost:5174/ws"]
    }
  }
}
```

Don't have `wscat`? Install it: `npm install -g wscat`.

Restart Claude Desktop. The 🧩 icon in the input box should list **anyfn** with the function count from your registry.

## 4. Try it

In any chat, type:

> List the anyfn tools you can call.

Then:

> Use the anyfn tool `tiktok_search` with query="sunset".

You'll see Claude call the tool, the phone will visibly drive TikTok, and the function returns the top results.

## Troubleshooting

- **"Cannot connect to MCP server"** — port forward isn't active. Re-run `adb forward tcp:5174 tcp:5174`.
- **"anyfn is unauthorized"** — you set a shared secret in Settings but didn't put it in the config. Add `"env": { "ANYFN_SECRET": "your-secret" }` and use a custom command, or clear the secret.
- **Phone screen locks mid-call** — disable screen lock during testing, or wake the phone manually. anyfn cannot unlock the device.
