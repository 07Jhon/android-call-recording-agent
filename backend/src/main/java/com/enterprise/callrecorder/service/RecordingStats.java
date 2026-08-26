package com.enterprise.callrecorder.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Statistiques d'enregistrement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingStats {
    private Long totalRecordings;
    private Long recordingCount;
    private Long pendingUploadCount;
    private Long uploadedCount;
    private Long failedUploadCount;
}
