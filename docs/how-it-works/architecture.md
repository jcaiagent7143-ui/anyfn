# Architecture

A 4,000-foot tour. The deep dives live in [scanner.md](scanner.md), [invoker.md](invoker.md), [bridge.md](bridge.md), and [inference.md](inference.md).

## Layers

```
┌─────────────────────────────────────────────────────────┐
│  External Agent (Claude / Gemini / Cursor / GPT / …)    │
└────────────────────────┬────────────────────────────────┘
                         │  MCP   OR   Android 16 AppFunctions
┌────────────────────────▼────────────────────────────────┐
│  Bridge   ←→   FunctionRegistry (Room)                  │
└────────────────────────┬────────────────────────────────┘
        ┌────────────────┼───────────────┐
        │                                │
┌───────▼────────┐                ┌──────▼──────────┐
│  Scanner       │                │  Invoker        │
│  (one-shot)    │                │  (per call)     │
└───────┬────────┘                └──────┬──────────┘
        │                                │
        │ AccessibilityService           │ UI Automator + Accessibility
        │                                │
        └────────────────┬───────────────┘
                         │
                ┌────────▼──────────┐
                │  Target apps      │
                │  (unmodified)     │
                └───────────────────┘
```

## Two distinct runtime modes

| Mode | Trigger | Reads | Writes |
|---|---|---|---|
| **Scan** | User taps "Scan" | Accessibility tree, app metadata | Function registry (Room), scan history |
| **Invoke** | Agent calls a tool | Accessibility tree (recovery), registry | UI Automator gestures on target apps |

Outside these two modes, anyfn is idle. The MCP foreground service is alive but only accepts connections — it doesn't read any app's state until an invocation comes in.

## Why this split

The decoupling between Scanner and Invoker is the heart of anyfn's design. The Scanner is slow, runs once, and is allowed to use an expensive LLM. The Invoker is fast, runs on every call, and never touches the network. A function discovered today still works tomorrow without re-calling the LLM, even if your phone is offline.

## Module boundaries

| Package | Owns | Depends on |
|---|---|---|
| `dev.anyfn.scanner` | discovery, UI extraction, inference orchestration | inference, accessibility, data |
| `dev.anyfn.invoker` | action execution, state waiting, error recovery | accessibility, data |
| `dev.anyfn.bridge` | MCP server, AppFunctions exporter | invoker, data |
| `dev.anyfn.inference` | LLM backends | data (settings) |
| `dev.anyfn.accessibility` | the AccessibilityService itself | (none) |
| `dev.anyfn.data` | Room, DataStore, repositories | core |
| `dev.anyfn.core` | pure-Kotlin domain types & MCP wire format | (none) |
| `dev.anyfn.ui` | Compose screens | every above |

`core` is a separate Gradle module so future hosts (desktop companion, headless test runner) can link against it without Android. Every other package lives inside `:app`.
