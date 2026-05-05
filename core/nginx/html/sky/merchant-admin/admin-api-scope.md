# Admin API Scope Semantics (Backend Contract)

Date: 2026-05-05  Owner: Agent 03 - Admin Reporting

This note documents the backend rules consumed by the static admin portal at
`core/nginx/html/sky/merchant-admin/`. It is the authoritative reference for
how the report and workspace endpoints behave in single-merchant fallback,
multi-merchant platform, and multi-merchant merchant-admin contexts.

## 1. Account types

| Account type | Source | Merchant context | Admin scope |
|--------------|--------|------------------|-------------|
| PLATFORM_ADMIN | super-merchant employee row | optional | global, may pass merchantId |
| MERCHANT_ADMIN | merchant-bound employee row | required | bound merchantId only |
| MERCHANT_STAFF | merchant-bound employee row | required | bound merchantId only |

All admin endpoints (`/admin/**`) require an admin JWT. The interceptor
populates `BaseContext` with `currentId`, `currentMerchantId`, and
`currentAccountType`. Service layer code never reads account type directly -
it goes through `MerchantScopeGuard`.

## 2. Report endpoints (`/admin/report/**`)

`ReportServiceImpl` resolves merchant scope through
`merchantScopeGuard.resolveAdminQueryMerchantId(null)` for every metric:

| Endpoint | Platform admin | Merchant admin / staff |
|----------|---------------|------------------------|
| `GET /turnoverStatistics` | aggregate across all merchants | bound merchant only |
| `GET /userStatistics` | global registered users from `user` table | bound merchant's distinct paying users |
| `GET /ordersStatistics` | aggregate orders across merchants | bound merchant only |
| `GET /top10` | global top10 dishes by sales | bound merchant only |
| `GET /export` | reuses business data, same scope | reuses business data, same scope |

The user-statistics endpoint switches its SQL via `UserMapper.xml`: when a
merchant id is bound it counts distinct paying users seen on `orders` for
that merchant within the date window; otherwise it counts new registrations
in the `user` table for the platform view.

## 3. Workspace endpoints (`/admin/workspace/**`)

`WorkspaceServiceImpl` applies the same guard, so the dashboard tiles always
match what the report screens and the Excel export show:

| Endpoint | Platform admin | Merchant admin / staff |
|----------|---------------|------------------------|
| `GET /businessData` | global "today" KPI tiles | bound merchant tiles |
| `GET /overviewOrders` | global order status counts | bound merchant counts |
| `GET /overviewDishes` | global dish enable / disable | bound merchant only |
| `GET /overviewSetmeals` | global setmeal enable / disable | bound merchant only |

## 4. Cross-merchant denial

`MerchantScopeGuard.resolveAdminQueryMerchantId` throws `BaseException`
when a merchant account explicitly passes a different merchant id. Static
admin pages must therefore not insert another merchant's id into report or
workspace requests when logged in as a merchant; the backend will reject
the call rather than silently returning the bound merchant data.

## 5. Single-merchant fallback

When `MultiMerchantSchemaSupport.isCoreSchemaReady()` returns false, the
guard does not throw on a missing context. The reports and dashboard then
behave like the legacy single-merchant build, and the Excel export uses the
same null merchant id - no behaviour change for existing single-merchant
deployments.

## 6. Frontend assumptions

The static portal under `core/nginx/html/sky/merchant-admin/` is expected to:

- Send the admin JWT via the `token` request header (existing behaviour).
- Not include a merchant id in `/admin/report/**` or `/admin/workspace/**`
  query strings; the backend resolves it from the JWT-derived context.
- Use the standard `yyyy-MM-dd` `begin` and `end` query parameters for the
  report endpoints.
- Treat the export endpoint as a binary download under the same scope as
  the on-screen reports.

If a future feature needs platform admins to inspect a specific merchant
via the same screens, only the platform admin's `/admin/report/**` calls
should grow a `merchantId` query parameter; the merchant-admin variant must
keep ignoring or rejecting that parameter.

## 7. Verification

Focused JUnit coverage lives at:

- `core/backend/sky-server/src/test/java/com/sky/service/impl/ReportServiceImplScopeTest.java`
- `core/backend/sky-server/src/test/java/com/sky/service/impl/WorkspaceServiceImplScopeTest.java`
- `core/backend/sky-server/src/test/java/com/sky/security/MerchantScopeGuardTest.java`
- `core/backend/sky-server/src/test/java/com/sky/service/impl/MerchantServiceImplScopeTest.java`

Run from `D:\sky-delivery`:

```
mvn -f core/backend/pom.xml test -pl sky-server "-Dtest=*Report*Test,*Workspace*Test,*Admin*Test,*Merchant*Scope*Test"
```
