package com.sysstatus.server.servernode;

import java.time.LocalDateTime;

public record ServerNodeDto(
        Long id,
        String name,
        String host,
        String osType,
        String gpuType,
        String location,
        String description,
        String status,
        String agentId,
        String agentVersion,
        String hostname,
        Double cpuUsage,
        Long memoryTotalMb,
        Long memoryUsedMb,
        Double memoryUsage,
        Integer gpuCount,
        Double gpuUsage,
        Long gpuMemoryTotalMb,
        Long gpuMemoryUsedMb,
        Double gpuMemoryUsage,
        LocalDateTime lastHeartbeatAt,
        boolean enabled
) {
    static ServerNodeDto from(ServerNode node) {
        return new ServerNodeDto(
                node.id(),
                node.name(),
                node.host(),
                node.osType(),
                node.gpuType(),
                node.location(),
                node.description(),
                node.status(),
                node.agentId(),
                node.agentVersion(),
                node.hostname(),
                node.cpuUsage(),
                node.memoryTotalMb(),
                node.memoryUsedMb(),
                node.memoryUsage(),
                node.gpuCount(),
                node.gpuUsage(),
                node.gpuMemoryTotalMb(),
                node.gpuMemoryUsedMb(),
                node.gpuMemoryUsage(),
                node.lastHeartbeatAt(),
                node.enabled()
        );
    }
}
