# Server Detail Drawer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the weak inline detail block with a right-side drawer and a structured backend detail payload for server snapshots.

**Architecture:** Keep the server list as the entry point. Move detail rendering into a drawer that shows one server at a time and groups the latest snapshot into clear sections: summary, online users, top processes, and GPU process breakdown. On the backend, stop returning raw snapshot JSON and return a dedicated DTO that pre-parses the latest snapshot with counts, ordering, and safe field exposure.

**Tech Stack:** Spring Boot 3, MySQL 5.7, Vue 3, TypeScript, Vite.

---

### Task 1: Backend detail DTO and endpoint

**Files:**
- Modify: `server/src/main/java/com/sysstatus/server/servernode/ServerNodeController.java`
- Modify: `server/src/main/java/com/sysstatus/server/servernode/ServerNodeService.java`
- Modify: `server/src/main/java/com/sysstatus/server/agent/MetricSnapshotRepository.java`
- Create: `server/src/main/java/com/sysstatus/server/servernode/ServerDetailResponse.java`
- Create: `server/src/main/java/com/sysstatus/server/servernode/ServerSnapshotSummary.java`
- Modify: `server/src/test/java/com/sysstatus/server/agent/AgentControllerIntegrationTest.java`
- Modify: `server/src/test/java/com/sysstatus/server/servernode/ServerNodeControllerIntegrationTest.java`

- [ ] **Step 1: Write the failing test**

```java
mockMvc.perform(get("/api/servers/" + serverId + "/detail"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.server.id").value(serverId))
        .andExpect(jsonPath("$.data.onlineUserCount").value(1))
        .andExpect(jsonPath("$.data.processes[0].processName").value("python"))
        .andExpect(jsonPath("$.data.gpus[0].processes[0].usedMemoryMb").value(16050));
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl server -Dtest=ServerNodeControllerIntegrationTest,AgentControllerIntegrationTest test`
Expected: FAIL because `/api/servers/{id}/detail` does not exist yet.

- [ ] **Step 3: Write minimal implementation**

```java
public record ServerDetailResponse(
        ServerNodeDto server,
        LocalDateTime collectedAt,
        Double cpuUsage,
        Long memoryTotalMb,
        Long memoryUsedMb,
        Double memoryUsage,
        int onlineUserCount,
        List<SessionMetric> sessions,
        List<ProcessMetric> processes,
        List<GpuMetric> gpus
) {
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -pl server -Dtest=ServerNodeControllerIntegrationTest,AgentControllerIntegrationTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add server/src/main/java/com/sysstatus/server/servernode server/src/main/java/com/sysstatus/server/agent server/src/test/java/com/sysstatus/server
git commit -m "feat: add structured server detail payload"
```

### Task 2: Drawer-based detail UI

**Files:**
- Modify: `web/src/App.vue`
- Modify: `web/src/types.ts`
- Modify: `web/src/api.ts`
- Modify: `web/src/api.test.ts`
- Modify: `web/src/styles.css`

- [ ] **Step 1: Write the failing test**

```ts
expect(details.server.name).toBe('A100');
expect(details.onlineUserCount).toBe(1);
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm run test --prefix web`
Expected: FAIL because the new `getServerDetail` client method and drawer state do not exist yet.

- [ ] **Step 3: Write minimal implementation**

```vue
<aside v-if="detailOpen" class="detail-drawer">
  <!-- summary header + section blocks -->
</aside>
```

- [ ] **Step 4: Run test to verify it passes**

Run: `npm run test --prefix web && npm run build --prefix web`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add web/src
git commit -m "feat: redesign server detail drawer"
```

### Task 3: End-to-end verification

**Files:**
- No new files; validate runtime behavior.

- [ ] **Step 1: Start backend and verify detail endpoint**

Run: `mvn -f server/pom.xml spring-boot:run`

- [ ] **Step 2: Call the detail endpoint**

Run: `Invoke-RestMethod http://127.0.0.1:8080/api/servers/3/detail`
Expected: JSON includes `server`, `onlineUserCount`, `processes`, and `gpus`.

- [ ] **Step 3: Open the UI and confirm the drawer layout**

Run the app at `http://127.0.0.1:5173/`
Expected: clicking `Detail` opens a right-side drawer with grouped sections and no cramped inline blocks.

