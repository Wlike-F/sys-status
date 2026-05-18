package com.sysstatus.agent.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysstatus.common.agent.AgentHeartbeatRequest;
import com.sysstatus.common.agent.AgentRegisterRequest;
import com.sysstatus.common.agent.AgentRegisterResponse;
import com.sysstatus.common.agent.AgentSnapshotRequest;

public class AgentApiClient {
    private final String serverUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AgentApiClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public AgentRegisterResponse register(AgentRegisterRequest request) throws IOException, InterruptedException {
        JsonNode data = post("/api/agent/register", request);
        return new AgentRegisterResponse(
                data.get("agentId").asText(),
                data.get("agentSecret").asText(),
                data.get("collectIntervalSeconds").asInt(),
                data.get("heartbeatIntervalSeconds").asInt()
        );
    }

    public void heartbeat(AgentHeartbeatRequest request) throws IOException, InterruptedException {
        post("/api/agent/heartbeat", request);
    }

    public void snapshot(AgentSnapshotRequest request) throws IOException, InterruptedException {
        post("/api/agent/snapshots", request);
    }

    private JsonNode post(String path, Object body) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Request failed: " + response.statusCode() + " " + response.body());
        }
        JsonNode root = objectMapper.readTree(response.body());
        if (root.get("code").asInt() != 0) {
            throw new IllegalStateException("API returned error: " + response.body());
        }
        return root.get("data");
    }
}
