package com.sysstatus.agent.config;

import java.util.HashMap;
import java.util.Map;

public record AgentConfig(String serverUrl, long serverId, String registerToken, boolean once) {
    public static AgentConfig fromArgs(String[] args) {
        Map<String, String> values = new HashMap<>();
        boolean once = false;
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("--")) {
                throw new IllegalArgumentException("Invalid argument format. Expected --key value pairs.");
            }
            if ("--once".equals(args[i])) {
                once = true;
                continue;
            }
            if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                throw new IllegalArgumentException("Invalid argument format. Expected --key value pairs.");
            }
            values.put(args[i], args[i + 1]);
            i++;
        }
        String serverUrl = required(values, "--server-url");
        long serverId = Long.parseLong(required(values, "--server-id"));
        String token = required(values, "--token");
        return new AgentConfig(trimTrailingSlash(serverUrl), serverId, token, once);
    }

    private static String required(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required argument: " + key);
        }
        return value;
    }

    private static String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
