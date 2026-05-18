package com.sysstatus.server.servernode;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysstatus.common.agent.AgentSnapshotRequest;
import com.sysstatus.common.agent.GpuMetric;
import com.sysstatus.common.agent.ProcessMetric;
import com.sysstatus.common.agent.SessionMetric;
import com.sysstatus.server.agent.MetricSnapshotRepository;
import com.sysstatus.server.agent.LatestSnapshotRecord;
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

    public ServerSnapshotDetail latestSnapshot(long id) {
        return loadSnapshotDetail(id).snapshot();
    }

    public ServerDetailResponse detail(long id) {
        SnapshotContext context = loadSnapshotDetail(id);
        return new ServerDetailResponse(context.server(), context.snapshot());
    }

    private SnapshotContext loadSnapshotDetail(long id) {
        repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Server not found: " + id));
        LatestSnapshotRecord latest = snapshotRepository.findLatestSnapshot(id)
                .orElseThrow(() -> new IllegalArgumentException("Snapshot not found: " + id));
        try {
            AgentSnapshotRequest request = objectMapper.readValue(latest.rawJson(), AgentSnapshotRequest.class);
            List<SessionMetric> sessions = sortSessions(request.sessions());
            List<ProcessMetric> processes = sortProcesses(request.processes());
            List<GpuMetric> gpus = sortGpus(request.gpus());
            ServerNodeDto server = get(id);
            ServerSnapshotDetail snapshot = new ServerSnapshotDetail(
                    id,
                    latest.collectedAt(),
                    request.hostname(),
                    request.osType(),
                    request.cpuUsage(),
                    request.memoryTotalMb(),
                    request.memoryUsedMb(),
                    latest.memoryUsage() != null ? latest.memoryUsage() : calculateMemoryUsage(request.memoryTotalMb(), request.memoryUsedMb()),
                    countOnlineUsers(sessions),
                    sessions,
                    processes,
                    gpus
            );
            return new SnapshotContext(server, snapshot);
        } catch (Exception error) {
            throw new IllegalStateException("Invalid snapshot payload for server: " + id, error);
        }
    }

    private List<SessionMetric> sortSessions(List<SessionMetric> sessions) {
        if (sessions == null) {
            return List.of();
        }
        return sessions.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(SessionMetric::loginTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private List<ProcessMetric> sortProcesses(List<ProcessMetric> processes) {
        if (processes == null) {
            return List.of();
        }
        return processes.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ProcessMetric::cpuUsage, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private List<GpuMetric> sortGpus(List<GpuMetric> gpus) {
        if (gpus == null) {
            return List.of();
        }
        return gpus.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(GpuMetric::gpuIndex, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private int countOnlineUsers(List<SessionMetric> sessions) {
        return (int) sessions.stream()
                .map(SessionMetric::username)
                .filter(username -> username != null && !username.isBlank())
                .distinct()
                .count();
    }

    private Double calculateMemoryUsage(Long totalMb, Long usedMb) {
        if (totalMb == null || totalMb == 0 || usedMb == null) {
            return null;
        }
        return Math.round((usedMb * 10000.0 / totalMb)) / 100.0;
    }

    private String installCommand(long serverId, String token) {
        return "java -jar sys-status-agent.jar --server-url %s --server-id %d --token %s"
                .formatted(publicBaseUrl, serverId, token);
    }

    private record SnapshotContext(ServerNodeDto server, ServerSnapshotDetail snapshot) {
    }
}
