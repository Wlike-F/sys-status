export type ServerStatus = 'PENDING' | 'ONLINE' | 'WARN' | 'OFFLINE' | 'DISABLED';
export type OsType = 'LINUX' | 'WINDOWS';

export interface ServerNode {
  id: number;
  name: string;
  host: string;
  osType: OsType;
  gpuType?: string;
  location?: string;
  description?: string;
  status: ServerStatus;
  agentId?: string;
  agentVersion?: string;
  hostname?: string;
  cpuUsage?: number;
  memoryTotalMb?: number;
  memoryUsedMb?: number;
  memoryUsage?: number;
  gpuCount?: number;
  gpuUsage?: number;
  gpuMemoryTotalMb?: number;
  gpuMemoryUsedMb?: number;
  gpuMemoryUsage?: number;
  lastHeartbeatAt?: string;
  enabled: boolean;
}

export interface CreateServerPayload {
  name: string;
  host: string;
  osType: OsType;
  gpuType: string;
  location: string;
  description: string;
}

export interface CreateServerResponse {
  id: number;
  registerToken: string;
  installCommand: string;
}

export interface SessionMetric {
  username?: string;
  terminal?: string;
  host?: string;
  loginTime?: string;
}

export interface ProcessMetric {
  pid?: number;
  username?: string;
  processName?: string;
  commandLine?: string;
  cpuUsage?: number;
  memoryMb?: number;
}

export interface GpuProcessMetric {
  pid?: number;
  username?: string;
  processName?: string;
  usedMemoryMb?: number;
}

export interface GpuMetric {
  gpuIndex?: number;
  name?: string;
  uuid?: string;
  utilizationPercent?: number;
  memoryTotalMb?: number;
  memoryUsedMb?: number;
  temperatureCelsius?: number;
  powerWatt?: number;
  processes?: GpuProcessMetric[];
}

export interface SnapshotDetail {
  serverId: number;
  collectedAt?: string;
  hostname?: string;
  osType?: OsType;
  cpuUsage?: number;
  memoryTotalMb?: number;
  memoryUsedMb?: number;
  memoryUsage?: number;
  onlineUserCount?: number;
  sessions?: SessionMetric[];
  processes?: ProcessMetric[];
  gpus?: GpuMetric[];
}

export interface ServerDetailResponse {
  server: ServerNode;
  snapshot: SnapshotDetail;
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
