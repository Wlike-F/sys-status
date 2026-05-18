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
                node.lastHeartbeatAt(),
                node.enabled()
        );
    }
}
