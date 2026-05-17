# anyfn desktop (v0.2 stub)

The Tauri-based desktop companion. **Not shipping in v0.1.** This directory exists to claim the structure so v0.2 can land without a layout change.

## What v0.2 will do

- Install anyfn to a connected phone in one click (bundles `adb` so the user doesn't need it).
- Manage the registry — enable, disable, edit functions from the laptop, not the phone.
- Run multi-step planner flows that chain anyfn calls across apps.
- Run anyfn against an Android emulator headlessly, useful for CI smoke tests.

## Why Tauri

- Tiny binaries, native window chrome, single Rust + Web stack.
- The MCP server is already in the Android app; the desktop's job is "find the device, forward the port, render the UI." That's a perfect Tauri profile — no need for a heavyweight Electron runtime.

## Planned layout

```
desktop/
├── src-tauri/          Rust glue + bundled adb
├── src/                React frontend
├── public/             Static assets
└── tauri.conf.json
```

Track progress: [#desktop label in issues](https://github.com/jcaiagent7143-ui/anyfn/labels/desktop).
