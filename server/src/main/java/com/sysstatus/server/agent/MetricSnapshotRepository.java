package com.sysstatus.server.agent;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysstatus.common.agent.AgentSnapshotRequest;

@Repository
public class MetricSnapshotRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public MetricSnapshotRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void insert(AgentSnapshotRequest request, Double memoryUsage, LocalDateTime now) {
        jdbcTemplate.update("""
                INSERT INTO metric_snapshot
                    (server_id, collected_at, hostname, os_type, cpu_usage,
                     memory_total_mb, memory_used_mb, memory_usage, raw_json)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                request.serverId(),
                Timestamp.valueOf(now),
                request.hostname(),
                request.osType(),
                request.cpuUsage(),
                request.memoryTotalMb(),
                request.memoryUsedMb(),
                memoryUsage,
                serializeRawPayload(request));
    }

    public Optional<LatestSnapshotRecord> findLatestSnapshot(long serverId) {
        List<LatestSnapshotRecord> rows = jdbcTemplate.query("""
                SELECT collected_at, memory_usage, raw_json
                FROM metric_snapshot
                WHERE server_id = ?
                ORDER BY collected_at DESC, id DESC
                LIMIT 1
                """, (rs, rowNum) -> new LatestSnapshotRecord(
                rs.getTimestamp("collected_at").toLocalDateTime(),
                readNullableDouble(rs, "memory_usage"),
                rs.getString("raw_json")
        ), serverId);
        return rows.stream().findFirst();
    }

    private Double readNullableDouble(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private String serializeRawPayload(AgentSnapshotRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException error) {
            return "{}";
        }
    }
}
