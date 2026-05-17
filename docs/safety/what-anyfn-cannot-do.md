# What anyfn cannot do

A short list. We will not change any of these in v0.x. Some we will not change ever.

## Will not — by design

- **Type passwords or 2FA codes.** No exceptions.
- **Operate on `FLAG_SECURE` screens.** Android marks them invisible to Accessibility. We do not work around this.
- **Bypass biometrics.** No fingerprint, FaceUnlock, or PIN automation.
- **Unlock the device.** If the screen is locked, every function call fails until the user unlocks.
- **Auto-grant Accessibility to itself.** The user must toggle it in Settings.
- **Phone home.** No telemetry, ever.

## Will not — for now

- **Drive Android Auto / Android TV / Wear OS.** Different input models. v1.0 may add these for non-driving / non-credential use cases.
- **Drive split-screen / multi-window.** Foreground only.
- **Replay against background apps without raising them to foreground.** UI Automator needs the window to be visible.
- **Coordinate two concurrent invocations on the same phone.** Calls are serialised.

## Limitations of the approach itself

- **Apps that aggressively obfuscate their UI** (no resource IDs, dynamic class names, frequent A/B tests) will produce flaky selectors. We mitigate with fallback selectors and manual overrides, but the floor is set by what the app exposes through Accessibility.
- **WebView-only screens** depend on the app exposing the underlying DOM through Accessibility. Many do; some don't.
- **Region-specific UIs** (the TikTok layout differs across markets) require region-specific inference. The fix is per-locale prompts and overrides.
- **Apps with anti-automation logic** (e.g. detection of UI Automator) can refuse to operate. We document these in [known-incompatible-apps.md](known-incompatible-apps.md).
