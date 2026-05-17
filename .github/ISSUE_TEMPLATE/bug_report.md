---
name: Bug report
about: Something is wrong inside anyfn itself
title: "[bug] "
labels: bug
assignees: ''
---

## What happened

A short, clear description.

## Steps to reproduce

1. ...
2. ...
3. ...

## What you expected

...

## Environment

- anyfn version: (Settings → About)
- Android version:
- Device model:
- Target app + version (if relevant):
- LLM backend: Anthropic / Gemini Nano

## Logs

```
adb logcat -d -s anyfn:* AccessibilityService:* > anyfn.log
```

Paste the relevant section here. Redact API keys.

## Screen recording

A 5–10 second clip helps a lot. GitHub accepts `.mp4` and `.gif` drops directly into the issue body.
