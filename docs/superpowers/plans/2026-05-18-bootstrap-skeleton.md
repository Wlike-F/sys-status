# Bootstrap Skeleton Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 更新技术约束并创建 Java 17、MySQL 5.7.35、Spring Boot、Vue、轻量 Agent 的项目骨架。

**Architecture:** 仓库采用“后端/Agent/Common 使用 Maven 多模块，前端使用独立 Vue 工作区”的结构。`common` 保存共享 DTO、枚举和工具；`server` 是 Spring Boot Web/API 服务；`agent` 是未来部署到服务器上的轻量采集程序；`web` 是 Vue 3 前端。

**Tech Stack:** Java 17, Maven 3.6+, Spring Boot 3.3.x, MySQL 5.7.35, Vue 3, Vite, TypeScript, pnpm/npm。

---

### Task 1: 文档约束更新

**Files:**
- Modify: `docs/02-architecture.md`
- Modify: `docs/05-development-prep.md`
- Modify: `docs/06-database.md`
- Modify: `README.md`

- [ ] **Step 1: 将 Java 版本改为 17**

把所有 `Java 21`、`JDK 21` 改成 `Java 17`、`JDK 17`。

- [ ] **Step 2: 将 MySQL 版本改为 5.7.35**

把所有 `MySQL 8.0` 改成 `MySQL 5.7.35`，并在开发准备文档中说明本地数据库使用免密登录。

- [ ] **Step 3: 检查 MySQL 5.7 兼容性说明**

在数据库文档中注明：MySQL 5.7 的 JSON 类型可用，但 MVP 建议对原始快照字段兼容为 `longtext`，避免不同 5.7 小版本或驱动配置造成迁移问题。

### Task 2: Maven 多模块骨架

**Files:**
- Create: `pom.xml`
- Create: `common/pom.xml`
- Create: `common/src/main/java/com/sysstatus/common/SysStatusCommonMarker.java`
- Create: `server/pom.xml`
- Create: `server/src/main/java/com/sysstatus/server/SysStatusServerApplication.java`
- Create: `server/src/main/resources/application.yml`
- Create: `agent/pom.xml`
- Create: `agent/src/main/java/com/sysstatus/agent/SysStatusAgentApplication.java`

- [ ] **Step 1: 创建父级 Maven POM**

父 POM 使用 `pom` packaging，聚合 `common`、`server`、`agent`，统一 Java 17、UTF-8、Spring Boot 版本、测试插件。

- [ ] **Step 2: 创建 common 模块**

`common` 只放共享模型和工具，初始只放 marker class，避免过早抽象。

- [ ] **Step 3: 创建 server 模块**

`server` 引入 Spring Boot Web、Validation、Jdbc、MySQL Driver、Test，主类能启动 Spring Boot。

- [ ] **Step 4: 创建 agent 模块**

`agent` 初始为普通 Java 应用，依赖 `common` 和 OSHI，后续实现采集逻辑。

### Task 3: Vue 前端骨架

**Files:**
- Create: `web/package.json`
- Create: `web/index.html`
- Create: `web/tsconfig.json`
- Create: `web/tsconfig.node.json`
- Create: `web/vite.config.ts`
- Create: `web/src/main.ts`
- Create: `web/src/App.vue`
- Create: `web/src/styles.css`

- [ ] **Step 1: 创建 Vite + Vue + TypeScript 最小结构**

页面先展示项目名、当前阶段和后续页面入口占位，不实现业务页面。

- [ ] **Step 2: 添加脚本**

`package.json` 包含 `dev`、`build`、`preview`、`typecheck`。

### Task 4: 骨架说明与 GitHub 推送

**Files:**
- Modify: `README.md`
- Modify: `progress.md`
- Modify: `task_plan.md`

- [ ] **Step 1: README 增加工程结构说明**

说明 `server`、`web`、`agent`、`common` 的职责。

- [ ] **Step 2: 运行验证命令**

运行：

```bash
mvn test
npm install --prefix web
npm run build --prefix web
```

- [ ] **Step 3: 提交并推送**

提交信息：

```bash
git commit -m "chore: 初始化项目工程骨架"
git push
```

## Self-review

- 覆盖了用户要求的前两步：更新约束和搭建工程骨架。
- 暂不实现服务器管理、Agent 注册、数据库建表和业务接口。
- 骨架采用适合本项目的清晰边界：Java 多模块共享模型，Vue 独立构建。
