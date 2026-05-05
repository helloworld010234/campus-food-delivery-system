# Sky Delivery Next Iteration Hardening Design

日期：2026-05-05

## 1. Purpose

This design defines the next Ralph-style hardening task package for `D:\sky-delivery`.

The current session only creates executable Markdown task artifacts. It does not implement the tasks, run tests, rewrite Ralph JSON, or dispatch agents.

## 2. Baseline

The previous comprehensive upgrade is considered the baseline. The next iteration must read and preserve:

- `docs/reviews/2026-05-05-next-iteration-handoff.md`
- `docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md`
- `docs/reviews/2026-05-05-comprehensive-upgrade-verification-matrix.md`
- `scripts/ralph/prd.json`
- `scripts/ralph/required-gates.md`

Baseline capabilities that must not regress:

- `MerchantScopeGuard` is the central merchant-scope guard.
- Mock WeChat Pay branch remains available for local and CI tests.
- Multi-merchant isolation is already absorbed into the previous comprehensive upgrade.
- Feynman SKIP/TIMEOUT rows are tool/environment limitations unless app code evidence proves otherwise.
- Coordinator-owned Ralph/memory files are special and should only be updated in the final integration step.

## 2.1 Archive-And-Protect Hard Gate

The task package itself does not archive, stage, commit, test, or execute project changes.

The future Claude Code execution must complete archive-and-protect preflight before dispatching any worker agent. If this preflight cannot be completed, execution must stop and report a blocker.

Required archive-and-protect actions:

1. Capture `git status --short` before any new work.
2. Record current dirty-state categories: source changes, docs, Ralph files, generated `target/` artifacts, untracked outputs, and archives.
3. Archive current Ralph/memory files before rewriting them:
   - `scripts/ralph/prd.json`
   - `scripts/ralph/progress.txt`
   - `scripts/ralph/required-gates.md`
   - `task_plan.md`
   - `findings.md`
   - `progress.md`
4. Preserve prior verified baseline reports:
   - `docs/reviews/2026-05-05-next-iteration-handoff.md`
   - `docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md`
   - `docs/reviews/2026-05-05-comprehensive-upgrade-verification-matrix.md`
5. Do not reset, checkout, clean, delete, or overwrite existing uncommitted changes.
6. Write a short preflight note into the next run's `progress.md` or Ralph progress file before worker dispatch.
7. Only after the archive path and protection note exist may worker agents start.

## 3. Scope

The next iteration is **comprehensive hardening**, not broad feature expansion. It targets deferred risks from the handoff:

1. Deployment verification and Docker hardening.
2. Miniapp anonymous public browse decision and cleanup.
3. Quality guardrails for scope checks and mock payment preservation.
4. Multi-merchant migration safety and SQL assumptions.
5. CI/E2E evidence classification and verification matrix updates.

## 4. Agent Model

The future Claude Code execution should use five parallel worker agents:

| Agent | Workstream | Primary Outcome |
|---|---|---|
| Agent A | Deployment Hardening | Docker/startup/deployment status becomes verified or explicitly blocked |
| Agent B | Miniapp Anonymous Browse & Cleanup | Public/private miniapp behavior is intentional and documented |
| Agent C | Quality Guardrails | Guard usage, mock pay protection, mojibake cleanup, and review gates are hardened |
| Agent D | Migration & Database Safety | Migration rerun safety and mapper assumptions are classified |
| Agent E | CI/E2E Evidence | CI/E2E feasibility and evidence matrix are updated |

The dispatch prompt should ask Claude Code to launch all five agents at once in a single window only after the archive-and-protect hard gate passes. The main thread should collect results and produce the final Ralph report after the workers finish.

## 5. ECC And Standards Requirement

Every future agent must use relevant skills or norms aggressively:

- `ecc-java-reviewer` for Java/Spring/service/security/test review.
- `ecc-java-build-resolver` for Maven, compilation, Docker-Java build, and dependency issues.
- `springboot-tdd` for Java feature/bugfix/refactor tests.
- `springboot-verification` for build/test/security/diff verification.
- `java-coding-standards` for Java naming, immutability, exceptions, Optional, streams, and layout.
- `springboot-patterns` for controller/service/mapper layering.
- `springboot-security` for auth, authorization, validation, secrets, CSRF, and headers.
- `database-migrations` for schema changes, rerun safety, rollback, and zero-downtime cautions.
- `jpa-patterns` or MyBatis-equivalent review principles where mapper/data-access assumptions matter.
- `playwright` and E2E verification practices for browser-facing checks.

If a skill is unavailable in the future executor, the prompt should require using the local standard documents or naming the missing skill and applying the closest documented norm.

## 6. Ralph Stories

The next Ralph loop should use these stories:

| Story | Workstream |
|---|---|
| US-001 | Ralph hardening control plane and previous state archive |
| US-002 | Deployment hardening |
| US-003 | Miniapp anonymous browse and cleanup |
| US-004 | Quality guardrails |
| US-005 | Migration and database safety |
| US-006 | CI/E2E evidence |
| US-007 | Parallel result integration and Ralph final report |

## 7. Gate Model

Each gate must be evidence based:

- `pass`: command, file, report, or review evidence exists.
- `blocked`: environment missing or product decision required, with owner.
- `fail`: runnable check produced a project defect.

No gate may stay `pending` in the final report.

## 8. Error Handling

The future executor must classify blockers instead of guessing:

- Docker unavailable: mark Docker/Testcontainers checks as `blocked`, not `fail`.
- Services not running: mark Playwright full E2E as `blocked`, not `fail`.
- Product decision missing: mark anonymous browse implementation as `blocked` or implement the conservative documented choice.
- Feynman SKIP/TIMEOUT: keep as tool limitation unless project code evidence says otherwise.

## 9. Testing Strategy

The task package should make tests explicit but not run them in this session. Future execution should prefer:

1. Focused Maven tests for changed Java areas.
2. Maven full module baseline when feasible.
3. Docker/Testcontainers only when Docker daemon is available.
4. Playwright smoke before full E2E.
5. Manual API checks where automation is not available.
6. Diff review with ECC Java Reviewer norms before final report.

## 10. Deliverables

This design produces Markdown artifacts only:

- source PRD.
- Ralph hardening plan.
- gate design.
- ECC skill usage matrix.
- single-window dispatch prompt.
- five worker prompts.
- Ralph final report template.

## 11. Acceptance Criteria

- All deliverables are Markdown files.
- No business code is changed by this task-package generation.
- Future Claude Code can start from one prompt and dispatch all five agents concurrently.
- Every worker prompt includes ECC/Spring/Java skill requirements and file ownership boundaries.
- The final report template is Ralph-style and evidence-first.
