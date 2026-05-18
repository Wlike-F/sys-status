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
      label: 'Online users',
      value: snapshot?.onlineUserCount ?? 0,
      hint: `${snapshot?.sessions?.length ?? 0} sessions`,
    },
    {
      label: 'CPU usage',
      value: formatPercent(snapshot?.cpuUsage),
      hint: server?.status === 'ONLINE' ? 'Latest sample' : statusLabel(server?.status ?? 'PENDING'),
    },
    {
      label: 'GPU usage',
      value: formatPercent(server?.gpuUsage),
      hint: `${server?.gpuCount ?? snapshot?.gpus?.length ?? 0} cards`,
    },
    {
      label: 'Memory usage',
      value: formatPercent(snapshot?.memoryUsage ?? server?.memoryUsage),
      hint: formatMemoryPair(snapshot?.memoryUsedMb, snapshot?.memoryTotalMb),
    },
    {
      label: 'GPU memory',
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
    PENDING: 'Pending',
    ONLINE: 'Online',
    WARN: 'Lagging',
    OFFLINE: 'Offline',
    DISABLED: 'Disabled',
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
        <h1>Server resource dashboard</h1>
      </div>
      <button class="icon-button" type="button" :disabled="loading" title="Refresh server list" @click="loadServers">
        <RefreshCw :size="18" :class="{ spinning: loading }" />
        <span>Refresh</span>
      </button>
    </header>

    <section class="metrics-strip" aria-label="Resource summary">
      <div class="metric-tile">
        <Server :size="19" />
        <span>Servers</span>
        <strong>{{ totals.online }} / {{ totals.total }}</strong>
      </div>
      <div class="metric-tile">
        <Terminal :size="19" />
        <span>Pending</span>
        <strong>{{ totals.pending }}</strong>
      </div>
      <div class="metric-tile">
        <Cpu :size="19" />
        <span>Avg CPU</span>
        <strong>{{ formatPercent(totals.avgCpu) }}</strong>
      </div>
      <div class="metric-tile">
        <Activity :size="19" />
        <span>Avg GPU</span>
        <strong>{{ formatPercent(totals.avgGpu) }}</strong>
      </div>
      <div class="metric-tile">
        <Database :size="19" />
        <span>Avg Memory</span>
        <strong>{{ formatPercent(totals.avgMemory) }}</strong>
      </div>
    </section>

    <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

    <div class="workspace">
      <section class="server-panel">
        <div class="panel-heading">
          <div>
            <p class="section-kicker">Servers</p>
            <h2>Server list</h2>
          </div>
          <span class="count-chip">{{ servers.length }} nodes</span>
        </div>

        <div v-if="servers.length === 0" class="empty-state">
          <MonitorCog :size="34" />
          <strong>No servers yet</strong>
          <span>Add a lab server and this view will show Agent status and resource usage.</span>
        </div>

        <article v-for="server in servers" :key="server.id" class="server-row">
          <div class="server-main">
            <span class="status-dot" :class="statusClass(server.status)" />
            <div>
              <div class="server-title">
                <strong>{{ server.name }}</strong>
                <span>{{ osLabel(server.osType) }}</span>
              </div>
              <p>{{ server.host }} / {{ server.gpuType || 'GPU not set' }}</p>
            </div>
          </div>

          <div class="resource-cell">
            <span>CPU</span>
            <strong>{{ formatPercent(server.cpuUsage) }}</strong>
          </div>
          <div class="resource-cell">
            <span>GPU</span>
            <strong>{{ formatPercent(server.gpuUsage) }}</strong>
            <small>{{ server.gpuCount ? `${server.gpuCount} cards` : '-' }}</small>
          </div>
          <div class="resource-cell">
            <span>Memory</span>
            <strong>{{ formatPercent(server.memoryUsage) }}</strong>
            <small>{{ formatMemory(server) }}</small>
          </div>
          <div class="resource-cell">
            <span>GPU MEM</span>
            <strong>{{ formatPercent(server.gpuMemoryUsage) }}</strong>
            <small>{{ formatGpuMemory(server) }}</small>
          </div>
          <div class="resource-cell wide">
            <span>Agent</span>
            <strong>{{ statusLabel(server.status) }}</strong>
            <small>{{ server.agentVersion || server.lastHeartbeatAt || 'Waiting' }}</small>
          </div>
          <button class="ghost-button" type="button" title="Regenerate install command" @click="refreshToken(server.id)">
            <RefreshCw :size="16" />
            <span>Token</span>
          </button>
          <button class="ghost-button" type="button" title="Open details" @click="openDetails(server)">
            <Terminal :size="16" />
            <span>Detail</span>
          </button>
        </article>
      </section>

      <aside class="side-panel">
        <form class="add-form" @submit.prevent="submitServer">
          <div class="panel-heading compact">
            <div>
              <p class="section-kicker">Register</p>
              <h2>Add server</h2>
            </div>
            <Plus :size="20" />
          </div>

          <label>
            <span>Name</span>
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
            <span>Location</span>
            <input v-model.trim="form.location" placeholder="Lab rack" />
          </label>
          <label>
            <span>Notes</span>
            <textarea v-model.trim="form.description" rows="3" placeholder="Training node / CPU host" />
          </label>

          <button class="primary-button" type="submit" :disabled="saving">
            <Plus :size="17" />
            <span v-if="saving">Adding</span>
            <span v-else>Add server</span>
          </button>
        </form>

        <section v-if="lastInstall" class="install-panel">
          <div class="install-title">
            <Activity :size="18" />
            <strong>Agent install command</strong>
          </div>
          <code>{{ lastInstall.installCommand }}</code>
          <button class="copy-button" type="button" title="Copy install command" @click="copyCommand">
            <Copy :size="16" />
            <span v-if="copied">Copied</span>
            <span v-else>Copy command</span>
          </button>
        </section>
      </aside>
    </div>

    <div v-if="detailOpen" class="detail-mask" @click="closeDetails" />
    <aside v-if="detailOpen" class="detail-drawer" aria-label="Server detail">
      <header class="detail-header">
        <div>
          <p class="eyebrow detail-eyebrow">Server Detail</p>
          <h2>{{ detailServer?.name || selectedServer?.name }}</h2>
          <p class="detail-subtitle">
            {{ detailServer?.host || '-' }} / {{ osLabel(detailServer?.osType) }} /
            {{ detailServer?.gpuType || 'GPU not set' }}
          </p>
        </div>
        <button class="drawer-close" type="button" title="Close detail" @click="closeDetails">
          <X :size="18" />
        </button>
      </header>

      <div class="detail-meta">
        <span class="status-pill" :class="statusClass(detailServer?.status)">
          {{ statusLabel(detailServer?.status ?? 'PENDING') }}
        </span>
        <span>Host {{ detailSnapshot?.hostname || detailServer?.hostname || 'Unknown' }}</span>
        <span>Captured {{ formatTimestamp(detailSnapshot?.collectedAt) }}</span>
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
        <span>Loading detail</span>
      </div>
      <p v-else-if="detailError" class="detail-error">{{ detailError }}</p>
      <template v-else-if="detailSnapshot">
        <section class="detail-section">
          <div class="section-head">
            <Users :size="16" />
            <strong>Online users</strong>
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
          <p v-else class="empty-note">No active sessions</p>
        </section>

        <section class="detail-section">
          <div class="section-head">
            <Terminal :size="16" />
            <strong>Top processes</strong>
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
                <small>{{ process.commandLine || '-' }}</small>
              </div>
              <div class="process-meta">
                <span>{{ process.username || '-' }}</span>
                <strong>{{ formatPercent(process.cpuUsage) }}</strong>
                <small>{{ formatMb(process.memoryMb) }}</small>
              </div>
            </article>
          </div>
          <p v-else class="empty-note">No process sample</p>
        </section>

        <section class="detail-section">
          <div class="section-head">
            <Activity :size="16" />
            <strong>GPU detail</strong>
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
                  <span>Memory</span>
                  <strong>{{ formatGpuMemoryPair(gpu.memoryUsedMb, gpu.memoryTotalMb) }}</strong>
                </div>
                <div>
                  <span>Temp</span>
                  <strong>{{ typeof gpu.temperatureCelsius === 'number' ? `${gpu.temperatureCelsius.toFixed(1)} C` : '-' }}</strong>
                </div>
                <div>
                  <span>Power</span>
                  <strong>{{ typeof gpu.powerWatt === 'number' ? `${gpu.powerWatt.toFixed(1)} W` : '-' }}</strong>
                </div>
                <div>
                  <span>Processes</span>
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
              <p v-else class="empty-note compact">No GPU processes</p>
            </article>
          </div>
          <p v-else class="empty-note">No GPU sample</p>
        </section>
      </template>
    </aside>
  </main>
</template>
