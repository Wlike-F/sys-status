package com.sysstatus.agent.collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sysstatus.common.agent.GpuMetric;
import com.sysstatus.common.agent.GpuProcessMetric;

public class NvidiaSmiGpuCollector {
    private static final List<String> GPU_QUERY = List.of(
            "nvidia-smi",
            "--query-gpu=index,name,uuid,utilization.gpu,memory.total,memory.used,temperature.gpu,power.draw",
            "--format=csv,noheader,nounits"
    );
    private static final List<String> PROCESS_QUERY = List.of(
            "nvidia-smi",
            "--query-compute-apps=gpu_uuid,pid,process_name,used_memory",
            "--format=csv,noheader,nounits"
    );

    private final CommandRunner commandRunner;

    public NvidiaSmiGpuCollector(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public List<GpuMetric> collect() {
        try {
            String gpuOutput = commandRunner.run(GPU_QUERY);
            String processOutput = commandRunner.run(PROCESS_QUERY);
            return parse(gpuOutput, processOutput);
        } catch (IOException | InterruptedException | RuntimeException error) {
            return List.of();
        }
    }

    private List<GpuMetric> parse(String gpuOutput, String processOutput) {
        Map<String, List<GpuProcessMetric>> processesByUuid = parseProcesses(processOutput);
        List<GpuMetric> gpus = new ArrayList<>();
        for (String line : lines(gpuOutput)) {
            List<String> columns = columns(line);
            if (columns.size() < 8) {
                continue;
            }
            String uuid = columns.get(2);
            gpus.add(new GpuMetric(
                    parseInteger(columns.get(0)),
                    columns.get(1),
                    uuid,
                    parseDouble(columns.get(3)),
                    parseLong(columns.get(4)),
                    parseLong(columns.get(5)),
                    parseDouble(columns.get(6)),
                    parseDouble(columns.get(7)),
                    processesByUuid.getOrDefault(uuid, List.of())
            ));
        }
        return gpus;
    }

    private Map<String, List<GpuProcessMetric>> parseProcesses(String processOutput) {
        Map<String, List<GpuProcessMetric>> processes = new LinkedHashMap<>();
        for (String line : lines(processOutput)) {
            List<String> columns = columns(line);
            if (columns.size() < 4) {
                continue;
            }
            String uuid = columns.get(0);
            Long pid = parseLong(columns.get(1));
            GpuProcessMetric metric = new GpuProcessMetric(
                    pid,
                    lookupUsername(pid),
                    columns.get(2),
                    parseLong(columns.get(3))
            );
            processes.computeIfAbsent(uuid, ignored -> new ArrayList<>()).add(metric);
        }
        return processes;
    }

    private String lookupUsername(Long pid) {
        if (pid == null) {
            return null;
        }
        try {
            return commandRunner.run(List.of("sh", "-c", "ps -o user= -p " + pid)).trim();
        } catch (IOException | InterruptedException | RuntimeException error) {
            return null;
        }
    }

    private static List<String> lines(String output) {
        if (output == null || output.isBlank()) {
            return List.of();
        }
        return output.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    private static List<String> columns(String line) {
        String[] values = line.split(",", -1);
        List<String> columns = new ArrayList<>(values.length);
        for (String value : values) {
            columns.add(value.trim());
        }
        return columns;
    }

    private static Integer parseInteger(String value) {
        Long parsed = parseLong(value);
        return parsed == null ? null : parsed.intValue();
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank() || "[Not Supported]".equalsIgnoreCase(value)) {
            return null;
        }
        return Long.parseLong(value.trim());
    }

    private static Double parseDouble(String value) {
        if (value == null || value.isBlank() || "[Not Supported]".equalsIgnoreCase(value)) {
            return null;
        }
        return Double.parseDouble(value.trim());
    }
}
