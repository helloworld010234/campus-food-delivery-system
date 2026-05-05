# Comprehensive Upgrade Ralph Blueprint

日期：2026-05-05

## 1. Purpose

This blueprint describes how to rebuild the current Ralph loop into a comprehensive Sky Delivery project upgrade loop.

This is not the Ralph execution file. It is a Markdown design artifact for a later `ralph-convert` step.

## 2. Current State

The repository currently contains a Ralph loop for multi-merchant isolation hardening. That work includes:

- `tasks/prd-multi-merchant-isolation.md`
- `scripts/ralph/prd.json`
- `scripts/ralph/progress.txt`
- `scripts/ralph/required-gates.md`
- root memory files such as `task_plan.md`, `findings.md`, and `progress.md`

This work should not be discarded. It should be archived and absorbed into the comprehensive upgrade under Agent 1.

## 3. Archive Strategy

Before overwriting `scripts/ralph/prd.json`, archive the existing Ralph run to:

```text
scripts/ralph/archive/2026-05-05-multi-merchant-isolation/
```

Archive at least:

- `scripts/ralph/prd.json`
- `scripts/ralph/progress.txt`
- optionally `scripts/ralph/required-gates.md`

The archive note should say that the old run was superseded by the comprehensive Sky Delivery upgrade and absorbed into Agent 1.

## 4. New Source PRD

The new source PRD is:

```text
tasks/prd-sky-delivery-comprehensive-upgrade.md
```

It should be converted later into:

```text
scripts/ralph/prd.json
```

Expected branch name:

```text
ralph/sky-delivery-comprehensive-upgrade
```

## 5. New Ralph Project

Recommended JSON fields:

```json
{
  "project": "Sky Delivery Comprehensive Upgrade",
  "branchName": "ralph/sky-delivery-comprehensive-upgrade",
  "description": "Evidence-driven comprehensive upgrade of the Sky Delivery project across backend security, order flows, admin reporting, miniapp UX, deployment engineering, and verification evidence.",
  "userStories": []
}
```

## 6. Story Mapping

| Story | Title | Agent |
|---|---|---|
| US-001 | Comprehensive upgrade control plane and old Ralph migration | Coordinator |
| US-002 | Backend security and permission boundary upgrade | Agent 1 |
| US-003 | Order and transaction flow upgrade | Agent 2 |
| US-004 | Admin reporting and workspace upgrade | Agent 3 |
| US-005 | Miniapp UX and API contract upgrade | Agent 4 |
| US-006 | Deployment, environment, and engineering upgrade | Agent 5 |
| US-007 | Verification system and quality evidence upgrade | Agent 6 |
| US-008 | Six-line integration, regression verification, and final report | Coordinator |

## 7. Required Gate Mapping

| Gate | Related Story |
|---|---|
| G-001 old Ralph archived and comprehensive PRD ready | US-001 |
| G-002 all 6 agent briefs complete | US-001 |
| G-003 backend security and scope verified | US-002 |
| G-004 order and transaction consistency verified | US-003 |
| G-005 admin report/workspace scope verified | US-004 |
| G-006 miniapp contract and UX flow verified | US-005 |
| G-007 deployment and environment reproducibility verified | US-006 |
| G-008 verification matrix and Feynman absorption complete | US-007 |
| G-009 integrated final report complete | US-008 |

## 8. Memory File Reset

When execution starts, update:

- `task_plan.md` to list the comprehensive upgrade stories.
- `findings.md` to reference the Feynman reports, existing multi-merchant work, and 6-agent risks.
- `progress.md` to record the transition from multi-merchant专项 to comprehensive upgrade.
- `scripts/ralph/progress.txt` to start the new run.

## 9. Execution Constraints

- Preserve unrelated dirty worktree changes.
- Stage and commit only intended files.
- Do not write temporary outputs to C drive.
- Do not treat Feynman REPL-only SKIPs as Sky Delivery defects.
- Keep current technology stack unless a story explicitly approves a new dependency.

