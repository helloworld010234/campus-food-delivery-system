# Single-Window Dispatch Prompt: Sky Delivery Next Iteration Hardening

Copy this entire prompt into Claude Code. It is designed for a single Claude Code window that can dispatch multiple agents concurrently.

```markdown
请在 `D:\sky-delivery` 中执行 **Sky Delivery Next Iteration Hardening**。

你必须使用单窗口多 agent 并行执行模型：先完成 archive-and-protect 预飞硬门禁，然后一次性并行派发 5 个 worker agents。不要串行地一个一个跑。

## Required Reading

先读取这些文件：

1. `docs/reviews/2026-05-05-next-iteration-handoff.md`
2. `docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md`
3. `docs/reviews/2026-05-05-comprehensive-upgrade-verification-matrix.md`
4. `tasks/prd-sky-delivery-next-iteration-hardening.md`
5. `docs/superpowers/specs/2026-05-05-next-iteration-hardening-design.md`
6. `docs/superpowers/plans/2026-05-05-next-iteration-hardening-ralph-plan.md`
7. `docs/superpowers/plans/2026-05-05-next-iteration-hardening-gates.md`
8. `docs/superpowers/plans/2026-05-05-ecc-skill-usage-matrix.md`
9. `docs/superpowers/agent-prompts/2026-05-05-next-hardening-agent-a-deployment.md`
10. `docs/superpowers/agent-prompts/2026-05-05-next-hardening-agent-b-miniapp.md`
11. `docs/superpowers/agent-prompts/2026-05-05-next-hardening-agent-c-quality.md`
12. `docs/superpowers/agent-prompts/2026-05-05-next-hardening-agent-d-migration.md`
13. `docs/superpowers/agent-prompts/2026-05-05-next-hardening-agent-e-ci-evidence.md`

## Hard Gate: Archive And Protect Before Any Worker

在派发任何 worker 之前，必须完成以下步骤。不能跳过。

1. 运行并记录 `git status --short`。
2. 把当前 dirty state 分组记录：source changes、docs、Ralph files、generated `target/` artifacts、untracked outputs、archives。
3. 归档当前 Ralph/memory 文件，至少包括：
   - `scripts/ralph/prd.json`
   - `scripts/ralph/progress.txt`
   - `scripts/ralph/required-gates.md`
   - `task_plan.md`
   - `findings.md`
   - `progress.md`
4. 归档目录使用 `scripts/ralph/archive/<YYYY-MM-DD>-next-hardening-preflight/` 或更精确的新专项名。
5. 在新一轮 `progress.md` 或 `scripts/ralph/progress.txt` 中写入保护说明：当前未提交改动已观察，不得 reset、clean、delete、checkout、revert 或覆盖。
6. 如果无法安全归档或保护，停止执行并报告 blocker。不要派发 worker。

## New Ralph Loop

预飞成功后，建立新 Ralph loop：

- Branch name / loop name: `ralph/sky-delivery-next-iteration-hardening`
- Stories: US-001 through US-007 from `tasks/prd-sky-delivery-next-iteration-hardening.md`
- Gates: G-001 through G-008 from `docs/superpowers/plans/2026-05-05-next-iteration-hardening-gates.md`

## Parallel Dispatch

预飞和新 loop 初始化完成后，一次性并行派发 5 个 worker agents：

1. Agent A — Deployment Hardening  
   Prompt: `docs/superpowers/agent-prompts/2026-05-05-next-hardening-agent-a-deployment.md`

2. Agent B — Miniapp Anonymous Browse & Cleanup  
   Prompt: `docs/superpowers/agent-prompts/2026-05-05-next-hardening-agent-b-miniapp.md`

3. Agent C — Quality Guardrails  
   Prompt: `docs/superpowers/agent-prompts/2026-05-05-next-hardening-agent-c-quality.md`

4. Agent D — Migration & Database Safety  
   Prompt: `docs/superpowers/agent-prompts/2026-05-05-next-hardening-agent-d-migration.md`

5. Agent E — CI/E2E Evidence  
   Prompt: `docs/superpowers/agent-prompts/2026-05-05-next-hardening-agent-e-ci-evidence.md`

必须并行派发，不要等待 Agent A 完成后再派发 Agent B。

## ECC / Standards Requirement

所有 worker 必须高强度使用适用的 ECC/Spring/Java skills 或规范，能用就用。至少要在交付摘要中写明：

- 用了哪些 skills / norms。
- 它们影响了哪些实现或验证决策。
- 因为这些规范额外跑了哪些验证。

参考：`docs/superpowers/plans/2026-05-05-ecc-skill-usage-matrix.md`

## Coordinator-Lite Role

主线程只做：

- 预飞归档和保护。
- 初始化 Ralph 状态。
- 一次性派发 5 个 agents。
- 收集 worker summaries。
- 更新 story/gate 状态。
- 渲染最终 Ralph-style report。

不要在 worker 运行前抢先实现 worker 的任务。

## Final Report

所有 worker 完成后，使用：

`docs/reviews/2026-05-05-next-hardening-final-report-template.md`

生成最终报告。报告必须包含：

- story status。
- gate status。
- changed files。
- verification evidence。
- blocker / follow-up。
- ECC skills / norms usage table。
- merge readiness。

最终状态中不能有 `pending` gate。只能是 `pass`、`fail`、`blocked`，并带 evidence / owner。

现在开始：先完成 archive-and-protect 预飞硬门禁，然后一次性并行派发 5 个 worker agents。
```

