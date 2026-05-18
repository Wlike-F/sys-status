package com.sysstatus.server.agent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgentControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void agentCanRegisterHeartbeatAndSubmitMinimalSnapshot() throws Exception {
        String serverResponse = mockMvc.perform(post("/api/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "A100-Server",
                                  "host": "192.168.1.11",
                                  "osType": "LINUX",
                                  "gpuType": "8 x NVIDIA A100"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long serverId = Long.parseLong(serverResponse.replaceAll("(?s).*\\\"id\\\":(\\d+).*", "$1"));
        String token = serverResponse.replaceAll("(?s).*\\\"registerToken\\\":\\\"([^\\\"]+)\\\".*", "$1");

        String registerResponse = mockMvc.perform(post("/api/agent/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serverId": %d,
                                  "registerToken": "%s",
                                  "hostname": "a100-linux",
                                  "osType": "LINUX",
                                  "agentVersion": "0.1.0"
                                }
                                """.formatted(serverId, token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.agentId", not(nullValue())))
                .andExpect(jsonPath("$.data.agentSecret", not(nullValue())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String agentId = registerResponse.replaceAll("(?s).*\\\"agentId\\\":\\\"([^\\\"]+)\\\".*", "$1");
        String agentSecret = registerResponse.replaceAll("(?s).*\\\"agentSecret\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(post("/api/agent/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serverId": %d,
                                  "agentId": "%s",
                                  "agentSecret": "%s",
                                  "agentVersion": "0.1.0"
                                }
                                """.formatted(serverId, agentId, agentSecret)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ONLINE"));

        mockMvc.perform(post("/api/agent/snapshots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serverId": %d,
                                  "agentId": "%s",
                                  "agentSecret": "%s",
                                  "hostname": "a100-linux",
                                  "osType": "LINUX",
                                  "cpuUsage": 12.5,
                                  "memoryTotalMb": 262144,
                                  "memoryUsedMb": 65536
                                }
                                """.formatted(serverId, agentId, agentSecret)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ONLINE"))
                .andExpect(jsonPath("$.data.cpuUsage").value(12.5));
    }
}
