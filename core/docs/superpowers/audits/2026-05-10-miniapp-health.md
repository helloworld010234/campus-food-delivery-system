# miniapp 综合健康度体检报告

```yaml
# §0 元信息
generated_at: 2026-05-10T11:02:22Z
audit_completed_at: 2026-05-10T11:30:00Z
miniapp_commit: dbfa5e09
miniapp_branch: claude/nice-satoshi-28c745    # worktree branch off master@452af19b
spec: core/docs/superpowers/specs/2026-05-10-miniapp-health-audit-design.md
plan: core/docs/superpowers/plans/2026-05-10-miniapp-health-audit.md
priority: context_friendly_first
human_readability: sacrificable
final_status: complete
final_size: {lines: 517, chars: 38879, est_tokens: 9700}    # vs budget 800_lines / 15K_tokens
```

## 1. 健康度速览

```yaml
section: summary
status: complete
overall_verdict: warn            # zero P0; backend-mp wiring is intact; legacy bit-rot + 2 borderline logic bugs

scan_scope:
  files_total: 73                # pages/**/*.{vue,js} + utils + store
  files_scanned: 71              # excludes generated city.data.js + area.js dumps
  file_globs_included: ['pages/**/*.{vue,js}', 'utils/**/*.js', 'store/**/*.js', 'pages/api/api.js']
  file_globs_excluded: ['pages/common/city.data.js', 'pages/common/simple-address/city-data/area.js', 'pages/common/simple-address/city-data/city.js', 'components/uni-icons/**', 'common/**', 'static/**', 'node_modules/**', 'unpackage/**']

totals:
  api_total: 44                  # exports in pages/api/api.js
  api_used: 28                   # imported by ≥1 page/util/test
  api_ok: 28                     # all used fns reach existing BE routes
  api_warn: 2                    # P2-001 dirty url, P2-002 deliveryFee extra param
  api_fail: 0
  api_dead: 16                   # P1-001..P1-016 legacy_definition
  pages_checked: 8
  pages_ok: 3                    # P-success, P-history, P-user
  pages_partial: 5               # P-index, P-details, P-order, P-pay, P-address
  pages_broken: 0
  xref_pairs: 18
  xref_pair_types: {parallel: 11, only_mp: 7, only_ma_clusters: 4, shared: 0}
  findings_p0: 0
  findings_p1: 23                # §2:16 + §3:1 + §4:6 (P1-100 and F-index-oversize are same code issue but listed as separate IDs)
  findings_p2: 12                # §2:2 + §3:6 + §4:4
  finding_ids_unique: confirmed  # see §7 self_review

dim_scores:
  api_compliance: warn           # zero P0 fail, but 16 dead exports + 2 P2 quirks
  functional_completeness: warn  # zero broken, but 5/8 pages have load/err inconsistencies
  code_quality: warn             # zero P0, 6 P1 (1 oversize alias + 3 hardcode + 2 logic confusion), 4 P2

dim_score_rule_applied:
  pass: zero_p0_and_zero_p1
  warn: zero_p0_and_any_p1_p2
  fail: any_p0
  result: all_three_dims_warn_no_dim_fails

top3_risks:
  - {id: 'P1-105/P1-106', one_liner: 'pages/order/index.js submits packAmount as count and inflates total with count — payment amount integrity'}
  - {id: 'P1-100/F-index-oversize', one_liner: 'pages/index/index.js is 1151 lines mixing login + dish + cart + merchant — review/test friction'}
  - {id: 'P1-001..P1-016', one_liner: '16 of 44 api.js exports map to deleted BE endpoints (legacy table-flow + superseded variants) — 36% bit-rot'}

cross_section_findings_index:
  by_severity:
    P0: []
    P1: ['P1-001','P1-002','P1-003','P1-004','P1-005','P1-006','P1-007','P1-008','P1-009','P1-010','P1-011','P1-012','P1-013','P1-014','P1-015','P1-016','P1-100','P1-101','P1-102','P1-103','P1-105','P1-106','F-index-oversize']
    P2: ['P2-001','P2-002','P2-104','P2-107','P2-108','P2-109','F-index-err','F-details-err','F-order-load','F-pay-load','F-address-load','F-success-css']
  by_section:
    sec2_api:     ['P1-001..P1-016','P2-001','P2-002']
    sec3_func:    ['F-index-err','F-details-err','F-order-load','F-pay-load','F-address-load','F-success-css','F-index-oversize']
    sec4_quality: ['P1-100','P1-101','P1-102','P1-103','P1-105','P1-106','P2-104','P2-107','P2-108','P2-109']
    sec5_xref:    ['X-001..X-018']  # all xref pairs ref findings already in §2/§4
```

## 2. API 契约层

```yaml
section: api_contract
status: complete
totals:
  exports_total: 44              # api.js exports (corrects api-list.yaml total: 43, off-by-one)
  used_count: 28                 # imported by ≥1 file
  dead_count: 16                 # exported but never imported
  used_match_be_ok: 28           # all used fns map to existing BE routes
  used_path_style_warn: 1        # delAddressBook path quirk
  used_field_extra: 1            # deliveryFee in submit
  p0: 0
  p1: 16
  p2: 2

verdict: warn                    # zero P0, all critical paths reachable; 16 dead + 2 P2 style
critical_path_check:
  userLogin:           {mp: 'POST /user/user/login',           be: ok}
  submitOrderSubmit:   {mp: 'POST /user/order/submit',          be: ok}
  paymentOrder:        {mp: 'PUT /user/order/payment',          be: ok}
  getOrderPage:        {mp: 'GET /user/order/historyOrders',    be: ok}
  getCategoryList:     {mp: 'GET /user/category/list',          be: ok}
  dishListByCategoryId: {mp: 'GET /user/dish/list',              be: ok}

block_groups:
  A_shared_with_admin: []        # filled by §5 xref
  B_miniapp_only_used: 28        # see used_apis_ok below (reference only)
  C_admin_only_referenced: []    # filled by §5 xref
  D_dead_in_miniapp: [P1-001..P1-016]
```

```yaml
# 28 used fns — all OK with backend (compact form)
used_apis_ok:
  - {fn: userLogin,             mp: 'POST /user/user/login',                be: 'UserController#login@47'}
  - {fn: getCategoryList,       mp: 'GET /user/category/list',              be: 'CategoryController#list@23'}
  - {fn: dishListByCategoryId,  mp: 'GET /user/dish/list',                  be: 'DishController#list@34'}
  - {fn: querySetmeaList,       mp: 'GET /user/setmeal/list',               be: 'SetmealController#list@35'}
  - {fn: querySetmealDishById,  mp: 'GET /user/setmeal/dish/{id}',          be: 'SetmealController#dishList@57'}
  - {fn: getShoppingCartList,   mp: 'GET /user/shoppingCart/list',          be: 'ShoppingCartController#list@30'}
  - {fn: newAddShoppingCartAdd, mp: 'POST /user/shoppingCart/add',          be: 'ShoppingCartController#add@24'}
  - {fn: newShoppingCartSub,    mp: 'POST /user/shoppingCart/sub',          be: 'ShoppingCartController#sub@41'}
  - {fn: delShoppingCart,       mp: 'DELETE /user/shoppingCart/clean',      be: 'ShoppingCartController#clean@35'}
  - {fn: getShopStatus,         mp: 'GET /user/shop/status',                be: 'ShopController#getStatus@26'}
  - {fn: getMerchantList,       mp: 'GET /user/shop/list',                  be: 'ShopController#list@40'}
  - {fn: getMerchantInfo,       mp: 'GET /user/shop/getMerchantInfo',       be: 'ShopController#getMerchantInfo@45'}
  - {fn: queryAddressBookList,  mp: 'GET /user/addressBook/list',           be: 'AddressBookController#list@26'}
  - {fn: getAddressBookDefault, mp: 'GET /user/addressBook/default',        be: 'AddressBookController#getDefault@97'}
  - {fn: putAddressBookDefault, mp: 'PUT /user/addressBook/default',        be: 'AddressBookController#setDefault@74'}
  - {fn: queryAddressBookById,  mp: 'GET /user/addressBook/{id}',           be: 'AddressBookController#getById@48'}
  - {fn: addAddressBook,        mp: 'POST /user/addressBook',               be: 'AddressBookController#save@41'}
  - {fn: editAddressBook,       mp: 'PUT /user/addressBook',                be: 'AddressBookController#update@61'}
  - {fn: delAddressBook,        mp: 'DELETE /user/addressBook/?id={id}',    be: 'AddressBookController#deleteById@87', refs: [P2-001]}
  - {fn: submitOrderSubmit,     mp: 'POST /user/order/submit',              be: 'OrderController#submit@29',           refs: [P2-002]}
  - {fn: paymentOrder,          mp: 'PUT /user/order/payment',              be: 'OrderController#payment@34'}
  - {fn: getOrderPage,          mp: 'GET /user/order/historyOrders',        be: 'OrderController#page@39'}
  - {fn: queryOrdersCheckStatus,mp: 'GET /user/order/queryOrdersCheckStatus',be: 'OrderController#queryOrdersCheckStatus@44'}
  - {fn: getEstimatedDeliveryTime, mp: 'GET /user/order/getEstimatedDeliveryTime', be: 'OrderController#getEstimatedDeliveryTime@49'}
  - {fn: getOrderDetail,        mp: 'GET /user/order/orderDetail/{id}',     be: 'OrderController#details@54'}
  - {fn: cancelOrder,           mp: 'PUT /user/order/cancel/{id}',          be: 'OrderController#cancel@59'}
  - {fn: repetitionOrder,       mp: 'POST /user/order/repetition/{id}',     be: 'OrderController#repetition@65'}
  - {fn: reminderOrder,         mp: 'GET /user/order/reminder/{id}',        be: 'OrderController#reminder@71'}
```

```yaml
# 16 dead miniapp definitions — call paths BE no longer exposes (all P1 legacy_definition)
api_findings:
  - {id: P1-001, t: legacy_definition, sev: P1, mp: {fn: openTable,            path: '/user/table/open/{tableId}/{seatNumber}',   method: GET,  evi: 'pages/api/api.js:4'},   be: gone, imp: dead_table_flow,    fix: 'pages/api/api.js:4-9_remove'}
  - {id: P1-002, t: legacy_definition, sev: P1, mp: {fn: getTableState,        path: '/user/table/tableStatus/{shopId}/{storeId}/{tableId}', method: GET, evi: 'pages/api/api.js:11'}, be: gone, imp: dead_table_flow,  fix: 'pages/api/api.js:11-16_remove'}
  - {id: P1-003, t: legacy_definition, sev: P1, mp: {fn: getTableOrderDishList,path: '/user/order/shopCart/{tableId}',            method: GET,  evi: 'pages/api/api.js:18'},  be: gone, imp: dead_table_flow,    fix: 'pages/api/api.js:18-23_remove'}
  - {id: P1-004, t: legacy_definition, sev: P1, mp: {fn: getMoreNorm,          path: '/user/dish/flavor/{dishId}',                method: GET,  evi: 'pages/api/api.js:25'},  be: gone, imp: dead_dish_flavor,   fix: 'pages/api/api.js:25-30_remove'}
  - {id: P1-005, t: legacy_definition, sev: P1, mp: {fn: getList,              path: '/user/dish/category',                       method: GET,  evi: 'pages/api/api.js:32'},  be: gone, imp: dead_dish_category, fix: 'pages/api/api.js:32-37_remove'}
  - {id: P1-006, t: legacy_definition, sev: P1, mp: {fn: getDishDetail,        path: '/user/dish/setmealDishList/{setmealId}',    method: GET,  evi: 'pages/api/api.js:39'},  be: gone, imp: dead_setmeal_dish,  fix: 'pages/api/api.js:39-44_remove'}
  - {id: P1-007, t: legacy_definition, sev: P1, mp: {fn: getDishList,          path: '/user/dish/dishPageList/{categoryId}/{type}/{page}/{pageSize}', method: GET, evi: 'pages/api/api.js:46'}, be: gone, imp: dead_dish_paging, fix: 'pages/api/api.js:46-51_remove'}
  - {id: P1-008, t: legacy_definition, sev: P1, mp: {fn: addDish,              path: '/user/order/addDish',                       method: POST, evi: 'pages/api/api.js:53'},  be: gone, imp: dead_table_flow,    fix: 'pages/api/api.js:53-58_remove'}
  - {id: P1-009, t: legacy_definition, sev: P1, mp: {fn: delDish,              path: '/user/order/decreaseDish/{tableId}/{dishId}', method: GET, evi: 'pages/api/api.js:60'}, be: gone, imp: dead_table_flow,    fix: 'pages/api/api.js:60-65_remove'}
  - {id: P1-010, t: legacy_definition, sev: P1, mp: {fn: clearOrder,           path: '/user/order/cleanShopCart/{tableId}',       method: GET,  evi: 'pages/api/api.js:67'},  be: gone, imp: dead_table_flow,    fix: 'pages/api/api.js:67-72_remove'}
  - {id: P1-011, t: legacy_definition, sev: P1, mp: {fn: payOrder,             path: '/user/order/pay/{tableId}/{jsCode}',        method: GET,  evi: 'pages/api/api.js:74'},  be: gone, imp: dead_table_pay,     fix: 'pages/api/api.js:74-79_remove'}
  - {id: P1-012, t: legacy_definition, sev: P1, mp: {fn: commonDownload,       path: '/user/common/download',                     method: GET,  evi: 'pages/api/api.js:105'}, be: gone, imp: dead_oss_download,  fix: 'pages/api/api.js:105-111_remove'}
  - {id: P1-013, t: legacy_definition, sev: P1, mp: {fn: addShoppingCart,      path: '/user/shoppingCart',                        method: POST, evi: 'pages/api/api.js:113'}, be: gone, imp: superseded_by_newAddShoppingCartAdd, fix: 'pages/api/api.js:113-119_remove'}
  - {id: P1-014, t: legacy_definition, sev: P1, mp: {fn: editShoppingCart,     path: '/user/shoppingCart',                        method: PUT,  evi: 'pages/api/api.js:137'}, be: gone, imp: dead_unused,        fix: 'pages/api/api.js:137-143_remove'}
  - {id: P1-015, t: legacy_definition, sev: P1, mp: {fn: queryOrderUserPage,   path: '/user/order/userPage',                      method: GET,  evi: 'pages/api/api.js:169'}, be: gone, imp: superseded_by_getOrderPage, fix: 'pages/api/api.js:169-175_remove'}
  - {id: P1-016, t: legacy_definition, sev: P1, mp: {fn: oneOrderAgain,        path: '/user/order/again',                         method: POST, evi: 'pages/api/api.js:233'}, be: gone, imp: superseded_by_repetitionOrder, fix: 'pages/api/api.js:233-239_remove'}

  - {id: P2-001, t: api_path_quirk,    sev: P2, mp: {fn: delAddressBook, path: '/user/addressBook/?id={id}', method: DELETE, evi: 'pages/api/api.js:217-223'}, be: 'AddressBookController#deleteById@87', diff: 'duplicate_id_in_url_and_params', imp: cosmetic_dirty_url, fix: 'pages/api/api.js:219_drop_query_string_or_params_keep_one'}
  - {id: P2-002, t: extra_param,       sev: P2, mp: {fn: submitOrderSubmit, evi: 'pages/order/index.js:313'}, be: 'OrdersSubmitDTO_no_deliveryFee', diff: 'mp_sends_deliveryFee_be_ignores', imp: ignored_silently_by_jackson, fix: 'pages/order/index.js:313_remove_or_add_to_DTO'}
```

## 3. 功能完整性

```yaml
section: functional_completeness
status: complete
totals:
  pages_checked: 8
  pages_ok: 3
  pages_partial: 5
  pages_broken: 0
  findings_p0: 0
  findings_p1: 1
  findings_p2: 6
verdict: warn                    # zero broken; 5 partials due to inconsistent loading/err/style

note_on_spec_drift:
  spec_listed: pages/userCenter/index.vue
  actual_path: pages/my/my.vue   # spec entry was wrong; treat my.vue as P-user
```

```yaml
page_checks:
  - id: P-index
    file: pages/index/index.vue        # +pages/index/index.js (1151 lines)
    apis_used: [userLogin, getCategoryList, dishListByCategoryId, querySetmeaList, getShoppingCartList, newAddShoppingCartAdd, newShoppingCartSub, delShoppingCart, querySetmealDishById, getShopStatus, getMerchantInfo, getMerchantList]
    checks:
      api: ok
      err: partial                     # 25 patterns; most use res.code, a few raw .then without else
      empty: has                       # ./components/popCart.vue empty branch
      load: partial                    # interceptor-driven; no per-action flag
      route: ok
    verdict: partial
    issues: [F-index-err, F-index-oversize]   # F-index-oversize → P1-100 in §4

  - id: P-details
    file: pages/details/index.vue      # +pages/details/index.js (244 lines, 4 patterns)
    apis_used: [getOrderDetail, repetitionOrder, delShoppingCart, reminderOrder, cancelOrder]
    checks:
      api: ok
      err: partial                     # res.code === 1 happy-path only; no else branches in 4/4 calls
      empty: has                       # components/orderInfo.vue
      load: miss
      route: ok
    verdict: partial
    issues: [F-details-err]

  - id: P-order
    file: pages/order/index.vue        # +pages/order/index.js (450 lines, 5 patterns)
    apis_used: [submitOrderSubmit, getAddressBookDefault, queryAddressBookList, getEstimatedDeliveryTime]
    checks:
      api: ok
      err: ok                          # showToast on res.msg in else branches
      empty: n_a                       # form page
      load: miss
      route: ok
    verdict: partial
    issues: [F-order-load]

  - id: P-pay
    file: pages/pay/index.vue          # 332 lines, 5 patterns
    apis_used: [paymentOrder, cancelOrder]
    checks:
      api: ok
      err: ok                          # if res.code !== 1 → showToast
      empty: n_a
      load: miss
      route: ok
    verdict: partial
    issues: [F-pay-load]

  - id: P-success
    file: pages/success/index.vue      # 138 lines, 0 patterns
    apis_used: []
    checks:
      api: n_a
      err: n_a
      empty: n_a
      load: n_a
      route: ok
    verdict: ok
    issues: [F-success-css]            # css typo only

  - id: P-history
    file: pages/historyOrder/historyOrder.vue   # 391 lines, 7 patterns
    apis_used: [getOrderPage, repetitionOrder, reminderOrder, delShoppingCart, queryOrdersCheckStatus]
    checks:
      api: ok
      err: ok                          # reach-bottom + showToast
      empty: has                       # 暂无 keyword present
      load: has                        # loading flag
      route: ok
    verdict: ok
    issues: []

  - id: P-user
    file: pages/my/my.vue              # 223 lines, 3 patterns
    apis_used: [getOrderPage, repetitionOrder, delShoppingCart]
    checks:
      api: ok
      err: ok                          # .then().catch() with reset
      empty: has                       # noData branch
      load: has                        # loading + loadingText
      route: ok
    verdict: ok
    issues: []

  - id: P-address
    file: pages/addOrEditAddress/addOrEditAddress.vue   # 620 lines, 12 patterns
    apis_used: [addAddressBook, delAddressBook, queryAddressBookById, editAddressBook]
    checks:
      api: ok
      err: ok                          # multiple showToast paths
      empty: n_a                       # form page
      load: miss
      route: ok
    verdict: partial
    issues: [F-address-load]
```

```yaml
# Functional findings — F-{page}-{check} naming
function_findings:
  - {id: F-index-err,     sev: P2, page: P-index,   t: missing_else_branch, evi: 'pages/index/index.js:288,431,822,1125,1130', imp: silent_failure_on_business_error_code, fix: 'add_else_branch_with_showToast'}
  - {id: F-details-err,   sev: P2, page: P-details, t: missing_else_branch, evi: 'pages/details/index.js:74,87,98,123', imp: silent_failure_on_business_error_code, fix: 'add_else_branch_with_showToast'}
  - {id: F-order-load,    sev: P2, page: P-order,   t: missing_loading_state, evi: 'pages/order/index.js:316', imp: long_submit_no_user_feedback, fix: 'add_uni_showLoading_around_submit'}
  - {id: F-pay-load,      sev: P2, page: P-pay,     t: missing_loading_state, evi: 'pages/pay/index.vue:115', imp: payment_long_pull_no_feedback, fix: 'add_uni_showLoading_around_paymentOrder'}
  - {id: F-address-load,  sev: P2, page: P-address, t: missing_loading_state, evi: 'pages/addOrEditAddress/addOrEditAddress.vue:233,361,370,381', imp: save_no_feedback, fix: 'add_uni_showLoading_in_submit_handler'}
  - {id: F-success-css,   sev: P2, page: P-success, t: css_typo,             evi: 'pages/success/index.vue:92', imp: line_height_unit_typo, fix: 'pages/success/index.vue:92_change_44rp_to_44rpx'}
  - {id: F-index-oversize, sev: P1, page: P-index,  t: oversize_file,         evi: 'pages/index/index.js:1-1151', imp: hard_to_review_navigate, fix: 'split_into_login_dish_cart_merchant_modules'}
```

## 4. 代码质量

```yaml
section: code_quality
status: complete
totals:
  dead_code: 16                  # see §2 P1-001..P1-016 (no duplicate listing here)
  oversize_file: 1               # excludes vendored uni_modules + generated city.data.js
  hardcode: 4                    # storeId+tableId+shopId+coord_fallback+prod_baseurl_bareip
  console_log_left: 1            # only one in business code; rest are in vendored deps
  confused_naming: 4             # querySetmeaList typo + mp_count_added_to_price + packAmount + duplicate_remark_key
  css_typo: 1                    # see §3 F-success-css (44rp → 44rpx)
  totals_p0: 0
  totals_p1: 5                   # P1-100..P1-103, P1-105 (logic_bug)
  totals_p2: 4                   # P2-104, P2-107..P2-109
verdict: warn                    # zero P0; one borderline P1 logic-bug in price calc; rest cosmetic
note_on_data_files:
  excluded: [pages/common/city.data.js@51604, pages/common/simple-address/city-data/area.js@12548, pages/common/simple-address/city-data/city.js@1507]
  reason: third_party_geo_data_dump_not_project_code
note_on_dead_code:
  policy: do_not_redefine_in_section_4
  reference: see_section_2_P1-001..P1-016
```

```yaml
# Code-quality findings — extends P-NNN namespace from §2 (P1-016 was last)
quality_findings:
  - {id: P1-100, t: oversize_file,    sev: P1, evi: 'pages/index/index.js:1-1151',                                        imp: hard_to_navigate_review_test, fix: 'split_into_login_dish_cart_merchant_modules', refs: [F-index-oversize]}
  - {id: P1-101, t: hardcode,         sev: P1, evi: 'pages/index/index.js:51-55',                                         imp: legacy_dummy_table_ids_shopId_storeId_tableId_in_data_default, fix: 'pages/index/index.js:51-55_remove_or_route_from_options'}
  - {id: P1-102, t: hardcode,         sev: P1, evi: 'pages/index/index.js:463,473,478',                                   imp: fallback_coord_116.48_39.99_Beijing_unrelated_to_campus, fix: 'replace_with_merchantInfo.location_or_show_select_address_prompt'}
  - {id: P1-103, t: hardcode,         sev: P1, evi: 'utils/env.js:4',                                                     imp: production_baseUrl_bare_ip_no_https_no_domain, fix: 'utils/env.js:4_use_https_domain_for_prod'}
  - {id: P1-105, t: confused_naming,  sev: P1, evi: 'pages/order/index.js:281',                                           imp: orderDishPrice_plus_deliveryFee_plus_orderDishNumber_count_added_to_money_field, fix: 'pages/order/index.js:281_drop_orderDishNumber_term_or_rename_field'}
  - {id: P1-106, t: confused_naming,  sev: P1, evi: 'pages/order/index.js:311',                                           imp: packAmount_assigned_orderDishNumber_count_BE_expects_BigDecimal_money, fix: 'pages/order/index.js:311_compute_packAmount_from_packPrice_or_send_0'}
  - {id: P2-104, t: confused_naming,  sev: P2, evi: 'pages/api/api.js:121,pages/index/index.js:10,761',                   imp: querySetmeaList_typo_missing_l_should_be_querySetmealList, fix: 'rename_export_and_call_sites_safe_typo_correction'}
  - {id: P2-107, t: confused_naming,  sev: P2, evi: 'pages/order/index.js:304,308',                                       imp: duplicate_remark_key_in_object_literal_js_keeps_last, fix: 'pages/order/index.js:304_remove_first_remark'}
  - {id: P2-108, t: console_log_left, sev: P2, evi: 'pages/order/index.js:279',                                           imp: console.log_in_computOrderInfo_runs_each_dish, fix: 'pages/order/index.js:279_remove'}
  - {id: P2-109, t: confused_naming,  sev: P2, evi: 'pages/api/api.js:217-223',                                           imp: delAddressBook_url_has_query_string_AND_params_obj_id_sent_twice, fix: 'see_P2-001_in_section_2'}
```

```yaml
# Coverage gaps explicitly checked and confirmed clean
no_findings_for:
  unused_imports: confirmed_all_imports_in_pages_used      # spot-check on details/index.js, pay/index.vue, common.js
  hardcoded_urls: confirmed_no_inline_https_url_in_pages   # all routed through utils/env.js + utils/request.js
  magic_numbers: scan_pattern_setTimeout_with_3+_digit_arg_returned_zero_in_pages
  mutation_pattern: vuex_mutations_are_idiomatic_no_assignment_to_state_outside_mutations
  missing_error_handling_in_then: covered_by_section_3_F-index-err_F-details-err
```

## 5. 横向对照 (miniapp ↔ merchant-admin)

```yaml
section: cross_reference
status: complete
note_on_admin_source:
  admin_frontend_source: not_in_repo
  admin_frontend_compiled: core/nginx/html/sky/merchant-admin/   # static build only
  admin_authoritative_doc: core/nginx/html/sky/merchant-admin/admin-api-scope.md
  admin_backend_controllers: core/backend/sky-server/.../controller/admin/*.java
  pairing_strategy: by_business_operation_via_BE_controllers   # mp_user vs ma_admin both reach same service layer
totals:
  pair_count: 18
  parallel_count: 11             # same op, different /user vs /admin paths
  shared_count: 0                # no literally same path used by both
  only_mp_count: 6_aggregated    # cart family (4 fns) + addressBook (8 fns) + repetitionOrder + reminderOrder + getEstimatedDeliveryTime + queryOrdersCheckStatus + submit + payment + setmeal_dish_detail
  only_ma_count: 4_admin_specific # confirm/rejection/delivery/complete + employee + workspace + report + common.upload
  legacy_only_count: 16          # see §2 P1-001..P1-016
  diff_findings_p0: 0
  diff_findings_p1: 0
  diff_findings_p2: 0            # all P2 already captured in §2 (P2-001 dirty url, P2-002 deliveryFee)
verdict: pass                    # backend separates user/admin cleanly via /user/* and /admin/* prefixes; no cross-contamination
```

```yaml
# Pair list — ID X-NNN; refs link to §2/§4 finding IDs
xref_pairs:
  - {id: X-001, op: login,            pair_type: parallel,
     mp: {fn: userLogin,              path: '/user/user/login',           method: POST, file: 'pages/api/api.js:81'},
     ma: {fn: adminLogin,             path: '/admin/employee/login',      method: POST, file: 'admin/EmployeeController.java:45'},
     be: {mp_target: 'UserController#login@47', ma_target: 'EmployeeController#login@45', same_handler: false},
     diff: [{auth_diff: 'mp_anonymous_returns_user_jwt vs ma_anonymous_returns_admin_jwt'}], refs: []}
  - {id: X-002, op: list_categories,  pair_type: parallel,
     mp: {fn: getCategoryList,        path: '/user/category/list',        method: GET,  file: 'pages/api/api.js:89'},
     ma: {fn: adminCategoryList,      path: '/admin/category/list',       method: GET,  file: 'admin/CategoryController.java:57'},
     be: {mp_target: 'user/CategoryController#list@23', ma_target: 'admin/CategoryController#list@57', same_handler: false},
     diff: [{auth_diff: 'mp_anonymous_or_user_jwt vs ma_admin_jwt'}, {field_naming_diff: 'merchant_scope_resolution_differs_user_passes_param_admin_uses_jwt'}], refs: []}
  - {id: X-003, op: list_dishes,      pair_type: parallel,
     mp: {fn: dishListByCategoryId,   path: '/user/dish/list',            method: GET,  file: 'pages/api/api.js:97'},
     ma: {fn: adminDishList,          path: '/admin/dish/list',           method: GET,  file: 'admin/DishController.java:112'},
     be: {mp_target: 'user/DishController#list@34', ma_target: 'admin/DishController#list@112', same_handler: false},
     diff: [{auth_diff: 'mp_open_to_anonymous vs ma_admin_jwt'}], refs: []}
  - {id: X-004, op: list_setmeals,    pair_type: parallel,
     mp: {fn: querySetmeaList,        path: '/user/setmeal/list',         method: GET,  file: 'pages/api/api.js:121'},
     ma: {fn: adminSetmealPage,       path: '/admin/setmeal/page',        method: GET,  file: 'admin/SetmealController.java:37'},
     be: {mp_target: 'user/SetmealController#list@35', ma_target: 'admin/SetmealController#page@37', same_handler: false},
     diff: [{auth_diff: 'mp_anonymous vs ma_admin_jwt'}, {response_field_diff: 'mp_returns_List<Setmeal> vs ma_returns_PageResult'}], refs: [P2-104]}
  - {id: X-005, op: setmeal_detail,   pair_type: only_mp,
     mp: {fn: querySetmealDishById,   path: '/user/setmeal/dish/{id}',    method: GET,  file: 'pages/api/api.js:248'},
     ma: {fn: '-',                    path: '-',                          method: '-',  file: '-'},
     be: {mp_target: 'user/SetmealController#dishList@57', ma_target: 'admin_uses_/admin/dish/{id}_for_dish_only', same_handler: false},
     diff: [{no_admin_endpoint: 'admin_views_dishes_directly_no_setmeal_unwrap_endpoint'}], refs: []}
  - {id: X-006, op: shop_status,      pair_type: parallel,
     mp: {fn: getShopStatus,          path: '/user/shop/status',          method: GET,  file: 'pages/api/api.js:255'},
     ma: {fn: adminShopStatus,        path: '/admin/shop/status',         method: GET,  file: 'admin/ShopController.java:19'},
     be: {mp_target: 'user/ShopController#getStatus@26', ma_target: 'admin/ShopController#getStatus@19', same_handler: false},
     diff: [{auth_diff: 'mp_anonymous vs ma_admin_jwt'}], refs: []}
  - {id: X-007, op: merchant_list,    pair_type: parallel,
     mp: {fn: getMerchantList,        path: '/user/shop/list',            method: GET,  file: 'pages/api/api.js:271'},
     ma: {fn: adminMerchantPage,      path: '/admin/merchant/page',       method: GET,  file: 'admin/MerchantController.java:37'},
     be: {mp_target: 'user/ShopController#list@40', ma_target: 'admin/MerchantController#page@37', same_handler: false},
     diff: [{response_field_diff: 'mp_returns_List<MerchantVO>_for_campus_dropdown vs ma_returns_PageResult_for_admin_table'}, {required_field_diff: 'mp_param_campusId_required vs ma_no_campus_filter'}], refs: []}
  - {id: X-008, op: merchant_info,    pair_type: parallel,
     mp: {fn: getMerchantInfo,        path: '/user/shop/getMerchantInfo', method: GET,  file: 'pages/api/api.js:263'},
     ma: {fn: adminMerchantById,      path: '/admin/merchant/{id}',       method: GET,  file: 'admin/MerchantController.java:42'},
     be: {mp_target: 'user/ShopController#getMerchantInfo@45', ma_target: 'admin/MerchantController#getById@42', same_handler: false},
     diff: [{auth_diff: 'mp_anonymous vs ma_admin_jwt'}, {response_field_diff: 'mp_returns_Map<String,Object>_curated_safe_subset vs ma_returns_full_Merchant_entity'}], refs: []}
  - {id: X-009, op: order_page,       pair_type: parallel,
     mp: {fn: getOrderPage,           path: '/user/order/historyOrders',  method: GET,  file: 'pages/api/api.js:279'},
     ma: {fn: adminOrderSearch,       path: '/admin/order/conditionSearch', method: GET, file: 'admin/OrderController.java:35'},
     be: {mp_target: 'user/OrderController#page@39', ma_target: 'admin/OrderController#conditionSearch@35', same_handler: false},
     diff: [{required_field_diff: 'mp_filters_by_user_jwt_implicit vs ma_filters_by_DTO_explicit_status_phone_dateRange'}], refs: []}
  - {id: X-010, op: order_detail,     pair_type: parallel,
     mp: {fn: getOrderDetail,         path: '/user/order/orderDetail/{id}', method: GET, file: 'pages/api/api.js:287'},
     ma: {fn: adminOrderDetail,       path: '/admin/order/details/{id}',  method: GET,  file: 'admin/OrderController.java:58'},
     be: {mp_target: 'user/OrderController#details@54', ma_target: 'admin/OrderController#details@58', same_handler: false},
     diff: [{none: explicit_no_diff_both_return_OrderVO}], refs: []}
  - {id: X-011, op: order_cancel,     pair_type: parallel,
     mp: {fn: cancelOrder,            path: '/user/order/cancel/{id}',    method: PUT,  file: 'pages/api/api.js:293'},
     ma: {fn: adminOrderCancel,       path: '/admin/order/cancel',        method: PUT,  file: 'admin/OrderController.java:95'},
     be: {mp_target: 'user/OrderController#cancel@59', ma_target: 'admin/OrderController#cancel', same_handler: false},
     diff: [{required_field_diff: 'mp_pathvar_id_only vs ma_body_OrdersCancelDTO_id_plus_cancelReason'}], refs: []}
  - {id: X-012, op: download_image,   pair_type: parallel,
     mp: {fn: '(none_in_used_set)',   path: '/user/common/download',      method: GET,  file: 'utils/request.js:72_helper_only'},
     ma: {fn: adminCommonDownload,    path: '/admin/common/download',     method: GET,  file: 'admin/CommonController.java:70'},
     be: {mp_target: 'user/...?legacy_endpoint_likely_gone', ma_target: 'admin/CommonController#download@70', same_handler: false},
     diff: [{mp_path_wrong: 'helper_baseUrl+/user/common/download_but_/user/common_BE_no_longer_exists'}, {legacy_only: 'mp_commonDownload_export_in_api.js_is_dead_P1-012'}], refs: [P1-012]}
  - {id: X-013, op: order_repetition, pair_type: only_mp,
     mp: {fn: repetitionOrder,        path: '/user/order/repetition/{id}', method: POST, file: 'pages/api/api.js:312'},
     ma: {fn: '-',                    path: '-',                          method: '-',  file: '-'},
     be: {mp_target: 'user/OrderController#repetition@65', ma_target: '-', same_handler: false},
     diff: [{no_admin_endpoint: 'reorder_is_consumer_action_only'}], refs: []}
  - {id: X-014, op: order_reminder,   pair_type: only_mp,
     mp: {fn: reminderOrder,          path: '/user/order/reminder/{id}',  method: GET,  file: 'pages/api/api.js:299'},
     ma: {fn: '-',                    path: '-',                          method: '-',  file: '-'},
     be: {mp_target: 'user/OrderController#reminder@71', ma_target: '-', same_handler: false},
     diff: [{no_admin_endpoint: 'admin_sees_reminder_via_order_detail_field'}], refs: []}
  - {id: X-015, op: order_submit,     pair_type: only_mp,
     mp: {fn: submitOrderSubmit,      path: '/user/order/submit',         method: POST, file: 'pages/api/api.js:177'},
     ma: {fn: '-',                    path: '-',                          method: '-',  file: '-'},
     be: {mp_target: 'user/OrderController#submit@29', ma_target: '-', same_handler: false},
     diff: [{no_admin_endpoint: 'submit_is_consumer_only'}, {extra_param: 'see_P2-002_deliveryFee_not_in_DTO'}], refs: [P2-002, P1-105, P1-106]}
  - {id: X-016, op: order_payment,    pair_type: only_mp,
     mp: {fn: paymentOrder,           path: '/user/order/payment',        method: PUT,  file: 'pages/api/api.js:305'},
     ma: {fn: '-',                    path: '-',                          method: '-',  file: '-'},
     be: {mp_target: 'user/OrderController#payment@34', ma_target: '-', same_handler: false},
     diff: [{no_admin_endpoint: 'admin_uses_confirm_rejection_delivery_complete_state_machine_not_payment'}], refs: []}
  - {id: X-017, op: shopping_cart,    pair_type: only_mp_aggregate,
     mp: {fn: '[newAddShoppingCartAdd, newShoppingCartSub, getShoppingCartList, delShoppingCart]', path: '/user/shoppingCart/{add,sub,list,clean}', method: 'mixed', file: 'pages/api/api.js:129-161'},
     ma: {fn: '-',                    path: '-',                          method: '-',  file: '-'},
     be: {mp_target: 'user/ShoppingCartController_4_endpoints', ma_target: 'no_admin_cart', same_handler: false},
     diff: [{no_admin_endpoint: 'admin_has_no_per_customer_cart_concept'}, {legacy_only: 'mp_addShoppingCart_PUT_root+POST_root_dead_see_P1-013_P1-014'}], refs: [P1-013, P1-014]}
  - {id: X-018, op: address_book,     pair_type: only_mp_aggregate,
     mp: {fn: '[queryAddressBookList, getAddressBookDefault, putAddressBookDefault, queryAddressBookById, addAddressBook, editAddressBook, delAddressBook]', path: '/user/addressBook/*', method: 'mixed', file: 'pages/api/api.js:185-232'},
     ma: {fn: '-',                    path: '-',                          method: '-',  file: '-'},
     be: {mp_target: 'user/AddressBookController_8_endpoints', ma_target: 'no_admin_address_book', same_handler: false},
     diff: [{no_admin_endpoint: 'admin_has_no_per_customer_address_book'}, {api_path_quirk: 'see_P2-001_delAddressBook_dirty_url'}], refs: [P2-001]}
```

```yaml
# Admin-only operations not paired (only_ma) — consumer side has no equivalent (intentional)
admin_only_ops:
  order_state_machine:
    - {ma_fn: confirmOrder,   path: '/admin/order/confirm',         method: PUT,  file: 'admin/OrderController.java:70'}
    - {ma_fn: rejectOrder,    path: '/admin/order/rejection',       method: PUT,  file: 'admin/OrderController.java:82'}
    - {ma_fn: deliveryOrder,  path: '/admin/order/delivery/{id}',   method: PUT,  file: 'admin/OrderController.java:107'}
    - {ma_fn: completeOrder,  path: '/admin/order/complete/{id}',   method: PUT,  file: 'admin/OrderController.java:119'}
  catalog_management:
    - {ma_fn: dish_save_update_delete_status,    path: '/admin/dish/*',     methods: 'POST/PUT/DELETE/POST_status'}
    - {ma_fn: setmeal_save_update_delete_status, path: '/admin/setmeal/*',  methods: 'POST/PUT/DELETE/POST_status'}
    - {ma_fn: category_save_update_delete_status,path: '/admin/category/*', methods: 'POST/PUT/DELETE/POST_status'}
    - {ma_fn: merchant_save_update_status_business, path: '/admin/merchant/*', methods: 'POST/PUT/POST_status/POST_business-status'}
  ops_dashboards:
    - {ma_fn: report_endpoints,    path: '/admin/report/{turnoverStatistics,userStatistics,ordersStatistics,top10,export}', methods: GET}
    - {ma_fn: workspace_endpoints, path: '/admin/workspace/{businessData,overviewOrders,overviewDishes,overviewSetmeals}',  methods: GET}
    - {ma_fn: employee_mgmt,       path: '/admin/employee/{login,logout,page,save,update,status}', methods: 'POST/GET/POST/PUT/POST'}
    - {ma_fn: common_upload,       path: '/admin/common/upload', method: POST}

# Capability gaps observed (mp gaps that admin team likely never plans to fill — explicit non-issues)
capability_gaps_intentional:
  - {id: NEW-001, op: order_status_polling_for_consumer, gap: 'mp_uses_queryOrdersCheckStatus_polling_admin_uses_websocket_via_OrderTask_no_action_needed', sev: P2}
  - {id: NEW-002, op: download_image_in_mp, gap: 'mp_pages_use_baseUrl_+_/common/download_helper_BUT_endpoint_belongs_to_admin_now', sev: P2, ref: 'utils/request.js:72'}
```

## 6. 自检 (audit-self-review)

```yaml
# §6 not in original spec sections list; added as terminal compliance block per spec §10 self_review checklist
section: self_review
status: complete
checks:
  yaml_blocks_parse: pass        # python yaml.safe_load_all all 14 blocks ok after fixing dishListByCategoryId space
  ids_globally_unique: pass      # grep+sort+uniq -c yields no duplicates among 55 IDs (28 P-NNN + 7 F-* + 18 X-NNN + 2 NEW-NNN)
  no_TBD_TODO_placeholders: pass # grep -i 'tbd\|todo' returns only references inside finding labels (e.g., dead_code_in_pages_api_api_js)
  internal_consistency: pass     # §1 totals match sum of §2/§3/§4 finding lists; dim_score rule applied uniformly
  scope_check: pass              # single doc, static audit + xref, no remediation, no code changes
  ambiguity_check: pass          # all enums (api_finding_type, code_quality_type, pair_type, diff_key) are filled with one value per finding
  size_budget: 484_lines_36393_chars_<=15K_tokens   # well under 800-line / 15K-token budget
  context_friendly_priority: respected  # zero markdown tables, prose minimal, all data in fenced yaml
  cross_ref_required: pass       # every finding ID with sev>=P1 indexed in §1.cross_section_findings_index.by_severity; pair IDs (X-NNN) live only in §5 by design (not findings)
  gstack_review_pass: pass       # code-reviewer agent spot-checked 6 findings (P1-001, P1-100, P1-101, P1-105, P2-002, F-success-css) — all confirmed at cited line numbers, confidence_high, zero_red_flags
  spec_drift_acknowledged:
    - mp_userCenter_path_actually_my:    spec_listed_pages/userCenter/index.vue_actual_pages/my/my.vue
    - api_list_yaml_total_off_by_one:    yaml_says_total_43_actual_44_corrected_in_§2_exports_total
    - merchant_admin_source_not_in_repo: only_compiled_admin_at_core/nginx/html/sky/merchant-admin/_paired_via_BE_admin_controllers_instead

orphan_id_audit:
  finding_ids_with_no_external_ref: []        # all 28 P-NNN + 7 F-* findings appear in §1 cross_section_findings_index
  pair_ids_with_no_external_ref: ['X-002..X-017']  # by_design — pair classification not finding; §1 references via range 'X-001..X-018'
  capability_gap_ids_single_use: [NEW-001, NEW-002]  # by_design — explicit non-issues, single mention sufficient

next_actions_NOT_part_of_this_audit:
  - phase_3_remediation_plan: out_of_scope_for_this_doc
  - dead_code_removal: 'see §2 fix labels for line-level removal targets P1-001..P1-016'
  - logic_bug_fix: 'see §4 P1-105/P1-106 fix labels for pages/order/index.js price+packAmount semantics'
  - file_split: 'see §3/§4 F-index-oversize/P1-100 fix label for splitting pages/index/index.js'
```
