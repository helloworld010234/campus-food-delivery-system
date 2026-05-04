# D:\feynman-test 项目修复内容提取

提取时间：2026-05-04

## 结论摘要

`D:\feynman-test` 中与项目修复最相关的内容分为两类：

1. `sky-delivery` 项目本身的待修复/改进项。
2. Feynman 测试环境或测试工具链的已修复/待修复项。

其中最明确、优先级最高的项目缺陷是：`SkyApplication.java` 缺少 `@EnableScheduling`，导致 `OrderTask.java` 中的 `@Scheduled` 定时任务不会执行。

## sky-delivery 项目待修复项

### 1. 缺少 `@EnableScheduling`，定时任务不会执行

来源：

- `D:\feynman-test\reports\final-test-harness-report.md`
- `D:\feynman-test\outputs\paper-code-audit-report.md`
- `D:\feynman-test\outputs\thesis-code-audit.md`

提取内容：

- `paper-code-audit` 发现真实 Bug：`@EnableScheduling` 在 `SkyApplication.java` 中缺失。
- `OrderTask.java` 中存在两个 `@Scheduled` 定时任务：
  - 每分钟处理超时未支付订单。
  - 每天凌晨一点处理一直处于派送中的订单。
- 如果没有 `@EnableScheduling`，这些 `@Scheduled` 方法不会执行。
- 最终报告明确建议：修复 `SkyApplication.java` 中缺失的 `@EnableScheduling`。

建议修复：

```java
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
@EnableScheduling
@Slf4j
public class SkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("server started");
    }
}
```

本地核验：

- 当前 `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\SkyApplication.java` 只有 `@SpringBootApplication`、`@EnableTransactionManagement`、`@EnableCaching`、`@Slf4j`。
- 当前项目中能搜到 `@Scheduled`：
  - `OrderTask.java`
  - `WebSocketTask.java`
- 未搜到 `@EnableScheduling`。

### 2. WebSocket 握手缺少认证校验

来源：

- `D:\feynman-test\reports\final-test-harness-report.md`
- `D:\feynman-test\outputs\paper-code-audit-report.md`
- `D:\feynman-test\outputs\thesis-code-audit.md`

提取内容：

- `WebSocketServer.java` 使用 `@ServerEndpoint("/ws/{sid}")`。
- `sid` 路径参数作为 session 标识。
- 审计报告没有看到 WebSocket 握手阶段的认证检查。
- 最终报告建议：为 WebSocket 握手添加认证校验。

建议修复方向：

- 握手时验证 JWT 或一次性连接 token。
- 将 `sid` 与已认证的员工/用户身份绑定，避免任意客户端伪造 `sid`。
- 对非法或过期 token 拒绝建立 WebSocket 连接。
- 可补充心跳、断线重连、连接清理策略。

### 3. 缺少 Dockerfile 和 docker-compose.yml

来源：

- `D:\feynman-test\reports\final-test-harness-report.md`
- `D:\feynman-test\outputs\docker-analysis.md`

提取内容：

- `docker-analysis` 搜索 `D:/sky-delivery/core/backend/` 后未发现 `Dockerfile`、`docker-compose.yml` 或 `*.dockerfile`。
- 最终报告建议：为 `sky-delivery` 项目添加 Dockerfile 和 docker-compose.yml。
- 运行依赖包括：
  - MySQL
  - Redis
  - Aliyun OSS
  - WeChat Pay

建议修复方向：

- 添加基于 Java 17 的多阶段构建 Dockerfile。
- 添加 `.dockerignore`，排除 `target/`、`.idea/`、`.git/` 等。
- 添加本地开发用 `docker-compose.yml`，至少包含 MySQL、Redis、App 三个服务。

### 4. JWT 与安全性补强

来源：

- `D:\feynman-test\outputs\paper-code-audit-report.md`
- `D:\feynman-test\outputs\thesis-code-audit.md`
- `D:\feynman-test\outputs\peer-review-report.md`

提取内容：

- JWT 管理端和用户端拦截器已实现。
- 但报告指出：
  - 未实现 refresh token。
  - 未实现 token blacklist。
  - token TTL 固定为 2 小时。
  - 安全性讨论中缺少 SQL 注入、XSS、密码哈希等说明。

建议修复方向：

- 明确 access token / refresh token 策略。
- 对退出登录、封禁、密码修改等场景增加 token 失效机制。
- 补充 WebSocket 与 REST API 的统一认证边界。
- 检查 SQL 注入、XSS、密码存储与日志脱敏。

## Feynman 测试环境/工具链修复项

### 1. npm 路径问题已修复

来源：

- `D:\feynman-test\reports\final-test-harness-report.md`
- `D:\feynman-test\logs\errors\npm-setup-error.md`
- `D:\feynman-test\outputs\feynman-npm-limitation.md`
- `D:\feynman-test\outputs\phase0-env-setup.md`

提取内容：

- 原问题：`settings.json` 中 `npmCommand` 硬编码了含空格的路径。
- 原路径示例：`D:/JAVA/Program Files/nodejs/`。
- 失败原因：Feynman 内部 npm 调用没有正确引用含空格路径，命令被截断为 `D:/JAVA/Program`。
- 报错表现：
  - `[feynman] npm failed while setting up bundled packages.`
  - `setup failed (0s)`
  - `'D:/JAVA/Program' is not recognized as an internal or external command`
- 已修复：修改为 `D:/nodejs/v22/node.exe`。
- 修复影响：修复后 chat/REPL 功能可用。

曾尝试但无效的方案：

- 修改 PATH 环境变量。
- 创建 feynman wrapper 脚本清理 PATH。
- 设置 NODE 环境变量。

永久修复建议：

- 将 Node.js 安装到无空格路径，例如 `D:\nodejs\`。
- 等待 Feynman 修复路径引用问题。
- 或使用 WSL2 环境运行 Feynman。

### 2. typebox 依赖缺失导致扩展加载失败

来源：

- `D:\feynman-test\logs\feynman-chat-fixed-test.log`
- `D:\feynman-test\reports\final-test-harness-report.md`

提取内容：

- 报错：`Cannot find module 'typebox'`。
- 位置：`E:\feynman\home\.feynman\npm-global\node_modules\pi-web-access\index.ts`。
- 最终报告将其归类为 npm 依赖管理问题。

修复方向：

- 补齐缺失依赖。
- 改进 Feynman 扩展依赖安装与校验。

### 3. `pi` CLI 因 `node:sqlite` 不可用

来源：

- `D:\feynman-test\reports\final-test-harness-report.md`
- `D:\feynman-test\outputs\jobs-test.md`

提取内容：

- `pi` CLI 命令因 `node:sqlite` 模块缺失不可用。
- 影响：调度、后台进程管理功能受限。

修复方向：

- 确认 Node 版本是否支持所需内置模块。
- 确认 Feynman/`pi` 运行时使用的 Node 版本与依赖要求一致。

### 4. 工作区检测误判

来源：

- `D:\feynman-test\reports\final-test-harness-report.md`

提取内容：

- replication 技能读取了错误项目：读取到 `hify`，而不是 `sky-delivery`。
- 影响：生成的复现指南针对错误项目。

修复方向：

- 明确传入工作区路径。
- 在生成复现指南前输出并确认项目根目录。
- 增加项目识别校验，例如检查 `pom.xml`、模块名、应用入口类。

## 优先级建议

1. 高优先级：给 `SkyApplication.java` 添加 `@EnableScheduling`，恢复订单超时/配送完成定时任务。
2. 高优先级：为 WebSocket 握手添加认证校验，避免 `sid` 被伪造。
3. 中优先级：补齐 Dockerfile、docker-compose.yml，提升部署与复现能力。
4. 中优先级：完善 JWT 生命周期、token 失效机制与安全性说明。
5. 工具链优先级：固定 Feynman/Node/npm 路径与依赖，避免测试结果被环境问题污染。

