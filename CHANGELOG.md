# Changelog

All notable changes to anyfn are documented here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Tauri desktop companion app scaffold (`desktop/`) for a one-click MCP forwarder.

## [0.1.0] — 2026-05-17

The first public release. Focus is on the discovery → invocation → bridge loop on a single Android device, with MCP as the primary transport.

### Added
- **Scanner**: package discovery, accessibility-driven UI tree extraction, LLM-powered function inference (Anthropic Claude + Gemini Nano fallback).
- **Invoker**: UI Automator + Accessibility action executor with state-waiting, error recovery, and structured result capture.
- **Bridge**: embedded Ktor MCP server on `localhost:5174` (WebSocket + HTTP+SSE + stdio-over-ADB) and Android 16 `AppFunctionService` exporter with feature detection.
- **UI**: onboarding, home, scanner, registry, playground, bridge status, settings, about — all in Jetpack Compose + Material 3.
- **Persistence**: Room registry of discovered functions; DataStore for settings.
- **Examples**: TikTok search, WhatsApp send, Spotify play, cross-app chain (TikTok → Notes), Grab order food.
- **Docs**: full Docusaurus-ready content in `/docs`.
- **Marketing site**: Next.js 14 scaffold in `/website`.
- **Launch artifacts**: demo script, HN post, X thread, LinkedIn post, Medium article, KOL outreach, press kit.

### Known limitations
- Banking, 2FA, and any app with `FLAG_SECURE` screens is unsupported by design — documented in [docs/safety/known-incompatible-apps.md](docs/safety/known-incompatible-apps.md).
- Android 16 AppFunctions export is feature-detected; it no-ops on Android 13–15.
- Local LLM inference (Phi-3, Llama 3.2) is not in this release.

[Unreleased]: https://github.com/jcaiagent7143-ui/anyfn/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/jcaiagent7143-ui/anyfn/releases/tag/v0.1.0
