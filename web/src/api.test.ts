import { describe, expect, it, vi } from 'vitest';
import { createServer, getLatestSnapshot, listServers } from './api';

describe('api client', () => {
  it('lists servers from backend response data', async () => {
    vi.stubGlobal('fetch', vi.fn(async () => ({
      ok: true,
      json: async () => ({
        code: 0,
        message: 'ok',
        data: [{ id: 1, name: 'A100', status: 'ONLINE' }],
      }),
    })));

    await expect(listServers()).resolves.toEqual([{ id: 1, name: 'A100', status: 'ONLINE' }]);
  });

  it('creates a server and returns install command payload', async () => {
    const fetchMock = vi.fn(async () => ({
      ok: true,
      json: async () => ({
        code: 0,
        message: 'ok',
        data: {
          id: 2,
          registerToken: 'token',
          installCommand: 'java -jar sys-status-agent.jar --server-id 2 --token token',
        },
      }),
    }));
    vi.stubGlobal('fetch', fetchMock);

    const payload = await createServer({
      name: 'P100',
      host: '192.168.1.13',
      osType: 'WINDOWS',
      gpuType: '8 x NVIDIA P100',
      location: '',
      description: '',
    });

    expect(payload.installCommand).toContain('--server-id 2');
    expect(fetchMock).toHaveBeenCalledWith('/api/servers', expect.objectContaining({ method: 'POST' }));
  });

  it('loads latest snapshot details for a server', async () => {
    const fetchMock = vi.fn(async () => ({
      ok: true,
      json: async () => ({
        code: 0,
        message: 'ok',
        data: {
          serverId: 4,
          sessions: [{ username: 'wff' }],
          processes: [{ pid: 100, processName: 'python.exe' }],
        },
      }),
    }));
    vi.stubGlobal('fetch', fetchMock);

    const snapshot = await getLatestSnapshot(4);

    expect(snapshot.sessions?.[0].username).toBe('wff');
    expect(fetchMock).toHaveBeenCalledWith('/api/servers/4/snapshot/latest', expect.any(Object));
  });
});
