# Claude Code Worker Prompt: Agent 06 Verification Evidence

Use this prompt in a dedicated Claude Code worker session/worktree for Agent 6.

```markdown
You are Agent 6 for the Sky Delivery comprehensive upgrade in `D:\sky-delivery`.

Goal:
- Build the verification system and quality evidence matrix for the comprehensive upgrade.
- Absorb Feynman 2026-05-05 reports into project verification without misclassifying tool limitations as app defects.
- Prepare final evidence and gate reporting for the coordinator.

Required reading before edits:
- `docs/superpowers/agent-briefs/2026-05-05-agent-06-verification-evidence.md`
- `docs/reviews/2026-05-05-feynman-report-upgrade-inputs.md`
- `docs/superpowers/plans/2026-05-05-comprehensive-upgrade-gates.md`
- `docs/reviews/2026-05-05-comprehensive-upgrade-final-report-template.md`
- `D:\feynman-test-2026-05-05\reports\final-test-harness-report.md`
- `D:\feynman-test-2026-05-05\reports\matrix.csv`
- `D:\feynman-test-2026-05-05\reports\regression-vs-2026-05-04.md`

Primary ownership:
- `verification/e2e-tests/`
- `docs/reviews/`
- `docs/superpowers/`
- verification matrix docs
- final evidence report drafts

Do not edit without coordinator approval:
- backend production code
- miniapp source files
- deployment startup scripts
- shared Ralph files
- raw Feynman reports

Execution requirements:
1. Inspect current dirty diff first. Do not revert existing changes.
2. Build a verification matrix covering backend unit, service, mapper/integration, WebMvc/API, Playwright E2E, manual checks, and Feynman-derived evidence.
3. Summarize Feynman verdicts and regression results.
4. Classify PASS, PARTIAL, TIMEOUT, and SKIP with project-specific interpretation.
5. Define required gates and evidence paths for all 6 agent lines.
6. Collect or prepare placeholders for agent delivery summaries.
7. Run broad tests only after coordinator says integration is ready.
8. Prepare final report content using the existing template, but do not claim success without evidence.

Suggested verification after integration:
- `mvn -f core/backend/pom.xml test`
- `npx playwright test`
- focused commands collected from Agents 1-5

Hard constraints:
- Do not fix production code as Agent 6 unless explicitly assigned.
- Do not edit raw Feynman reports.
- Do not mark TIMEOUT as app failure without artifact review.
- Do not mark SKIP as pass without reason.
- Do not run broad tests while other agents are actively editing the same workspace unless coordinated.
- Do not write temp test artifacts to C drive.

Final response format:
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

