# Thesis Upgrade Ralph Loop Plan

## Goal
Upgrade `C:\Users\g'y'c\Desktop\毕业论文\初稿1.docx` into a separate thesis DOCX copy, using `D:\feynman-test` and `D:\sky-delivery` as evidence, while preserving the school template and avoiding any source-code changes.

## Current Mode
Ralph Loop: iterative story execution with required gates checked before any final status claim.

## Stories
- [x] US-001 Ralph control plane and memory files
- [x] US-002 Evidence inventory and upgrade map
- [x] US-003 Thesis content assets and rewrite plan
- [x] US-004 Upgraded DOCX generation
- [x] US-005 Non-render QA, gate evidence, and final packaging

## Required Gates
- [x] G-001 Original thesis file remains untouched.
- [x] G-002 No source-code changes are present.
- [x] G-003 Upgraded DOCX exists as a separate copy.
- [x] G-004 School template elements are preserved: cover, title hierarchy, headers/footers, font norms.
- [x] G-005 Non-render DOCX QA passed with structural/openability evidence.
- [x] G-006 Ralph stories are all marked done with evidence.
- [x] G-007 `task_plan.md`, `findings.md`, `progress.md`, and Ralph progress files are updated.

## Decisions
- The source-code fix track is abandoned. Code may be read as evidence only.
- The thesis must not mention the abandoned code-fix attempt or any process around it.
- Work products and intermediate files stay under `D:\sky-delivery`, not C:.
- Original thesis file is read-only source material; final deliverable is a new DOCX copy in the thesis folder unless a tool limitation requires a staging copy first.

## Active Iteration
Final Ralph gate check before response.

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| `rg.exe` access denied in this workspace | Initial context discovery | Use PowerShell-native file search and `Select-String` instead |
| Worker subagents for code repair disconnected | Isolated code-fix delegation | Abandoned code repair per user instruction; cleaned residual source/generated changes |
