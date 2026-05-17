# Inference

Source: `android/app/src/main/kotlin/dev/anyfn/inference/`.

The Scanner asks "what callable functions does this app expose?" The answer comes from an LLM. This page documents how the question is asked and how the answer is parsed.

## Backends

| Backend | Where it runs | Cost | Latency (typical) | Status |
|---|---|---|---|---|
| `AnthropicBackend` | Anthropic's cloud, Sonnet 4.6 by default | ~$0.005 per app | 1.5–2.5s | Stable |
| `GeminiNanoBackend` | On-device via AICore | Free | 4–6s on Pixel 8 Pro | Feature-detected, v0.2 |
| Local MLC-LLM | On-device, Llama 3.2 3B | Free | 6–10s | v0.3 |

`LlmBackendSelector` routes to the user's chosen backend and falls back to the other if the chosen one isn't ready (no API key, no AICore, no model file).

## Prompt

Stored at `app/src/main/assets/prompts/infer_functions.md`. The template variables `{package_name}`, `{app_label}`, `{ui_tree_json}` are substituted at render time.

The prompt asks for:

- `name` — snake_case, app-prefixed
- `description` — a natural-language sentence
- `parameters` — JSON-Schema-shaped array
- `ui_path` — ordered action list
- `confidence` — float 0–1
- `requires_review` / `destructive` — boolean flags

We tell the model to **only return functions with confidence > 0.6** and to **output JSON only**. Empirically Sonnet honours both consistently; Gemini Nano occasionally wraps the output in markdown fences, which `parseFunctions` strips.

## UI tree compaction

The compaction rules in `UITreeExtractor.kt` are the second half of the prompt. We drop:

- Invisible nodes with no signal
- Pure layout `Group` wrappers
- Nodes deeper than 14 levels
- Children past index 24 of any container
- Text past 80 characters

These exist so the prompt fits comfortably in the model's context. A typical TikTok home screen produces a ~2 KB tree before compaction and ~700 bytes after.

## Parsing

`AnthropicBackend.parseFunctions` strips optional markdown fences, parses the result as a JSON array, then validates each entry against the [`AppFunction`](../../android/core/src/main/kotlin/dev/anyfn/core/model/AppFunction.kt) constructor. Entries that throw are dropped, not propagated — one bad function should not kill an entire scan.

## Pinning the model

Edit `AnthropicBackend.DEFAULT_MODEL` if you want a different Claude version. We pin Sonnet 4.6 because:

- It's the cheapest Claude model that reliably returns well-formed JSON of this shape.
- Sonnet 3.x and earlier sometimes return Markdown-wrapped responses despite the instruction.
- Opus is overkill — the task is simple structured extraction, not reasoning.

## Privacy

Inference is the **only** time anyfn sends UI content off-device, and only if you chose the Anthropic backend. Gemini Nano runs entirely on-device. Either way, UI snapshots are never persisted off-device by anyfn itself — what the LLM provider logs is their policy, not ours.
