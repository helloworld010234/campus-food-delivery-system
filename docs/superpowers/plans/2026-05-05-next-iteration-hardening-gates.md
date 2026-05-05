# Sky Delivery Next Iteration Hardening Gates

日期：2026-05-05

## 1. Gate Vocabulary

| Status | Meaning |
|---|---|
| pass | Evidence exists and the requirement is satisfied. |
| blocked | The requirement could not run because of missing environment, missing product decision, or external precondition. Owner and unblock path are listed. |
| fail | The requirement ran and found a project defect. |

`pending` is allowed during execution but forbidden in the final report.

## 2. Required Gates

| Gate | Owner | Evidence Required |
|---|---|---|
| G-001 Previous state archived and protected before execution | Main thread | Archive path, dirty-state record, protection note, new loop name, baseline findings |
| G-002 Deployment hardening classified | Agent A | Docker/startup/env/deployment evidence |
| G-003 Miniapp public/private behavior decided | Agent B | Implementation, blocker, or documented product decision |
| G-004 Quality guardrails installed | Agent C | Tests/docs/review evidence for guard and mock-pay protection |
| G-005 Migration safety classified | Agent D | SQL idempotency and mapper assumption evidence |
| G-006 CI/E2E evidence classified | Agent E | Maven/Testcontainers/Playwright matrix evidence |
| G-007 ECC/Spring skill usage recorded | All agents | Each delivery summary names skills/norms used |
| G-008 Final Ralph-style report rendered | Main thread | Final report with no pending gates |

## 3. Gate Details

### G-001 Previous State Archived And Protected Before Execution

Pass evidence:

- Archive folder exists under `scripts/ralph/archive/`.
- Current Ralph files are copied into the archive before overwrite.
- New loop name is `ralph/sky-delivery-next-iteration-hardening`.
- `findings.md` lists previous baseline capabilities that must not regress.
- `git status --short` is captured before worker dispatch.
- A protection note says existing uncommitted work must not be reset, cleaned, deleted, reverted, or overwritten.
- Worker dispatch happens only after archive and protection evidence exists.

Blocked conditions:

- Repository state prevents safe archive.
- Existing changes cannot be protected without a user decision.

### G-002 Deployment Hardening Classified

Pass evidence:

- Docker Compose validation executed successfully, or Docker is explicitly not the official path and docs say so.
- Startup/stop scripts are reviewed.
- Health-check endpoint behavior is verified or corrected in docs.

Blocked conditions:

- Docker daemon unavailable.
- Required services unavailable.

### G-003 Miniapp Public/Private Behavior Decided

Pass evidence:

- Anonymous browse is implemented and verified, or product decision states it is not supported.
- Private cart/order APIs remain login-gated.
- `webscoket.js` status is resolved.
- 401 merchant-id behavior is tested or documented.

Blocked conditions:

- Product decision absent for anonymous browse.

### G-004 Quality Guardrails Installed

Pass evidence:

- `MerchantScopeUtils` mojibake cleaned or targeted follow-up recorded.
- Guard usage checklist exists.
- Mock payment branch has test/comment protection.
- ECC Java review findings are resolved or recorded.

Blocked conditions:

- Java build fails for unrelated baseline reasons and cannot be isolated.

### G-005 Migration Safety Classified

Pass evidence:

- Migration idempotency is verified or classified unsafe.
- Database docs reflect actual rerun safety.
- Mapper assumptions are documented.

Blocked conditions:

- Database tooling unavailable for optional live validation.

### G-006 CI/E2E Evidence Classified

Pass evidence:

- Maven baseline is run or blocked with reason.
- `SkyApplicationIT` is run or blocked by Docker absence.
- Playwright smoke is run or blocked by service absence.
- Verification matrix updated.

Blocked conditions:

- Docker unavailable.
- Services unavailable.

### G-007 ECC/Spring Skill Usage Recorded

Pass evidence:

- Each agent delivery summary includes a "Skills / norms used" section.
- Applicable ECC/Spring/Java/database/E2E norms are named.

Fail conditions:

- Agent performs Java/Spring changes without naming or applying relevant ECC/Spring norms.

### G-008 Final Ralph-Style Report Rendered

Pass evidence:

- Final report exists.
- All stories and gates are pass/fail/blocked.
- No pending status remains.
- Merge readiness is explicit.
