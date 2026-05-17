# Invoker

The execution side. Source: `android/app/src/main/kotlin/dev/anyfn/invoker/`.

## Inputs

- A function name (string) plus a map of arguments.

## Outputs

A sealed [`InvocationResult`](../../android/core/src/main/kotlin/dev/anyfn/core/model/InvocationResult.kt) — `Success`, `Failure`, or `NeedsConfirmation`.

## Pipeline

```
call(name, args)
  ↓
  lookup function in registry
  ↓
  preconditions:  Accessibility on? destructive-confirm needed?
  ↓
  launch target app, await foreground
  ↓
  for each step in ui_path:
       classifyCurrentState()  →  Continue | Retry | Abort
       runStep(step, args)     →  true | false
       awaitIdle()
  ↓
  captureSummary()  →  output
  ↓
  Success(output, durationMs)
```

## Action vocabulary

The `ui_path` is a list of typed actions. The Invoker maps each to UI Automator:

| Action | UI Automator call | Notes |
|---|---|---|
| `Click` | `UiObject2.click()` | Falls back to `fallback_selector` if primary missing |
| `TypeText` | `UiObject2.text = value` | Resolves `value_from_param` against the argument map |
| `Scroll` | `device.swipe(...)` | Uses gesture, not `fling`, for predictability |
| `WaitFor` | polls UI Automator AND Accessibility | Whichever sees the element first wins |
| `PressEnter` / `PressBack` | `device.pressEnter/Back()` | |
| `Launch` | resolved launch intent + `startActivity` | |

## Why UI Automator and not pure Accessibility?

Accessibility can read the world but its action surface (`AccessibilityService.dispatchGesture`) is finicky cross-app — many apps consume gesture intents differently than they consume touch events. UI Automator drives the InputDispatcher directly, which means a tap is indistinguishable from a human tap. Apps that block one rarely block both.

## Error recovery

`ErrorRecovery.classifyCurrentState()` runs before every step. It samples the visible Accessibility text and returns one of:

- **Continue** — nothing weird. Proceed.
- **Retry(backoff)** — a transient ("no internet") banner. Wait, then re-run the step.
- **Abort(reason, message)** — login wall, secure screen, destructive confirmation dialog. Stop with structured failure.

The classifier is conservative on purpose. False positives cause spurious cancellations, which are worse than letting the next step fail and report a clean `ELEMENT_NOT_FOUND`.

## Destructive confirmation

Functions tagged `destructive: true` require a `__confirm` token on first call. The Invoker returns `NeedsConfirmation` with a fresh single-use token; the agent re-calls with `__confirm=<token>` in arguments. This is the same model Anthropic's MCP "elicit user input" pattern uses.

## Result capture

After the last step, `ResultCapturer.captureSummary()` walks the foreground UI:

- If a scrollable list dominates the screen → enumerate the top 10 items.
- Otherwise → return the visible text summary (capped at 2 KB).

The agent sees this as the function's return value. Function authors can override the capture logic by adding a final `WaitFor` step whose selector targets the element they want quoted.
