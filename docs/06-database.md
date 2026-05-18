# 数据库设计草案

## 设计原则

- 服务器、Agent、快照分离。
- 最新状态查询要快。
- 历史数据只保留短期，避免数据库快速膨胀。
- GPU、进程等明细可先使用关系表，必要时对原始快照保留 JSON。

## 核心表

### app_user

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| username | varchar | 登录用户名 |
| password_hash | varchar | 密码哈希 |
| role | varchar | `MEMBER` / `ADMIN` |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

### server_node

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| name | varchar | 服务器名称 |
| host | varchar | IP 或域名 |
| os_type | varchar | `LINUX` / `WINDOWS` |
| gpu_type | varchar | GPU 类型描述 |
| location | varchar | 位置 |
| description | text | 备注 |
| status | varchar | 在线状态 |
| last_heartbeat_at | datetime | 最近心跳 |
| enabled | boolean | 是否启用 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

### agent_credential

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| server_id | bigint | 服务器 ID |
| agent_id | varchar | Agent 标识 |
| agent_secret_hash | varchar | Agent 密钥哈希 |
| agent_version | varchar | Agent 版本 |
| registered_at | datetime | 注册时间 |
| last_seen_at | datetime | 最近连接时间 |

### agent_register_token

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| server_id | bigint | 服务器 ID |
| token_hash | varchar | 一次性 Token 哈希 |
| used | boolean | 是否已使用 |
| expires_at | datetime | 过期时间 |
| created_at | datetime | 创建时间 |

### metric_snapshot

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| server_id | bigint | 服务器 ID |
| collected_at | datetime | 采集时间 |
| cpu_usage | decimal | CPU 使用率 |
| memory_total_mb | bigint | 内存总量 |
| memory_used_mb | bigint | 已用内存 |
| memory_usage | decimal | 内存使用率 |
| online_users | int | 在线用户数 |
| process_count | int | 进程数 |
| gpu_count | int | GPU 数 |
| avg_gpu_usage | decimal | 平均 GPU 使用率 |
| raw_json | json/text | 原始快照，便于排错和兼容 |

### user_session_snapshot

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| snapshot_id | bigint | 快照 ID |
| server_id | bigint | 服务器 ID |
| username | varchar | 用户名 |
| terminal | varchar | 终端/会话 |
| login_from | varchar | 登录来源 |
| login_at | datetime | 登录时间 |

### process_snapshot

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| snapshot_id | bigint | 快照 ID |
| server_id | bigint | 服务器 ID |
| pid | bigint | PID |
| ppid | bigint | 父 PID |
| username | varchar | 用户名 |
| process_name | varchar | 进程名 |
| command_line | text | 命令行 |
| cpu_usage | decimal | CPU 占用 |
| memory_mb | bigint | 内存 MB |
| gpu_memory_mb | bigint | GPU 显存 MB |
| started_at | datetime | 启动时间 |

### gpu_snapshot

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| snapshot_id | bigint | 快照 ID |
| server_id | bigint | 服务器 ID |
| gpu_index | int | GPU 编号 |
| uuid | varchar | GPU UUID |
| name | varchar | GPU 名称 |
| temperature | int | 温度 |
| power_watt | decimal | 功耗 |
| utilization | decimal | GPU 使用率 |
| memory_total_mb | bigint | 显存总量 |
| memory_used_mb | bigint | 已用显存 |

### gpu_process_snapshot

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| snapshot_id | bigint | 快照 ID |
| server_id | bigint | 服务器 ID |
| gpu_index | int | GPU 编号 |
| pid | bigint | PID |
| username | varchar | 用户名 |
| process_name | varchar | 进程名 |
| used_memory_mb | bigint | 使用显存 |

## 最新状态优化

可以增加 `server_latest_state` 表或 Redis 缓存，用于看板快速查询：

| 字段 | 类型 | 说明 |
|---|---|---|
| server_id | bigint | 服务器 ID |
| snapshot_id | bigint | 最新快照 ID |
| summary_json | json/text | 看板摘要 |
| updated_at | datetime | 更新时间 |

MVP 可以先直接查询最新 `metric_snapshot`，等数据量上来后再优化。
