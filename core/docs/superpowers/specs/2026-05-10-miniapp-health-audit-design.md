# miniapp 综合健康度体检报告 — 设计规范

> Phase 1 (Brainstorm) 产出。Phase 2 将由 writing-plans skill 基于本文档生成实施 plan。
>
> **最高优先级：上下文友好 (context-friendly)。可完全牺牲人类可读性。**

---

## 0. 元信息

```yaml
spec_id: miniapp-health-audit-2026-05-10
goal: produce_one_yaml_dense_markdown_report
project: sky-delivery
target_dir: core/miniapp
peer_dir: core/web/merchant-admin
backend_dir: core/backend/sky-server
output_path: core/docs/superpowers/audits/2026-05-10-miniapp-health.md
priority: context_friendly_first
human_readability: sacrificable
report_size_target_lines: <=800
report_size_target_tokens: <=15000
brainstorm_session: brainstorm-verify_phase_1
spec_author: agent
spec_date: 2026-05-10
```

---

## 1. 设计目标

```yaml
purpose:
  - assess_miniapp_health_static
  - cross_ref_with_merchant_admin
  - findable_by_id_for_future_chats
  - reloadable_into_future_context_efficiently

non_goals:
  - no_live_run_in_wechat_devtools
  - no_remediation_plan_with_priorities
  - no_ci_integration
  - no_test_writing
  - no_code_changes_in_this_phase

constraints:
  total_lines_max: 800
  total_tokens_target: <15000
  finding_lines_max: 5
  format: yaml_in_fenced_md_blocks
  table_use: forbidden
  prose_use: minimal
  emoji_use: only_as_status_indicators_optional
```

---

## 2. 文档结构

```yaml
sections:
  - id: 0_meta
    name: yaml_frontmatter
    purpose: agent_quick_summary
    fields: [generated_at, miniapp_commit, miniapp_branch, scan_scope, totals]

  - id: 1_summary
    name: health_summary
    purpose: 30_second_readout
    fields: [overall_verdict, dim_scores, key_metrics, top3_risks]

  - id: 2_api
    name: api_contract_layer
    purpose: per_api_compliance_check
    fields: [api_findings_by_block]

  - id: 3_func
    name: functional_completeness
    purpose: per_page_journey_check
    fields: [page_checks_list]

  - id: 4_quality
    name: code_quality
    purpose: dead_code_oversize_hardcode_etc
    fields: [findings_list]

  - id: 5_xref
    name: cross_ref_with_merchant_admin
    purpose: parallel_implementation_diff
    fields: [pair_list]
```

---

## 3. ID 与严重度约定

```yaml
id_schemes:
  api_finding: P{0|1|2}-{seq:03d}        # e.g. P0-001
  func_finding: F-{page_id}-{check}      # e.g. F-order-load
  quality_finding: P{0|1|2}-{seq:03d}    # shares P-NNN namespace
  xref_pair: X-{seq:03d}                 # e.g. X-001
  capability_gap: NEW-{seq:03d}          # e.g. NEW-001

id_uniqueness: all_ids_globally_unique_within_report

severity:
  P0: blocks_core_flow
  P1: degrades_function_or_maintainability
  P2: style_or_optimization

severity_examples:
  P0: [api_path_wrong, missing_required_field, login_break, payment_break]
  P1: [dead_code, oversize_file, hardcode_id, missing_error_toast, hardcode_coord]
  P2: [css_typo, console_log_left, unused_import, magic_number, confused_naming]

severity_assignment_rule:
  - if_blocks_user_journey: P0
  - elif_affects_correctness_or_maintainability: P1
  - else: P2
```

---

## 4. §1 健康度速览 — 字段规范

```yaml
section_1_schema:
  generated_at: iso8601_utc
  miniapp_commit: short_sha
  miniapp_branch: branch_name
  scan_scope:
    files_total: int
    files_scanned: int
    file_globs_included: [pages/**/*.{vue,js}, utils/**/*.js, store/**/*.js, pages/api/api.js]
    file_globs_excluded: [components/uni-icons/**, common/**, static/**, node_modules/**]

  totals:
    api_total: int                # APIs imported by any page
    api_ok: int
    api_warn: int
    api_fail: int
    api_dead: int                 # defined but never imported
    findings_p0: int
    findings_p1: int
    findings_p2: int
    pages_checked: int            # 8
    pages_ok: int
    pages_partial: int
    pages_broken: int
    xref_pairs: int               # 12-18 expected

  dim_scores:
    api_compliance: pass | warn | fail
    functional_completeness: pass | warn | fail
    code_quality: pass | warn | fail

  dim_score_rule:
    pass: zero_p0_and_zero_p1
    warn: zero_p0_and_any_p1_p2
    fail: any_p0

  top3_risks:
    - id: P0-XXX_or_P1-XXX
      one_liner: <=80_chars_no_prose
```

---

## 5. §2 API 契约层 — 字段规范

```yaml
section_2_schema:
  api_finding:
    id: P{sev}-{seq:03d}
    t: api_finding_type
    sev: P0 | P1 | P2
    be:                              # backend reality
      controller: ClassName#methodName
      path: /user/...
      method: GET | POST | PUT | DELETE
      req_fields: [field1, field2]
      resp_fields: [field1, field2]
      auth: anonymous | user_jwt | admin_jwt
    mp:                              # miniapp reality
      fn: function_name
      file: pages/api/api.js:LINE
      path: /...
      method: GET | POST | PUT | DELETE
      params: [...]
      called_by: [page_file:line, ...]
    diff: short_label_from_diff_keys_enum
    evi: file:lines
    imp: short_label
    fix: file:line:action_short

  api_finding_type_enum:
    api_path_mismatch: mp_path_does_not_match_be
    api_method_mismatch: mp_method_diff_from_be
    field_mismatch: req_or_resp_field_naming_diff
    missing_required: mp_omits_be_required_field
    extra_param: mp_sends_field_be_does_not_accept
    wrong_auth: mp_calls_without_required_jwt
    response_field_diff: mp_consumes_field_be_does_not_emit
    dead_endpoint: mp_calls_path_be_no_longer_exposes
    legacy_definition: mp_defines_fn_for_removed_endpoint

  block_groups:
    A_shared_with_admin: list_of_finding_ids
    B_miniapp_only: list_of_finding_ids
    C_admin_only_referenced_for_completeness: list_of_finding_ids
    D_dead_in_miniapp: list_of_finding_ids

  expected_count_estimate:
    A: 2_to_4
    B: 8_to_12
    C: 0_to_2
    D: 8_to_15
```

---

## 6. §3 功能完整性 — 字段规范

```yaml
section_3_schema:
  page_check:
    id: P-{page_short}
    file: pages/.../target.vue
    verdict: ok | partial | broken
    apis_used: [fn_name, fn_name]
    checks:
      api: ok | fail | partial            # API call results
      err: has | miss | partial           # error handling presence
      empty: ok | miss | n_a              # empty-state UI
      load: has | miss | n_a              # loading-state UI
      route: ok | fail                    # route param/jump correctness
    issues: [P0-XXX, P1-XXX, P2-XXX]

  page_enum:
    P-index: pages/index/index.vue
    P-details: pages/details/index.vue
    P-order: pages/order/index.vue
    P-pay: pages/pay/index.vue
    P-success: pages/success/index.vue
    P-history: pages/historyOrder/historyOrder.vue
    P-user: pages/userCenter/index.vue
    P-address: pages/addOrEditAddress/addOrEditAddress.vue

  verdict_rule:
    ok: all_5_checks_pass
    partial: at_least_1_non_critical_check_fail
    broken: api_fail_on_critical_path_or_2plus_checks_fail

  critical_apis:
    - userLogin                             # P-index
    - submitOrderSubmit                     # P-order
    - paymentForOrder                       # P-pay
    - getOrderPage                          # P-history
```

---

## 7. §4 代码质量 — 字段规范

```yaml
section_4_schema:
  finding:
    id: P{sev}-{seq:03d}
    t: code_quality_type
    sev: P0 | P1 | P2
    evi: file:lines_or_pattern_match
    imp: short_label
    fix: file:line_action_short

  code_quality_type_enum:
    dead_code: api_or_fn_defined_but_unimported_unused
    oversize_file: lines_gt_800
    hardcode: literal_id_url_coord_business_value
    css_typo: css_unit_or_property_misspelled
    confused_naming: identifier_misleading_re_actual_value
    console_log_left: production_console_calls
    unused_import: import_without_use
    magic_number: numeric_literal_without_const_name
    missing_error_handling: throw_without_catch_or_ui_feedback
    mutation_pattern: state_mutation_breaking_immutability

  scan_methods:
    dead_code: |
      grep_all_imports_from_pages_api_api_js
      compute_set_of_used_function_names
      diff_against_exports_of_api_js
      report_unimported_exports
    oversize_file: |
      glob_pages_star_star_dot_vue_or_js
      filter_lines_gt_800
    hardcode: |
      grep_pattern_pool: [
        "tableId\\s*[:=]\\s*[\"'][0-9]+[\"']",
        "\\b1[12][0-9]\\.[0-9]+\\s*,\\s*[34][0-9]\\.[0-9]+\\b",
        "https?://[^\"']+",
        "[\"']/api/[^\"']+[\"']"
      ]
    css_typo: |
      grep_in_vue_style_blocks
      patterns: [
        ":\\s*\\d+rp(?!x)",
        ":\\s*\\d+p(?!x)",
        ":\\s*#[0-9a-fA-F]{3,8}\\s*[^;}\\n]"
      ]
    console_log_left: grep_console\.log
    unused_import: grep_import_diff_against_used_identifiers

  expected_count_estimate:
    dead_code: 8_to_15
    oversize_file: 1_to_3
    hardcode: 3_to_8
    css_typo: 1_to_3
    console_log_left: 5_to_20
    confused_naming: 1_to_3
```

---

## 8. §5 横向对照 (xref) — 字段规范

```yaml
section_5_schema:
  pair_types:
    parallel: same_op_different_paths_user_vs_admin
    shared: literally_same_endpoint_called_by_both
    only_mp: miniapp_uses_no_admin_equivalent
    only_ma: admin_uses_miniapp_should_have_equivalent_but_doesnt
    legacy_only: defined_but_unused_in_both

  pair:
    id: X-{seq:03d}
    op: business_operation_short_name
    pair_type: <pair_types_enum>
    mp:
      fn: function_name | -
      path: /... | -
      method: GET | POST | PUT | DELETE | -
      file: file:line | -
      params: [...] | -
      auth: anonymous | user_jwt | -
    ma:
      fn: function_name | -
      path: /... | -
      method: GET | POST | PUT | DELETE | -
      file: file:line | -
      params: [...] | -
      auth: anonymous | admin_jwt | -
    be:
      mp_target: Controller#method@path | -
      ma_target: Controller#method@path | -
      same_handler: bool
    diff:
      - <diff_key>: <ref_id_or_short_label>
    refs: [P0-XXX, P1-XXX, NEW-XXX]

  diff_key_enum:
    mp_path_wrong: mp_calls_path_that_doesnt_exist
    method_mismatch: mp_uses_wrong_http_verb
    field_naming_diff: same_data_different_field_name
    required_field_diff: one_side_treats_field_required_other_optional
    auth_diff: different_auth_requirements
    default_value_diff: different_defaults_for_same_optional
    error_handling_diff: different_error_response_shape
    no_user_endpoint: be_only_exposes_admin_for_this_op
    no_admin_endpoint: be_only_exposes_user_for_this_op
    none: explicit_no_diff

  expected_pair_count: 12_to_18
```

---

## 9. 数据采集 / 实施约束

```yaml
implementation:
  manual_inspection: true
  scan_tools_allowed: [grep, ripgrep, wc, find, ts-prune, knip, eslint_unused]
  no_test_writing: true
  no_code_changes: true
  no_live_run: true
  output_path: core/docs/superpowers/audits/2026-05-10-miniapp-health.md
  size_target: <=800_lines_<=15000_tokens
  evidence_format: file_path_colon_line_or_lines_no_quoted_code
  cross_ref_required: every_finding_referenced_at_least_once_from_summary_or_xref

backend_inspection_targets:
  - core/backend/sky-server/src/main/java/com/sky/controller/user/*.java
  - core/backend/sky-server/src/main/java/com/sky/controller/admin/*.java
  - core/backend/sky-server/src/main/java/com/sky/controller/notify/*.java
  - core/backend/sky-pojo/src/main/java/com/sky/dto/*.java
  - core/backend/sky-pojo/src/main/java/com/sky/vo/*.java

miniapp_inspection_targets:
  - core/miniapp/pages/api/api.js
  - core/miniapp/pages/**/*.vue
  - core/miniapp/pages/**/*.js
  - core/miniapp/utils/*.js
  - core/miniapp/store/**/*.js
  - core/miniapp/manifest.json

merchant_admin_inspection_targets:
  - core/web/merchant-admin/src/api/**/*.{js,ts}
  - core/web/merchant-admin/src/views/**/*.vue
  - core/web/merchant-admin/src/utils/request.{js,ts}
```

---

## 10. 自检 (Self-review)

```yaml
self_review:
  placeholder_scan: pass
  no_TBD_TODO: confirmed
  internal_consistency: pass
  scope_check: single_doc_static_audit_with_xref_no_remediation
  ambiguity_check: pass
  enums_complete: pass
  ids_globally_unique: confirmed
  ref_links_planned: every_finding_id_referenced_from_top3_or_xref
  size_budget: 800_lines_15K_tokens_realistic
  context_friendly_priority: respected_no_tables_no_prose
```

---

## 11. 后续阶段 (Handoff)

```yaml
next_phase: writing-plans
plan_output_path: core/docs/superpowers/plans/2026-05-10-miniapp-health-audit.md
plan_should_contain:
  - task_breakdown_for_each_section
  - exact_file_globs_for_each_scan
  - grep_commands_with_expected_outputs
  - id_assignment_strategy
  - cross_ref_population_order
  - final_size_check_step
plan_should_NOT_contain:
  - test_writing
  - code_changes
  - automated_tooling_setup
  - ci_integration
  - live_miniapp_run
```
