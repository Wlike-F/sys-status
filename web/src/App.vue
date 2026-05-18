<script setup lang="ts">
const modules = [
  { name: 'server', label: 'Spring Boot API 服务', status: '骨架已规划' },
  { name: 'web', label: 'Vue 资源看板', status: '当前模块' },
  { name: 'agent', label: 'Linux / Windows 轻量采集端', status: '骨架已规划' },
  { name: 'common', label: '共享 DTO 与枚举', status: '骨架已规划' },
];
</script>

<template>
  <main class="app-shell">
    <section class="workspace">
      <p class="eyebrow">Sys Status</p>
      <h1>实验室服务器状态监控平台</h1>
      <p class="summary">
        当前处于工程骨架阶段。下一步会接入服务器管理、Agent 注册和资源快照上报闭环。
      </p>

      <div class="module-grid" aria-label="项目模块">
        <article v-for="module in modules" :key="module.name" class="module-card">
          <span class="module-name">{{ module.name }}</span>
          <strong>{{ module.label }}</strong>
          <small>{{ module.status }}</small>
        </article>
      </div>
    </section>
  </main>
</template>
