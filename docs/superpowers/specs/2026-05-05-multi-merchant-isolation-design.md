# Sky Delivery 多商户隔离设计文档

**日期**: 2026-05-05  
**范围**: 核心业务全链路多商户隔离验证与修补  
**基线 Commit**: `bbe5490`  
**分支**: `claude/jwt-blacklist`

---

## 1. 目标与边界

本轮目标是建立并执行一套核心业务多商户隔离方案：先形成越权矩阵，再按矩阵修补服务层和必要的 mapper 查询边界，最后通过单元测试、WebMvcTest/MockMvc、后端全量测试和 Playwright E2E 证明隔离有效。

本设计使用两层模型：

| 层级 | 规则 |
|---|---|
| 公开浏览层 | 用户端可以跨商户浏览启用商户、公开分类、菜品、套餐和门店信息。 |
| 私有业务层 | 购物车、下单、支付、订单详情、历史订单、取消、复购、催单必须绑定明确商户，并校验商品、套餐、购物车、订单同属该商户。 |
| 后台管理层 | 平台账号可以跨商户；商户账号只能访问 token 中 `merchantId` 对应的数据。 |

本轮覆盖员工、商户、分类、菜品、套餐、订单、购物车、报表和工作台。不做独立 schema/库隔离，不重写认证模型，不限制用户公开浏览其他商户，不一次性把所有 mapper 改成 `...AndMerchantId` 形式，只在风险矩阵需要的地方补强。

---

## 2. 当前上下文

已有隔离基础：

| 组件 | 当前职责 |
|---|---|
| `BaseContext` | 保存当前登录 id、当前商户 id、当前账号类型。 |
| `JwtTokenAdminInterceptor` | 从后台 JWT 写入 `empId`、`merchantId`、`accountType`。 |
| `MerchantScopeUtils` | 提供商户账号判定、请求商户解析和基础归属断言。 |
| `MultiMerchantSchemaSupport` | 检测当前数据库是否具备多商户列，用于兼容旧 schema。 |
| 现有服务层 | 分类、菜品、套餐、订单、员工等模块已有部分归属校验。 |

需要系统化补强的信号：

| 风险信号 | 说明 |
|---|---|
| 隔离规则散落 | 不同服务各自调用 `MerchantScopeUtils`，缺少统一入口和矩阵化测试。 |
| 用户私有链路商户不够显式 | 购物车、下单、复购等流程需要统一禁止多商户模式下的隐式默认商户。 |
| 报表用户统计缺少商户过滤 | `ReportServiceImpl.getUserSratistics()` 当前未注入 `merchantId`，商户账号可能看到全局用户统计。 |
| 批量与按 id 操作依赖调用方纪律 | 多个 mapper 存在 `where id = #{id}`、`delete where id in (...)`，需要服务层先查后校验，关键处补 scoped mapper。 |
| 公开接口需要回归保护 | 用户公开菜单浏览允许跨商户，修补私有链路时不能把公开浏览误锁死。 |

---

## 3. 推荐方案

采用 **Scope Guard + 越权矩阵**。

新增一个轻量 Spring 组件 `MerchantScopeGuard`，在不替代 `MerchantScopeUtils` 的前提下，把“当前调用者能否访问某条业务记录”的判断收拢成可复用方法。`MerchantScopeUtils` 继续负责当前账号类型和请求商户解析；`MerchantScopeGuard` 负责业务边界校验。

核心方法建议：

```java
public interface MerchantScopeGuard {
    Long requireExplicitMerchantId(Long merchantId, Long shopId, String operation);
    Long resolveAdminQueryMerchantId(Long requestedMerchantId);
    Long resolveMerchantWriteId(Long requestedMerchantId, Long shopId, String operation);
    void assertMerchantAccountCanAccess(Long ownerMerchantId, String resourceName);
    void assertSameMerchant(Long expectedMerchantId, Long actualMerchantId, String resourceName);
    boolean isPublicUserReadAllowed();
}
```

实际实现可以是一个普通 `@Component` 类，不一定需要接口；执行阶段按现有代码风格决定。关键是所有核心业务模块都通过同一套 guard 语义表达隔离规则。

---

## 4. 越权矩阵

| 模块 | 入口/行为 | 允许 | 拒绝 | 修补方式 |
|---|---|---|---|---|
| 员工 | 创建、分页、详情、启停、更新 | 平台账号全局；商户账号仅自身商户员工 | 商户账号创建平台账号或访问其他商户员工 | 保留现有先查后校验，补测试覆盖创建和按 id 操作。 |
| 商户 | 新增、编辑、状态、营业状态 | 平台账号管理商户；商户账号仅改自身营业状态 | 商户账号编辑其他商户或平台级字段 | `updateBusinessStatus` 保持自身校验；平台级写入继续拒绝商户账号。 |
| 分类 | 新增、分页、列表、详情前置、启停、删除 | 平台可指定商户；商户仅自身；公开列表可跨商户 | 商户账号按 id 操作其他商户分类 | 现有 `getAccessibleCategory` 保留，补公开列表回归和跨商户写测试。 |
| 菜品 | 新增、分页、详情、上下架、删除、更新 | 商户仅自身菜品；公开菜品列表可跨商户 | 商品与分类商户不一致；商户账号操作其他商户菜品 | 保留 `validateCategoryOwnership`，补批量删除和公开列表回归测试。 |
| 套餐 | 新增、分页、详情、上下架、删除、更新 | 商户仅自身套餐；公开套餐列表可跨商户 | 套餐分类跨商户；套餐内商品跨商户；商户账号操作其他商户套餐 | 在保存/更新时增加套餐内 dish 归属校验。 |
| 购物车 | 添加、减少、列表、清空 | 当前用户的 `userId + merchantId` 购物车 | 缺少商户；把商户 A 商品加入商户 B 购物车；清空所有商户购物车 | 多商户 schema 下要求明确商户；清空只按当前商户清。 |
| 订单 | 提交、支付、历史、详情、取消、复购、催单 | 用户仅自身订单；后台商户仅自身订单；平台全局 | 用户操作他人订单；商户处理其他商户订单；订单商品来源混商户 | 复用 `getAccessibleOrder`，提交和复购增加同商户购物车校验。 |
| 报表 | 营业额、订单、用户、Top10、导出 | 平台全局或指定商户；商户仅自身 | 商户账号看到全局用户/订单/营业数据 | 给用户统计和导出路径注入 resolved merchantId。 |
| 工作台 | 今日营业、订单概览、菜品概览、套餐概览 | 平台全局或指定商户；商户仅自身 | 商户账号看到全局概览 | 保留现有 merchantId map，补测试锁定。 |

---

## 5. 数据流

### 5.1 用户公开浏览

```
Request: /user/shop/list 或 /user/dish/list?merchantId=2
  -> Controller 接收 merchantId/shopId
  -> Service 按启用商户和公开状态查询
  -> 返回公开菜单/门店信息
```

公开浏览不要求用户 token，不绑定当前用户，也不视为越权。

### 5.2 用户私有购物车与订单

```
Authenticated user request
  -> JwtTokenUserInterceptor 写入 BaseContext.currentId
  -> Controller 传入 merchantId/shopId
  -> MerchantScopeGuard.requireExplicitMerchantId()
  -> 校验 dish/setmeal/cart/order 的 merchantId
  -> mapper 按 userId + merchantId 操作
```

多商户 schema ready 时，私有写入缺少商户直接失败。旧 schema 兼容模式下可以继续使用默认商户行为。

### 5.3 后台管理

```
Admin request
  -> JwtTokenAdminInterceptor 写入 currentId/currentMerchantId/currentAccountType
  -> 平台账号: requested merchantId 可为空或指定
  -> 商户账号: requested merchantId 必须为空或等于 currentMerchantId
  -> 按 id 操作先读取实体，再 assert owner merchantId
```

后台越权统一使用业务异常响应；认证失败仍由拦截器返回 401。

---

## 6. 错误处理

| 场景 | 行为 |
|---|---|
| 多商户私有写入缺少 `merchantId/shopId` | 抛业务异常，提示当前操作必须指定商户。 |
| 商户账号显式请求其他商户 | 抛业务异常，提示无权访问其他商户数据。 |
| 商品/套餐/分类商户不一致 | 抛业务异常，阻止写入。 |
| 购物车清空缺少商户 | 多商户模式下失败，不清空所有商户购物车。 |
| 用户访问他人订单 | 抛业务异常，保持现有用户归属校验。 |
| 商户账号访问其他商户订单/报表 | 列表和统计未显式传商户时限定到当前商户；显式传其他商户或按 id 命中其他商户记录时抛业务异常。 |
| 公开浏览指定不存在或禁用商户 | 列表接口返回空列表；详情类接口沿用现有业务失败风格，不泄露额外权限信息。 |

---

## 7. 测试策略

测试从越权矩阵开始，先补失败用例，再做实现。

| 层级 | 覆盖内容 |
|---|---|
| Guard 单元测试 | 平台账号、商户账号、用户私有链路、缺少商户、显式跨商户请求。 |
| Service 单元测试 | 员工、商户、分类、菜品、套餐、购物车、订单、报表和工作台的核心越权规则。 |
| WebMvcTest/MockMvc | 使用真实 JWT claim 或 mock interceptor context，覆盖后台商户 A 请求商户 B 数据被拒绝。 |
| Mapper/集成测试 | 对统计、批量删除、购物车和订单查询做必要 SQL 约束验证。 |
| Playwright/API E2E | 保留现有 8 个 E2E；新增少量 API E2E 验证商户 A 不能查商户 B 订单/报表，公开菜单跨商户仍可访问。 |

最低测试矩阵：

| 模块 | 必测场景 |
|---|---|
| 员工 | 商户 A 不能查看/修改商户 B 员工；商户账号不能创建平台账号。 |
| 商户 | 商户 A 不能修改商户 B 营业状态；平台账号可修改。 |
| 分类 | 商户 A 不能启停/删除商户 B 分类；公开分类列表可按商户查询。 |
| 菜品 | 商户 A 不能修改/删除商户 B 菜品；菜品分类必须同商户；公开菜品列表可跨商户。 |
| 套餐 | 套餐分类和套餐内商品必须同商户；商户 A 不能操作商户 B 套餐。 |
| 购物车 | `userId + merchantId` 隔离；缺少商户失败；跨商户商品加入失败；清空只影响当前商户。 |
| 订单 | 提交只消费同商户购物车；用户不能访问他人订单；商户不能处理其他商户订单；复购按原订单商户写入购物车。 |
| 报表/工作台 | 商户账号所有统计只返回当前商户；平台账号保留全局视图。 |

最终验证命令：

```powershell
mvn -f core/backend/pom.xml test
```

```powershell
npx playwright test
```

Playwright 命令在 `verification/e2e-tests` 下执行，使用现有配置和服务启动方式。

---

## 8. 执行切分

执行阶段使用 ECC Java/Spring 能力，按下面顺序推进：

1. 建立越权矩阵测试清单和 `MerchantScopeGuard` 单元测试。
2. 实现 guard，并将员工/商户/分类/菜品/套餐接入测试。
3. 修补购物车私有链路，禁止多商户模式下隐式清空或写入默认商户。
4. 修补订单提交、复购、后台处理和详情链路的同商户校验。
5. 修补报表/工作台统计，尤其是用户统计和导出。
6. 增加公开浏览回归测试，确认跨商户菜单浏览仍允许。
7. 跑后端全量测试和 E2E，修复回归。

每个阶段可以独立提交，提交信息使用 Conventional Commits。

---

## 9. 验收标准

| 编号 | 标准 |
|---|---|
| AC-1 | 越权矩阵覆盖员工、商户、分类、菜品、套餐、订单、购物车、报表和工作台。 |
| AC-2 | 商户账号无法访问或修改其他商户的后台业务数据。 |
| AC-3 | 用户私有链路必须绑定明确商户，且商品、套餐、购物车、订单归属一致。 |
| AC-4 | 用户公开浏览仍允许跨商户访问启用商户的公开菜单和门店信息。 |
| AC-5 | 报表和工作台对商户账号按当前 `merchantId` 限定。 |
| AC-6 | 后端 `mvn test` 通过，现有和新增 E2E 通过。 |
| AC-7 | 设计和实现不改变现有 JWT 登录模型，不要求独立 schema/库隔离。 |

---

## 10. 后续交接

本设计经用户确认并提交后，下一步进入 `writing-plans`，生成可执行计划。执行计划需要使用 ECC 的 Java/Spring/TDD/verification 能力，按照越权矩阵逐模块落地。
