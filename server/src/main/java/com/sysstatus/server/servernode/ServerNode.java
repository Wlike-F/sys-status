package com.sysstatus.server.servernode;

import java.time.LocalDateTime;

public record ServerNode(
        Long id,
        String name,
        String host,
        String osType,
        String gpuType,
        String location,
        String description,
        String status,
        String registerToken,
        String agentId,
        String agentVersion,
        String hostname,
        Double cpuUsage,
        Long memoryTotalMb,
        Long memoryUsedMb,
        Double memoryUsage,
        LocalDateTime lastHeartbeatAt,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
