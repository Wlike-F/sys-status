package com.sysstatus.server.servernode;

import java.time.LocalDateTime;
import java.util.List;

import com.sysstatus.common.agent.GpuMetric;
import com.sysstatus.common.agent.ProcessMetric;
import com.sysstatus.common.agent.SessionMetric;

public record ServerSnapshotDetail(
        long serverId,
        LocalDateTime collectedAt,
        String hostname,
        String osType,
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
