# 实验室服务器状态监控平台

这是一个面向实验室共享服务器的 Web 监控小工具，目标是让成员快速看到局域网内服务器的在线状态、在线/总用户数、CPU/内存/GPU 占用，以及具体到用户和进程的资源使用情况。

## 当前定位

- 后端：Spring Boot
- 前端：Vue
- 采集：每台服务器部署轻量 Agent，支持 Linux 和 Windows
- 管理：网页端手动添加局域网服务器，生成 Agent 注册信息
- 场景：实验室成员都可以查看资源占用情况，便于选择空闲机器和减少资源冲突

## 文档入口

- [项目总览](docs/00-overview.md)
- [文档目录](docs/README.md)
- [需求分析](docs/01-requirements.md)
- [总体架构设计](docs/02-architecture.md)
- [Agent 采集端设计](docs/03-agent-design.md)
- [API 接口文档](docs/04-api.md)
- [开发准备与环境](docs/05-development-prep.md)
- [数据库设计草案](docs/06-database.md)
- [GitHub 协作规范](docs/07-github-workflow.md)

## 工程结构

```text
sys-status/
  common/   Java 共享模块，放 DTO、枚举、签名工具等跨模块代码
  server/   Spring Boot 后端，负责 API、服务器管理、Agent 注册和快照接收
  agent/    轻量采集端，后续部署到 Linux / Windows 服务器本机
  web/      Vue 3 前端，负责资源看板和服务器管理页面
  docs/     中文项目文档
```

当前后端和 Agent 使用 Maven 多模块管理，前端使用独立 Vite 工作区管理。

## 本地运行

创建数据库：

```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS \`sys-status\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

启动后端：

```bash
mvn install -DskipTests
cd server
mvn spring-boot:run
```

启动前端：

```bash
npm install --prefix web
npm run dev --prefix web
```

Agent 一次性注册、心跳和快照上报：

```bash
mvn -f agent/pom.xml org.codehaus.mojo:exec-maven-plugin:3.1.0:java \
  -Dexec.mainClass=com.sysstatus.agent.SysStatusAgentApplication \
  "-Dexec.args=--server-url http://127.0.0.1:8080 --server-id 1 --token 页面生成的Token"
```

## 推荐建设顺序

1. 先完成后端基础工程、数据库表、服务器管理接口。
2. 再完成 Agent 注册、心跳和快照上报接口。
3. 先做 Linux A100 服务器采集闭环，验证 GPU 进程归属。
4. 再接入 Windows CPU 主机和 Windows P100 主机。
5. 最后完善 Vue 看板、筛选、排序、历史趋势和告警。

## 当前状态

当前仓库处于开发前文档阶段，尚未生成 Spring Boot / Vue 工程代码。
