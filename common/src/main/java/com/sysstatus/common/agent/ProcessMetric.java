package com.sysstatus.common.agent;

public record ProcessMetric(
        Long pid,
        String username,
        String processName,
        String commandLine,
        Double cpuUsage,
        Long memoryMb
) {
}
