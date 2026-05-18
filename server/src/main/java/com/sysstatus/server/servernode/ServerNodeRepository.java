package com.sysstatus.server.servernode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ServerNodeRepository {
    private final JdbcTemplate jdbcTemplate;

    public ServerNodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long create(CreateServerRequest request, String registerToken, LocalDateTime now) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("""
                    INSERT INTO server_node
                        (name, host, os_type, gpu_type, location, description, status, register_token, enabled, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, 'PENDING', ?, 1, ?, ?)
                    """, new String[]{"id"});
            statement.setString(1, request.name());
            statement.setString(2, request.host());
            statement.setString(3, request.osType());
            statement.setString(4, request.gpuType());
            statement.setString(5, request.location());
            statement.setString(6, request.description());
            statement.setString(7, registerToken);
            statement.setTimestamp(8, Timestamp.valueOf(now));
            statement.setTimestamp(9, Timestamp.valueOf(now));
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create server node");
        }
        return key.longValue();
    }

    public List<ServerNode> findAllEnabled() {
        return jdbcTemplate.query("""
                SELECT * FROM server_node
                WHERE enabled = 1
                ORDER BY id DESC
                """, mapper());
    }

    public Optional<ServerNode> findById(long id) {
        List<ServerNode> nodes = jdbcTemplate.query("SELECT * FROM server_node WHERE id = ?", mapper(), id);
        return nodes.stream().findFirst();
    }

    public void updateRegisterToken(long id, String registerToken, LocalDateTime now) {
        jdbcTemplate.update("""
                UPDATE server_node
                SET register_token = ?, updated_at = ?
                WHERE id = ?
                """, registerToken, Timestamp.valueOf(now), id);
    }

    public void markRegistered(long serverId, String agentId, String agentVersion, String hostname, LocalDateTime now) {
        jdbcTemplate.update("""
                UPDATE server_node
                SET agent_id = ?, agent_version = ?, hostname = ?, status = 'ONLINE',
                    last_heartbeat_at = ?, updated_at = ?
                WHERE id = ?
                """, agentId, agentVersion, hostname, Timestamp.valueOf(now), Timestamp.valueOf(now), serverId);
    }

    public void markHeartbeat(long serverId, String agentVersion, LocalDateTime now) {
        jdbcTemplate.update("""
                UPDATE server_node
                SET agent_version = ?, status = 'ONLINE', last_heartbeat_at = ?, updated_at = ?
                WHERE id = ?
                """, agentVersion, Timestamp.valueOf(now), Timestamp.valueOf(now), serverId);
    }

    public void updateSnapshot(long serverId, String hostname, String osType, Double cpuUsage,
                               Long memoryTotalMb, Long memoryUsedMb, Double memoryUsage,
                               Integer gpuCount, Double gpuUsage, Long gpuMemoryTotalMb,
                               Long gpuMemoryUsedMb, Double gpuMemoryUsage,
                               LocalDateTime now) {
        jdbcTemplate.update("""
                UPDATE server_node
                SET hostname = ?, os_type = ?, status = 'ONLINE', cpu_usage = ?,
                    memory_total_mb = ?, memory_used_mb = ?, memory_usage = ?,
                    gpu_count = ?, gpu_usage = ?, gpu_memory_total_mb = ?,
                    gpu_memory_used_mb = ?, gpu_memory_usage = ?,
                    last_heartbeat_at = ?, updated_at = ?
                WHERE id = ?
                """,
                hostname, osType, cpuUsage, memoryTotalMb, memoryUsedMb, memoryUsage,
                gpuCount, gpuUsage, gpuMemoryTotalMb, gpuMemoryUsedMb, gpuMemoryUsage,
                Timestamp.valueOf(now), Timestamp.valueOf(now), serverId);
    }

    private RowMapper<ServerNode> mapper() {
        return (rs, rowNum) -> new ServerNode(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("host"),
                rs.getString("os_type"),
                rs.getString("gpu_type"),
                rs.getString("location"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getString("register_token"),
                rs.getString("agent_id"),
                rs.getString("agent_version"),
                rs.getString("hostname"),
                getDouble(rs, "cpu_usage"),
                getLong(rs, "memory_total_mb"),
                getLong(rs, "memory_used_mb"),
                getDouble(rs, "memory_usage"),
                getInteger(rs, "gpu_count"),
                getDouble(rs, "gpu_usage"),
                getLong(rs, "gpu_memory_total_mb"),
                getLong(rs, "gpu_memory_used_mb"),
                getDouble(rs, "gpu_memory_usage"),
                getDateTime(rs, "last_heartbeat_at"),
                rs.getBoolean("enabled"),
                getDateTime(rs, "created_at"),
                getDateTime(rs, "updated_at")
        );
    }

    private static Double getDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static Long getLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Integer getInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }
}
