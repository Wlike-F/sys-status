package com.sysstatus.server.agent;

import java.time.LocalDateTime;

public record LatestSnapshotRecord(
        LocalDateTime collectedAt,
        Double memoryUsage,
        String rawJson
) {
}
