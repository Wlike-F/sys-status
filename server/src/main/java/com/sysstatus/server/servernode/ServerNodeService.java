package com.sysstatus.server.servernode;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sysstatus.server.shared.TokenGenerator;

@Service
public class ServerNodeService {
    private final ServerNodeRepository repository;
    private final TokenGenerator tokenGenerator;
    private final Clock clock;
    private final String publicBaseUrl;

    public ServerNodeService(ServerNodeRepository repository,
                             TokenGenerator tokenGenerator,
                             @Value("${sys-status.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.repository = repository;
        this.tokenGenerator = tokenGenerator;
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

    private String installCommand(long serverId, String token) {
        return "java -jar sys-status-agent.jar --server-url %s --server-id %d --token %s"
                .formatted(publicBaseUrl, serverId, token);
    }
}
