package com.sysstatus.agent.collector;

import java.util.List;

import com.sysstatus.common.agent.GpuMetric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NvidiaSmiGpuCollectorTest {
    @Test
    void parsesA100GpuAndComputeProcessCsv() {
        CommandRunner runner = command -> {
            String joined = String.join(" ", command);
            if (joined.contains("--query-gpu")) {
                return """
                        0, NVIDIA A100-SXM4-40GB, GPU-a100-0, 91, 40960, 32100, 62, 245.13
                        1, NVIDIA A100-SXM4-40GB, GPU-a100-1, 12, 40960, 1024, 38, 79.40
                        """;
            }
            if (joined.contains("--query-compute-apps")) {
                return """
                        GPU-a100-0, 12345, python, 16050
                        GPU-a100-0, 22345, python, 16050
                        """;
            }
            if (joined.contains("ps -o user= -p 12345")) {
                return "zhangsan";
            }
            if (joined.contains("ps -o user= -p 22345")) {
                return "lisi";
            }
            throw new IllegalArgumentException("Unexpected command: " + joined);
        };

        List<GpuMetric> gpus = new NvidiaSmiGpuCollector(runner).collect();

        assertEquals(2, gpus.size());
        GpuMetric first = gpus.get(0);
        assertEquals(0, first.gpuIndex());
        assertEquals("NVIDIA A100-SXM4-40GB", first.name());
        assertEquals("GPU-a100-0", first.uuid());
        assertEquals(91.0, first.utilizationPercent());
        assertEquals(40960, first.memoryTotalMb());
        assertEquals(32100, first.memoryUsedMb());
        assertEquals(2, first.processes().size());
        assertEquals(12345, first.processes().get(0).pid());
        assertEquals("zhangsan", first.processes().get(0).username());
        assertEquals("python", first.processes().get(0).processName());
        assertEquals(16050, first.processes().get(0).usedMemoryMb());
    }
}
