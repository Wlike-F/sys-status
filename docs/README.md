# 文档目录

这里集中保存实验室服务器状态监控平台的中文设计文档。文件名使用英文短名，正文使用中文，方便在 Windows、GitHub 和命令行工具之间稳定浏览。

## 阅读顺序

1. [项目总览](00-overview.md)
2. [需求分析](01-requirements.md)
3. [总体架构设计](02-architecture.md)
4. [Agent 采集端设计](03-agent-design.md)
5. [API 接口文档](04-api.md)
6. [开发准备与环境](05-development-prep.md)
7. [数据库设计草案](06-database.md)
8. [GitHub 协作规范](07-github-workflow.md)
9. [Linux A100 采集接入说明](08-linux-a100-agent.md)

## 当前实现进度

项目已经具备 Spring Boot 后端、Vue 看板、手动添加服务器、Agent 注册/心跳/快照上报，以及 Linux `nvidia-smi` GPU 采集的最小闭环。当前 Linux A100 Agent 默认持续运行；本地调试可以增加 `--once` 参数只上报一次。
