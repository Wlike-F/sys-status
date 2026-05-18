package com.sysstatus.server.agent;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AgentCredentialRepository {
    private final JdbcTemplate jdbcTemplate;

    public AgentCredentialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(long serverId, String agentId, String agentSecret, String agentVersion, LocalDateTime now) {
        jdbcTemplate.update("""
                INSERT INTO agent_credential
                    (server_id, agent_id, agent_secret, agent_version, registered_at, last_seen_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                serverId, agentId, agentSecret, agentVersion,
                Timestamp.valueOf(now), Timestamp.valueOf(now));
    }

    public Optional<AgentCredential> find(long serverId, String agentId, String agentSecret) {
        List<AgentCredential> credentials = jdbcTemplate.query("""
                SELECT server_id, agent_id, agent_secret
                FROM agent_credential
                WHERE server_id = ? AND agent_id = ? AND agent_secret = ?
                """,
                (rs, rowNum) -> new AgentCredential(
                        rs.getLong("server_id"),
                        rs.getString("agent_id"),
                        rs.getString("agent_secret")
                ),
                serverId, agentId, agentSecret);
        return credentials.stream().findFirst();
    }

    public void touch(String agentId, LocalDateTime now) {
        jdbcTemplate.update("""
                UPDATE agent_credential
                SET last_seen_at = ?
                WHERE agent_id = ?
                """, Timestamp.valueOf(now), agentId);
    }

    public record AgentCredential(long serverId, String agentId, String agentSecret) {
    }
}
