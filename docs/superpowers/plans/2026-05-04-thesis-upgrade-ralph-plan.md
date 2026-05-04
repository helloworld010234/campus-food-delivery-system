# Thesis Upgrade Ralph Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce a separate upgraded DOCX thesis while preserving the school template, using Ralph stories and required gates as the execution loop.

**Architecture:** The work is paper-only. Evidence and staged content are stored under `D:\sky-delivery`, the original DOCX remains untouched, and the final DOCX is created as a separate copy from the original template.

**Tech Stack:** DOCX artifact workflow, python-docx/OOXML helpers where needed, non-render DOCX structure QA, Ralph JSON/story tracking.

---

## File Structure

- Modify: `D:\sky-delivery\task_plan.md` for story and gate status.
- Modify: `D:\sky-delivery\findings.md` for evidence inventory and section mapping.
- Modify: `D:\sky-delivery\progress.md` for session progress and verification evidence.
- Modify: `D:\sky-delivery\scripts\ralph\prd.json` for story pass status.
- Modify: `D:\sky-delivery\scripts\ralph\required-gates.md` for gate evidence.
- Modify: `D:\sky-delivery\scripts\ralph\progress.txt` for Ralph loop notes.
- Create: `D:\sky-delivery\docs\thesis-upgrade\staged-content.md` for replacement text, diagram/table content, and DOCX insertion instructions.
- Create: `D:\sky-delivery\docs\thesis-upgrade\work\` for scripts and non-render QA outputs.
- Create: `C:\Users\g'y'c\Desktop\毕业论文\初稿1-升级版.docx` as the final DOCX copy.

## Task 1: Ralph Control And Evidence Baseline

**Files:**
- Modify: `D:\sky-delivery\task_plan.md`
- Modify: `D:\sky-delivery\findings.md`
- Modify: `D:\sky-delivery\progress.md`
- Modify: `D:\sky-delivery\scripts\ralph\prd.json`
- Modify: `D:\sky-delivery\scripts\ralph\required-gates.md`

- [x] **Step 1: Initialize Ralph files**

Create PRD, Ralph JSON, required gates, and memory files.

- [x] **Step 2: Validate Ralph JSON**

Run:

```powershell
Get-Content -Raw -Encoding UTF8 -LiteralPath "D:\sky-delivery\scripts\ralph\prd.json" | ConvertFrom-Json
```

Expected: JSON parses and exposes the project name and ordered user stories.

- [x] **Step 3: Mark US-001 and US-002 evidence state**

US-001 and US-002 are tracked in `scripts/ralph/prd.json`, `task_plan.md`, and `progress.md`.

## Task 2: Stage Thesis Upgrade Content

**Files:**
- Create: `D:\sky-delivery\docs\thesis-upgrade\staged-content.md`
- Modify: `D:\sky-delivery\scripts\ralph\prd.json`
- Modify: `D:\sky-delivery\task_plan.md`
- Modify: `D:\sky-delivery\progress.md`

- [ ] **Step 1: Create staging directory**

Run:

```powershell
New-Item -ItemType Directory -Force -Path "D:\sky-delivery\docs\thesis-upgrade" | Out-Null
```

Expected: directory exists.

- [ ] **Step 2: Write staged content**

Write `staged-content.md` with:

- replacement rules for abstract, Chapter 1, Chapter 2, Chapter 4, Chapter 5, Chapter 6, and appendices;
- architecture diagram content;
- deployment diagram content;
- ER relationship content;
- role-permission matrix;
- detailed test-case table;
- interface example content;
- style constraints from `C:\Users\g'y'c\Desktop\论文提示词.md`.

- [ ] **Step 3: Validate staging content**

Run:

```powershell
Select-String -Encoding UTF8 -LiteralPath "D:\sky-delivery\docs\thesis-upgrade\staged-content.md" -Pattern "本次修复|Feynman 发现 bug 后已修复|至于.*呢"
```

Expected: no matches.

- [ ] **Step 4: Mark US-003 passing if all acceptance criteria are met**

Update Ralph files only after the staging file exists and validation has no forbidden phrases.

## Task 3: Generate The Upgraded DOCX

**Files:**
- Create: `D:\sky-delivery\docs\thesis-upgrade\work\build_thesis_upgrade.py`
- Create: `C:\Users\g'y'c\Desktop\毕业论文\初稿1-升级版.docx`
- Modify: `D:\sky-delivery\scripts\ralph\prd.json`
- Modify: `D:\sky-delivery\progress.md`

- [ ] **Step 1: Record original hash**

Run:

```powershell
Get-FileHash -Algorithm SHA256 -LiteralPath "C:\Users\g'y'c\Desktop\毕业论文\初稿1.docx"
```

Expected: hash is recorded in progress and required gates.

- [ ] **Step 2: Build upgraded DOCX from original template**

Use a script in `D:\sky-delivery\docs\thesis-upgrade\work\` to copy the original DOCX and apply controlled content upgrades. The script must not modify the original DOCX.

- [ ] **Step 3: Verify original hash is unchanged**

Run the same `Get-FileHash` command again.

Expected: original hash matches the recorded hash.

- [ ] **Step 4: Check no source-code changes**

Run:

```powershell
git status --short
```

Expected: no modified files under `core/`.

## Task 4: Non-render DOCX QA

**Files:**
- Create: `D:\sky-delivery\docs\thesis-upgrade\work\qa-report.json`
- Modify: `D:\sky-delivery\scripts\ralph\required-gates.md`
- Modify: `D:\sky-delivery\scripts\ralph\prd.json`
- Modify: `D:\sky-delivery\task_plan.md`
- Modify: `D:\sky-delivery\progress.md`

- [ ] **Step 1: Open DOCX with tooling**

Run a non-render QA script from `D:\sky-delivery\docs\thesis-upgrade\work\` to open the upgraded DOCX and inspect package structure.

```powershell
python "D:\sky-delivery\docs\thesis-upgrade\work\qa_docx_no_render.py"
```

Expected: JSON/text QA report is generated without rendering pages.

- [ ] **Step 2: Inspect DOCX structure**

Check sections, paragraphs, tables, images, styles, headers/footers, and package relationships. Record that this is not a visual page-layout check.

- [ ] **Step 3: Update required gates**

Record QA command, original hash comparison, git status, and non-render structural QA notes.

- [ ] **Step 4: Mark US-004 and US-005 only if gate evidence is present**

Do not mark final stories as passing until all required gates have evidence.

## Self-Review Checklist

- The plan covers all five Ralph stories.
- The plan contains no source-code modification task.
- The final check requires fresh evidence before any readiness statement.
- No placeholders remain for implementation decisions that would block the next story.
