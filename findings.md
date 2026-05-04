# Thesis Upgrade Findings

## Active Evidence Sources

- Original thesis DOCX: `C:\Users\g'y'c\Desktop\毕业论文\初稿1.docx`
- Extracted thesis text: `D:\feynman-test\inputs\thesis.txt`
- Thesis markdown source from Feynman: `D:\feynman-test\inputs\thesis.md`
- Design spec: `D:\sky-delivery\docs\superpowers\specs\2026-05-04-thesis-upgrade-design.md`
- Writing style prompt: `C:\Users\g'y'c\Desktop\论文提示词.md`

## Key Feynman Outputs

- `D:\feynman-test\outputs\peer_review_thesis.md`: identifies missing architecture/ER diagrams, thin testing section, weak Redis/transaction/security discussion, and lack of technical comparison.
- `D:\feynman-test\outputs\thesis-code-audit.md`: confirms JWT, WebSocket, scheduled order task design, AOP autofill, and row-level merchant scoping; warns that `merchant_id` is row-level tenanting, not strict schema isolation.
- `D:\feynman-test\outputs\thesis_vs_codebase_comparison.md`: confirms Spring Boot 3.4.4, MyBatis, MySQL driver, Redis, JWT, uni-app, and WebSocket; warns that Vue 2 exists only inside uni-app and static/admin assets, not as a standalone Vue 2 source project.
- `D:\feynman-test\outputs\literature-review-food-delivery-multitenant-notifications.md`: provides comparison material for food delivery architecture, multi-tenant database patterns, and real-time notification choices.
- `D:\feynman-test\reports\final-test-harness-report.md`: summarizes Feynman results and points to useful artifacts, but code-fix suggestions are not part of this paper-only task.

## Source-Code Evidence Areas

- Backend Maven modules: `D:\sky-delivery\core\backend\pom.xml`, `sky-common`, `sky-pojo`, `sky-server`.
- Multi-merchant migration: `D:\sky-delivery\core\backend\scripts\phase1_multi_merchant_schema.sql`.
- JWT interceptors and context: `JwtTokenAdminInterceptor.java`, `JwtTokenUserInterceptor.java`, `BaseContext.java`, `JwtUtil.java`.
- Merchant scope: `MultiMerchantSchemaSupport.java`, `MerchantScopeUtils.java`, account type constants.
- Order workflow: `OrderServiceImpl.java`, `OrdersMapper.java`, mapper XML files.
- WebSocket notification: `WebSocketServer.java`, `WebSocketConfiguration.java`, order notification calls.
- Miniapp: `D:\sky-delivery\core\miniapp\manifest.json`, `pages.json`, `utils\merchant.js`, `utils\request.js`.
- Web management assets: `D:\sky-delivery\core\nginx\html\sky` and `merchant-admin`.

## Upgrade Map

| Thesis Area | Upgrade Type | Evidence Basis |
|-------------|--------------|----------------|
| Abstract and keywords | Text rewrite | Correct architecture and boundary language |
| Chapter 1 | Text refinement | Campus scenario and evidence-bounded contribution |
| Chapter 2 | Technical comparison table/text | Literature review and source stack evidence |
| Chapter 4 | New diagrams/tables plus text rewrite | Architecture, deployment, ER, role-permission matrix |
| Chapter 5 | Targeted text rewrite | Code audit, workflow source files, current thesis structure |
| Chapter 6 | Major test table expansion | Peer review, final-test report, source-verifiable flows |
| Appendices | Interface/table enrichment | Existing code paths and mapper/entity fields |

## Claims To Avoid Or Reframe

- Avoid claiming strict schema isolation. Use shared-table row-level merchant scope based on `merchant_id`.
- Avoid claiming a standalone Vue 2 admin source project if only built/static assets or uni-app evidence is available.
- Avoid claiming production-grade payment, load testing, long-term reliability, or formal user research without evidence.
- Avoid mentioning abandoned code-fix attempts or subagent work in the thesis.

## Current Ralph State

- US-001 is treated as initialized.
- US-002 evidence inventory and upgrade map are done for the current loop.
- US-003 is done. `D:\sky-delivery\docs\thesis-upgrade\staged-content.md` contains the staged replacement text, diagrams, tables, test content, interface examples, and DOCX insertion strategy.
- US-004 is done. `C:\Users\g'y'c\Desktop\毕业论文\初稿1-升级版.docx` exists as a separate copy.
- US-005 is done under the no-render instruction. `D:\sky-delivery\docs\thesis-upgrade\work\qa-report.json` records non-render DOCX openability and structure checks.

## Non-render QA Evidence

- Original thesis SHA256: `E7639ACDF4904F97F84A0CB45DFB0484B47FA8AA7BE9E3583023C9C3687A538C`.
- Upgraded thesis SHA256: `78A32675099E20777ADCC57271845393E2E3D060326DB314AAA9CF789BC23CBE`.
- Upgraded DOCX paragraphs: 400.
- Upgraded DOCX tables: 13.
- Upgraded DOCX sections: 4.
- Header non-empty section count: 2.
- Footer non-empty section count: 2.
- QA checks passed: openability, required package parts, zip integrity, expected content presence, table increase, and forbidden phrase absence.
