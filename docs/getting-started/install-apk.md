# Install the APK

The fastest way to try anyfn on a phone you already own.

## 1. Download

Grab the latest APK from the [GitHub Releases page](https://github.com/jcaiagent7143-ui/anyfn/releases/latest). Pick `anyfn-vX.Y.Z.apk` (signed release build).

## 2. Allow sideloading

Android 13+ requires explicit per-source permission to install APKs.

- Open the APK in your browser's Downloads.
- Android prompts: *"For your security, your phone isn't allowed to install unknown apps from this source."* Tap **Settings** → toggle **Allow from this source** → back.
- Tap **Install**.

## 3. Open anyfn

The launcher icon is a sky-blue `fn(·)` mark. Tap it, follow the onboarding flow (Accessibility + LLM key), and you're done.

## Verifying the build

Each release tag is signed and the SHA-256 of the APK is published in the release notes. Verify with:

```bash
shasum -a 256 anyfn-v0.1.0.apk
```

The hash should match the one in the release body.

## Updating

Releases are not auto-updated. Subscribe to the repo to get a GitHub notification on each tag, then re-download. We will move to F-Droid in v0.3 for auto-updates.
