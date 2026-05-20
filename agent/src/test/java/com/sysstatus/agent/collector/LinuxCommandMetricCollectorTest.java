package com.sysstatus.agent.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sysstatus.common.agent.ProcessMetric;
import com.sysstatus.common.agent.SessionMetric;

class LinuxCommandMetricCollectorTest {
    @Test
    void collectsLoginSessionsFromWhoOutput() {
        CommandRunner runner = command -> """
                wff      pts/0        2026-05-18 08:48 (10.235.1.20)
                alice    pts/3        2026-05-18 09:17 (10.235.1.31)
                """;

        List<SessionMetric> sessions = new LinuxCommandMetricCollector(runner).collectSessions();

        assertEquals(2, sessions.size());
        assertEquals("wff", sessions.get(0).username());
        assertEquals("pts/0", sessions.get(0).terminal());
        assertEquals("10.235.1.20", sessions.get(0).host());
        assertEquals("2026-05-18T08:48:00", sessions.get(0).loginTime());
    }

    @Test
    void collectsTopProcessesFromPsOutput() {
        CommandRunner runner = command -> """
                  12345 wff       230.5 18874368 python          python train.py --epochs 200
                  22345 alice      42.1  2097152 java            java -jar worker.jar
                """;

        List<ProcessMetric> processes = new LinuxCommandMetricCollector(runner).collectProcesses();

        assertEquals(2, processes.size());
        assertEquals(12345L, processes.get(0).pid());
        assertEquals("wff", processes.get(0).username());
        assertEquals("python", processes.get(0).processName());
        assertEquals("python train.py --epochs 200", processes.get(0).commandLine());
        assertEquals(230.5, processes.get(0).cpuUsage());
        assertEquals(18432L, processes.get(0).memoryMb());
    }
}
