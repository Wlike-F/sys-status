# GitHub 协作规范

## 仓库建议

仓库名建议：`sys-status` 或 `lab-server-monitor`

推荐分支：

| 分支 | 用途 |
|---|---|
| main | 稳定分支，只合并可运行版本 |
| develop | 日常开发集成分支 |
| feature/* | 功能分支 |
| fix/* | 修复分支 |
| docs/* | 文档分支 |

## Commit 规范

建议使用简洁的 Conventional Commits：

```text
docs: 完善需求分析和接口文档
feat(server): 添加服务器管理接口
feat(agent): 实现 Linux CPU 内存采集
fix(web): 修复进程表排序问题
```

常用类型：

| 类型 | 说明 |
|---|---|
| docs | 文档 |
| feat | 新功能 |
| fix | 修复 |
| refactor | 重构 |
| test | 测试 |
| chore | 构建、依赖、脚手架 |

## Issue 分类

建议使用标签：

- `backend`
- `frontend`
- `agent`
- `linux`
- `windows`
- `gpu`
- `docs`
- `bug`
- `enhancement`

## Pull Request 要求

每个 PR 至少说明：

- 做了什么。
- 如何验证。
- 是否影响 Agent 部署或数据库结构。
- 截图或接口示例，前端改动建议附图。

## 里程碑

### v0.1 文档与工程骨架

- 中文需求文档。
- API 文档。
- Spring Boot / Vue / Agent 工程骨架。
- GitHub 仓库初始化。

### v0.2 服务器管理与 Agent 注册

- 服务器增删改查。
- 注册 Token。
- Agent 注册和心跳。
- 在线/离线判断。

### v0.3 Linux A100 闭环

- Linux 基础指标。
- 在线用户。
- 进程列表。
- A100 GPU 和 GPU 进程。
- 前端展示。

### v0.4 Windows 支持

- Windows CPU 主机基础指标。
- Windows P100 GPU 指标。
- Windows 进程用户归属。

### v0.5 看板完善

- 总览仪表盘。
- 筛选排序。
- 短期历史趋势。
- Agent 异常提示。
