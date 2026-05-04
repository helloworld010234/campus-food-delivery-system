# Ralph Required Gates

## Gate Status

| Gate | Status | Evidence |
|------|--------|----------|
| G-001 Original thesis file remains untouched | pass | Original SHA256 before and after generation: `E7639ACDF4904F97F84A0CB45DFB0484B47FA8AA7BE9E3583023C9C3687A538C` |
| G-002 No source-code changes are present | pass | `git status --short -- core` returned no output |
| G-003 Upgraded DOCX exists as a separate copy | pass | `C:\Users\g'y'c\Desktop\毕业论文\初稿1-升级版.docx`, SHA256 `78A32675099E20777ADCC57271845393E2E3D060326DB314AAA9CF789BC23CBE` |
| G-004 School template elements are preserved | pass | Non-render QA opened DOCX and found 4 sections, heading structure present, header_nonempty_count=2, footer_nonempty_count=2 |
| G-005 Non-render DOCX QA passed with structural/openability evidence | pass | `docs/thesis-upgrade/work/qa-report.json`: upgraded_opens=true, zip_has_required_parts=true, zip_not_corrupt=true, tables_added=true, expected_content_present=true, no_forbidden_phrases=true |
| G-006 Ralph stories are all marked done with evidence | pass | `scripts/ralph/prd.json` marks US-001 through US-005 with `passes: true` and notes |
| G-007 Memory files are updated | pass | `task_plan.md`, `findings.md`, `progress.md`, and `scripts/ralph/progress.txt` updated for final gate state |

## Required Final Check Procedure

Before any final response that implies the work is ready:

1. Read `scripts/ralph/prd.json` and verify every `userStories[].passes` value is `true`.
2. Read this file and verify every gate has passing evidence.
3. Read `task_plan.md`, `findings.md`, and `progress.md` and confirm they reflect the latest work.
4. Run `git status --short` and confirm there are no source-code modifications.
5. Do not use rendering or page PNG checks; if any story or gate is not passing, record the blocker and continue the next Ralph iteration.
