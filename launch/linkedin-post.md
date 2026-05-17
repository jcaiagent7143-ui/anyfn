# LinkedIn launch post

Slightly longer than the X thread, slightly less irreverent, but the same core message. Lead with the demo video; LinkedIn's algorithm rewards video.

---

Last week I watched a senior engineer at a major SEA fintech tell me, with a straight face, that their "agentic strategy" was to wait for Google to finish App Functions and then evaluate whether to invest 18 months of engineering in adopting it.

That is the standard answer. It is also the wrong answer. Every Android app maker is being asked to make a 12-to-24-month bet on a single OS vendor's idea of how AI agents will talk to apps — and to do it before the standards are stable, before the user demand is proven, and before competitors have moved.

Most of them will not.

**anyfn** is a bet that they don't have to.

It's an open-source Android runtime, MIT-licensed, that turns every app already installed on your phone into a callable tool for AI agents. Not by asking developers to rewrite anything. Not by waiting for a standards committee. By reading the apps' UIs through Android's Accessibility framework, using an LLM to infer what they can do, and replaying the resulting action paths through UI Automator when an agent calls them.

What it gives you, today:

→ A standard Model Context Protocol (MCP) server that Claude Desktop, Cursor, Gemini, and any compatible agent can talk to.
→ On Android 16, native AppFunction registration — Gemini Nano discovers anyfn's tools like first-party actions.
→ A local function registry that survives offline, with per-function user overrides.
→ Per-call destructive-action confirmation, so an agent cannot send money or post publicly without your explicit ok.
→ Zero telemetry. Everything runs on the device. The only network call anyfn makes is to your chosen LLM provider, at scan time.

The headline demo: from Claude Desktop on a Mac, I can say "Search TikTok for sunset hiking, save the top three to my notes app." Two completely unrelated apps. Zero developer cooperation. One sentence from me.

What anyfn deliberately refuses to do is also important. It does not work with banking apps, password managers, 2FA, or any app that sets FLAG_SECURE. It does not type passwords. It does not unlock the device. The threat model is documented up front in the repo. You will not be surprised.

The repo is at github.com/jcaiagent7143-ui/anyfn. It includes the full source, an APK, five worked examples, complete documentation, and a roadmap to v1.0.

If you build agentic experiences and you are tired of waiting for the long tail of app developers to opt in, this is your shortcut. If you build apps and are tired of being asked to add yet another integration, this is your reprieve.

I'd love your feedback, particularly on which apps you'd most want to see anyfn handle out of the box.
