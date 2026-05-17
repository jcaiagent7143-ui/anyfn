# Write a custom inference prompt

The default prompt lives at `android/app/src/main/assets/prompts/infer_functions.md`. You can override it without forking — anyfn reads `/sdcard/Android/data/dev.anyfn/files/prompts/infer_functions.md` first if present.

## When to override

- You want functions named in a different convention (camelCase, dotted).
- You want the inferer to ignore certain UI patterns (ads, in-app surveys).
- You want richer parameter schemas (e.g. accept lists by default).

## How to override

1. Author your prompt as `infer_functions.md`.
2. Push it to the device:

```bash
adb push infer_functions.md /sdcard/Android/data/dev.anyfn/files/prompts/
```

3. Restart anyfn (or rescan a single app).

## Required outputs

Whatever prompt you ship, the LLM **must** return a JSON array where each element has the keys documented in [api-reference/mcp-server.md](../api-reference/mcp-server.md). Malformed entries are silently dropped.

## Few-shot examples

Adding 1–2 example functions inline dramatically improves consistency. Pin your most-used app first:

```markdown
Example output for TikTok:

[
  {
    "name": "tiktok_search",
    "description": "Search TikTok for videos matching the query.",
    "parameters": [ { "name": "query", "type": "string", "description": "Search terms", "required": true } ],
    "ui_path": [ ... ],
    "confidence": 0.9
  }
]
```

Few-shot examples cost ~200 tokens but reliably improve inferred function quality.

## Sharing prompts

If you tune a prompt that works well, contribute it back as a PR to `inference-prompts/contrib/<your-handle>/`. We don't curate this folder beyond ensuring it's legal — community wisdom lives there.
