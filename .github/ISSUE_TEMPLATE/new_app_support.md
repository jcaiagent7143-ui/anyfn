---
name: New app support
about: anyfn doesn't yet handle a specific app well — let's fix that
title: "[app] "
labels: app-support
assignees: ''
---

## The app

- Name:
- Package: (e.g. `com.zhiliaoapp.musically`)
- Version:
- Country / locale (UI varies by region):

## What you wanted to do

The function as you'd phrase it to an agent. Example:
> "Search TikTok for 'sunset' and save the top 3 videos to my favourites."

## What happened on scan

- [ ] anyfn failed to launch the app
- [ ] anyfn launched but the inference returned zero functions
- [ ] anyfn returned functions but their schemas are wrong
- [ ] anyfn returned the right schemas but invocation fails

## Dumps

Run a scan with debug mode on (Settings → Debug → Dump UI trees), then attach:

```bash
adb pull /sdcard/Android/data/dev.anyfn/files/dumps/<package>.json
```

Attach the JSON here. Redact anything personal.

## Invocation log

If invocation was attempted:

```bash
adb logcat -d -s anyfn:* > anyfn.log
```

Paste the relevant section.

## Notes

Anything else worth knowing — region-locked UI, A/B-tested screens, login walls, etc.
