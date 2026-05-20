package com.sysstatus.agent.collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysstatus.common.agent.ProcessMetric;

public class WindowsProcessMetricCollector {
    private static final List<String> COMMAND = List.of(
            "powershell",
            "-NoProfile",
            "-ExecutionPolicy",
            "Bypass",
            "-Command",
            """
            $logicalProcessors = (Get-CimInstance Win32_ComputerSystem).NumberOfLogicalProcessors
            $meta = @{}
            Get-CimInstance Win32_Process | ForEach-Object {
              $owner = $null
              try { $owner = $_.GetOwner().User } catch { $owner = $null }
              $meta[$_.ProcessId] = [PSCustomObject]@{
                username = $owner
                commandLine = $_.CommandLine
              }
            }
            Get-CimInstance Win32_PerfFormattedData_PerfProc_Process |
              Where-Object { $_.IDProcess -gt 0 -and $_.Name -ne 'Idle' -and $_.Name -ne '_Total' } |
              Sort-Object PercentProcessorTime -Descending |
              Select-Object -First 80 |
              ForEach-Object {
                $detail = $meta[$_.IDProcess]
                [PSCustomObject]@{
                  pid = [long]$_.IDProcess
                  username = $detail.username
                  processName = $_.Name
                  commandLine = $detail.commandLine
                  cpuUsage = [math]::Round(([double]$_.PercentProcessorTime / [double]$logicalProcessors), 1)
                  memoryMb = [math]::Round(([double]$_.WorkingSetPrivate / 1MB), 0)
                }
              } | ConvertTo-Json -Depth 4
            """
    );

    private final CommandRunner commandRunner;
    private final ObjectMapper objectMapper;

    public WindowsProcessMetricCollector(CommandRunner commandRunner, ObjectMapper objectMapper) {
        this.commandRunner = commandRunner;
        this.objectMapper = objectMapper;
    }

    public List<ProcessMetric> collectProcesses() {
        try {
            return parse(commandRunner.run(COMMAND));
        } catch (IOException | InterruptedException | RuntimeException error) {
            return List.of();
        }
    }

    private List<ProcessMetric> parse(String output) throws IOException {
        if (output == null || output.isBlank()) {
            return List.of();
        }
        JsonNode root = objectMapper.readTree(output);
        List<ProcessMetric> processes = new ArrayList<>();
        if (root.isArray()) {
            for (JsonNode node : root) {
                addProcess(processes, node);
            }
            return processes;
        }
        addProcess(processes, root);
        return processes;
    }

    private void addProcess(List<ProcessMetric> processes, JsonNode node) {
        if (node == null || node.isNull()) {
            return;
        }
        processes.add(new ProcessMetric(
                node.path("pid").isMissingNode() ? null : node.path("pid").asLong(),
                blankToNull(node.path("username").asText(null)),
                blankToNull(node.path("processName").asText(null)),
                blankToNull(node.path("commandLine").asText(null)),
                node.path("cpuUsage").isMissingNode() ? null : node.path("cpuUsage").asDouble(),
                node.path("memoryMb").isMissingNode() ? null : node.path("memoryMb").asLong()
        ));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
