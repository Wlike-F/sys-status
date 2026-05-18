package com.sysstatus.common.agent;

public record AgentRegisterRequest(
        Long serverId,
        String registerToken,
        String hostname,
        String osType,
        String agentVersion
) {
}
