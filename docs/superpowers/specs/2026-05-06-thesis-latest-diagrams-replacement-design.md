# 论文最新图表替换与内容校准设计

日期：2026-05-06

## 1. 背景与目标

当前工作目标是在已有论文增强版 `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx` 上继续修改，解决论文图片缺少、旧图片不再适用以及最新图表存在内容准确性问题。

最新图表位于 `D:\architecture-diagrams\sky-delivery-thesis\`，共 10 张，包含 `fig-2-1`、`fig-4-1` 至 `fig-4-4`、`fig-5-1` 至 `fig-5-5`。这些图表将作为后续论文中唯一保留的图片来源。旧增强版中已有的旧图、界面截图、验证截图及其旧图题说明需要移除。

本次设计目标是：以当前 `D:\sky-delivery` 源码为唯一事实源，修正最新 10 张图表中的文字、路径、字段、状态、接口和流程内容，再将修正后的图表替换进增强版论文，同时保留论文现有正文结构和学校模板格式。

## 2. 已确认约束

1. 修改目标为 `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx`。
2. 先备份目标 DOCX，再生成修正版 DOCX，避免不可逆覆盖。
3. 原始论文 `初稿1.docx` 不作为本轮修改对象。
4. 只保留 `D:\architecture-diagrams\sky-delivery-thesis\` 中修正后的 10 张图表。
5. 抛弃增强版中原有的 11 张旧图、界面截图、验证截图和对应旧图题说明。
6. 图片优化优先解决文字和内容准确性问题；视觉调整只服务于防止信息覆盖、信息缺失、文字截断和连线含义不清。
7. 图表内容以当前 `D:\sky-delivery` 源码、配置、SQL、实体类、拦截器、服务实现和小程序工具代码为准。
8. 不写入源码无法证明的能力，不把 Mock 支付、静态页面或测试证据描述成生产级能力。
9. 保留增强版论文的封面、目录、章节标题、正文主体、页眉页脚、页码、样式和学校模板部件。
10. 按用户要求跳过最终 DOCX 页面渲染校验，只做非渲染结构 QA。
11. 临时文件、脚本、修正图片和 QA 报告优先放在 `D:\sky-delivery` 或 `D:\architecture-diagrams` 下，避免在 C 盘生成中间产物。

## 3. 图表清单与论文编号

| 论文编号 | 最新图表文件 | 论文用途 |
|---|---|---|
| 表2-1 | `fig-2-1-tech-selection.png` | 技术选型表 |
| 图4-1 | `fig-4-1-system-architecture.png` | 系统总体框架图 |
| 图4-2 | `fig-4-2-deployment-structure.png` | 系统部署与证据采集结构图 |
| 图4-3 | `fig-4-3-core-er-model.png` | 核心数据模型 ER 图 |
| 图4-4 | `fig-4-4-role-permission-matrix.png` | 角色权限矩阵 |
| 图5-1 | `fig-5-1-login-auth-flow.png` | 登录认证与接口边界流程图 |
| 图5-2 | `fig-5-2-merchant-switch-flow.png` | 商户切换与作用域传递流程图 |
| 图5-3 | `fig-5-3-shopping-cart-scope-flow.png` | 购物车商户隔离流程图 |
| 图5-4 | `fig-5-4-order-submit-flow.png` | 订单提交与支付边界流程图 |
| 图5-5 | `fig-5-5-order-notification-flow.png` | 订单通知与 WebSocket 推送流程图 |

论文中不再保留旧版的“小程序端界面实现证据”“Web 管理端界面运行证据”“系统验证结果证据”等截图图题。第六章可保留原文字和测试表，但不插入旧截图。

## 4. 内容校准范围

图表修正时重点核对以下事实：

1. 认证与 Token：
   - 用户端 Token Header 名为 `authentication`。
   - 管理端 Token Header 名为 `token`。
   - 用户端登录接口为 `POST /user/user/login`。
   - 管理端登录接口为 `POST /admin/employee/login`。

2. JWT Claims：
   - `USER_ID = "userId"`。
   - `EMP_ID = "empId"`。
   - `MERCHANT_ID = "merchantId"`。
   - `ACCOUNT_TYPE = "accountType"`。

3. 账号类型：
   - `STUDENT_USER = 0`。
   - `PLATFORM_ADMIN = 1`。
   - `MERCHANT_ADMIN = 2`。
   - `MERCHANT_STAFF = 3`。

4. 实体字段：
   - `User` 包含 `id`, `openid`, `name`, `phone`, `sex`, `idNumber`, `avatar`, `createTime`，不包含 `password` 字段。
   - `Orders` 状态常量为 `PENDING_PAYMENT(1)`, `TO_BE_CONFIRMED(2)`, `CONFIRMED(3)`, `DELIVERY_IN_PROGRESS(4)`, `COMPLETED(5)`, `CANCELLED(6)`。
   - `Orders` 支付状态为 `UN_PAID(0)`, `PAID(1)`, `REFUND(2)`。
   - `ShoppingCart` 包含 `id`, `userId`, `merchantId`, `dishId`, `setmealId`, `name`, `dishFlavor`, `number`, `amount`, `image`, `createTime`。
   - `OrderDetail` 包含 `id`, `orderId`, `dishId`, `setmealId`, `name`, `dishFlavor`, `number`, `amount`, `image`。

5. WebSocket：
   - 端点为 `/ws/{sid}`。
   - 认证方式为 URL 参数 `?token=JWT_TOKEN`。
   - Session 存储为 `ConcurrentHashMap<String, Session> SESSION_MAP`。
   - key 为 `sid`，即 `empId` 字符串。
   - 支持广播 `sendToAllClient()` 和单发 `sendToClient(sid, message)`。

6. 商户作用域：
   - 前端 `withMerchantScope(params, merchantId)` 将 `merchantId` 或 `shopId` 注入请求参数。
   - 后端通过 `MerchantScopeUtils.resolveQueryMerchantId()` 解析商户范围。
   - 商户账号只能访问自身绑定商户数据。
   - 平台管理员不受商户作用域限制。

7. 订单事务：
   - `submitOrder()` 使用 `@Transactional`。
   - Redis 分布式锁 key 为 `order:submit:lock:{userId}`，过期时间为 10 秒。
   - Mock 支付开启时直接调用 `paySuccess()`。
   - `paySuccess()` 更新为 `status = TO_BE_CONFIRMED(2)`，`payStatus = PAID(1)`。

8. 白名单路径：
   - 用户端白名单包含 `/user/user/login`, `/user/shop/**`, `/user/dish/list`, `/user/category/list`, `/user/setmeal/list`, `/user/common/download`。
   - 管理端白名单包含 `/admin/employee/login`, `/admin/common/download`。

## 5. 图表修正策略

每张图表以对应 HTML 为主要编辑入口，重新导出 PNG。修正原则如下：

1. 如果图中文字与源码不一致，直接按当前源码修正。
2. 如果图中存在信息覆盖、文字压线、节点文字截断或标签相互遮挡，优先通过调整节点尺寸、换行、缩短标签、移动连接线和增大局部留白解决。
3. 如果图中缺少关键事实，补充到对应节点、注释或图例中。
4. 如果图中存在过度展开、导致论文阅读负担过重的信息，保留关键路径，细节放入图注或论文正文过渡句中。
5. 每张图保留浅色学术风格、白色背景和 1600x1200 画布，不为了美化而重写全部结构。

## 6. DOCX 替换策略

DOCX 处理采取局部替换：

1. 先读取目标 DOCX 的段落与图片关系，定位旧图题和旧插图说明段。
2. 删除旧增强版中与旧图对应的插图、过渡句和图题。
3. 保留原有章节正文、表 3.1、表 4.1、表 5.1、表 6.1，以及原论文已有正文段落。
4. 在第二章 `2.4 技术选型与适配性分析` 附近插入 `表2-1 技术选型表`。
5. 在第四章架构、部署、数据模型和安全边界相关小节附近插入 `图4-1` 至 `图4-4`。
6. 在第五章认证、商户切换、购物车、订单提交、订单通知相关小节附近插入 `图5-1` 至 `图5-5`。
7. 插入图题时采用中文规范编号，保持与论文已有正文风格一致。
8. 不插入第六章旧验证截图；第六章保留文字和测试表。

## 7. 非渲染 QA 标准

最终不执行页面 PNG 渲染校验。验收改为以下结构性检查：

1. 目标 DOCX 可作为 OOXML ZIP 包正常打开。
2. 修正版 DOCX 中媒体数量与 10 张新图表插入相符，并且旧图题不再出现。
3. `document.xml.rels` 中存在新图片关系。
4. 新增图题包含 `表2-1`、`图4-1` 至 `图4-4`、`图5-1` 至 `图5-5`。
5. 旧图题“小程序端界面实现证据”“Web管理端界面运行证据”“系统验证结果证据”等不再出现。
6. 封面、目录、章节标题数量和主要模板部件未被整体替换。
7. 生成 QA 报告记录输入文件、输出文件、插入图片清单、移除旧图题清单、跳过渲染校验的原因。

## 8. 风险与应对

1. 最新图表内容与源码差异较多。应对方式是逐图建立校准清单，先修正事实错误，再调整排版。
2. HTML 图表结构可能不易编辑。应对方式是优先局部改 HTML/CSS；必要时复用现有生成方式重建单张图，但不改变整体风格。
3. 删除旧图片可能误删正文。应对方式是按旧图题锚点和图片关系定位，只删除图文增强段落，不删除原论文主体段落。
4. 插入 10 张图可能导致目录页码变化。由于用户要求跳过页面渲染校验，本轮只做结构 QA，并在交付说明中明确该边界。
5. 当前仓库存在大量既有未提交改动。实施时只修改本任务相关的图表、脚本、QA 报告和 DOCX，不回退无关改动。

## 9. 后续实施入口

用户审阅并批准本设计文档后，下一步进入实施计划阶段。实施计划需要拆分为：源码事实抽取、10 张图表逐图校准、PNG 重新导出、DOCX 旧图清理、10 张新图插入、非渲染 QA、最终交付说明。
