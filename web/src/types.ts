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

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
