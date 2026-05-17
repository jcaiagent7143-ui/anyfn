# Build from source

For developers who want to hack on anyfn.

## Prerequisites

- JDK 17 (Temurin recommended)
- Android SDK 36 (compileSdk) + build tools 35.0.0
- A device or emulator running Android 13 (API 33) or newer
- ~3 GB free disk for the Gradle cache

## Clone

```bash
git clone https://github.com/jcaiagent7143-ui/anyfn.git
cd anyfn/android
```

## Configure

Copy `keystore.properties.example` to `keystore.properties` if you plan to build release APKs locally. Debug builds don't need this.

For LLM inference, you'll set the Anthropic key at runtime from the Settings screen — there's nothing to configure in source.

## Build

```bash
./gradlew :app:assembleDebug          # debug APK
./gradlew :app:installDebug           # build and install on the connected device
./gradlew :app:assembleRelease        # signed release APK (needs keystore.properties)
./gradlew test                         # unit tests
./gradlew connectedAndroidTest         # instrumented tests, needs device/emulator
./gradlew ktlintCheck detekt           # lint
```

## Project layout

```
android/
├── app/      Android application module (Compose UI, services, glue)
└── core/     Pure-Kotlin shared module (domain types, MCP wire format)
```

The split exists so the desktop companion (v0.2) can depend on `core` without dragging in Android.

## Common issues

- **"SDK not found"**: set `ANDROID_HOME` or create `android/local.properties` with `sdk.dir=/path/to/sdk`.
- **Ktor SLF4J warnings at startup**: harmless; logging is silenced for release builds.
- **Hilt KSP fails**: bump JDK to 17, not 11 — Hilt 2.52 dropped JDK 11.
