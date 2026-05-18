# Agent 采集端设计

## 定位

Agent 是部署在每台被监控服务器上的轻量采集程序，只负责只读采集和上报，不提供远程控制能力。

## 部署形态

### Linux

- 运行方式：`java -jar sys-status-agent.jar --config agent.yml`
- 服务方式：注册为 `systemd` 服务。
- 推荐权限：普通用户可采集基础指标；如果需要完整进程用户归属，建议使用具备读取进程信息权限的服务账号。

### Windows

- 运行方式：`java -jar sys-status-agent.jar --config agent.yml`
- 服务方式：MVP 可先命令行后台运行；稳定后用 WinSW 或 NSSM 注册为 Windows Service。
- 推荐权限：管理员权限运行能获得更完整的进程所属用户和性能计数器数据。

## 配置文件示例

```yaml
server:
  baseUrl: "http://192.168.1.100:8080"
agent:
  serverId: 1
  registerToken: "一次性注册Token"
  collectIntervalSeconds: 5
  heartbeatIntervalSeconds: 5
  hostnameOverride: ""
```

注册成功后，Agent 本地保存长期凭证：

```yaml
agent:
  agentId: "agent_xxx"
  agentSecret: "后端签发的长期密钥"
```

## Linux 采集项

| 类型 | 采集方式 |
|---|---|
| CPU 总体 | `/proc/stat` 或 OSHI |
| 内存 | `/proc/meminfo` 或 OSHI |
| 磁盘 | `df` 或 OSHI |
| 在线用户 | `who`、`w` |
| 进程 | `ps -eo pid,ppid,user,comm,args,%cpu,%mem,rss,etime` |
| GPU | `nvidia-smi --query-gpu=... --format=csv,noheader,nounits` |
| GPU 进程 | `nvidia-smi --query-compute-apps=pid,process_name,used_memory --format=csv,noheader,nounits` |

## Windows 采集项

| 类型 | 采集方式 |
|---|---|
| CPU 总体 | WMI/CIM 或 OSHI |
| 内存 | WMI/CIM 或 OSHI |
| 在线用户 | `quser` 或 WMI/CIM |
| 进程 | PowerShell `Get-Process`、CIM、性能计数器 |
| 进程用户 | PowerShell `Get-Process -IncludeUserName`，通常需要管理员权限 |
| GPU | `nvidia-smi`，适用于 P100 主机 |
| GPU 进程 | `nvidia-smi --query-compute-apps=...` 后按 PID 关联系统进程 |

## 统一快照模型

Agent 上报的数据应转换为统一结构：

- `server`: 主机基础信息。
- `cpu`: 总体 CPU 使用率、核心数。
- `memory`: 总量、已用、使用率。
- `sessions`: 当前登录会话。
- `processes`: 进程列表。
- `gpus`: GPU 列表。
- `gpuProcesses`: GPU 进程列表。
- `errors`: 单项采集失败信息。

## 采集失败处理

Agent 不应因为某个采集项失败而整体退出。例如 Windows 普通显卡无法采集 NVIDIA 指标时，只上报 GPU 采集错误，其他 CPU/内存/进程数据照常上报。

错误上报示例：

```json
{
  "component": "gpu",
  "level": "WARN",
  "message": "未检测到 nvidia-smi，跳过 NVIDIA GPU 采集"
}
```

## 轻量化原则

- 不内置大型 Web 框架。
- 默认只保留本地最小配置。
- 不在服务器上开额外监听端口。
- 所有通信由 Agent 主动发起，适合局域网和防火墙环境。
- 采集间隔默认 5 秒，可配置。
