# FAQ

## Why does anyfn exist?

Google's plan for the agentic phone is `@AppFunction`: every Android developer rewrites their app, ships a new build, and waits for users to update. We bet most apps will not do this in our lifetimes. anyfn covers the gap until they do.

## Will Google's AppFunctions kill anyfn?

We hope so. The day Spotify, WhatsApp, and TikTok all ship native AppFunctions is the day anyfn becomes a backstop. Until then, anyfn fills the void.

## Is this an accessibility-abusing keylogger?

No. anyfn never logs keystrokes; typing is done via UI Automator, not by recording IME events. UI content is only sent off-device during a scan, and only to the LLM provider you configured. Read [safety/permissions-model.md](safety/permissions-model.md) for the full surface.

## Does it work on iOS?

No. iOS doesn't expose anything comparable to AccessibilityService / UI Automator at the third-party level. The closest analogue is Apple's App Intents — same problem as Android AppFunctions, every developer has to rewrite. Theory: someone will build the iOS equivalent of anyfn around Apple's Vision framework and screen recording. We're not building it.

## What about ROOT? Magisk?

Not needed. anyfn relies on documented Android APIs only — Accessibility, UI Automator, foreground services. If you root your phone, anyfn keeps working but doesn't get any new powers.

## How is this not a security disaster?

It can be a disaster on a careless device — same as letting Tasker, MacroDroid, or any other accessibility automation tool drive your apps. Three mitigations:

1. The bridge binds to loopback by default. LAN mode requires a shared secret.
2. Destructive functions require an explicit `__confirm` token per call.
3. Banking / 2FA / password managers are unsupported by design.

If those don't fit your threat model, don't install. We don't pretend anyfn is appropriate for a corporate-managed device with sensitive data on it.

## Why MCP and not some custom protocol?

Because the AI agent ecosystem is converging on MCP, and anyfn working in Claude Desktop, Cursor, Gemini, and (soon) every other MCP-aware agent for free is worth more than any custom wire format we'd invent.

## Why open source?

Because no closed-source app can earn the level of trust this kind of capability requires. The code is the contract. Read it before you grant Accessibility.

## How much does inference cost?

With Claude Sonnet 4.6: about $0.30 for a full scan of a 60-app phone. Rescans of a single app: about $0.005. Gemini Nano on-device: free.

## Will you publish to the Play Store?

No. Google will reject anyfn for the same reasons they reject every legitimate accessibility tool — they cannot tell us apart from malware. F-Droid is on the roadmap for v0.3.
