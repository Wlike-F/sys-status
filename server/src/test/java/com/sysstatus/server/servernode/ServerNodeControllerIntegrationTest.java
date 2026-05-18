package com.sysstatus.server.servernode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServerNodeControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createServerReturnsRegisterTokenAndInstallCommand() throws Exception {
        mockMvc.perform(post("/api/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "A100-Server",
                                  "host": "192.168.1.11",
                                  "osType": "LINUX",
                                  "gpuType": "8 x NVIDIA A100",
                                  "location": "lab-rack",
                                  "description": "Linux training server"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.registerToken", not(nullValue())))
                .andExpect(jsonPath("$.data.installCommand", containsString("--server-id")))
                .andExpect(jsonPath("$.data.installCommand", containsString("--token")));
    }

    @Test
    void listServersIncludesCreatedServerSummary() throws Exception {
        mockMvc.perform(post("/api/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "P100-Windows",
                                  "host": "192.168.1.13",
                                  "osType": "WINDOWS",
                                  "gpuType": "8 x NVIDIA P100"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/servers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data[0].name").exists())
                .andExpect(jsonPath("$.data[0].status").exists());
    }

    @Test
    void regenerateRegisterTokenReturnsDifferentToken() throws Exception {
        String response = mockMvc.perform(post("/api/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "CPU-Windows",
                                  "host": "192.168.1.12",
                                  "osType": "WINDOWS",
                                  "gpuType": "ordinary GPU"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long serverId = Long.parseLong(response.replaceAll("(?s).*\\\"id\\\":(\\d+).*", "$1"));
        String firstToken = response.replaceAll("(?s).*\\\"registerToken\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(post("/api/servers/" + serverId + "/register-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.registerToken", not(firstToken)));
    }
}
