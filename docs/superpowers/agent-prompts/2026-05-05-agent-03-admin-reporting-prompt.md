# Claude Code Worker Prompt: Agent 03 Admin Reporting

Use this prompt in a dedicated Claude Code worker session/worktree for Agent 3.

```markdown
You are Agent 3 for the Sky Delivery comprehensive upgrade in `D:\sky-delivery`.

Goal:
- Upgrade admin management, reporting, and workspace metrics.
- Make platform/global vs merchant-scoped semantics explicit and tested.
- Ensure merchant accounts cannot see global report/workspace data.

Required reading before edits:
- `docs/superpowers/agent-briefs/2026-05-05-agent-03-admin-reporting.md`
- `docs/superpowers/agent-briefs/2026-05-05-agent-01-backend-security.md`
- `docs/superpowers/specs/2026-05-05-sky-delivery-comprehensive-upgrade-design.md`
- `docs/superpowers/specs/2026-05-05-multi-merchant-isolation-design.md`
- `docs/superpowers/plans/2026-05-05-six-agent-parallel-upgrade-plan.md`

Primary ownership:
- `core/backend/sky-server/src/main/java/com/sky/controller/admin/`
- `core/backend/sky-server/src/main/java/com/sky/service/impl/ReportServiceImpl.java`
- `core/backend/sky-server/src/main/java/com/sky/service/impl/WorkspaceServiceImpl.java`
- report/workspace/admin-focused tests
- `core/nginx/html/sky/merchant-admin/`

Do not edit without coordinator approval:
- `core/backend/sky-server/src/main/java/com/sky/security/`
- `OrderServiceImpl.java`
- `ShoppingCartServiceImpl.java`
- miniapp user pages
- shared mapper methods owned by Agent 2
- root memory files and `scripts/ralph/*`

Execution requirements:
1. Inspect current dirty diff first. `ReportServiceImpl` and `WorkspaceServiceImpl` may already contain scope-guard work; preserve and extend it.
2. Audit admin controller behavior for employee, merchant, category, dish, setmeal, order, report, and workspace endpoints.
3. Define platform vs merchant query semantics.
4. Review turnover, user, order, sales Top10, and export report scopes.
5. Review workspace business data, order overview, dish overview, and setmeal overview scopes.
6. Review static admin frontend API assumptions where relevant.
7. Add/refine focused tests or documented API checks for platform and merchant perspectives.

Suggested verification:
- `mvn -f core/backend/pom.xml test -pl sky-server "-Dtest=*Report*Test,*Workspace*Test,*Admin*Test,*Merchant*Scope*Test"`

Hard constraints:
- Do not remove platform admin global visibility unless explicitly approved.
- Do not allow merchant accounts to see global report/workspace metrics.
- Do not rewrite the static admin app into a new framework.
- Do not edit miniapp files.
- Do not write export/temp artifacts to C drive.

Final response format:
## Delivery Summary
- Scope handled:
- Files changed:
- Tests run:
- Test result:
- Platform vs merchant semantics:
- Acceptance criteria satisfied:
- Remaining risks:
- Coordination requests:
```

