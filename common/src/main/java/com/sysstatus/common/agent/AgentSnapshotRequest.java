package com.sysstatus.common.agent;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentSnapshotRequest(
        Long serverId,
        String agentId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String agentSecret,
        String hostname,
        String osType,
        Double cpuUsage,
        Long memoryTotalMb,
        Long memoryUsedMb,
        List<SessionMetric> sessions,
        List<ProcessMetric> processes,
        List<GpuMetric> gpus
) {
}
