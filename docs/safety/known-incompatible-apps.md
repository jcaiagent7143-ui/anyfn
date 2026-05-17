# Known incompatible apps

A living list of apps anyfn does not currently work with, and why. Open a PR to add to it.

## By policy — banking, payments, 2FA, password managers

We **deliberately** do not support any app whose primary purpose is to authorise spending, hold credentials, or generate authentication codes.

- Chase, BoA, Wells Fargo, Citi, Capital One
- Revolut, Wise, Monzo, N26
- Cash App, Venmo, Zelle, Apple Cash
- PayPal, Stripe Dashboard
- DBS digibank, OCBC, Maybank, CIMB
- 1Password, Bitwarden, LastPass, Dashlane, Apple Passwords, Google Password Manager
- Authy, Google Authenticator, Microsoft Authenticator, Duo Mobile
- Yubico Authenticator, FreeOTP

For these, anyfn surfaces `FailureReason.SECURE_SCREEN_BLOCKED` (or returns zero inferred functions) and refuses to retry.

## By app behaviour — `FLAG_SECURE` on all screens

These apps mark every window `FLAG_SECURE`, which blanks the Accessibility tree. Scan returns zero functions; invocations fail with `SECURE_SCREEN_BLOCKED`.

- Signal (by design — encrypted messaging)
- Telegram (when "Hide content in app switcher" is on)
- Most adult content apps
- Many enterprise MDM-managed apps

## By app behaviour — anti-automation

These apps detect UI Automator and refuse to operate. Scan may succeed; invocations get stuck or trigger CAPTCHAs.

- Pokémon GO
- Various crypto wallets (Trust Wallet, MetaMask, Phantom — they set `FLAG_SECURE` on key views)
- Some gambling apps in regulated regions

## Partially supported

These work for read-only functions but fail on actions that require login flows we won't automate.

- Gmail — read ok, send ok, account switching not automated
- Instagram — post/follow/search ok, login not automated
- TikTok — search/save/follow ok, accept-terms popups need manual dismissal once
- WhatsApp Web flows (linking a device) — not automated

## Reporting a new incompatibility

Open a [new app support issue](../../.github/ISSUE_TEMPLATE/new_app_support.md). Include the package name, app version, region, and the UI dump from `Settings → Debug → Dump UI trees`. We'll either add it here or fix the inference and add it to the supported table.
