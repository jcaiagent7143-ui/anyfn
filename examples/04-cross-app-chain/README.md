# Example: TikTok → Notes (cross-app chain)

The headline demo. The agent searches TikTok for a topic, picks the top 3 results, and saves them as a single note in Google Keep.

## Prerequisites

- TikTok and Google Keep installed and signed in
- Functions `tiktok_search` and `keep_create_note` in the registry
- Claude Desktop wired to anyfn

## Prompt

See [`prompt.txt`](prompt.txt).

## What happens

1. Claude calls `tiktok_search({ query: "sunset hiking" })`.
2. anyfn returns the top 5 results as text.
3. Claude picks the top 3 by view count and composes a Markdown summary.
4. Claude calls `keep_create_note({ title: "Sunset hiking inspo", body: "<the markdown>" })`.
5. anyfn opens Keep, taps +, types the title, tabs to body, pastes the markdown, taps back to save.
6. Claude reports: "Saved as 'Sunset hiking inspo' in Google Keep."

## Why this is the demo

Two unrelated apps. Zero developer cooperation. One sentence from the user. This is what the agentic phone is supposed to look like — and it doesn't require Spotify and Google to attend the same standards meeting.

## Files

- [`prompt.txt`](prompt.txt)
- [`claude-config.json`](claude-config.json)
- [`demo.gif`](demo.gif) — placeholder for the 30-second hero recording
