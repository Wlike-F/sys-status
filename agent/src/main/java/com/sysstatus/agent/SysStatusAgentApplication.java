package com.sysstatus.agent;

import java.net.InetAddress;
import java.util.List;

import com.sysstatus.agent.client.AgentApiClient;
import com.sysstatus.agent.config.AgentConfig;
import com.sysstatus.agent.collector.NvidiaSmiGpuCollector;
import com.sysstatus.agent.collector.ProcessCommandRunner;
import com.sysstatus.common.agent.AgentHeartbeatRequest;
import com.sysstatus.common.agent.AgentRegisterRequest;
import com.sysstatus.common.agent.AgentRegisterResponse;
import com.sysstatus.common.agent.AgentSnapshotRequest;
import com.sysstatus.common.agent.GpuMetric;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

public class SysStatusAgentApplication {
    private static final String AGENT_VERSION = "0.1.0";

    public static void main(String[] args) throws Exception {
        AgentConfig config = AgentConfig.fromArgs(args);
        AgentApiClient client = new AgentApiClient(config.serverUrl());
        String hostname = InetAddress.getLocalHost().getHostName();
        String osType = System.getProperty("os.name").toLowerCase().contains("win") ? "WINDOWS" : "LINUX";

        AgentRegisterResponse registerResponse = client.register(new AgentRegisterRequest(
                config.serverId(),
                config.registerToken(),
                hostname,
                osType,
                AGENT_VERSION
        ));

        do {
            submitHeartbeatAndSnapshot(config, client, hostname, osType, registerResponse);
            if (config.once()) {
                break;
            }
            Thread.sleep(registerResponse.collectIntervalSeconds() * 1000L);
        } while (true);

        System.out.println("sys-status agent submitted snapshot.");
    }

    private static void submitHeartbeatAndSnapshot(AgentConfig config, AgentApiClient client, String hostname,
                                                   String osType, AgentRegisterResponse registerResponse) throws Exception {
        client.heartbeat(new AgentHeartbeatRequest(
                config.serverId(),
                registerResponse.agentId(),
                registerResponse.agentSecret(),
                AGENT_VERSION
        ));
        SnapshotMetrics metrics = collectMetrics();
        List<GpuMetric> gpus = "LINUX".equals(osType)
                ? new NvidiaSmiGpuCollector(new ProcessCommandRunner(5)).collect()
                : List.of();
        client.snapshot(new AgentSnapshotRequest(
                config.serverId(),
                registerResponse.agentId(),
                registerResponse.agentSecret(),
                hostname,
                osType,
                metrics.cpuUsage(),
                metrics.memoryTotalMb(),
                metrics.memoryUsedMb(),
                gpus
        ));
    }

    private static SnapshotMetrics collectMetrics() {
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        double cpuUsage = processor.getSystemCpuLoad(1000) * 100.0;
        long totalMb = memory.getTotal() / 1024 / 1024;
        long usedMb = (memory.getTotal() - memory.getAvailable()) / 1024 / 1024;
        return new SnapshotMetrics(round(cpuUsage), totalMb, usedMb);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record SnapshotMetrics(double cpuUsage, long memoryTotalMb, long memoryUsedMb) {
    }
}
