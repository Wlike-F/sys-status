package com.sysstatus.agent.collector;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.sysstatus.common.agent.ProcessMetric;
import com.sysstatus.common.agent.SessionMetric;

public class LinuxCommandMetricCollector {
    private static final List<String> WHO_COMMAND = List.of("who");
    private static final List<String> PS_COMMAND = List.of(
            "ps",
            "-eo",
            "pid=,user=,pcpu=,rss=,comm=,args=",
            "--sort=-pcpu"
    );
    private static final DateTimeFormatter INPUT_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter OUTPUT_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final CommandRunner commandRunner;

    public LinuxCommandMetricCollector(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public List<SessionMetric> collectSessions() {
        try {
            return parseSessions(commandRunner.run(WHO_COMMAND));
        } catch (IOException | InterruptedException | RuntimeException error) {
            return List.of();
        }
    }

    public List<ProcessMetric> collectProcesses() {
        try {
            return parseProcesses(commandRunner.run(PS_COMMAND));
        } catch (IOException | InterruptedException | RuntimeException error) {
            return List.of();
        }
    }

    private List<SessionMetric> parseSessions(String output) {
        List<SessionMetric> sessions = new ArrayList<>();
        for (String line : lines(output)) {
            String[] parts = line.trim().split("\\s+", 5);
            if (parts.length < 4) {
                continue;
            }
            sessions.add(new SessionMetric(
                    blankToNull(parts[0]),
                    blankToNull(parts[1]),
                    parts.length >= 5 ? normalizeHost(parts[4]) : null,
                    parseLoginTime(parts[2], parts[3])
            ));
        }
        return sessions;
    }

    private List<ProcessMetric> parseProcesses(String output) {
        List<ProcessMetric> processes = new ArrayList<>();
        for (String line : lines(output)) {
            String[] parts = line.trim().split("\\s+", 6);
            if (parts.length < 5) {
                continue;
            }
            Long pid = parseLong(parts[0]);
            Double cpu = parseDouble(parts[2]);
            Long rssKb = parseLong(parts[3]);
            processes.add(new ProcessMetric(
                    pid,
                    blankToNull(parts[1]),
                    blankToNull(parts[4]),
                    parts.length >= 6 ? blankToNull(parts[5]) : blankToNull(parts[4]),
                    cpu,
                    rssKb == null ? null : rssKb / 1024
            ));
            if (processes.size() >= 80) {
                break;
            }
        }
        return processes;
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

    private static String parseLoginTime(String date, String time) {
        try {
            return LocalDateTime.parse(date + " " + time, INPUT_TIME).format(OUTPUT_TIME);
        } catch (RuntimeException error) {
            return date + " " + time;
        }
    }

    private static String normalizeHost(String value) {
        String host = blankToNull(value);
        if (host == null) {
            return null;
        }
        if (host.startsWith("(") && host.endsWith(")") && host.length() > 2) {
            return host.substring(1, host.length() - 1);
        }
        return host;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static Long parseLong(String value) {
        try {
            return value == null || value.isBlank() ? null : Long.parseLong(value.trim());
        } catch (NumberFormatException error) {
            return null;
        }
    }

    private static Double parseDouble(String value) {
        try {
            return value == null || value.isBlank() ? null : Double.parseDouble(value.trim());
        } catch (NumberFormatException error) {
            return null;
        }
    }
}
