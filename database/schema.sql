-- 🔴 CRITICAL: Call Recording Metadata Table

CREATE TABLE IF NOT EXISTS call_recordings (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL,
    call_log_id VARCHAR(255),
    phone_number VARCHAR(20) NOT NULL,
    call_type VARCHAR(50) NOT NULL CHECK (call_type IN ('INCOMING', 'OUTGOING', 'MISSED', 'REJECTED')),
    started_at BIGINT NOT NULL,
    ended_at BIGINT,
    duration BIGINT,
    
    -- Fichier
    file_name VARCHAR(255),
    file_size BIGINT,
    storage_key VARCHAR(255),
    
    -- Statut
    status VARCHAR(50) NOT NULL DEFAULT 'RECORDING' CHECK (
        status IN (
            'RECORDING',
            'PROCESSING',
            'PENDING_UPLOAD',
            'UPLOADING',
            'UPLOADED',
            'UPLOAD_FAILED',
            'DELETED'
        )
    ),
    upload_attempts INT DEFAULT 0,
    last_upload_error TEXT,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_at TIMESTAMP,
    deleted_at TIMESTAMP,
    
    -- Indexes
    CONSTRAINT recording_file_unique UNIQUE (file_name),
    CONSTRAINT valid_timestamps CHECK (ended_at IS NULL OR ended_at >= started_at)
);

-- Indexes pour les recherches fréquentes
CREATE INDEX idx_device_id ON call_recordings(device_id);
CREATE INDEX idx_phone_number ON call_recordings(phone_number);
CREATE INDEX idx_status ON call_recordings(status);
CREATE INDEX idx_started_at ON call_recordings(started_at DESC);
CREATE INDEX idx_created_at ON call_recordings(created_at DESC);

-- 🔴 CRITICAL: Audit Log Table

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    recording_id BIGINT,
    action VARCHAR(50) NOT NULL,
    actor_id VARCHAR(255),
    actor_role VARCHAR(50),
    details JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (recording_id) REFERENCES call_recordings(id) ON DELETE CASCADE
);

CREATE INDEX idx_audit_recording_id ON audit_logs(recording_id);
CREATE INDEX idx_audit_actor_id ON audit_logs(actor_id);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at DESC);

-- 🔴 CRITICAL: Access Permissions Table

CREATE TABLE IF NOT EXISTS recording_access (
    id BIGSERIAL PRIMARY KEY,
    recording_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    access_type VARCHAR(50) NOT NULL CHECK (access_type IN ('VIEW', 'DOWNLOAD', 'DELETE')),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by VARCHAR(255),
    expires_at TIMESTAMP,
    
    FOREIGN KEY (recording_id) REFERENCES call_recordings(id) ON DELETE CASCADE,
    CONSTRAINT unique_access UNIQUE (recording_id, user_id, access_type)
);

CREATE INDEX idx_user_access ON recording_access(user_id, expires_at);
CREATE INDEX idx_recording_access ON recording_access(recording_id);

-- 🟡 Device Registration Table

CREATE TABLE IF NOT EXISTS registered_devices (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255) UNIQUE NOT NULL,
    device_name VARCHAR(255),
    android_version INT,
    manufacturer VARCHAR(255),
    model VARCHAR(255),
    is_recording_supported BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    last_seen_at TIMESTAMP,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_device_id ON registered_devices(device_id);
CREATE INDEX idx_last_seen_at ON registered_devices(last_seen_at DESC);

-- 🟡 Upload Queue Table (pour retry automatique)

CREATE TABLE IF NOT EXISTS upload_queue (
    id BIGSERIAL PRIMARY KEY,
    recording_id BIGINT NOT NULL UNIQUE,
    retry_count INT DEFAULT 0,
    last_error TEXT,
    next_retry_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (recording_id) REFERENCES call_recordings(id) ON DELETE CASCADE
);

CREATE INDEX idx_queue_next_retry ON upload_queue(next_retry_at);
