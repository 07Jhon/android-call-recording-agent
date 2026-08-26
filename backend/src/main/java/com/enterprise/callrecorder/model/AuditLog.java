package com.enterprise.callrecorder.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entité pour l'audit des actions
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_recording_id", columnList = "recording_id"),
    @Index(name = "idx_audit_actor_id", columnList = "actor_id"),
    @Index(name = "idx_audit_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recording_id")
    private Long recordingId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 255)
    private String actorId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
