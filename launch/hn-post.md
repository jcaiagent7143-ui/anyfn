# Show HN

**Title:** Show HN: anyfn – Make every Android app agent-ready in 60 seconds

**URL:** https://github.com/jcaiagent7143-ui/anyfn

**Body:**

Hi HN,

I've been frustrated watching Google ship `@AppFunction` as the path forward for the agentic phone. The deal is: every Android developer rewrites their app, ships a new build, waits for users to update. We will be old before this happens. Spotify hasn't shipped a Google Assistant-friendly intent in three years; WhatsApp has zero.

anyfn is the alternative. It's an Android runtime that:

1. Walks every installed app on your phone, snapshots the UI through `AccessibilityService`, and asks an LLM "what callable things does this app expose?"
2. Persists the inferred function schemas + UI action paths in a local SQLite registry.
3. Re-publishes that registry as (a) a standard MCP server on `localhost:5174` and (b) native Android 16 `AppFunctionService` entries Gemini Nano can call.
4. When an agent calls a function, anyfn replays the UI path via UI Automator with structured error recovery and per-call destructive confirmation.

End result: from Claude Desktop on my Mac I can say "Search TikTok for sunset hiking, save the top 3 to my notes app" and it just works. Two unrelated apps. Zero developer cooperation.

A few things that might be of interest to this crowd:

- The Scanner is decoupled from the Invoker. Inference happens once with a real LLM (Claude Sonnet 4.6 or on-device Gemini Nano). Invocation is offline — no model in the hot path. A function discovered today still works tomorrow without a network connection.
- The whole thing is on-device. No telemetry, no analytics, no phone-home. The only network call anyfn makes is to your chosen LLM provider, at scan time, with the UI tree.
- Banking apps, password managers, and anything that sets `FLAG_SECURE` are unsupported by design. anyfn refuses to type credentials and refuses to work around the secure-screen flag. The list of incompatible apps is documented up front.
- MIT licensed. The repo includes a Tauri desktop companion stub for v0.2 (one-click install without `adb`), a marketing site, and 5 worked examples (TikTok, WhatsApp, Spotify, cross-app chain, Grab).

Known limitations: Android 16 AppFunctions is in private preview, so that path is feature-detected and may no-op on your specific device. The Anthropic backend costs ~$0.30 for a full scan of a 60-app phone (rescans are cheaper). LLM inference is non-deterministic — same phone, same app, different scan can return slightly different selectors. We let users pin overrides per function.

Source + APK + docs: https://github.com/jcaiagent7143-ui/anyfn

I'd particularly love feedback on:
- The threat model in [docs/safety/permissions-model.md](https://github.com/jcaiagent7143-ui/anyfn/blob/main/docs/safety/permissions-model.md). I tried to be honest about what anyfn can and cannot do.
- The split between MCP and AppFunctions. Should anyfn deprecate its own MCP server once AppFunctions is GA, or are they fundamentally different use cases (desktop agent vs on-device agent)?
- Apps you'd want to see in the first-party supported list.

Happy to answer questions.
