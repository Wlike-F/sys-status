<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
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
} from '@lucide/vue';
import { createServer, listServers, regenerateRegisterToken } from './api';
import type { CreateServerPayload, CreateServerResponse, OsType, ServerNode, ServerStatus } from './types';

const servers = ref<ServerNode[]>([]);
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const lastInstall = ref<CreateServerResponse | null>(null);
const copied = ref(false);

const form = reactive<CreateServerPayload>({
  name: '',
  host: '',
  osType: 'LINUX',
  gpuType: '',
  location: '',
  description: '',
});

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

onMounted(() => {
  void loadServers();
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
    PENDING: '\u5f85\u63a5\u5165',
    ONLINE: '\u5728\u7ebf',
    WARN: '\u5ef6\u8fdf',
    OFFLINE: '\u79bb\u7ebf',
    DISABLED: '\u505c\u7528',
  };
  return labels[status] ?? status;
}

function statusClass(status: ServerStatus) {
  return `status-${status.toLowerCase()}`;
}

function osLabel(osType: OsType) {
  return osType === 'WINDOWS' ? 'Windows' : 'Linux';
}

function formatPercent(value?: number) {
  return typeof value === 'number' ? `${value.toFixed(1)}%` : '-';
}

function formatMemory(server: ServerNode) {
  if (!server.memoryTotalMb || !server.memoryUsedMb) {
    return '-';
  }
  return `${Math.round(server.memoryUsedMb / 1024)} / ${Math.round(server.memoryTotalMb / 1024)} GB`;
}

function formatGpuMemory(server: ServerNode) {
  if (!server.gpuMemoryTotalMb || server.gpuMemoryUsedMb === undefined) {
    return '-';
  }
  return `${Math.round(server.gpuMemoryUsedMb / 1024)} / ${Math.round(server.gpuMemoryTotalMb / 1024)} GB`;
}

function average(values: Array<number | undefined>) {
  const present = values.filter((value): value is number => typeof value === 'number');
  if (present.length === 0) {
    return undefined;
  }
  return present.reduce((sum, value) => sum + value, 0) / present.length;
}

function toErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : '\u8bf7\u6c42\u5931\u8d25';
}
</script>

<template>
  <main class="shell">
    <header class="topbar">
      <div>
        <p class="eyebrow">Lab Resource Console</p>
        <h1>&#23454;&#39564;&#23460;&#26381;&#21153;&#22120;&#29366;&#24577;&#30417;&#25511;</h1>
      </div>
      <button class="icon-button" type="button" :disabled="loading" title="&#21047;&#26032;&#26381;&#21153;&#22120;&#21015;&#34920;" @click="loadServers">
        <RefreshCw :size="18" :class="{ spinning: loading }" />
        <span>&#21047;&#26032;</span>
      </button>
    </header>

    <section class="metrics-strip" aria-label="&#36164;&#28304;&#25688;&#35201;">
      <div class="metric-tile">
        <Server :size="19" />
        <span>&#26381;&#21153;&#22120;</span>
        <strong>{{ totals.online }} / {{ totals.total }}</strong>
      </div>
      <div class="metric-tile">
        <Terminal :size="19" />
        <span>&#24453;&#25509;&#20837;</span>
        <strong>{{ totals.pending }}</strong>
      </div>
      <div class="metric-tile">
        <Cpu :size="19" />
        <span>&#24179;&#22343; CPU</span>
        <strong>{{ formatPercent(totals.avgCpu) }}</strong>
      </div>
      <div class="metric-tile">
        <Activity :size="19" />
        <span>&#24179;&#22343; GPU</span>
        <strong>{{ formatPercent(totals.avgGpu) }}</strong>
      </div>
      <div class="metric-tile">
        <Database :size="19" />
        <span>&#24179;&#22343;&#20869;&#23384;</span>
        <strong>{{ formatPercent(totals.avgMemory) }}</strong>
      </div>
    </section>

    <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

    <div class="workspace">
      <section class="server-panel">
        <div class="panel-heading">
          <div>
            <p class="section-kicker">Servers</p>
            <h2>&#26381;&#21153;&#22120;&#21015;&#34920;</h2>
          </div>
          <span class="count-chip">{{ servers.length }} &#21488;</span>
        </div>

        <div v-if="servers.length === 0" class="empty-state">
          <MonitorCog :size="34" />
          <strong>&#36824;&#27809;&#26377;&#26381;&#21153;&#22120;</strong>
          <span>&#28155;&#21152;&#19968;&#21488;&#23616;&#22495;&#32593;&#26381;&#21153;&#22120;&#21518;&#65292;&#36825;&#37324;&#20250;&#26174;&#31034; Agent &#25509;&#20837;&#29366;&#24577;&#12290;</span>
        </div>

        <article v-for="server in servers" :key="server.id" class="server-row">
          <div class="server-main">
            <span class="status-dot" :class="statusClass(server.status)" />
            <div>
              <div class="server-title">
                <strong>{{ server.name }}</strong>
                <span>{{ osLabel(server.osType) }}</span>
              </div>
              <p>{{ server.host }} / {{ server.gpuType || '\u672a\u586b\u5199 GPU' }}</p>
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
            <span>&#20869;&#23384;</span>
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
            <small>{{ server.agentVersion || server.lastHeartbeatAt || '\u7b49\u5f85\u6ce8\u518c' }}</small>
          </div>
          <button class="ghost-button" type="button" title="&#37325;&#26032;&#29983;&#25104;&#23433;&#35013;&#21629;&#20196;" @click="refreshToken(server.id)">
            <RefreshCw :size="16" />
            <span>Token</span>
          </button>
        </article>
      </section>

      <aside class="side-panel">
        <form class="add-form" @submit.prevent="submitServer">
          <div class="panel-heading compact">
            <div>
              <p class="section-kicker">Register</p>
              <h2>&#28155;&#21152;&#26381;&#21153;&#22120;</h2>
            </div>
            <Plus :size="20" />
          </div>

          <label>
            <span>&#21517;&#31216;</span>
            <input v-model.trim="form.name" required placeholder="A100-Server" />
          </label>
          <label>
            <span>IP / &#22495;&#21517;</span>
            <input v-model.trim="form.host" required placeholder="192.168.1.11" />
          </label>
          <label>
            <span>&#31995;&#32479;</span>
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
            <span>&#20301;&#32622;</span>
            <input v-model.trim="form.location" placeholder="&#23454;&#39564;&#23460;&#26426;&#26588;" />
          </label>
          <label>
            <span>&#22791;&#27880;</span>
            <textarea v-model.trim="form.description" rows="3" placeholder="&#35757;&#32451;&#26381;&#21153;&#22120; / CPU &#20027;&#26426;" />
          </label>

          <button class="primary-button" type="submit" :disabled="saving">
            <Plus :size="17" />
            <span v-if="saving">&#28155;&#21152;&#20013;</span>
            <span v-else>&#28155;&#21152;&#26381;&#21153;&#22120;</span>
          </button>
        </form>

        <section v-if="lastInstall" class="install-panel">
          <div class="install-title">
            <Activity :size="18" />
            <strong>Agent &#23433;&#35013;&#21629;&#20196;</strong>
          </div>
          <code>{{ lastInstall.installCommand }}</code>
          <button class="copy-button" type="button" title="&#22797;&#21046;&#23433;&#35013;&#21629;&#20196;" @click="copyCommand">
            <Copy :size="16" />
            <span v-if="copied">&#24050;&#22797;&#21046;</span>
            <span v-else>&#22797;&#21046;&#21629;&#20196;</span>
          </button>
        </section>
      </aside>
    </div>
  </main>
</template>
