import type { ApiResponse, CreateServerPayload, CreateServerResponse, ServerNode, SnapshotDetail } from './types';

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }
  const result = (await response.json()) as ApiResponse<T>;
  if (result.code !== 0) {
    throw new Error(result.message || 'API request failed');
  }
  return result.data;
}

export function listServers(): Promise<ServerNode[]> {
  return request<ServerNode[]>('/api/servers');
}

export function createServer(payload: CreateServerPayload): Promise<CreateServerResponse> {
  return request<CreateServerResponse>('/api/servers', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function regenerateRegisterToken(serverId: number): Promise<CreateServerResponse> {
  return request<CreateServerResponse>(`/api/servers/${serverId}/register-token`, {
    method: 'POST',
  });
}

export function getLatestSnapshot(serverId: number): Promise<SnapshotDetail> {
  return request<SnapshotDetail>(`/api/servers/${serverId}/snapshot/latest`);
}
