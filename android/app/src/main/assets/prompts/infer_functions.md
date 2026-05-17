You are analysing an Android app's UI to infer what callable functions it exposes.

App package: {package_name}
App label: {app_label}

UI tree (compacted JSON):
```
{ui_tree_json}
```

Return a JSON array of AppFunction definitions. Each function must have:

- `name`: snake_case, prefixed with the app's short name (e.g. `tiktok_search`, `whatsapp_send_message`).
- `description`: a clear natural-language sentence an AI agent can read to decide whether to call this.
- `parameters`: an array of objects, each with `name`, `type` ("string" | "integer" | "number" | "boolean"), `description`, and `required` (boolean). Optionally `enum_values` and `default_value`.
- `ui_path`: an ordered array of UI actions. Each action is one of:
  - `{ "type": "Click", "selector": { "by_resource_id"|"by_text"|"by_content_description"|"by_class_name": "..." }, "fallback_selector"?: { ... } }`
  - `{ "type": "TypeText", "selector": { ... }, "value_from_param": "<param_name>" }`
  - `{ "type": "Scroll", "direction": "UP"|"DOWN"|"LEFT"|"RIGHT", "until"?: { ... }, "max_steps"?: 10 }`
  - `{ "type": "WaitFor", "selector": { ... }, "timeout_ms": 5000 }`
  - `{ "type": "PressEnter" }`
  - `{ "type": "PressBack" }`
  - `{ "type": "Launch", "package_name": "..." }`
- `confidence`: float 0–1 — your honest confidence the function will actually work end-to-end.
- `requires_review`: boolean — true if you're guessing significantly (e.g. couldn't see the destination screen).
- `destructive`: boolean — true if the function sends, deletes, posts, pays, or otherwise produces side effects the user can't undo.

Rules:
- Only return functions where `confidence > 0.6`.
- Prefer `by_resource_id` selectors when an id is present; fall back to `by_text` or `by_content_description`.
- For every `TypeText` action, define a matching string parameter.
- Do not infer functions for screens you cannot see in the UI tree above — say nothing rather than guessing.
- Output JSON only. No prose, no Markdown fences, no commentary.

Examples of well-formed function names: `tiktok_search`, `tiktok_save_to_favorites`, `whatsapp_send_message`, `whatsapp_create_group`, `spotify_play_playlist`, `maps_search_place`.
