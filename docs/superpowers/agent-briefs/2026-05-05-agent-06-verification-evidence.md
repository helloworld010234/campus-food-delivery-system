# Agent 06 Brief: 验证体系与质量证据

日期：2026-05-05

## 1. Mission

Agent 6 负责验证体系、质量门禁和证据归档。目标是把 Maven、Playwright、API/manual checks、Feynman 报告和各 agent 交付结果汇总为可追踪的质量矩阵。

## 2. Background

Feynman 2026-05-05 测试报告包含 PASS、PARTIAL、TIMEOUT、SKIP 和回归对比。Sky Delivery 项目已有 Maven 测试、Playwright E2E 和若干文档证据，但需要统一质量门禁，避免把工具限制误判为项目缺陷。

## 3. Input Materials

- `D:\feynman-test-2026-05-05\reports\final-test-harness-report.md`
- `D:\feynman-test-2026-05-05\reports\matrix.csv`
- `D:\feynman-test-2026-05-05\reports\regression-vs-2026-05-04.md`
- `docs/reviews/2026-05-05-feynman-report-upgrade-inputs.md`
- `docs/superpowers/plans/2026-05-05-comprehensive-upgrade-gates.md`
- `docs/reviews/2026-05-05-comprehensive-upgrade-final-report-template.md`
- `verification/e2e-tests/`

## 4. Primary File Ownership

Primary write scope:

- `verification/e2e-tests/`
- `docs/reviews/`
- `docs/superpowers/`
- verification matrix docs
- final evidence report drafts

Coordinate before editing:

- backend production code
- miniapp source files
- deployment startup scripts
- shared Ralph files

## 5. Forbidden Scope

Agent 6 must not:

- fix production code while acting as verification owner unless explicitly assigned.
- mark TIMEOUT as functional failure without artifact review.
- mark SKIP as pass without documented reason.
- overwrite raw Feynman reports.
- edit unrelated dirty files.
- claim full pass if any gate is pending or blocked.

## 6. Execution Tasks

1. Build a verification matrix with categories:
   - backend unit tests.
   - service tests.
   - mapper/integration checks.
   - WebMvc/API tests.
   - Playwright E2E.
   - manual checks.
   - Feynman-derived evidence.
2. Summarize Feynman reports:
   - total verdict counts.
   - Layer 1 stable checks.
   - Layer 2/3 REPL-only SKIPs.
   - Layer 4 PASS/PARTIAL/TIMEOUT.
   - regression vs 2026-05-04.
3. Define quality gates:
   - required pass checks.
   - allowed skip conditions.
   - timeout interpretation.
   - evidence path requirements.
4. Coordinate with other agents:
   - collect focused test commands.
   - collect delivery summaries.
   - capture blockers and residual risks.
5. Run or design final checks:
   - `mvn -f core/backend/pom.xml test`.
   - `npx playwright test`.
   - focused API/manual checks.
6. Prepare final report using the template.

## 7. Execution-Stage Notes

- Start by separating app verification from Feynman tool verification. A Feynman slash-command SKIP is not a Sky Delivery app failure.
- Preserve raw Feynman reports as immutable inputs. Write summaries in project docs instead of editing source reports.
- Treat PARTIAL artifacts as evidence requiring review, not automatic success.
- Treat TIMEOUT as an execution outcome. Check whether the artifact was produced before deciding risk.
- For SKIP, record the exact missing precondition or interaction limitation.
- Use absolute paths in evidence references when useful for local review.
- Do not run broad tests while other agents are actively editing the same workspace unless coordinated.
- Prefer running final broad tests after integration order is complete.
- If Playwright requires services to be running, record startup assumptions and coordinate with Agent 5.
- If Maven tests fail because of unrelated dirty generated artifacts, document the root cause rather than cleaning user changes.
- Do not delete target directories or generated reports unless coordinator approves.
- Do not write temporary test artifacts to C drive.
- Avoid inflated success language. Gates are pass, pending, blocked, or fail with evidence.

## 8. Suggested Verification

Final commands after integration:

```powershell
mvn -f core/backend/pom.xml test
```

```powershell
npx playwright test
```

Focused commands should be collected from Agent 1-5 delivery summaries.

## 9. Acceptance Criteria

- Feynman report absorption matrix exists.
- Verification matrix lists commands, expected scope, results, and evidence paths.
- Required gates are mapped to agent outputs.
- PASS/PARTIAL/TIMEOUT/SKIP interpretation is documented.
- Final report template is filled only after evidence is available.

## 10. Delivery Summary Format

```markdown
## Delivery Summary

- Scope handled:
- Files changed:
- Tests run:
- Test result:
- Feynman report interpretation:
- Gate status:
- Acceptance criteria satisfied:
- Remaining risks:
- Coordination requests:
```

