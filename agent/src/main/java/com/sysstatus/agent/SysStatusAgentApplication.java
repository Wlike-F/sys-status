package com.sysstatus.agent;

import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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
import com.sysstatus.common.agent.ProcessMetric;
import com.sysstatus.common.agent.SessionMetric;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

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
        SystemInfo systemInfo = new SystemInfo();
        List<GpuMetric> gpus = new NvidiaSmiGpuCollector(
                new ProcessCommandRunner(5),
                "WINDOWS".equals(osType)
        ).collect();
        client.snapshot(new AgentSnapshotRequest(
                config.serverId(),
                registerResponse.agentId(),
                registerResponse.agentSecret(),
                hostname,
                osType,
                metrics.cpuUsage(),
                metrics.memoryTotalMb(),
                metrics.memoryUsedMb(),
                collectSessions(systemInfo),
                collectProcesses(systemInfo),
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

    private static List<SessionMetric> collectSessions(SystemInfo systemInfo) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return systemInfo.getOperatingSystem().getSessions().stream()
                .map(session -> new SessionMetric(
                        session.getUserName(),
                        session.getTerminalDevice(),
                        session.getHost(),
                        Instant.ofEpochMilli(session.getLoginTime())
                                .atZone(ZoneId.systemDefault())
                                .format(formatter)
                ))
                .toList();
    }

    private static List<ProcessMetric> collectProcesses(SystemInfo systemInfo) {
        OperatingSystem os = systemInfo.getOperatingSystem();
        return os.getProcesses(
                        process -> process.getResidentSetSize() > 0,
                        Comparator.comparingDouble(OSProcess::getProcessCpuLoadCumulative).reversed(),
                        80
                ).stream()
                .map(process -> new ProcessMetric(
                        (long) process.getProcessID(),
                        process.getUser(),
                        process.getName(),
                        process.getCommandLine(),
                        round(process.getProcessCpuLoadCumulative() * 100.0),
                        process.getResidentSetSize() / 1024 / 1024
                ))
                .toList();
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record SnapshotMetrics(double cpuUsage, long memoryTotalMb, long memoryUsedMb) {
    }
}
