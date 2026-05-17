# AppFunctions export (Android 16+)

When anyfn runs on Android 16 with AICore available, it registers every enabled function in the registry as a native `AppFunctionService` entry. Gemini Nano discovers and calls these without any extra wiring.

## Status

The Android 16 AppFunctions API is in private preview as of 2026-05. The shape is:

- `android.app.appfunctions.AppFunctionService` — base service, declared in the manifest.
- `@AppFunction` — annotation on the methods that implement each function.
- Function metadata lives in an XML descriptor at `res/xml/app_functions.xml`.

anyfn cannot use the annotation processor at compile time for *runtime-discovered* functions (the registry is built from a scan, not source). Instead we:

1. Generate the descriptor XML at scan time from `FunctionRepository.observeEnabled()`.
2. Bind the service entry to a generic dispatcher that routes incoming calls back to `ActionExecutor`.
3. Re-register on every registry update.

## Verifying

In anyfn → Bridge, the **Android 16 AppFunctions** row shows one of:

- `published N functions` — N entries are visible to Gemini.
- `Android 16 AppFunctions API not available on this device` — your phone is older or lacks AICore.
- `failed: <reason>` — typically a permission issue; check logs.

## From Gemini

Gemini on Android 16 ranks AppFunctions by relevance to the user's prompt. anyfn's functions appear with their descriptions verbatim from the registry, so write good descriptions. The model picks `tiktok_search` over `tiktok_open_explore` for "find videos about X" because the description says so.

## Limitations

- AppFunctions does not stream — every response is single-shot. Long-running invocations (>30s) may time out at the OS layer.
- AppFunctions runs the function in the caller's process. anyfn's invocation still happens via UI Automator on the device, but the request/response shape is constrained by the AppFunctions API contract.
- AppFunctions does not currently expose confirmation tokens. Destructive functions surface the confirmation prompt to the user through Gemini's own UI flow; anyfn returns the `NeedsConfirmation` payload and Gemini decides how to elicit consent.
