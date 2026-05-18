package com.sysstatus.common.agent;

public record GpuProcessMetric(
        Long pid,
        String username,
        String processName,
        Long usedMemoryMb
) {
}
