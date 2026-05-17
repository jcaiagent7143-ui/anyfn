<div align="center">

# anyfn

**Make every Android app agent-ready in 60 seconds.**
*No code changes. No SDK. No waiting for developers.*

[![License: MIT](https://img.shields.io/badge/License-MIT-22C55E.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android%2013%2B-0EA5E9.svg)](#)
[![CI](https://img.shields.io/github/actions/workflow/status/jcaiagent7143-ui/anyfn/android-build.yml?branch=main&label=build)](.github/workflows/android-build.yml)
[![PRs welcome](https://img.shields.io/badge/PRs-welcome-0EA5E9.svg)](CONTRIBUTING.md)
[![Discord](https://img.shields.io/badge/discord-join-5865F2.svg)](https://discord.gg/anyfn)

[Demo](#demo) · [Quick start](#quick-start) · [How it works](#how-it-works) · [Docs](docs/index.md) · [Discord](https://discord.gg/anyfn)

</div>

---

## The problem

Google's pitch for the agentic phone is `@AppFunction` — annotate every entry point in your app, ship a new build, wait for users to update. That's the deal. Every Android app. Every developer. Every release.

It will not happen. Spotify has not shipped a Google Assistant-friendly intent in three years. WhatsApp has zero. Most apps with a million-plus installs are maintained by teams who will rewrite for AI exactly when there is a number next to the ticket — which is approximately never.

So agents on Android remain stuck doing 2014-era Assistant tricks. Set a timer. Send a text. Done.

## What anyfn does

- **Auto-discovers** what every installed app on your phone can do, without touching the app's source.
- **Infers** an `AppFunction`-shaped schema for each capability using an LLM that reads the app's UI tree.
- **Stores** the inferred functions in a local registry you can edit, override, or version.
- **Executes** function calls by replaying UI action paths via UI Automator + Accessibility.
- **Exposes** the whole registry as (a) a standard MCP server for Claude Desktop / Cursor / Gemini / GPT / local models, and (b) native Android 16 `AppFunctionService` entries Gemini Nano can call directly.
- **Runs entirely on-device.** No telemetry. No phone-home. Bring your own Claude / Gemini key, or use on-device Gemini Nano.

## Demo

<div align="center">

![anyfn demo placeholder](launch/press-kit/demo.gif)

**60 seconds. No edits where it counts.**

</div>

## Quick start

```bash
# 1. Clone and assemble
git clone https://github.com/jcaiagent7143-ui/anyfn.git
cd anyfn/android
./gradlew :app:installDebug

# 2. On the device: open anyfn, grant Accessibility, paste your Anthropic API key,
#    tap "Scan device". Wait ~2 minutes for the first scan.

# 3. From your Mac, forward the MCP port over USB
./scripts/adb-forward.sh

# 4. Point your agent at it
cat <<EOF >> ~/Library/Application\ Support/Claude/claude_desktop_config.json
{
  "mcpServers": {
    "anyfn": { "command": "wscat", "args": ["-c", "ws://localhost:5174"] }
  }
}
EOF
```

Restart Claude Desktop. Ask it: *"List tools anyfn knows about."* You'll see TikTok, WhatsApp, Spotify, Notes, whatever you have installed.

Full walkthrough: [docs/getting-started/install-apk.md](docs/getting-started/install-apk.md).

## How it works

```
┌─────────────────────────────────────────────────────────┐
│  External Agent                                         │
│  (Gemini on Android 16 / Claude Desktop / Cursor / …)   │
└────────────────────────┬────────────────────────────────┘
                         │  MCP protocol  OR  AppFunctions API
┌────────────────────────▼────────────────────────────────┐
│  anyfn Bridge Layer                                     │
│  - AppFunctionsExporter (Android 16 OS)                 │
│  - McpServer (Ktor, WebSocket/HTTP, any client)         │
└────────────────────────┬────────────────────────────────┘
┌────────────────────────▼────────────────────────────────┐
│  anyfn Function Registry (Room/SQLite)                  │
└────────┬─────────────────────────────────────┬──────────┘
         │                                     │
┌────────▼─────────────┐               ┌───────▼──────────┐
│  Scanner             │               │  Invoker         │
│  - AppDiscovery      │               │  - ActionExecutor│
│  - UITreeExtractor   │               │  - UIAutomator   │
│  - FunctionInferrer  │               │  - StateWaiter   │
│    (LLM-powered)     │               │  - ErrorRecovery │
└──────────────────────┘               └──────────────────┘
                         │ Accessibility + UI Automator
                ┌────────▼────────────────────────┐
                │  Target Apps (unmodified)       │
                │  TikTok, WhatsApp, Grab, …      │
                └─────────────────────────────────┘
```

The Scanner walks every user-installed app, snapshots its UI tree through `AccessibilityService`, and asks an LLM "what are the callable things here?" The LLM returns JSON schemas plus an ordered list of UI actions to invoke each one. The Registry stores them. The Bridge re-publishes them — either as Android 16 AppFunctions or over MCP. When an agent calls a function, the Invoker replays the saved UI path with state-waiting and error recovery. The whole loop runs on the phone.

Deeper: [docs/how-it-works/architecture.md](docs/how-it-works/architecture.md).

## Use it from

**Claude Desktop** — append to `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "anyfn": { "command": "wscat", "args": ["-c", "ws://localhost:5174"] }
  }
}
```

**Cursor** — Settings → MCP → Add server, URL `ws://localhost:5174`.

**Android 16 Gemini** — nothing. anyfn registers itself as an `AppFunctionService` and Gemini discovers it like any first-party action.

**GPT / Ollama / your own agent** — speak MCP at `ws://localhost:5174`.

## What works today

| App | Status | Functions inferred |
|---|---|---|
| TikTok | stable | search, save_to_favorites, follow, comment |
| WhatsApp | stable | send_message, send_voice_note, create_group |
| Spotify | stable | play_playlist, search_track, like_song |
| Google Maps | stable | search_place, start_navigation, save_pin |
| Notes (Google Keep) | stable | create_note, append_to_note, list_notes |
| Grab | beta | order_food, book_ride, list_orders |
| Gmail | beta | send_email, archive, search |
| Instagram | beta | post_story, search, follow |
| YouTube | beta | search, subscribe, save_to_playlist |
| Banking apps | **unsupported by design** | — |

See [docs/safety/known-incompatible-apps.md](docs/safety/known-incompatible-apps.md) for the full incompatibility list.

## What's coming

- v0.2 — Tauri desktop app: install anyfn from your Mac, no `adb` required.
- v0.2 — local Phi-3 / Llama 3.2 inference via MLC-LLM, no cloud key needed.
- v0.3 — multi-step planner that chains anyfn functions across apps with a single agent call.
- v0.3 — F-Droid release.

Full roadmap: [docs/roadmap.md](docs/roadmap.md).

## Safety & limits

anyfn is a power tool. It does what you tell it inside any app you have open. Read [docs/safety/permissions-model.md](docs/safety/permissions-model.md) before you point an agent at it.

- Destructive actions (send money, delete account, post publicly) require explicit per-call confirmation by default.
- Banking, 2FA, password manager, and any app that sets `FLAG_SECURE` is blocked at the Accessibility layer. We do not work around this — see [docs/safety/what-anyfn-cannot-do.md](docs/safety/what-anyfn-cannot-do.md).
- The MCP server binds to `127.0.0.1` by default. LAN mode requires a shared secret.

## Contributing

The single highest-leverage contribution is **adding support for a new app**. Open the [`new_app_support`](.github/ISSUE_TEMPLATE/new_app_support.md) issue template, paste the failing scan, and we'll work it out together. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Community

- Discord: <https://discord.gg/anyfn>
- X: [@anyfn_dev](https://x.com/anyfn_dev)
- Discussions: <https://github.com/jcaiagent7143-ui/anyfn/discussions>

## Acknowledgments

- Anthropic for the [Model Context Protocol](https://modelcontextprotocol.io).
- Google for [Android AppFunctions](https://developer.android.com/reference/android/app/appfunctions/package-summary) — we'd love to be obsoleted by universal adoption.
- The Android Accessibility team, whose work makes this possible at all.

## License

[MIT](LICENSE).
