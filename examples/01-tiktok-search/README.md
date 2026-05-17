# Example: TikTok search from Claude Desktop

End-to-end: ask Claude on your Mac to find sunset videos on the phone's TikTok.

## What you'll see

The phone autonomously opens TikTok, taps the search icon, types your query, presses enter, and Claude reads back the top results.

## Prerequisites

- anyfn installed and scanned (TikTok must be in the registry)
- Claude Desktop running with the anyfn MCP server wired up — see [docs/guides/connect-claude-desktop.md](../../docs/guides/connect-claude-desktop.md)

## Prompt

See [`prompt.txt`](prompt.txt).

Paste into Claude Desktop and run.

## Expected output

```
1. Golden hour above Half Dome | @yosemiteclimber | 2.4M views
2. Sunset trail run in Kyoto | @keiko.runs | 890k views
3. The colors of Santorini at dusk | @greekisland | 1.1M views
…
```

## What the function looks like in the registry

```json
{
  "name": "tiktok_search",
  "description": "Search TikTok for videos matching the query and return the top results.",
  "parameters": [
    { "name": "query", "type": "string", "description": "Search terms", "required": true }
  ],
  "ui_path": [
    { "type": "Click", "selector": { "by_content_description": "Search" } },
    { "type": "TypeText", "selector": { "by_resource_id": "com.zhiliaoapp.musically:id/search_box" }, "value_from_param": "query" },
    { "type": "PressEnter" },
    { "type": "WaitFor", "selector": { "by_text": "Top" }, "timeout_ms": 5000 }
  ],
  "confidence": 0.91,
  "destructive": false
}
```

## Files

- [`prompt.txt`](prompt.txt) — paste this into Claude
- [`claude-config.json`](claude-config.json) — the MCP block to merge into `claude_desktop_config.json`
- [`demo.gif`](demo.gif) — placeholder for the 5-second recording (replace once you've built the APK and recorded a real run)
