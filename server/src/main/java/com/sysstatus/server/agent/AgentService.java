package com.sysstatus.server.agent;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sysstatus.common.agent.AgentHeartbeatRequest;
import com.sysstatus.common.agent.AgentRegisterRequest;
import com.sysstatus.common.agent.AgentRegisterResponse;
import com.sysstatus.common.agent.AgentSnapshotRequest;
import com.sysstatus.common.agent.AgentStatusResponse;
import com.sysstatus.common.agent.GpuMetric;
import com.sysstatus.server.servernode.ServerNode;
import com.sysstatus.server.servernode.ServerNodeRepository;
import com.sysstatus.server.shared.TokenGenerator;

@Service
public class AgentService {
    private final ServerNodeRepository serverNodeRepository;
    private final AgentCredentialRepository credentialRepository;
    private final MetricSnapshotRepository snapshotRepository;
    private final TokenGenerator tokenGenerator;
    private final Clock clock;
    private final int collectIntervalSeconds;
    private final int heartbeatIntervalSeconds;

    public AgentService(ServerNodeRepository serverNodeRepository,
                        AgentCredentialRepository credentialRepository,
                        MetricSnapshotRepository snapshotRepository,
                        TokenGenerator tokenGenerator,
                        @Value("${sys-status.agent.collect-interval-seconds:5}") int collectIntervalSeconds,
                        @Value("${sys-status.agent.heartbeat-interval-seconds:5}") int heartbeatIntervalSeconds) {
        this.serverNodeRepository = serverNodeRepository;
        this.credentialRepository = credentialRepository;
        this.snapshotRepository = snapshotRepository;
        this.tokenGenerator = tokenGenerator;
        this.clock = Clock.systemDefaultZone();
        this.collectIntervalSeconds = collectIntervalSeconds;
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    public AgentRegisterResponse register(AgentRegisterRequest request) {
        ServerNode server = serverNodeRepository.findById(request.serverId())
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + request.serverId()));
        if (!server.registerToken().equals(request.registerToken())) {
            throw new IllegalArgumentException("Invalid register token");
        }

        String agentId = "agent_%d_%s".formatted(request.serverId(), tokenGenerator.nextToken());
        String agentSecret = tokenGenerator.nextToken();
        LocalDateTime now = LocalDateTime.now(clock);
        credentialRepository.create(request.serverId(), agentId, agentSecret, request.agentVersion(), now);
        serverNodeRepository.markRegistered(request.serverId(), agentId, request.agentVersion(), request.hostname(), now);
        return new AgentRegisterResponse(agentId, agentSecret, collectIntervalSeconds, heartbeatIntervalSeconds);
    }

    public AgentStatusResponse heartbeat(AgentHeartbeatRequest request) {
        validateCredential(request.serverId(), request.agentId(), request.agentSecret());
        LocalDateTime now = LocalDateTime.now(clock);
        credentialRepository.touch(request.agentId(), now);
        serverNodeRepository.markHeartbeat(request.serverId(), request.agentVersion(), now);
        return new AgentStatusResponse("ONLINE", null, null);
    }

    public AgentStatusResponse snapshot(AgentSnapshotRequest request) {
        validateCredential(request.serverId(), request.agentId(), request.agentSecret());
        LocalDateTime now = LocalDateTime.now(clock);
        Double memoryUsage = calculateMemoryUsage(request.memoryTotalMb(), request.memoryUsedMb());
        GpuSummary gpuSummary = calculateGpuSummary(request);
        snapshotRepository.insert(request, memoryUsage, now);
        credentialRepository.touch(request.agentId(), now);
        serverNodeRepository.updateSnapshot(
                request.serverId(),
                request.hostname(),
                request.osType(),
                request.cpuUsage(),
                request.memoryTotalMb(),
                request.memoryUsedMb(),
                memoryUsage,
                gpuSummary.gpuCount(),
                gpuSummary.gpuUsage(),
                gpuSummary.gpuMemoryTotalMb(),
                gpuSummary.gpuMemoryUsedMb(),
                gpuSummary.gpuMemoryUsage(),
                now
        );
        return new AgentStatusResponse("ONLINE", request.cpuUsage(), memoryUsage);
    }

    private void validateCredential(Long serverId, String agentId, String agentSecret) {
        credentialRepository.find(serverId, agentId, agentSecret)
                .orElseThrow(() -> new IllegalArgumentException("Invalid agent credential"));
    }

    private Double calculateMemoryUsage(Long totalMb, Long usedMb) {
        if (totalMb == null || usedMb == null || totalMb <= 0) {
            return null;
        }
        return usedMb * 100.0 / totalMb;
    }

    private GpuSummary calculateGpuSummary(AgentSnapshotRequest request) {
        if (request.gpus() == null || request.gpus().isEmpty()) {
            return new GpuSummary(null, null, null, null, null);
        }

        int gpuCount = request.gpus().size();
        double usageSum = 0.0;
        int usageCount = 0;
        long memoryTotalMb = 0;
        long memoryUsedMb = 0;
        for (GpuMetric gpu : request.gpus()) {
            if (gpu.utilizationPercent() != null) {
                usageSum += gpu.utilizationPercent();
                usageCount++;
            }
            if (gpu.memoryTotalMb() != null) {
                memoryTotalMb += gpu.memoryTotalMb();
            }
            if (gpu.memoryUsedMb() != null) {
                memoryUsedMb += gpu.memoryUsedMb();
            }
        }

        Double gpuUsage = usageCount == 0 ? null : round(usageSum / usageCount);
        Long total = memoryTotalMb == 0 ? null : memoryTotalMb;
        Long used = total == null ? null : memoryUsedMb;
        Double memoryUsage = total == null ? null : round(memoryUsedMb * 100.0 / memoryTotalMb);
        return new GpuSummary(gpuCount, gpuUsage, total, used, memoryUsage);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record GpuSummary(
            Integer gpuCount,
            Double gpuUsage,
            Long gpuMemoryTotalMb,
            Long gpuMemoryUsedMb,
            Double gpuMemoryUsage
    ) {
    }
}
