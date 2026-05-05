# Claude Code Worker Prompt: Agent 01 Backend Security

Use this prompt in a dedicated Claude Code worker session/worktree for Agent 1.

```markdown
You are Agent 1 for the Sky Delivery comprehensive upgrade in `D:\sky-delivery`.

Goal:
- Upgrade backend security and permission boundaries.
- Absorb the existing multi-merchant isolation work into the broader backend security line.
- Keep public browsing behavior intact while enforcing strict private/admin boundaries.

Required reading before edits:
- `docs/superpowers/agent-briefs/2026-05-05-agent-01-backend-security.md`
- `docs/superpowers/specs/2026-05-05-sky-delivery-comprehensive-upgrade-design.md`
- `docs/superpowers/plans/2026-05-05-six-agent-parallel-upgrade-plan.md`
- `docs/superpowers/specs/2026-05-05-multi-merchant-isolation-design.md`
- `tasks/prd-multi-merchant-isolation.md`

Primary ownership:
- `core/backend/sky-common/src/main/java/com/sky/context/`
- `core/backend/sky-common/src/main/java/com/sky/constant/`
- `core/backend/sky-server/src/main/java/com/sky/interceptor/`
- `core/backend/sky-server/src/main/java/com/sky/security/`
- `core/backend/sky-server/src/test/java/com/sky/security/`
- security/scope-focused backend tests

Do not edit without coordinator approval:
- `OrderServiceImpl.java`
- `ShoppingCartServiceImpl.java`
- `ReportServiceImpl.java`
- `WorkspaceServiceImpl.java`
- mapper interfaces/XML shared with other agents
- root memory files and `scripts/ralph/*`

Execution requirements:
1. Inspect current dirty diff first. Do not revert existing changes.
2. Build or update a permission matrix for platform admin, merchant admin, merchant staff, and student user.
3. Review admin/user JWT interceptors, `BaseContext`, `MerchantScopeUtils`, `MerchantScopeGuard`, and token blacklist behavior.
4. Normalize guard semantics for:
   - admin query merchant resolution.
   - merchant write resolution.
   - explicit merchant id requirement.
   - owner merchant access assertion.
   - same-merchant assertion.
5. Add or refine focused tests for cross-merchant denial, platform visibility, missing context, token blacklist, and public browsing boundary.
6. Preserve compatibility mode when multi-merchant schema is unavailable.

Suggested verification:
- `mvn -f core/backend/pom.xml test -pl sky-server "-Dtest=*Scope*Test,*Security*Test,*Jwt*Test,*TokenBlacklist*Test"`

Hard constraints:
- Do not block public browse endpoints intentionally allowed by product rules.
- Do not rewrite auth to a new framework.
- Clear `BaseContext` before/after tests.
- Do not edit generated `target/` files.
- Do not write temp files to C drive.

Final response format:
## Delivery Summary
- Scope handled:
- Files changed:
- Tests run:
- Test result:
- Permission matrix summary:
- Acceptance criteria satisfied:
- Remaining risks:
- Coordination requests:
```

