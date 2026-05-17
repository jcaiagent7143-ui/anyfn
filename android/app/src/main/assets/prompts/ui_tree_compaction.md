# UI tree compaction rules

These rules are enforced in `UITreeExtractor.kt`. They exist so the inference prompt stays under ~4 KB while preserving enough signal for the LLM to recognise interactive elements.

1. Drop nodes that are invisible, non-clickable, non-editable, non-scrollable, AND have no text AND no content description. Descend into their children regardless.
2. For each retained node, emit:
   - `type` — the last segment of the class name (e.g. `Button`, `EditText`, `RecyclerView`).
   - `id` — the segment after `/` in `viewIdResourceName`, when present.
   - `text` and `contentDescription` — trimmed, truncated to 80 chars with an ellipsis.
   - `clickable`, `editable`, `scrollable` — only when true.
   - `bounds` — `left,top,right,bottom` for layout disambiguation.
3. Cap tree depth at 14. Beyond that, descendants are dropped.
4. Cap children per node at 24. Lists with more children typically repeat the same template, so the first 24 are enough.
5. Collapse pure-layout `Group` nodes whose only child carries all the signal.
