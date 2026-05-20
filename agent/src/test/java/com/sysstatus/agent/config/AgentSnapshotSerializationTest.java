package com.sysstatus.agent.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysstatus.common.agent.AgentSnapshotRequest;

class AgentSnapshotSerializationTest {
    @Test
    void snapshotPayloadIncludesAgentSecretForCredentialValidation() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new AgentSnapshotRequest(
                1L,
                "agent_1_test",
                "secret_test",
                "local",
                "WINDOWS",
                12.5,
                1024L,
                512L,
                List.of(),
                List.of(),
                List.of()
        ));

        assertTrue(json.contains("\"agentSecret\":\"secret_test\""));
    }
}
