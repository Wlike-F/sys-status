package com.sysstatus.common.agent;

public record AgentRegisterResponse(
        String agentId,
        String agentSecret,
        int collectIntervalSeconds,
        int heartbeatIntervalSeconds
) {
}
