package com.enterprise.callrecorder.service;

import com.enterprise.callrecorder.model.CallRecording;
import com.enterprise.callrecorder.model.CallStatus;
import com.enterprise.callrecorder.repository.CallRecordingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 🔴 CRITICAL: Service for managing call recordings
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CallRecordingService {

    private final CallRecordingRepository recordingRepository;
    private final StorageService storageService;
    private final AuditService auditService;

    /**
     * Créer un nouvel enregistrement
     */
    public CallRecording createRecording(CallRecording recording) {
        log.info("Creating recording for device: {}, phone: {}", recording.getDeviceId(), recording.getPhoneNumber());
        
        CallRecording saved = recordingRepository.save(recording);
        auditService.logAction(saved.getId(), "CREATED", null, "Recording created");
        
        return saved;
    }

    /**
     * Récupérer un enregistrement par ID
     */
    public Optional<CallRecording> getRecordingById(Long id) {
        return recordingRepository.findById(id);
    }

    /**
     * Récupérer les enregistrements par appareil
     */
    public Page<CallRecording> getRecordingsByDevice(String deviceId, Pageable pageable) {
        return recordingRepository.findByDeviceId(deviceId, pageable);
    }

    /**
     * Récupérer les enregistrements par numéro de téléphone
     */
    public Page<CallRecording> getRecordingsByPhoneNumber(String phoneNumber, Pageable pageable) {
        return recordingRepository.findByPhoneNumber(phoneNumber, pageable);
    
    /**
     * Récupérer les enregistrements en attente d'upload
     */
    public List<CallRecording> getPendingUploads() {
        return recordingRepository.findByStatusIn(List.of(
            CallStatus.PENDING_UPLOAD,
            CallStatus.UPLOAD_FAILED
        ));
    }

    /**
     * Marquer un enregistrement comme uploadé
     */
    public CallRecording markAsUploaded(Long id, String storageKey) {
        CallRecording recording = recordingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recording not found: " + id));

        recording.setStatus(CallStatus.UPLOADED);
        recording.setStorageKey(storageKey);
        recording.setUploadedAt(LocalDateTime.now());
        recording.setUploadAttempts(0);

        CallRecording saved = recordingRepository.save(recording);
        auditService.logAction(id, "UPLOADED", null, "Recording uploaded successfully");
        
        return saved;
    }

    /**
     * Mettre à jour le statut
     */
    public CallRecording updateStatus(Long id, CallStatus status) {
        CallRecording recording = recordingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recording not found: " + id));

        recording.setStatus(status);
        auditService.logAction(id, "STATUS_CHANGED", null, "Status changed to " + status);
        
        return recordingRepository.save(recording);
    }

    /**
     * Incrémenter les tentatives d'upload et enregistrer l'erreur
     */
    public CallRecording recordUploadError(Long id, String error) {
        CallRecording recording = recordingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recording not found: " + id));

        recording.setUploadAttempts(recording.getUploadAttempts() + 1);
        recording.setLastUploadError(error);
        
        // Après 3 tentatives, marquer comme failed
        if (recording.getUploadAttempts() >= 3) {
            recording.setStatus(CallStatus.UPLOAD_FAILED);
        }

        return recordingRepository.save(recording);
    }

    /**
     * Supprimer un enregistrement (soft delete)
     */
    public CallRecording deleteRecording(Long id) {
        CallRecording recording = recordingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recording not found: " + id));

        recording.setStatus(CallStatus.DELETED);
        recording.setDeletedAt(LocalDateTime.now());
        
        // Supprimer du stockage
        if (recording.getStorageKey() != null) {
            storageService.deleteFile(recording.getStorageKey());
        }

        auditService.logAction(id, "DELETED", null, "Recording deleted");
        
        return recordingRepository.save(recording);
    }

    /**
     * Obtenir les statistiques
     */
    public RecordingStats getStats() {
        return RecordingStats.builder()
            .totalRecordings(recordingRepository.count())
            .recordingCount(recordingRepository.countByStatus(CallStatus.RECORDING))
            .pendingUploadCount(recordingRepository.countByStatus(CallStatus.PENDING_UPLOAD))
            .uploadedCount(recordingRepository.countByStatus(CallStatus.UPLOADED))
            .failedUploadCount(recordingRepository.countByStatus(CallStatus.UPLOAD_FAILED))
            .build();
    }
}
