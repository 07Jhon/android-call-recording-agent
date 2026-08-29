package com.enterprise.callrecorder.service;

import com.enterprise.callrecorder.model.AuditLog;
import com.enterprise.callrecorder.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Service pour l'audit et la journalisation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditRepository;

    /**
     * Enregistrer une action
     */
    public void logAction(Long recordingId, String action, String actorId, String details) {
        AuditLog auditLog = AuditLog.builder()
            .recordingId(recordingId)
            .action(action)
            .actorId(actorId)
            .details(details)
            .createdAt(LocalDateTime.now())
            .build();

        auditRepository.save(auditLog);
        log.info("Audit log: {} - {} - {}", recordingId, action, details);
    }
}
