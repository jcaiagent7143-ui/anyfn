# Your first scan

A scan walks every user-installed app on your phone, asks an LLM to infer what callable functions each app exposes, and persists the result in a local registry.

## What to expect

For a phone with 60 user-installed apps and Claude Sonnet 4.6 as the inference backend, a fresh scan takes about **2–4 minutes**. Most of that is launching the app and waiting for it to stabilise — inference itself is ~2 seconds per app.

## Run it

1. Open anyfn → tap **Scan device**.
2. Watch the progress card. Each app launches briefly, anyfn snapshots a few UI states, the LLM infers functions, then anyfn moves on.
3. When the scan finishes, head to **Registry** to see the functions it found.

## Why some apps return zero functions

- The app shows a login wall first. Sign in once, manually, then rescan that app.
- The app uses a `WebView` for its main UI. anyfn handles the WebView's exposed Accessibility tree but some sites are aggressive about disabling it.
- The app sets `FLAG_SECURE` (banking, password managers). anyfn refuses to work around this. See [safety/what-anyfn-cannot-do.md](../safety/what-anyfn-cannot-do.md).

## Re-scanning a single app

In Registry, long-press an app heading → **Rescan**. Useful when the app updates and a previously stable function breaks.

## Editing inferred functions

Tap any function → **Open playground** → **Edit ui_path**. You can rewrite selectors, reorder steps, or change parameter schemas. Edits are sticky across rescans (anyfn diffs new inferences against your overrides and keeps yours).

## Backing up the registry

The registry lives at `/data/data/dev.anyfn/databases/anyfn.db`. From an `adb` shell on a debug build:

```bash
adb exec-out run-as dev.anyfn.debug cat databases/anyfn.db > anyfn-registry.db
```

Restore by copying back into place. This is your portable function library.
