# X launch thread — 8 tweets

Pin tweet 1. Drop one tweet every 30 seconds for the first few minutes so the algorithm sees engagement on a moving thread.

---

**1/ [hook + demo]**

Google says every Android app needs a rewrite to be agentic.

We refuse.

anyfn turns every app already on your phone into a tool any AI can call. 60 seconds to set up. MIT licensed.

[VIDEO: 60-second demo]

github.com/jcaiagent7143-ui/anyfn

---

**2/ [what it does]**

What anyfn actually does:

1. Walks every installed app
2. Reads their UI through Accessibility
3. Asks an LLM "what callable things does this expose?"
4. Re-publishes as MCP + Android 16 AppFunctions
5. Replays via UI Automator on every call

No SDK. No code change. No waiting for devs.

---

**3/ [demo blow-by-blow]**

The hero move:

Me, in Claude Desktop on my Mac:
> "Search TikTok for sunset hiking. Save the top 3 to my notes app."

Phone unlocks itself? No. I unlock it.
Then: TikTok opens. Search runs. Results read back. Keep opens. Note typed. Saved.

Two apps. Zero devs. One sentence.

---

**4/ [why MCP]**

We picked MCP because the agent world is converging on it.

Wire anyfn to Claude Desktop, Cursor, Gemini, GPT, your local Llama — same registry, every client.

```
{
  "mcpServers": {
    "anyfn": { "command": "wscat", "args": ["-c", "ws://localhost:5174/ws"] }
  }
}
```

That's the whole config.

---

**5/ [safety]**

What anyfn won't do:

— Type passwords
— Touch banking, 2FA, or password manager apps
— Work around FLAG_SECURE
— Phone home (zero telemetry)
— Unlock your device

The list of incompatible apps is documented up front. We don't pretend otherwise.

---

**6/ [the why]**

Spotify hasn't shipped an Assistant-friendly intent in 3 years.
WhatsApp has zero AppFunctions.
Grab, Lazada, TikTok — nothing.

If we wait for app developers to opt in, we'll wait forever.

anyfn opts them in for us.

---

**7/ [open source]**

It's MIT. The repo includes:

— full Android source (Kotlin, Compose, Hilt, Ktor)
— pure-Kotlin core module for headless test rigs
— marketing site
— 5 worked examples (incl. cross-app chain)
— a Tauri desktop companion stub for v0.2

PRs especially welcome for new app support.

github.com/jcaiagent7143-ui/anyfn

---

**8/ [CTA]**

If you've ever wanted your phone to just *do the thing* without waiting for every app developer in the world to ship an SDK update, this is for you.

⭐ on GitHub if you like the direction.

Discord: discord.gg/anyfn

We ship.

---

## Repost angle (24h later)

> 24h in, anyfn is at <N> stars and <N> people in Discord. Most-requested apps so far: Lazada, Shopee, Telegram. Working on inference tuning for all three.
>
> The thing nobody expected: it's a hit in SEA. Half the early users are in Singapore, Jakarta, Manila.
>
> We see you.
