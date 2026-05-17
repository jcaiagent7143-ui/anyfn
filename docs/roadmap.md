# Roadmap

Last updated: 2026-05-17.

## v0.1 — Public beta (shipped)

- Scanner + Invoker + Bridge loop end-to-end on a single device.
- Claude Desktop / Cursor / Gemini (Android 16) integrations.
- 9 stable apps, 4 beta apps in the supported table.
- Full docs + 5 examples + launch artifacts.

## v0.2 — Desktop companion (Jun–Jul 2026)

- Tauri-based "anyfn Desktop" app: install + manage the phone without `adb`.
- Local Phi-3 / Llama 3.2 backends via MLC-LLM — no cloud key needed.
- Per-function permission editor: enable / disable / mark destructive from the desktop.
- 15+ stable apps.

## v0.3 — Multi-step planner (Aug–Sep 2026)

- A built-in agent that chains anyfn functions across apps with a single MCP tool call.
- F-Droid release.
- Multilingual prompts and per-locale registry overrides.
- 30+ stable apps.

## v0.4 — Wear OS + tablet (Q4 2026)

- Watch-side bridge for "do this on my phone from my watch" flows.
- Foldable / large-screen layout for the registry editor.

## v1.0 — Stability + ecosystem (2027 H1)

- Versioned, signed function manifests so contributors can share registries.
- "anyfn Hub" — a public catalogue of community-maintained registries you can subscribe to.
- Optional, fully-local end-to-end pipeline: on-device LLM + Accessibility + UI Automator. No cloud, no MCP, no telemetry.

## Not on the roadmap

- iOS port. See [FAQ](faq.md).
- Anything that involves typing passwords or bypassing 2FA. Ever.
- Telemetry or analytics. Ever.
