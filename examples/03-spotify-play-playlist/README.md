# Example: Spotify play a playlist

Hands-free "play that one playlist". Non-destructive — the worst it can do is start music.

## Prerequisites

- Spotify (free or premium) signed in
- The playlist name is unique enough that the in-app search returns it as the top result
- Function `spotify_play_playlist` in the registry

## Prompt

> Using the anyfn tool spotify_play_playlist, play "Discover Weekly".

## What you'll see

The phone opens Spotify, taps Search, types "Discover Weekly", taps the first result, hits play. Returns the playlist name and the now-playing track.

## Tuning notes

Spotify's home tab reshuffles aggressively. The default inferred function searches rather than tapping a home-tab tile, because search is stable across redesigns. If you want a "play my saved playlist X" function with no search, tune the `ui_path` (Registry → tap function → edit) to navigate to Your Library directly.

## Files

- [`prompt.txt`](prompt.txt)
- [`claude-config.json`](claude-config.json)
- [`demo.gif`](demo.gif) — placeholder
