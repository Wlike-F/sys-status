# 实验室服务器状态监控平台设计说明

## 设计结论

本项目采用 Spring Boot + Vue + 轻量 Agent 架构。后端负责用户、服务器资产、Agent 注册、快照接收和查询 API；前端负责资源看板和服务器管理；Agent 部署在 Linux/Windows 服务器本机，采集 CPU、内存、在线用户、进程、NVIDIA GPU 和 GPU 进程后上报后端。

## 需求范围

MVP 面向实验室内部成员，默认所有登录用户都能查看所有服务器、用户、进程和 GPU 占用。管理员额外拥有服务器管理和 Agent 注册 Token 生成功能。

首批服务器包括：

- Linux 8 卡 NVIDIA A100。
- Windows CPU 主机，普通显卡。
- Windows 8 卡 NVIDIA P100。

## 推荐架构

```text
Agent(Linux/Windows) -> Spring Boot API -> Database -> Vue Dashboard
```

Agent 方案优先于 SSH/WinRM 远程轮询，因为它不要求后端保存服务器管理员密码，跨平台适配更清晰，也更容易处理 Windows 和 Linux 采集差异。

## 主要模块

- `server`：Spring Boot 后端。
- `web`：Vue 前端。
- `agent`：轻量采集端。
- `common`：可选共享模块，保存 DTO、枚举和签名工具。

## 数据流

1. 管理员在网页端添加服务器。
2. 后端生成一次性注册 Token 和安装命令。
3. 管理员在目标服务器启动 Agent。
4. Agent 注册成功后获得长期凭证。
5. Agent 周期性心跳和上报资源快照。
6. 前端查询后端 API 展示当前状态。

## 错误处理

- 单项采集失败不影响其他指标上报。
- Agent 心跳延迟超过阈值后，服务器状态从 `ONLINE` 变为 `WARN` 或 `OFFLINE`。
- GPU 采集不可用时，页面展示“GPU 指标不可用”，而不是让整台服务器离线。

## 测试重点

- Linux A100 的 GPU 进程 PID 与用户映射。
- Windows P100 的 `nvidia-smi` 采集兼容性。
- Windows 进程所属用户采集权限。
- Agent 离线、Token 错误、重复注册、采集部分失败。
- 前端筛选、排序、自动刷新和离线状态展示。

## 文档索引

- 文档目录：`docs/README.md`
- 需求分析：`docs/01-requirements.md`
- 总体架构：`docs/02-architecture.md`
- Agent 设计：`docs/03-agent-design.md`
- API 文档：`docs/04-api.md`
- 开发准备：`docs/05-development-prep.md`
- 数据库设计：`docs/06-database.md`
- GitHub 规范：`docs/07-github-workflow.md`
