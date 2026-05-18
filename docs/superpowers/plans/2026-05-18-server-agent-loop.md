# Server And Agent Loop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现后端基础服务器管理能力、MySQL 5.7.35 数据库初始化、Agent 注册/心跳/最小快照上报闭环。

**Architecture:** 后端使用 Spring Boot + JDBC，按 `servernode`、`agent`、`shared` 分包，避免现在引入过重 ORM。数据库使用用户指定的 MySQL 库名 `sys-status`，SQL 中用反引号处理连字符；测试使用 H2 MySQL 模式和同一份 `schema.sql`。

**Tech Stack:** Java 17, Spring Boot 3.3.x, JdbcTemplate, MySQL 5.7.35, JUnit 5, MockMvc。

---

### Task 1: 数据库和配置

**Files:**
- Modify: `server/src/main/resources/application.yml`
- Create: `server/src/main/resources/schema.sql`
- Modify: `docs/05-development-prep.md`

- [ ] 创建 MySQL 数据库：`CREATE DATABASE IF NOT EXISTS \`sys-status\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
- [ ] 将 JDBC URL 改为 `jdbc:mysql://localhost:3306/sys-status?...`
- [ ] 创建 `server_node`、`agent_credential`、`metric_snapshot` 表。

### Task 2: 服务器管理 API

**Files:**
- Create: `server/src/test/java/com/sysstatus/server/servernode/ServerNodeControllerIntegrationTest.java`
- Create: `server/src/main/java/com/sysstatus/server/shared/ApiResponse.java`
- Create: `server/src/main/java/com/sysstatus/server/servernode/*.java`

- [ ] 先写 MockMvc 测试，覆盖新增服务器、列表查询、重新生成 Token。
- [ ] 实现 Controller、Service、JdbcRepository。
- [ ] 安装命令返回可直接用于 Agent 的参数。

### Task 3: Agent 注册/心跳/快照 API

**Files:**
- Create: `common/src/main/java/com/sysstatus/common/agent/*.java`
- Create: `server/src/test/java/com/sysstatus/server/agent/AgentControllerIntegrationTest.java`
- Create: `server/src/main/java/com/sysstatus/server/agent/*.java`

- [ ] 先写 MockMvc 测试：使用服务器 Token 注册 Agent，随后心跳和上报快照。
- [ ] 实现 Agent 注册并生成长期凭证。
- [ ] 心跳更新服务器在线状态和最近心跳。
- [ ] 最小快照更新 CPU/内存摘要，并插入 `metric_snapshot`。

### Task 4: Agent 最小客户端

**Files:**
- Modify: `agent/pom.xml`
- Create: `agent/src/main/java/com/sysstatus/agent/config/AgentConfig.java`
- Create: `agent/src/main/java/com/sysstatus/agent/client/AgentApiClient.java`
- Modify: `agent/src/main/java/com/sysstatus/agent/SysStatusAgentApplication.java`

- [ ] Agent 支持命令行参数：`--server-url`、`--server-id`、`--token`。
- [ ] Agent 调用注册、心跳、最小快照接口。
- [ ] 不做常驻采集循环，先完成一次性闭环验证。

### Task 5: 验证和提交

**Files:**
- Modify: `progress.md`
- Modify: `task_plan.md`

- [ ] 运行 `mysql -u root -e "SHOW DATABASES LIKE 'sys-status';"` 验证库存在。
- [ ] 运行 `mvn test`。
- [ ] 启动后端并用 HTTP 请求验证最小闭环。
- [ ] 提交并推送。
