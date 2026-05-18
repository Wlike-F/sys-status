package com.sysstatus.common.agent;

public record SessionMetric(
        String username,
        String terminal,
        String host,
        String loginTime
) {
}
