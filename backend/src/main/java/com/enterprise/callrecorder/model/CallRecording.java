package com.enterprise.callrecorder.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity for Call Recordings
 */
@Entity
@Table(
    name = "call_recordings",
    indexes = {
        @Index(name = "idx_device_id", columnList = "device_id"),
        @Index(name = "idx_phone_number", columnList = "phone_number"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallRecording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String deviceId;

    @Column(length = 255)
    private String callLogId;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CallType callType;

    @Column(nullable = false)
    private Long startedAt;

    private Long endedAt;

    private Long duration;

    // File information
    @Column(length = 255, unique = true)
    private String fileName;

    private Long fileSize;

    @Column(length = 255)
    private String storageKey;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private CallStatus status = CallStatus.RECORDING;

    @Column(columnDefinition = "integer default 0")
    @Builder.Default
    private Integer uploadAttempts = 0;

    @Column(columnDefinition = "TEXT")
    private String lastUploadError;

    // Audit
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime uploadedAt;

    private LocalDateTime deletedAt;
}