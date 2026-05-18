package com.sysstatus.agent.config;

import java.util.HashMap;
import java.util.Map;

public record AgentConfig(String serverUrl, long serverId, String registerToken) {
    public static AgentConfig fromArgs(String[] args) {
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 >= args.length || !args[i].startsWith("--")) {
                throw new IllegalArgumentException("Invalid argument format. Expected --key value pairs.");
            }
            values.put(args[i], args[i + 1]);
        }
        String serverUrl = required(values, "--server-url");
        long serverId = Long.parseLong(required(values, "--server-id"));
        String token = required(values, "--token");
        return new AgentConfig(trimTrailingSlash(serverUrl), serverId, token);
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
