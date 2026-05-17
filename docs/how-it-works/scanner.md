# Scanner

The discovery side. Source: `android/app/src/main/kotlin/dev/anyfn/scanner/`.

## Inputs

- The user's installed app list (from `PackageManager`).
- Live `AccessibilityNodeInfo` trees of those apps, sampled while each is in the foreground.
- An LLM backend (Claude via Anthropic API, or Gemini Nano via AICore).

## Outputs

A list of [`AppFunction`](../../android/core/src/main/kotlin/dev/anyfn/core/model/AppFunction.kt) records, written into the function registry.

## Steps

1. **List apps** — `AppDiscovery.listInstalledApps()`. Filters out system apps unless the user opted in. Excludes apps with no launcher intent.
2. **For each app**:
   1. Launch it (`AppDiscovery.launch`), wait ~2.5s for the splash to settle.
   2. Subscribe to the AccessibilityService and grab up to 3 window snapshots, 1.5s apart. Multiple snapshots capture different screens (e.g. home → search results).
   3. Compact each snapshot into a JSON `UiTree` via `UITreeExtractor` — caps depth, child counts, and text length so prompts stay under ~4 KB.
   4. Send each compacted tree to the chosen LLM with the `infer_functions.md` prompt.
   5. Merge the returned function lists, keep the highest-confidence entry per `name`.
   6. Persist to Room via `FunctionRepository.upsertAll`.
3. **Done** — emit a `ScanProgress.Done` event the UI subscribes to.

## Determinism

The Scanner is **not** deterministic — same phone, same app, different scan can return different selectors. We accept this because:

- Apps themselves shuffle DOM-equivalent UIs across versions.
- LLMs do not return byte-identical output across requests.

We mitigate by:

- Deduplicating by function name within a single scan.
- Letting the user pin overrides per function (sticky across rescans).
- Storing the inference confidence alongside each function so the Invoker can prefer high-confidence steps.

## Costs

With Claude Sonnet 4.6 and the default prompt:

- ~1,500 input tokens per UI snapshot
- ~400 output tokens per app (3–6 functions)
- For 60 apps with 3 snapshots each: ~270K input + ~24K output ≈ **$0.30 per full scan**

Gemini Nano on-device is free but slower (~5s per app on a Pixel 8 Pro vs 2s for Sonnet).

## Failure modes

| Symptom | Cause | Fix |
|---|---|---|
| `Accessibility service is not connected` | The user disabled the service mid-scan | Re-enable, rescan |
| `no UI snapshots captured for X` | App crashed on launch, or its splash is `FLAG_SECURE` | Skip the app; surface in scan log |
| Inference returns `[]` | LLM saw nothing actionable in the UI tree | Often means the app showed a login wall first |
| `429` from Anthropic | Concurrent scans against the same key | We rate-limit to 4 in-flight; further calls back off |
