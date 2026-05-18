package com.sysstatus.common.agent;

public record AgentHeartbeatRequest(
        Long serverId,
        String agentId,
        String agentSecret,
        String agentVersion
) {
}
