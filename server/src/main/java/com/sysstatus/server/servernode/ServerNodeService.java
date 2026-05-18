package com.sysstatus.server.servernode;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sysstatus.server.agent.MetricSnapshotRepository;
import com.sysstatus.server.shared.TokenGenerator;

@Service
public class ServerNodeService {
    private final ServerNodeRepository repository;
    private final MetricSnapshotRepository snapshotRepository;
    private final TokenGenerator tokenGenerator;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final String publicBaseUrl;

    public ServerNodeService(ServerNodeRepository repository,
                             MetricSnapshotRepository snapshotRepository,
                             TokenGenerator tokenGenerator,
                             ObjectMapper objectMapper,
                             @Value("${sys-status.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.repository = repository;
        this.snapshotRepository = snapshotRepository;
        this.tokenGenerator = tokenGenerator;
        this.objectMapper = objectMapper;
        this.clock = Clock.systemDefaultZone();
        this.publicBaseUrl = publicBaseUrl;
    }

    public ServerCreateResponse create(CreateServerRequest request) {
        String token = tokenGenerator.nextToken();
        long id = repository.create(request, token, LocalDateTime.now(clock));
        return new ServerCreateResponse(id, token, installCommand(id, token));
    }

    public List<ServerNodeDto> list() {
        return repository.findAllEnabled().stream()
                .map(ServerNodeDto::from)
                .toList();
    }

    public ServerNodeDto get(long id) {
        return repository.findById(id)
                .map(ServerNodeDto::from)
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + id));
    }

    public RegisterTokenResponse regenerateToken(long id) {
        repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Server not found: " + id));
        String token = tokenGenerator.nextToken();
        repository.updateRegisterToken(id, token, LocalDateTime.now(clock));
        return new RegisterTokenResponse(id, token, installCommand(id, token));
    }

    public JsonNode latestSnapshot(long id) {
        repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Server not found: " + id));
        String rawJson = snapshotRepository.findLatestRawJson(id)
                .orElseThrow(() -> new IllegalArgumentException("Snapshot not found: " + id));
        try {
            JsonNode snapshot = objectMapper.readTree(rawJson);
            if (snapshot instanceof ObjectNode objectNode) {
                objectNode.remove("agentSecret");
            }
            return snapshot;
        } catch (Exception error) {
            throw new IllegalStateException("Invalid snapshot payload for server: " + id, error);
        }
    }

    private String installCommand(long serverId, String token) {
        return "java -jar sys-status-agent.jar --server-url %s --server-id %d --token %s"
                .formatted(publicBaseUrl, serverId, token);
    }
}
