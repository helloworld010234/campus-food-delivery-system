# Claude Code Parallel Prompt Index

日期：2026-05-05

Use these prompts to run the Sky Delivery comprehensive upgrade with one coordinator and six parallel Claude Code workers.

## Coordinator

- `docs/superpowers/agent-prompts/2026-05-05-coordinator-prompt.md`

## Workers

- `docs/superpowers/agent-prompts/2026-05-05-agent-01-backend-security-prompt.md`
- `docs/superpowers/agent-prompts/2026-05-05-agent-02-order-transaction-prompt.md`
- `docs/superpowers/agent-prompts/2026-05-05-agent-03-admin-reporting-prompt.md`
- `docs/superpowers/agent-prompts/2026-05-05-agent-04-miniapp-ux-prompt.md`
- `docs/superpowers/agent-prompts/2026-05-05-agent-05-devops-engineering-prompt.md`
- `docs/superpowers/agent-prompts/2026-05-05-agent-06-verification-evidence-prompt.md`

## Recommended Dispatch Pattern

1. Start the coordinator session first.
2. Let the coordinator inspect dirty state and decide whether to create worktrees or use strict file ownership in one branch.
3. Start six worker sessions with their matching prompts.
4. Workers should not edit shared Ralph/memory files.
5. Workers return delivery summaries to the coordinator.
6. Coordinator integrates in order: Agent 1, Agent 2, Agent 3, Agent 4, Agent 5, Agent 6.
7. Coordinator runs final gates and fills the final report.

## Worktree Option

If using git worktrees, create one worktree per worker branch, for example:

```powershell
git worktree add ..\sky-delivery-agent-01 -b codex/agent-01-backend-security
git worktree add ..\sky-delivery-agent-02 -b codex/agent-02-order-transaction
git worktree add ..\sky-delivery-agent-03 -b codex/agent-03-admin-reporting
git worktree add ..\sky-delivery-agent-04 -b codex/agent-04-miniapp-ux
git worktree add ..\sky-delivery-agent-05 -b codex/agent-05-devops-engineering
git worktree add ..\sky-delivery-agent-06 -b codex/agent-06-verification-evidence
```

Only run these commands if the coordinator chooses the worktree strategy and the current branch state is safe.

## Single-Branch Option

If staying in one branch, enforce file ownership from:

- `docs/superpowers/plans/2026-05-05-six-agent-parallel-upgrade-plan.md`

The coordinator must integrate one worker at a time and review conflicts manually.

