# anyfn — one pager

**Tagline.** Make every Android app agent-ready in 60 seconds. No code changes. No SDK.

**What it is.** anyfn is an open-source Android runtime that turns every installed app on a user's phone into a callable tool for AI agents — without modifying the app's source code. It combines AccessibilityService, UI Automator, and on-device LLM inference to auto-discover what each app can do, exposes those capabilities as standard MCP tools and as Android 16 AppFunctions, and lets any agent chain actions across apps as if they were native functions.

**Why now.**
- Google's `@AppFunction` SDK requires every developer to rewrite their app for AI. Most won't, not soon.
- The MCP standard has emerged as the lingua franca of AI tool use. Claude, Cursor, Gemini, and major open-source agents all speak it.
- Android exposes enough surface area (Accessibility + UIAutomator) for a third-party runtime to bridge the gap.

**Who it is for.**
- Developers and indie hackers building AI agents that need to do real things on a real phone.
- Power users who want their existing Android apps to "just work" with Claude / Gemini / Cursor.
- Researchers exploring agentic mobile UX without app-vendor cooperation.

**What it ships with (v0.1, 2026-05-17).**
- 9 stable apps + 4 beta apps in the first-party showcase.
- MCP server on `localhost:5174` — WebSocket, HTTP, SSE.
- Android 16 AppFunctions feature-detected, optional.
- Anthropic Claude Sonnet 4.6 + on-device Gemini Nano backends.
- 5 worked examples, full docs, launch artifacts, marketing site, Tauri desktop companion stub.

**What it deliberately does not do.**
- No banking, 2FA, or password manager support. Apps with `FLAG_SECURE` are unsupported by design.
- No password typing, ever.
- No telemetry. Zero analytics. The only network call is to your chosen LLM provider, at scan time.
- No background activity outside scans and explicit invocations.

**Distribution model.**
- MIT licensed.
- Distributed via GitHub Releases APK. F-Droid on the v0.3 roadmap.
- We will not publish to Google Play.

**Founders.**
- Built in Singapore by a small team with backgrounds in Android internals, distributed systems, and LLM tooling.
- Contact: hello@anyfn.dev / press@anyfn.dev.

**Links.**
- GitHub: github.com/jcaiagent7143-ui/anyfn
- Site: anyfn.dev
- Discord: discord.gg/anyfn
- X: @anyfn_dev
