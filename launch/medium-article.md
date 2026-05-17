# How I made every Android app agent-ready in a weekend

*Published 2026-05-17 on Medium and dev.to. ~1,500 words.*

---

It is May 2026 and Google's plan for the agentic phone is still: every developer rewrites every app, ships a new build, and waits for users to update. The mechanism is `@AppFunction` — an annotation you add to your Android source to expose a callable function that Gemini can dispatch.

It is a good annotation. It is also a plan that will not work. Spotify hasn't shipped a Google Assistant-friendly intent in three years. WhatsApp has zero. Most apps with a million-plus monthly active users are maintained by teams who will rewrite for AI exactly when there is a revenue number attached to the ticket — which, for most apps, is never.

So either we wait. Or someone else opts the apps in for them.

This is the story of building that "someone else." It's open source, it's MIT-licensed, and it took a weekend of evenings.

## The premise

Modern Android exposes two surfaces that, when combined, can drive any third-party app:

1. **`AccessibilityService`** — a system-level API that lets a privileged app read the live UI tree of whatever's on screen. It is the same API screen readers use, and it returns a structured `AccessibilityNodeInfo` graph: every visible button, every text field, every list, with their bounds, ids, and contents.

2. **`androidx.test.uiautomator`** — Google's testing harness. It lets a test process (or in our case, a service masquerading as one) issue clicks, types, and swipes that the InputDispatcher sees as indistinguishable from human touch. Cross-process. No instrumentation in the target app required.

The Accessibility API tells you what's there. UI Automator lets you act on it.

What's missing is the translator between "this is a TikTok screen with a search icon" and "to search TikTok, tap that icon, then type into the field that appears, then press enter." That translator is the only piece of work an LLM has to do. Once it does, the result is a JSON document — a function schema plus an ordered list of UI actions — that anyone can execute, deterministically, forever.

That document is what anyfn produces, stores, and replays.

## The architecture

```
Agent  →  MCP server  →  Function registry (SQLite)  →  UI Automator → target apps
                              ↑
                          Scanner (LLM)
```

Two distinct runtime modes:

- **Scan time**: the Scanner walks every installed app, snapshots the UI tree via AccessibilityService, hands the compacted tree to an LLM with a structured prompt, gets back a list of `AppFunction` JSON objects, and writes them into a local Room database.
- **Invoke time**: the embedded Ktor MCP server receives `tools/call`, looks up the function, and asks the Invoker to replay the stored UI path.

This split is the single most important design decision. Inference is slow, expensive, and non-deterministic — perfect for a one-shot setup phase. Invocation is fast, free, and must be reliable — perfect for a network-free replay loop. A function discovered today still works tomorrow without re-calling the LLM, even if your phone is offline. Even if Anthropic's API is down. Even if you cancel your API key.

## What the LLM actually does

The prompt is in `app/src/main/assets/prompts/infer_functions.md`. Boiled down:

> Here is a compacted JSON of an Android app's UI tree. Return a JSON array of callable functions. Each function has a snake_case name, a description, a parameter schema, and an ordered list of UI actions to execute it. Only return functions you have >60% confidence in.

The compaction is doing a lot of work. A raw AccessibilityNodeInfo tree for a modern app is enormous — 200+ KB of XML for a single TikTok screen. We trim to ~700 bytes by:

- Dropping invisible nodes with no signal.
- Collapsing pure-layout `Group` wrappers.
- Capping depth at 14 and children-per-container at 24.
- Truncating text fields at 80 characters.

Claude Sonnet 4.6 handles this consistently. It costs about $0.005 per app, ~$0.30 for a full scan of a 60-app phone. Gemini Nano on AICore handles it more slowly and for free; we fall back to it when there's no API key.

## What the Invoker actually does

Given `tiktok_search({ query: "sunset hiking" })`:

```
1. Look up the function in the registry
2. Check preconditions: Accessibility service is running, no destructive-confirmation needed
3. Launch TikTok via PackageManager.getLaunchIntentForPackage
4. Wait until the foreground package matches "com.zhiliaoapp.musically"
5. For each step in the stored ui_path:
   a. Classify the current UI state — popup? login wall? secure screen?
   b. Execute the step (click, type, scroll, wait, press enter)
   c. Wait for the UI to settle
6. Capture the resulting screen as text
7. Return it
```

Step 5a is the interesting part. Before every step, we sample the visible Accessibility text and look for:

- Login walls ("Sign in", "Continue with Google") → abort with `LOGIN_REQUIRED`. anyfn never types passwords.
- Network errors ("No internet", "Couldn't connect") → retry once with backoff.
- Destructive dialogs ("Delete account", "Send money") → abort unless the call carries a confirmation token.
- Secure screens (empty Accessibility tree, even though the window is up) → abort with `SECURE_SCREEN_BLOCKED`. This is how Android tells us "this is a banking app, walk away."

This last one is non-negotiable. anyfn explicitly refuses to work on banking apps, password managers, and 2FA flows. The list of incompatible apps is published up front, and we will not accept PRs that try to work around it.

## The bridge

The Bridge is the public surface. It's an embedded Ktor server on `localhost:5174` that speaks a minimal subset of the Model Context Protocol: `initialize`, `tools/list`, `tools/call`. WebSocket primary, SSE fallback, HTTP-POST one-shot for the stdio-over-ADB use case.

Auth is loopback-only by default. LAN mode (your phone and laptop on the same Wi-Fi) requires a shared secret. There is no per-tool authorization in v0.1 — if you have access to the bridge, you have access to every enabled function. Use the registry's per-function enable toggle to limit blast radius.

On Android 16 with AICore, the same registry is also published as `AppFunctionService` entries. Gemini Nano on-device discovers them like first-party actions. The MCP transport and the AppFunctions transport read from the same source of truth.

## The result

Tested on a Pixel 8 Pro with ~60 user-installed apps. A clean scan takes 3 minutes and turns up 200-ish functions across the supported apps. Once that's done, from Claude Desktop on my Mac:

> "Search TikTok for sunset hiking, save the top 3 to my notes app."

Claude calls `tiktok_search({ query: "sunset hiking" })`. The phone opens TikTok, runs the search, returns the top 5 results. Claude picks 3, composes a Markdown summary, calls `keep_create_note({ title: "Sunset hiking inspo", body: "..." })`. The phone opens Keep, creates the note, saves.

Two apps. Zero developer cooperation. One sentence.

This is what the agentic phone is supposed to look like, and Google's plan can't get us there on its own. We need a bridge for the long tail of apps that will never voluntarily integrate. anyfn is that bridge — until those apps integrate, or until they don't.

## What's next

v0.2 is a Tauri desktop companion (install + manage the phone without `adb`) and on-device Phi-3 / Llama 3.2 inference via MLC-LLM, so no cloud key is needed.

v0.3 is a built-in multi-step planner that chains anyfn functions across apps with a single MCP call.

v1.0 is "anyfn Hub" — a signed, versioned catalogue of community-maintained function registries. You subscribe to one, you get a curated set of functions that someone else tuned.

What anyfn will never be: a thing that types your passwords, touches your banking apps, or sends telemetry. Those are hard constraints. They are also why open source is the only way to build this. Closed-source anything in this category does not deserve your Accessibility permission.

Code: github.com/jcaiagent7143-ui/anyfn
Discord: discord.gg/anyfn

Star the repo if this is the direction you want. Open an issue if there's an app you'd like supported. The thing about building a hammer is that suddenly everything looks like a nail — and yes, that's the point.
