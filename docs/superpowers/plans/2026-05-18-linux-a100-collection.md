# Linux A100 Collection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the first Linux NVIDIA A100 collection path so the Agent can collect GPU card metrics and GPU process memory from `nvidia-smi`, submit them to Spring Boot, and show GPU summary data in the Vue dashboard.

**Architecture:** Keep the Agent lightweight. Add a small `NvidiaSmiGpuCollector` that shells out to `nvidia-smi` only when available, parses CSV output into common DTO records, and lets the existing snapshot upload carry the data. The backend stores detailed GPU payloads in `metric_snapshot.raw_json` and denormalizes GPU summary values onto `server_node` for fast dashboard rendering.

**Tech Stack:** Java 17, Maven, JUnit 5, Spring Boot JDBC, MySQL 5.7.35, Vue 3, TypeScript, Vitest.

---

### Task 1: Common GPU Snapshot DTOs

**Files:**
- Modify: `common/src/main/java/com/sysstatus/common/agent/AgentSnapshotRequest.java`
- Create: `common/src/main/java/com/sysstatus/common/agent/GpuMetric.java`
- Create: `common/src/main/java/com/sysstatus/common/agent/GpuProcessMetric.java`

- [ ] Add records for GPU card and GPU process data.
- [ ] Extend `AgentSnapshotRequest` with a `List<GpuMetric> gpus` field.
- [ ] Run `mvn -pl common test` and confirm compile failures guide any missing imports.

### Task 2: Agent Linux NVIDIA Collector

**Files:**
- Create: `agent/src/main/java/com/sysstatus/agent/collector/CommandRunner.java`
- Create: `agent/src/main/java/com/sysstatus/agent/collector/ProcessCommandRunner.java`
- Create: `agent/src/main/java/com/sysstatus/agent/collector/NvidiaSmiGpuCollector.java`
- Create: `agent/src/test/java/com/sysstatus/agent/collector/NvidiaSmiGpuCollectorTest.java`
- Modify: `agent/src/main/java/com/sysstatus/agent/SysStatusAgentApplication.java`

- [ ] Write a failing parser test using sample A100 `nvidia-smi` GPU and process CSV.
- [ ] Implement command runner abstraction and collector parser.
- [ ] Attach `gpus` to the Agent snapshot request.
- [ ] Run `mvn -pl agent -am test`.

### Task 3: Backend Snapshot Storage and Summary

**Files:**
- Modify: `server/src/main/resources/schema.sql`
- Modify: `server/src/main/java/com/sysstatus/server/servernode/ServerNode.java`
- Modify: `server/src/main/java/com/sysstatus/server/servernode/ServerNodeDto.java`
- Modify: `server/src/main/java/com/sysstatus/server/servernode/ServerNodeRepository.java`
- Modify: `server/src/main/java/com/sysstatus/server/agent/MetricSnapshotRepository.java`
- Modify: `server/src/main/java/com/sysstatus/server/agent/AgentService.java`
- Modify: `server/src/test/java/com/sysstatus/server/agent/AgentControllerIntegrationTest.java`

- [ ] Write a failing integration assertion that a snapshot with two A100 GPUs updates `gpuCount`, `gpuUsage`, and GPU memory summary on `/api/servers`.
- [ ] Add denormalized GPU fields to `server_node`.
- [ ] Serialize the GPU payload into `metric_snapshot.raw_json`.
- [ ] Run `mvn -pl server -am test`.

### Task 4: Vue Dashboard GPU Summary

**Files:**
- Modify: `web/src/types.ts`
- Modify: `web/src/App.vue`
- Modify: `web/src/styles.css`
- Modify: `web/src/api.test.ts` only if API contract helpers need updates.

- [ ] Extend `ServerNode` type with GPU summary fields.
- [ ] Add GPU average tile and per-server GPU/memory display.
- [ ] Run `npm run test --prefix web` and `npm run build --prefix web`.

### Task 5: Local DB Migration, Docs, and Git

**Files:**
- Modify: `docs/03-agent-design.md`
- Modify: `docs/04-api.md`
- Modify: `task_plan.md`
- Modify: `progress.md`

- [ ] Apply local MySQL `ALTER TABLE server_node` statements because the existing `sys-status` database already has the old table shape.
- [ ] Document the Linux A100 Agent command and `nvidia-smi` dependency.
- [ ] Run full verification: `mvn test`, `npm run test --prefix web`, `npm run build --prefix web`, `npm audit --prefix web --audit-level=moderate`.
- [ ] Commit and push.
