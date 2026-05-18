package com.sysstatus.common.agent;

public record AgentStatusResponse(
        String status,
        Double cpuUsage,
        Double memoryUsage
) {
}
