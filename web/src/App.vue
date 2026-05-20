<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import {
  Activity,
  Copy,
  Cpu,
  Database,
  MonitorCog,
  Plus,
  RefreshCw,
  Server,
  Terminal,
  Users,
  X,
} from '@lucide/vue';
import { createServer, getServerDetail, listServers, regenerateRegisterToken } from './api';
import type {
  CreateServerPayload,
  CreateServerResponse,
  OsType,
  ServerDetailResponse,
  ServerNode,
  ServerStatus,
} from './types';

const servers = ref<ServerNode[]>([]);
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const lastInstall = ref<CreateServerResponse | null>(null);
const copied = ref(false);
const selectedServer = ref<ServerNode | null>(null);
const detail = ref<ServerDetailResponse | null>(null);
const detailLoading = ref(false);
const detailError = ref('');

const form = reactive<CreateServerPayload>({
  name: '',
  host: '',
  osType: 'LINUX',
  gpuType: '',
  location: '',
  description: '',
});

const detailOpen = computed(() => Boolean(selectedServer.value));
const detailServer = computed(() => detail.value?.server ?? selectedServer.value);
const detailSnapshot = computed(() => detail.value?.snapshot ?? null);

const totals = computed(() => {
  const online = servers.value.filter((server) => server.status === 'ONLINE').length;
  const pending = servers.value.filter((server) => server.status === 'PENDING').length;
  const avgCpu = average(servers.value.map((server) => server.cpuUsage));
  const avgMemory = average(servers.value.map((server) => server.memoryUsage));
  const avgGpu = average(servers.value.map((server) => server.gpuUsage));
  return {
    total: servers.value.length,
    online,
    pending,
    avgCpu,
    avgMemory,
    avgGpu,
  };
});

const detailCards = computed(() => {
  const server = detailServer.value;
  const snapshot = detailSnapshot.value;
  return [
    {
      label: '在线用户',
      value: snapshot?.onlineUserCount ?? 0,
      hint: `会话 ${snapshot?.sessions?.length ?? 0}`,
    },
    {
      label: 'CPU 使用率',
      value: formatPercent(snapshot?.cpuUsage),
      hint: server?.status === 'ONLINE' ? '最新采样' : statusLabel(server?.status ?? 'PENDING'),
    },
    {
      label: 'GPU 使用率',
      value: formatPercent(server?.gpuUsage),
      hint: `${server?.gpuCount ?? snapshot?.gpus?.length ?? 0} 张卡`,
    },
    {
      label: '内存使用率',
      value: formatPercent(snapshot?.memoryUsage ?? server?.memoryUsage),
      hint: formatMemoryPair(snapshot?.memoryUsedMb, snapshot?.memoryTotalMb),
    },
    {
      label: '显存使用率',
      value: formatPercent(server?.gpuMemoryUsage),
      hint: formatGpuMemoryPair(server?.gpuMemoryUsedMb, server?.gpuMemoryTotalMb),
    },
  ];
});

onMounted(() => {
  void loadServers();
});

watch(
  detailOpen,
  (open) => {
    document.body.style.overflow = open ? 'hidden' : '';
  },
  { immediate: true },
);

onBeforeUnmount(() => {
  document.body.style.overflow = '';
});

async function loadServers() {
  loading.value = true;
  errorMessage.value = '';
  try {
    servers.value = await listServers();
  } catch (error) {
    errorMessage.value = toErrorMessage(error);
  } finally {
    loading.value = false;
  }
}

async function submitServer() {
  saving.value = true;
  errorMessage.value = '';
  try {
    lastInstall.value = await createServer({ ...form });
    resetForm();
    await loadServers();
  } catch (error) {
    errorMessage.value = toErrorMessage(error);
  } finally {
    saving.value = false;
  }
}

async function refreshToken(serverId: number) {
  errorMessage.value = '';
  try {
    lastInstall.value = await regenerateRegisterToken(serverId);
  } catch (error) {
    errorMessage.value = toErrorMessage(error);
  }
}

async function openDetails(server: ServerNode) {
  selectedServer.value = server;
  detail.value = null;
  detailError.value = '';
  detailLoading.value = true;
  errorMessage.value = '';
  const requestServerId = server.id;
  try {
    const response = await getServerDetail(server.id);
    if (selectedServer.value?.id === requestServerId) {
      detail.value = response;
    }
  } catch (error) {
    if (selectedServer.value?.id === requestServerId) {
      detailError.value = toErrorMessage(error);
    }
  } finally {
    if (selectedServer.value?.id === requestServerId) {
      detailLoading.value = false;
    }
  }
}

function closeDetails() {
  selectedServer.value = null;
  detail.value = null;
  detailError.value = '';
  detailLoading.value = false;
}

async function copyCommand() {
  if (!lastInstall.value) {
    return;
  }
  await navigator.clipboard.writeText(lastInstall.value.installCommand);
  copied.value = true;
  window.setTimeout(() => {
    copied.value = false;
  }, 1200);
}

function resetForm() {
  form.name = '';
  form.host = '';
  form.osType = 'LINUX';
  form.gpuType = '';
  form.location = '';
  form.description = '';
}

function statusLabel(status: ServerStatus) {
  const labels: Record<ServerStatus, string> = {
    PENDING: '待接入',
    ONLINE: '在线',
    WARN: '延迟',
    OFFLINE: '离线',
    DISABLED: '禁用',
  };
  return labels[status] ?? status;
}

function statusClass(status: ServerStatus | undefined) {
  return `status-${(status ?? 'PENDING').toLowerCase()}`;
}

function osLabel(osType?: OsType) {
  return osType === 'WINDOWS' ? 'Windows' : 'Linux';
}

function formatPercent(value?: number) {
  return typeof value === 'number' ? `${value.toFixed(1)}%` : '-';
}

function formatMemory(server: ServerNode) {
  if (typeof server.memoryTotalMb !== 'number' || typeof server.memoryUsedMb !== 'number') {
    return '-';
  }
  return `${Math.round(server.memoryUsedMb / 1024)} / ${Math.round(server.memoryTotalMb / 1024)} GB`;
}

function formatMemoryPair(usedMb?: number, totalMb?: number) {
  if (typeof usedMb !== 'number' || typeof totalMb !== 'number') {
    return '-';
  }
  return `${Math.round(usedMb / 1024)} / ${Math.round(totalMb / 1024)} GB`;
}

function formatGpuMemory(server: ServerNode) {
  if (typeof server.gpuMemoryTotalMb !== 'number' || typeof server.gpuMemoryUsedMb !== 'number') {
    return '-';
  }
  return `${Math.round(server.gpuMemoryUsedMb / 1024)} / ${Math.round(server.gpuMemoryTotalMb / 1024)} GB`;
}

function formatGpuMemoryPair(usedMb?: number, totalMb?: number) {
  if (typeof usedMb !== 'number' || typeof totalMb !== 'number') {
    return '-';
  }
  return `${Math.round(usedMb / 1024)} / ${Math.round(totalMb / 1024)} GB`;
}

function formatMb(value?: number) {
  return typeof value === 'number' ? `${Math.round(value)} MB` : '-';
}

function formatTimestamp(value?: string) {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  }).format(date);
}

function formatShortTimestamp(value?: string) {
  if (!value) {
    return '尚未上报';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

function average(values: Array<number | undefined>) {
  const present = values.filter((value): value is number => typeof value === 'number');
  if (present.length === 0) {
    return undefined;
  }
  return present.reduce((sum, value) => sum + value, 0) / present.length;
}

function toErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : 'Request failed';
}
</script>

<template>
  <main class="shell">
    <header class="topbar">
      <div>
        <p class="eyebrow">Lab Resource Console</p>
        <h1>实验室服务器资源看板</h1>
      </div>
      <button class="icon-button" type="button" :disabled="loading" title="刷新服务器列表" @click="loadServers">
        <RefreshCw :size="18" :class="{ spinning: loading }" />
        <span>刷新</span>
      </button>
    </header>

    <section class="metrics-strip" aria-label="Resource summary">
      <div class="metric-tile">
        <Server :size="19" />
        <span>在线 / 总数</span>
        <strong>{{ totals.online }} / {{ totals.total }}</strong>
      </div>
      <div class="metric-tile">
        <Terminal :size="19" />
        <span>待接入</span>
        <strong>{{ totals.pending }}</strong>
      </div>
      <div class="metric-tile">
        <Cpu :size="19" />
        <span>平均 CPU</span>
        <strong>{{ formatPercent(totals.avgCpu) }}</strong>
      </div>
      <div class="metric-tile">
        <Activity :size="19" />
        <span>平均 GPU</span>
        <strong>{{ formatPercent(totals.avgGpu) }}</strong>
      </div>
      <div class="metric-tile">
        <Database :size="19" />
        <span>平均内存</span>
        <strong>{{ formatPercent(totals.avgMemory) }}</strong>
      </div>
    </section>

    <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

    <div class="workspace">
      <section class="server-panel">
        <div class="panel-heading">
          <div>
            <p class="section-kicker">Servers</p>
            <h2>服务器列表</h2>
          </div>
          <span class="count-chip">{{ servers.length }} 台</span>
        </div>

        <div v-if="servers.length === 0" class="empty-state">
          <MonitorCog :size="34" />
          <strong>暂无服务器</strong>
          <span>添加节点后会显示 Agent 状态与资源采样。</span>
        </div>

        <article v-for="server in servers" :key="server.id" class="server-row">
          <div class="server-main">
            <span class="status-dot" :class="statusClass(server.status)" />
            <div>
              <div class="server-title">
                <strong>{{ server.name }}</strong>
                <span>{{ osLabel(server.osType) }}</span>
              </div>
              <p>{{ server.host }} / {{ server.gpuType || '未配置 GPU' }}</p>
              <small>最近上报 {{ formatShortTimestamp(server.lastHeartbeatAt) }}</small>
            </div>
          </div>

          <div class="server-metrics">
            <div class="resource-cell">
              <span>CPU</span>
              <strong>{{ formatPercent(server.cpuUsage) }}</strong>
            </div>
            <div class="resource-cell">
              <span>内存</span>
              <strong>{{ formatPercent(server.memoryUsage) }}</strong>
              <small>{{ formatMemory(server) }}</small>
            </div>
            <div class="resource-cell">
              <span>GPU</span>
              <strong>{{ formatPercent(server.gpuUsage) }}</strong>
              <small>{{ server.gpuCount ? `${server.gpuCount} cards` : '-' }}</small>
            </div>
            <div class="resource-cell">
              <span>显存</span>
              <strong>{{ formatPercent(server.gpuMemoryUsage) }}</strong>
              <small>{{ formatGpuMemory(server) }}</small>
            </div>
            <div class="resource-cell wide">
              <span>Agent</span>
              <strong>{{ statusLabel(server.status) }}</strong>
              <small>{{ server.agentVersion || '等待上报' }}</small>
            </div>
          </div>

          <div class="server-actions">
            <button class="ghost-button" type="button" title="重新生成接入命令" @click="refreshToken(server.id)">
              <RefreshCw :size="16" />
              <span>Token</span>
            </button>
            <button class="ghost-button strong" type="button" title="查看明细" @click="openDetails(server)">
              <Terminal :size="16" />
              <span>明细</span>
            </button>
          </div>
        </article>
      </section>

      <aside class="side-panel">
        <form class="add-form" @submit.prevent="submitServer">
          <div class="panel-heading compact">
            <div>
              <p class="section-kicker">Register</p>
              <h2>添加服务器</h2>
            </div>
            <Plus :size="20" />
          </div>

          <label>
            <span>名称</span>
            <input v-model.trim="form.name" required placeholder="A100-Server" />
          </label>
          <label>
            <span>IP / Host</span>
            <input v-model.trim="form.host" required placeholder="192.168.1.11" />
          </label>
          <label>
            <span>OS</span>
            <select v-model="form.osType">
              <option value="LINUX">Linux</option>
              <option value="WINDOWS">Windows</option>
            </select>
          </label>
          <label>
            <span>GPU</span>
            <input v-model.trim="form.gpuType" placeholder="8 x NVIDIA A100" />
          </label>
          <label>
            <span>位置</span>
            <input v-model.trim="form.location" placeholder="Lab rack" />
          </label>
          <label>
            <span>备注</span>
            <textarea v-model.trim="form.description" rows="3" placeholder="Training node / CPU host" />
          </label>

          <button class="primary-button" type="submit" :disabled="saving">
            <Plus :size="17" />
            <span v-if="saving">添加中</span>
            <span v-else>添加服务器</span>
          </button>
        </form>

        <section v-if="lastInstall" class="install-panel">
          <div class="install-title">
            <Activity :size="18" />
            <strong>Agent 接入命令</strong>
          </div>
          <code>{{ lastInstall.installCommand }}</code>
          <button class="copy-button" type="button" title="复制接入命令" @click="copyCommand">
            <Copy :size="16" />
            <span v-if="copied">已复制</span>
            <span v-else>复制命令</span>
          </button>
        </section>
      </aside>
    </div>

    <div v-if="detailOpen" class="detail-mask" @click="closeDetails" />
    <aside v-if="detailOpen" class="detail-drawer" aria-label="Server detail">
      <header class="detail-header">
        <div>
          <p class="eyebrow detail-eyebrow">服务器明细</p>
          <h2>{{ detailServer?.name || selectedServer?.name }}</h2>
          <p class="detail-subtitle">
            {{ detailServer?.host || '-' }} / {{ osLabel(detailServer?.osType) }} /
            {{ detailServer?.gpuType || '未配置 GPU' }}
          </p>
        </div>
        <button class="drawer-close" type="button" title="关闭明细" @click="closeDetails">
          <X :size="18" />
        </button>
      </header>

      <div class="detail-meta">
        <span class="status-pill" :class="statusClass(detailServer?.status)">
          {{ statusLabel(detailServer?.status ?? 'PENDING') }}
        </span>
        <span>主机 {{ detailSnapshot?.hostname || detailServer?.hostname || '未知' }}</span>
        <span>采样 {{ formatTimestamp(detailSnapshot?.collectedAt) }}</span>
      </div>

      <div class="detail-summary-grid">
        <article v-for="card in detailCards" :key="card.label" class="detail-summary-card">
          <span>{{ card.label }}</span>
          <strong>{{ card.value }}</strong>
          <small>{{ card.hint }}</small>
        </article>
      </div>

      <div v-if="detailLoading" class="detail-loading">
        <RefreshCw :size="18" class="spinning" />
        <span>加载明细</span>
      </div>
      <p v-else-if="detailError" class="detail-error">{{ detailError }}</p>
      <template v-else-if="detailSnapshot">
        <section class="detail-section">
          <div class="section-head">
            <Users :size="16" />
            <strong>在线用户</strong>
            <span>{{ detailSnapshot.onlineUserCount ?? 0 }}</span>
          </div>
          <div v-if="detailSnapshot.sessions?.length" class="session-list">
            <article
              v-for="session in detailSnapshot.sessions.slice(0, 8)"
              :key="`${session.username}-${session.terminal}-${session.loginTime}`"
              class="session-row"
            >
              <div>
                <strong>{{ session.username || '-' }}</strong>
                <p>{{ session.terminal || '-' }} / {{ session.host || '-' }}</p>
              </div>
              <span>{{ formatTimestamp(session.loginTime) }}</span>
            </article>
          </div>
          <p v-else class="empty-note">本次采样没有返回登录会话。Linux Agent 更新后会优先读取 who 输出。</p>
        </section>

        <section class="detail-section">
          <div class="section-head">
            <Terminal :size="16" />
            <strong>Top 进程</strong>
            <span>{{ detailSnapshot.processes?.length ?? 0 }}</span>
          </div>
          <div v-if="detailSnapshot.processes?.length" class="process-list">
            <article
              v-for="(process, index) in detailSnapshot.processes.slice(0, 8)"
              :key="`${process.pid ?? 'proc'}-${process.processName ?? index}`"
              class="process-row"
            >
              <div class="process-main">
                <strong>{{ process.processName || '-' }}</strong>
                <small>PID {{ process.pid ?? '-' }} / {{ process.commandLine || '-' }}</small>
              </div>
              <div class="process-meta">
                <span>{{ process.username || '-' }}</span>
                <strong>{{ formatPercent(process.cpuUsage) }}</strong>
                <small>{{ formatMb(process.memoryMb) }}</small>
              </div>
            </article>
          </div>
          <p v-else class="empty-note">本次采样没有返回进程列表。请用新 agent 包重启后刷新。</p>
        </section>

        <section class="detail-section">
          <div class="section-head">
            <Activity :size="16" />
            <strong>GPU 明细</strong>
            <span>{{ detailSnapshot.gpus?.length ?? 0 }}</span>
          </div>
          <div v-if="detailSnapshot.gpus?.length" class="gpu-list">
            <article v-for="gpu in detailSnapshot.gpus" :key="gpu.uuid || gpu.gpuIndex" class="gpu-card">
              <div class="gpu-card-head">
                <strong>GPU{{ gpu.gpuIndex }} / {{ gpu.name || '-' }}</strong>
                <span>{{ formatPercent(gpu.utilizationPercent) }}</span>
              </div>
              <div class="gpu-stat-grid">
                <div>
                  <span>显存</span>
                  <strong>{{ formatGpuMemoryPair(gpu.memoryUsedMb, gpu.memoryTotalMb) }}</strong>
                </div>
                <div>
                  <span>温度</span>
                  <strong>{{ typeof gpu.temperatureCelsius === 'number' ? `${gpu.temperatureCelsius.toFixed(1)} C` : '-' }}</strong>
                </div>
                <div>
                  <span>功耗</span>
                  <strong>{{ typeof gpu.powerWatt === 'number' ? `${gpu.powerWatt.toFixed(1)} W` : '-' }}</strong>
                </div>
                <div>
                  <span>进程</span>
                  <strong>{{ gpu.processes?.length ?? 0 }}</strong>
                </div>
              </div>
              <div v-if="gpu.processes?.length" class="gpu-process-list">
                <article
                  v-for="(process, index) in gpu.processes"
                  :key="`${gpu.gpuIndex}-${process.pid ?? process.processName ?? index}`"
                  class="gpu-process-row"
                >
                  <div>
                    <strong>{{ process.username || '-' }}</strong>
                    <p>{{ process.processName || '-' }}</p>
                  </div>
                  <span>{{ formatMb(process.usedMemoryMb) }}</span>
                </article>
              </div>
              <p v-else class="empty-note compact">当前无 GPU 计算进程</p>
            </article>
          </div>
          <p v-else class="empty-note">本次采样没有返回 GPU 信息。</p>
        </section>
      </template>
    </aside>
  </main>
</template>
