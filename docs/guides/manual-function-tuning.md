# Manually tune a function

LLM inference gets you 80% of the way. The last 20% — sticky popups, region-specific UI, A/B experiments — is yours.

## When to tune

- An invocation fails repeatedly with `ELEMENT_NOT_FOUND` on a specific step.
- The function returns the wrong shape (e.g. lists when you wanted a single item).
- The function works but is slower than it could be (too many `WaitFor`s).

## Where to edit

Registry → tap function → **Edit ui_path**. The editor shows the inferred JSON and lets you change:

- **Selectors** — `by_resource_id`, `by_text`, `by_content_description`, `by_class_name`. Prefer resource id when available.
- **Step order** — drag to reorder. The executor stops at the first failing step.
- **Wait timeouts** — increase for slow apps, decrease for snappy ones.
- **Parameter schemas** — rename, change type, mark optional.

## Example: WhatsApp send_message

The inferred path looks like this:

```json
[
  { "type": "Click", "selector": { "by_text": "New chat" } },
  { "type": "TypeText", "selector": { "by_text": "Search name or number" }, "value_from_param": "recipient" },
  { "type": "Click", "selector": { "by_text": "{recipient_param_value}" } },
  { "type": "TypeText", "selector": { "by_resource_id": "com.whatsapp:id/entry" }, "value_from_param": "body" },
  { "type": "Click", "selector": { "by_content_description": "Send" } }
]
```

The third step won't work — `{recipient_param_value}` is a literal string. Replace it with a stable identifier:

```json
{ "type": "Click", "selector": { "by_resource_id": "com.whatsapp:id/contact_row_container" } }
```

Save. The function will now resolve the first contact result instead of trying to find a button literally named after the recipient.

## Adding a wait

After clicking Send, WhatsApp animates the message into the thread. To return a useful result, wait for it:

```json
{ "type": "WaitFor", "selector": { "by_resource_id": "com.whatsapp:id/message_sent_indicator" }, "timeout_ms": 3000 }
```

## Sticky overrides

Your edits are sticky. On the next rescan, anyfn diffs the new inference against your overrides and keeps yours unless you clear them (long-press → **Reset to inferred**).
