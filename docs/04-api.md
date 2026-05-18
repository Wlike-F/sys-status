# API 接口文档

## 基础约定

- API 前缀：`/api`
- 数据格式：JSON
- 时间格式：ISO-8601，例如 `2026-05-18T09:30:00+08:00`
- 前端用户接口使用登录态或 JWT。
- Agent 接口使用 `Agent-Id`、`Timestamp`、`Signature` 请求头签名，MVP 可先用 Bearer Token 简化。

## 通用响应

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

错误响应：

```json
{
  "code": 40001,
  "message": "参数错误",
  "data": null
}
```

## 1. 认证接口

### 登录

`POST /api/auth/login`

请求：

```json
{
  "username": "lab_user",
  "password": "password"
}
```

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "jwt-token",
    "user": {
      "id": 1,
      "username": "lab_user",
      "role": "MEMBER"
    }
  }
}
```

### 当前用户

`GET /api/auth/me`

响应字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 用户 ID |
| username | string | 用户名 |
| role | string | `MEMBER` 或 `ADMIN` |

## 2. 服务器管理接口

### 获取服务器列表

`GET /api/servers`

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| keyword | 否 | 名称、IP、备注关键字 |
| status | 否 | `ONLINE`、`WARN`、`OFFLINE`、`DISABLED` |

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "id": 1,
      "name": "A100-Server",
      "host": "192.168.1.11",
      "osType": "LINUX",
      "gpuSummary": "8 x NVIDIA A100",
      "status": "ONLINE",
      "onlineUsers": 3,
      "totalUsers": 12,
      "cpuUsage": 65.2,
      "memoryUsage": 71.4,
      "gpuUsage": 83.1,
      "lastHeartbeatAt": "2026-05-18T09:30:00+08:00"
    }
  ]
}
```

### 新增服务器

`POST /api/servers`

权限：管理员

请求：

```json
{
  "name": "P100-Server",
  "host": "192.168.1.13",
  "osType": "WINDOWS",
  "gpuType": "NVIDIA_P100",
  "location": "实验室机柜",
  "description": "Windows 8 卡 P100 服务器"
}
```

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 3,
    "registerToken": "one-time-token",
    "installCommand": "java -jar sys-status-agent.jar --server-url http://192.168.1.100:8080 --server-id 3 --token one-time-token"
  }
}
```

### 获取服务器详情

`GET /api/servers/{serverId}`

### 更新服务器

`PATCH /api/servers/{serverId}`

权限：管理员

### 删除服务器

`DELETE /api/servers/{serverId}`

权限：管理员

### 重新生成 Agent 注册 Token

`POST /api/servers/{serverId}/register-token`

权限：管理员

## 3. 看板接口

### 总览

`GET /api/dashboard/overview`

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "serverTotal": 3,
    "serverOnline": 2,
    "onlineUsers": 5,
    "busyServers": 1,
    "updatedAt": "2026-05-18T09:30:00+08:00"
  }
}
```

### 服务器最新快照

`GET /api/servers/{serverId}/snapshot/latest`

响应包含 CPU、内存、GPU、进程和会话摘要。

### 服务器在线用户

`GET /api/servers/{serverId}/sessions`

### 服务器进程列表

`GET /api/servers/{serverId}/processes`

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| username | 否 | 按用户筛选 |
| keyword | 否 | 按进程名或命令筛选 |
| sort | 否 | `cpu`、`memory`、`gpuMemory` |
| order | 否 | `asc`、`desc` |

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "pid": 12345,
      "username": "zhangsan",
      "processName": "python",
      "commandLine": "python train.py --config exp.yaml",
      "cpuUsage": 230.5,
      "memoryMb": 18432,
      "gpuIndexes": [0, 1],
      "gpuMemoryMb": 30210,
      "startedAt": "2026-05-18T08:00:00+08:00"
    }
  ]
}
```

### GPU 列表

`GET /api/servers/{serverId}/gpus`

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "gpuIndex": 0,
      "name": "NVIDIA A100-SXM4-40GB",
      "uuid": "GPU-xxxx",
      "temperature": 62,
      "powerWatt": 245,
      "utilization": 91,
      "memoryTotalMb": 40960,
      "memoryUsedMb": 32100,
      "processes": [
        {
          "pid": 12345,
          "username": "zhangsan",
          "processName": "python",
          "usedMemoryMb": 16050
        }
      ]
    }
  ]
}
```

## 4. Agent 接口

### Agent 注册

`POST /api/agent/register`

请求：

```json
{
  "serverId": 3,
  "registerToken": "one-time-token",
  "hostname": "P100-WIN",
  "osType": "WINDOWS",
  "agentVersion": "0.1.0"
}
```

响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "agentId": "agent_3_xxx",
    "agentSecret": "secret_xxx",
    "collectIntervalSeconds": 5,
    "heartbeatIntervalSeconds": 5
  }
}
```

### Agent 心跳

`POST /api/agent/heartbeat`

请求：

```json
{
  "agentId": "agent_3_xxx",
  "serverId": 3,
  "agentVersion": "0.1.0",
  "timestamp": "2026-05-18T09:30:00+08:00"
}
```

### Agent 上报资源快照

`POST /api/agent/snapshots`

请求结构：

```json
{
  "agentId": "agent_3_xxx",
  "serverId": 3,
  "collectedAt": "2026-05-18T09:30:00+08:00",
  "cpu": {
    "cores": 64,
    "usagePercent": 65.2
  },
  "memory": {
    "totalMb": 262144,
    "usedMb": 187170,
    "usagePercent": 71.4
  },
  "sessions": [],
  "processes": [],
  "gpus": [],
  "gpuProcesses": [],
  "errors": []
}
```

## 5. 状态枚举

### 服务器状态

| 值 | 说明 |
|---|---|
| ONLINE | 在线 |
| WARN | 心跳延迟或部分采集失败 |
| OFFLINE | 离线 |
| DISABLED | 已禁用 |

### 系统类型

| 值 | 说明 |
|---|---|
| LINUX | Linux |
| WINDOWS | Windows |

### 用户角色

| 值 | 说明 |
|---|---|
| MEMBER | 普通成员 |
| ADMIN | 管理员 |
