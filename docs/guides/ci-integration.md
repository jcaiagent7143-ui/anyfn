# Run anyfn in CI

You can use anyfn as a black-box runner inside a CI pipeline — handy for end-to-end smoke tests of apps you can't yet instrument.

## Pattern

1. Boot an Android emulator (API 33+) inside the CI job.
2. Install anyfn from the latest release APK.
3. Pre-seed the function registry — `adb push` a `seed.db` of curated functions.
4. Start the bridge: `adb shell am start-foreground-service -n dev.anyfn/.bridge.McpForegroundService`.
5. Forward the port: `adb forward tcp:5174 tcp:5174`.
6. Call functions over MCP from your CI worker (Node, Python, Go — anything that speaks WebSocket).

## GitHub Actions example

```yaml
- name: Boot emulator + install anyfn
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 34
    target: google_apis
    script: |
      adb install anyfn-v0.1.0.apk
      adb push tests/seed-registry.db /data/local/tmp/seed.db
      adb shell run-as dev.anyfn cp /data/local/tmp/seed.db databases/anyfn.db
      adb shell am start-foreground-service -n dev.anyfn/.bridge.McpForegroundService
      adb forward tcp:5174 tcp:5174
      node tests/e2e.mjs
```

## Stability tips

- Use the same emulator AVD across runs so UI snapshots stay deterministic.
- Pin the LLM model — different versions infer subtly different selectors.
- Capture screenshots on each step (Settings → Debug → Dump UI trees) to make failures debuggable.
- Don't run inference in CI. Pre-bake the registry and commit it — CI is for invocation tests.

## Limitations

This is **not** suitable for shipping against real banking, auth, or 2FA flows. Use Espresso or a vendor-specific test framework for those. anyfn shines for UX-level smoke tests across third-party apps.
