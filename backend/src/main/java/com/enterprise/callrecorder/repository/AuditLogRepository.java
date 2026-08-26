package com.enterprise.callrecorder.repository;

import com.enterprise.callrecorder.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository pour AuditLog
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByRecordingId(Long recordingId);
    List<AuditLog> findByActorId(String actorId);
}
