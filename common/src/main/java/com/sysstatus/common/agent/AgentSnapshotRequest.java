package com.sysstatus.common.agent;

import java.util.List;

public record AgentSnapshotRequest(
        Long serverId,
        String agentId,
        String agentSecret,
        String hostname,
        String osType,
        Double cpuUsage,
        Long memoryTotalMb,
        Long memoryUsedMb,
        List<GpuMetric> gpus
) {
}
