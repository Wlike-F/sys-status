# 开发准备与环境

## 本地开发目录建议

```text
sys-status/
  server/        Spring Boot 后端
  web/           Vue 前端
  agent/         轻量采集端
  common/        可选：共享 DTO、签名工具、枚举
  docs/          项目文档
```

## 推荐技术栈

### 后端

- JDK 21
- Spring Boot 3.x
- Maven
- Spring Security
- MyBatis-Plus 或 Spring Data JPA
- MySQL 8.0
- Redis 可选

### 前端

- Node.js LTS
- pnpm
- Vue 3
- Vite
- TypeScript
- Pinia
- Vue Router
- ECharts
- Element Plus 或 Naive UI

### Agent

- JDK 21
- Java HTTP Client 或轻量 HTTP 库
- OSHI 用于跨平台基础指标采集
- `nvidia-smi` 用于 NVIDIA GPU 指标
- Windows 服务包装：WinSW 或 NSSM

## 开发前需要确认

| 项目 | 建议 |
|---|---|
| 后端部署机器 | 选择一台稳定在线的局域网机器，固定 IP |
| 数据库 | MVP 用 MySQL 8.0 即可 |
| Agent 运行权限 | Windows P100 主机建议管理员权限运行 |
| 端口 | 后端默认 `8080`，前端开发默认 `5173` |
| 刷新频率 | 前端 5 秒刷新，Agent 5 秒采集 |
| 历史保留 | MVP 保留 7 天短期历史 |

## 开发阶段划分

### 第一阶段：工程骨架

- 初始化 Git 仓库。
- 创建 Spring Boot 后端工程。
- 创建 Vue 前端工程。
- 创建 Agent 模块。
- 配置统一代码风格、README、文档目录。

### 第二阶段：服务器管理

- 数据库建表。
- 服务器增删改查。
- 生成注册 Token。
- 前端服务器列表和添加服务器表单。

### 第三阶段：Agent 通信闭环

- Agent 注册接口。
- Agent 心跳接口。
- Agent 快照上报接口。
- 后端在线/离线状态判断。
- 页面显示 Agent 状态。

### 第四阶段：Linux 采集

- CPU/内存采集。
- 在线用户采集。
- 进程列表采集。
- NVIDIA A100 GPU 采集。
- GPU 进程 PID 与用户映射。

### 第五阶段：Windows 采集

- CPU/内存采集。
- 在线用户采集。
- 进程列表和用户归属采集。
- P100 GPU 采集。
- Windows Service 部署方案。

### 第六阶段：看板完善

- 总览卡片。
- 服务器详情。
- 进程筛选排序。
- GPU 卡片和进程表。
- 自动刷新和异常提示。

## 建议的本地配置文件

后端：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sys_status?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: sys_status
    password: change_me

sys-status:
  agent:
    heartbeat-warn-seconds: 20
    heartbeat-offline-seconds: 60
    snapshot-retention-days: 7
```

Agent：

```yaml
server:
  baseUrl: "http://后端IP:8080"
agent:
  serverId: 1
  registerToken: "页面生成的Token"
  collectIntervalSeconds: 5
```

## 验收标准

MVP 验收时至少满足：

- 能从页面添加 3 台服务器。
- 3 台服务器能显示在线/离线状态。
- Linux A100 主机能显示 8 张 GPU 和 GPU 进程。
- Windows P100 主机能显示 GPU 基础占用和 GPU 进程。
- CPU 主机能显示 CPU/内存/用户/进程。
- 进程列表能按用户筛选，按 CPU/内存/GPU 显存排序。
