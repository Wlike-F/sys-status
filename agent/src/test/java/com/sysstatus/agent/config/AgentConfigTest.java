package com.sysstatus.agent.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentConfigTest {
    @Test
    void parsesRequiredCommandLineArguments() {
        AgentConfig config = AgentConfig.fromArgs(new String[]{
                "--server-url", "http://localhost:8080",
                "--server-id", "7",
                "--token", "register-token",
                "--once"
        });

        assertEquals("http://localhost:8080", config.serverUrl());
        assertEquals(7L, config.serverId());
        assertEquals("register-token", config.registerToken());
        assertEquals(true, config.once());
    }
}
