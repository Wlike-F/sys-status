package com.sysstatus.common.agent;

import java.util.List;

public record GpuMetric(
        Integer gpuIndex,
        String name,
        String uuid,
        Double utilizationPercent,
        Long memoryTotalMb,
        Long memoryUsedMb,
        Double temperatureCelsius,
        Double powerWatt,
        List<GpuProcessMetric> processes
) {
}
