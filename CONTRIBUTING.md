# Contributing to anyfn

Thanks for being here. The single highest-leverage contribution to anyfn is **adding support for a new app** or **fixing a broken inference**. This document explains how.

## Ground rules

1. Be kind. We follow the [Contributor Covenant](CODE_OF_CONDUCT.md).
2. Privacy is a hard constraint. No telemetry, no analytics, no phone-home. PRs that introduce these will be closed.
3. The license is MIT. By contributing you agree your code ships under MIT.
4. Small PRs > big PRs. One concern per PR.

## Setup

```bash
git clone https://github.com/jcaiagent7143-ui/anyfn.git
cd anyfn/android
./gradlew :app:installDebug
```

You'll need:

- JDK 17
- Android SDK 36
- A real Android 13+ device or emulator (Accessibility behaviour differs on emulators — prefer real hardware)
- An Anthropic API key (or Gemini Nano on Android 14+ AICore)

Run `scripts/setup-dev.sh` once to install the git hooks and verify the toolchain.

## How the codebase is laid out

See [docs/how-it-works/architecture.md](docs/how-it-works/architecture.md). The five modules you'll spend time in:

| Module | Where | What it does |
|---|---|---|
| Scanner | [android/app/src/main/kotlin/dev/anyfn/scanner](android/app/src/main/kotlin/dev/anyfn/scanner) | Discovers apps, dumps UI trees, infers functions |
| Invoker | [android/app/src/main/kotlin/dev/anyfn/invoker](android/app/src/main/kotlin/dev/anyfn/invoker) | Replays UI action paths |
| Bridge | [android/app/src/main/kotlin/dev/anyfn/bridge](android/app/src/main/kotlin/dev/anyfn/bridge) | MCP server + AppFunctions export |
| Inference | [android/app/src/main/kotlin/dev/anyfn/inference](android/app/src/main/kotlin/dev/anyfn/inference) | LLM backends and prompts |
| UI | [android/app/src/main/kotlin/dev/anyfn/ui](android/app/src/main/kotlin/dev/anyfn/ui) | Jetpack Compose screens |

## Adding support for a new app

1. Open [a `new_app_support` issue](.github/ISSUE_TEMPLATE/new_app_support.md) so we know it's in flight.
2. Install the app, run a scan, and grab the UI tree dump from `adb pull /sdcard/Android/data/dev.anyfn/files/dumps/<package>.json`.
3. If the LLM inference is wrong, tune the prompt in `android/app/src/main/assets/prompts/infer_functions.md` and add a few-shot example for the app.
4. If the inference is right but invocation fails, the fix is usually in `invoker/ErrorRecovery.kt` (a missed popup) or `invoker/StateWaiter.kt` (a missed transition).
5. Add an end-to-end test in `android/app/src/androidTest/.../<App>InvocationTest.kt`.
6. Update the supported-apps table in [README.md](README.md).
7. Open a PR. Include a 5-second screen recording of the function working.

## Style

- Kotlin 2.0, explicit types on public APIs.
- Errors cross module boundaries as sealed `Result` types, never as exceptions.
- Inject via Hilt; no static singletons.
- One unit test minimum for any class with logic.
- ktlint + detekt must pass.
- Commits follow [Conventional Commits](https://www.conventionalcommits.org/).

## PR checklist

- [ ] `./gradlew lint test assembleDebug` passes locally.
- [ ] Added or updated a unit test.
- [ ] Updated docs if you changed user-facing behaviour.
- [ ] No new dependencies without justification in the PR body.
- [ ] No telemetry / network beacons of any kind.

## Releasing (maintainers)

1. Bump version in `android/gradle/libs.versions.toml`.
2. Update [CHANGELOG.md](CHANGELOG.md).
3. Tag `vX.Y.Z` — the `release.yml` workflow builds the signed APK and attaches it to the GitHub Release.

## Where to talk

- Bug or feature: GitHub issue.
- Open-ended discussion: [Discussions](https://github.com/jcaiagent7143-ui/anyfn/discussions).
- Real-time: [Discord](https://discord.gg/anyfn).
