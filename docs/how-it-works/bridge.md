# Bridge

The public surface. Source: `android/app/src/main/kotlin/dev/anyfn/bridge/`.

Two transports, same registry.

## MCP server

`McpServer` is a Ktor CIO server bound to `127.0.0.1:5174` by default. It speaks a minimal subset of the [Model Context Protocol](https://modelcontextprotocol.io):

| Method | What it does |
|---|---|
| `initialize` | Returns server info and capability flags |
| `tools/list` | Returns every enabled function in the registry as an `McpTool` |
| `tools/call` | Dispatches to `ActionExecutor.call(name, arguments)` |

Transports:

- `GET /` — health/discovery JSON
- `WebSocket /ws` — primary, recommended
- `POST /rpc` — single-shot JSON-RPC for clients that don't speak WebSocket
- `SSE /sse` — fallback for environments that block long-lived WebSockets

## Auth model

By default the server binds to loopback only — anything on the device with `INTERNET` permission could talk to it, but you have to be on the device. LAN mode (`Settings → Bridge → Allow LAN connections`) widens the bind to `0.0.0.0` and **requires** a shared secret to be set; the server rejects unauthenticated requests with HTTP 401 / WS close code 4401.

There is no per-tool authorization in v0.1. If you have access to the bridge, you have access to every enabled function. Use the registry's per-function enable toggle to limit blast radius.

## Foreground service

`McpForegroundService` keeps the server alive across Doze and background limits. It posts a low-importance ongoing notification with a single "Stop" action so users can kill the bridge with one tap.

The service uses `foregroundServiceType="specialUse"` with the manifest property `mcp_bridge`. This is the Android 14+ requirement — anyfn does not pretend to be a `mediaPlayback` or `dataSync` service.

## AppFunctions exporter (Android 16+)

`AppFunctionsExporter` reflectively probes `android.app.appfunctions.AppFunctionService`. When present, it registers every enabled function as a native AppFunction — Gemini discovers them automatically.

We use reflection instead of compile-time bindings because the SDK is in private preview as of 2026-05; the stable API surface is not yet finalised. When it ships GA, the exporter will be rewritten without changing callers.

## What the bridge does NOT do

- It does not authenticate the *user*. Authentication is enforced at the OS layer (lock screen) and the LLM provider layer (API key). The bridge trusts the device.
- It does not log function arguments or results to disk by default. Debug mode (Settings → Debug) opts into JSONL traces at `/sdcard/Android/data/dev.anyfn/files/traces/`.
- It does not call Anthropic. Inference happens at scan time; the bridge only invokes already-discovered functions.
