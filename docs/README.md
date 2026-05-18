# 文档目录

这里集中存放实验室服务器状态监控平台的中文设计文档。文件名使用英文短名，是为了避免 Windows、GitHub、命令行工具在中文路径上出现编码或链接兼容问题；正文标题和内容仍然使用中文。

## 阅读顺序

1. [项目总览](00-overview.md)
2. [需求分析](01-requirements.md)
3. [总体架构设计](02-architecture.md)
4. [Agent 采集端设计](03-agent-design.md)
5. [API 接口文档](04-api.md)
6. [开发准备与环境](05-development-prep.md)
7. [数据库设计草案](06-database.md)
8. [GitHub 协作规范](07-github-workflow.md)

## 当前设计结论

项目采用 `Spring Boot + Vue + 轻量 Agent`。Agent 部署在 Linux/Windows 服务器本机，负责只读采集和主动上报；后端保存服务器资产、Agent 状态、资源快照；前端展示实验室共享服务器当前资源占用。
