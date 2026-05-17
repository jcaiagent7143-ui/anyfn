# Demo video script — 60 seconds

The goal: in 60 seconds, a viewer who has never heard of anyfn says "wait, I need this." No voiceover required — captions only — so the video works on muted X/LinkedIn feeds.

## Equipment

- Phone: Pixel 8 Pro or equivalent on Android 14+ (Android 16 if you want to show the Gemini integration too).
- Mac: anything modern, Claude Desktop installed.
- Recording: `scrcpy --max-fps=30 --record demo-phone.mp4` for the phone, QuickTime for the Mac.
- Composite in iMovie / DaVinci Resolve — split screen, phone right (vertical), Mac left.

## Shot list

| Time | Phone (right) | Mac (left) | Caption |
|---|---|---|---|
| 0:00 | App drawer | Claude Desktop blank | **"Google says every app needs to be rewritten."** |
| 0:03 | Open anyfn → tap Scan | (still) | **"anyfn says no."** |
| 0:06–0:25 | Scanner progress, apps flashing | (still) | **"60 apps. ~3 minutes. Once."** *(speed up to 5×)* |
| 0:25 | Registry screen | (still) | **"Every callable thing, in one list."** |
| 0:30 | Bridge → Start | Open `claude_desktop_config.json` | **"One MCP server."** |
| 0:35 | (idle, lock screen) | Type: *"Search TikTok for sunset hiking. Save the top 3 to my notes."* | **"One sentence."** |
| 0:40 | TikTok opens, search animates | Claude streams "Calling tiktok_search…" | (no caption) |
| 0:45 | Top results visible | Claude streams "Calling keep_create_note…" | (no caption) |
| 0:50 | Google Keep opens, note typed | Claude: "Saved as 'Sunset hiking inspo'." | **"Two apps. Zero developers."** |
| 0:55 | Logo card | Logo card | **"anyfn — make every app agent-ready in 60 seconds. github.com/anyfn"** |

## Captions

Use the project's font palette: white text on the bottom 1/3 of the frame, sky-blue underline on the keywords. Keep each caption on screen ≥ 1.5 seconds.

## What NOT to show

- Banking apps. Even once. Even as a counter-example. Don't.
- Your real Anthropic key. Set a dummy one for the recording or blur the Settings tab.
- The MCP secret. Same.
- Personal chats / contacts. Use a test account if recording WhatsApp.

## After recording

1. Export at 1080×1920 (vertical) for X and TikTok, plus 1920×1080 (horizontal) for YouTube + LinkedIn.
2. Drop the vertical version at `launch/press-kit/demo.gif` (also a 5-second GIF condensation for the README).
3. Drop the horizontal version on YouTube; link from the README.
