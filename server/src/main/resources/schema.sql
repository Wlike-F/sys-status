CREATE TABLE IF NOT EXISTS server_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    host VARCHAR(255) NOT NULL,
    os_type VARCHAR(20) NOT NULL,
    gpu_type VARCHAR(100),
    location VARCHAR(255),
    description TEXT,
    status VARCHAR(20) NOT NULL,
    register_token VARCHAR(64) NOT NULL,
    agent_id VARCHAR(128),
    agent_version VARCHAR(50),
    hostname VARCHAR(255),
    cpu_usage DECIMAL(6,2),
    memory_total_mb BIGINT,
    memory_used_mb BIGINT,
    memory_usage DECIMAL(6,2),
    last_heartbeat_at DATETIME,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS agent_credential (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    server_id BIGINT NOT NULL,
    agent_id VARCHAR(128) NOT NULL,
    agent_secret VARCHAR(128) NOT NULL,
    agent_version VARCHAR(50),
    registered_at DATETIME NOT NULL,
    last_seen_at DATETIME NOT NULL,
    CONSTRAINT uk_agent_credential_agent_id UNIQUE (agent_id)
);

CREATE TABLE IF NOT EXISTS metric_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    server_id BIGINT NOT NULL,
    collected_at DATETIME NOT NULL,
    hostname VARCHAR(255),
    os_type VARCHAR(20),
    cpu_usage DECIMAL(6,2),
    memory_total_mb BIGINT,
    memory_used_mb BIGINT,
    memory_usage DECIMAL(6,2),
    raw_json LONGTEXT
);
