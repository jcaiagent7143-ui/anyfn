# Security Policy

## Supported versions

| Version | Supported |
|---|---|
| 0.1.x | ✅ |
| < 0.1 | ❌ |

## Reporting a vulnerability

Email **security@anyfn.dev**. Please include:

- A description of the issue and its impact
- Steps to reproduce, ideally with a minimal sample app or screen recording
- The anyfn version (`Settings → About → Version`)
- The Android version and device model
- Whether the issue is exploitable without physical access to the device

We will acknowledge receipt within **3 business days** and aim to triage within **7 days**. Coordinated-disclosure window is 90 days from initial report unless extended by mutual agreement.

Do **not** open public GitHub issues for security reports.

## Scope

In scope:

- Privilege escalation in the anyfn app itself
- MCP server flaws that allow unauthenticated remote access beyond the loopback interface
- Inference prompt injection that causes the Invoker to execute UI actions the user did not authorize
- Any path that bypasses the destructive-action confirmation prompt

Out of scope:

- Third-party apps' security models. anyfn relies on Android's Accessibility / UI Automator surface; vulnerabilities in TikTok, WhatsApp, etc. should be reported to those vendors.
- Issues that require an already-rooted device or an attacker with physical USB access.
- Social-engineering attacks against the user (e.g. tricking them into granting Accessibility permission to a malicious app — that is the broader Android model, not anyfn-specific).

## Hardening guidance for users

- Keep the MCP server bound to `127.0.0.1` (default).
- If you enable LAN mode, set a long shared secret in `Settings → Bridge → Shared secret`.
- Review and prune the registry after each scan. You do not have to expose every inferred function.
- Leave "Require confirmation for destructive actions" on.
- Do not grant Accessibility to anyfn on a device that holds banking apps you cannot afford to compromise.
