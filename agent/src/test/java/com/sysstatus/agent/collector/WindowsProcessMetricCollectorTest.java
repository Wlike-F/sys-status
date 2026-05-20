package com.sysstatus.agent.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysstatus.common.agent.ProcessMetric;

class WindowsProcessMetricCollectorTest {
    @Test
    void parsesWindowsPerfDataAndProcessMetadata() {
        CommandRunner runner = command -> {
            String joined = String.join(" ", command);
            if (joined.contains("Win32_PerfFormattedData_PerfProc_Process")) {
                return """
                        [
                          {
                            "pid": 1234,
                            "username": "wff",
                            "processName": "python.exe",
                            "commandLine": "python train.py --epochs 200",
                            "cpuUsage": 18.5,
                            "memoryMb": 2048
                          },
                          {
                            "pid": 2234,
                            "username": "alice",
                            "processName": "code.exe",
                            "commandLine": "Code.exe",
                            "cpuUsage": 8.0,
                            "memoryMb": 1024
                          }
                        ]
                        """
                        .trim();
            }
            throw new IllegalArgumentException("Unexpected command: " + joined);
        };

        List<ProcessMetric> processes = new WindowsProcessMetricCollector(runner, new ObjectMapper()).collectProcesses();

        assertEquals(2, processes.size());
        assertEquals(1234L, processes.get(0).pid());
        assertEquals("wff", processes.get(0).username());
        assertEquals("python.exe", processes.get(0).processName());
        assertEquals("python train.py --epochs 200", processes.get(0).commandLine());
        assertEquals(18.5, processes.get(0).cpuUsage());
        assertEquals(2048L, processes.get(0).memoryMb());
    }
}
