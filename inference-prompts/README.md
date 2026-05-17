# inference-prompts

Top-level prompts used by the Scanner. The default copies live inside the APK at `app/src/main/assets/prompts/`; this directory is the **canonical, version-controlled source** that PRs target.

## Files

- [`infer_functions.md`](../android/app/src/main/assets/prompts/infer_functions.md) — the main inference prompt. Tweaking this affects every scan.
- [`ui_tree_compaction.md`](../android/app/src/main/assets/prompts/ui_tree_compaction.md) — the compaction rules `UITreeExtractor.kt` enforces. The prompt and the code must stay in sync.

## Future

Community-contributed prompts will live at `inference-prompts/contrib/<handle>/`. We don't curate that folder beyond ensuring it's legal — community wisdom lives there.
