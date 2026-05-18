package com.sysstatus.common.agent;

public record AgentSnapshotRequest(
        Long serverId,
        String agentId,
        String agentSecret,
        String hostname,
        String osType,
        Double cpuUsage,
        Long memoryTotalMb,
        Long memoryUsedMb
) {
}
