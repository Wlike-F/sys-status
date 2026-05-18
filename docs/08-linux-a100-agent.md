# Linux A100 采集接入说明

## 目标

第 6 步接入 Linux A100 采集端，让 Agent 在 A100 服务器上读取 CPU、内存、GPU 卡级指标和 GPU 进程显存占用，并上报到 Spring Boot 后端。前端看板会展示 GPU 平均占用、GPU 显存占用和 GPU 卡数。

## A100 服务器前置条件

- Linux 系统可以访问后端地址，例如 `http://<后端所在机器IP>:8080`
- 已安装 Java 17，并能执行 `java -version`
- 已安装 NVIDIA 驱动，并能执行 `nvidia-smi`
- 当前运行 Agent 的 Linux 用户可以读取 `nvidia-smi` 和 `ps -o user= -p <pid>` 输出

## 接入流程

1. 在网页端添加服务器：
   - 系统选择 `Linux`
   - GPU 填写建议：`8 x NVIDIA A100`
   - 保存后复制页面给出的 Agent 安装命令

2. 在开发机打包 Agent：

```bash
mvn -pl agent -am package
```

3. 将 Agent jar 复制到 A100 服务器，并重命名为 `sys-status-agent.jar`：

```bash
scp agent/target/sys-status-agent-0.1.0-SNAPSHOT.jar user@a100-server:/opt/sys-status/sys-status-agent.jar
```

4. 在 A100 服务器上运行页面生成的命令：

```bash
java -jar /opt/sys-status/sys-status-agent.jar --server-url http://<后端IP>:8080 --server-id <服务器ID> --token <注册Token>
```

默认情况下 Agent 会持续运行并按后端返回的采集间隔循环上报。单次调试可以追加 `--once`：

```bash
java -jar /opt/sys-status/sys-status-agent.jar --server-url http://<后端IP>:8080 --server-id <服务器ID> --token <注册Token> --once
```

## 当前采集内容

Agent 在 Linux 上通过以下命令采集 GPU：

```bash
nvidia-smi --query-gpu=index,name,uuid,utilization.gpu,memory.total,memory.used,temperature.gpu,power.draw --format=csv,noheader,nounits
nvidia-smi --query-compute-apps=gpu_uuid,pid,process_name,used_memory --format=csv,noheader,nounits
ps -o user= -p <pid>
```

当前已上报并展示：

- GPU 数量
- GPU 平均利用率
- GPU 总显存、已用显存、显存占用率
- 每张 GPU 的名称、UUID、温度、功耗
- GPU 进程 PID、用户名、进程名、显存占用

其中每张 GPU 和 GPU 进程明细目前存入 `metric_snapshot.raw_json`，看板先展示汇总字段；后续详情页会直接读取这些明细。

## 注意事项

- 如果 `nvidia-smi` 不存在或执行失败，Agent 不会中断，会继续上报 CPU/内存，GPU 列表为空。
- MySQL 5.7 已有库需要执行一次表结构迁移，给 `server_node` 增加 GPU 汇总列；当前本地库 `sys-status` 已完成迁移。
- 现在注册 Token 仍是 MVP 方案，后续会改成更严格的 Agent 签名和 Token 生命周期管理。
