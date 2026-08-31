package com.enterprise.callrecorder.service;

import com.enterprise.callrecorder.model.CallRecording;
import com.enterprise.callrecorder.model.CallStatus;
import com.enterprise.callrecorder.model.CallType;
import com.enterprise.callrecorder.repository.CallRecordingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CallRecordingService {

    private final CallRecordingRepository callRecordingRepository;
    private final StorageService storageService;

    public CallRecording processUpload(
            String deviceId,
            String phoneNumber,
            CallType callType,
            Long duration,
            Long fileSize,
            MultipartFile file
    ) throws Exception {

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            originalFileName = "recording";
        }

        String fileName = System.currentTimeMillis() + "_" + originalFileName;

        String storageKey = storageService.saveFile(
                file.getBytes(),
                fileName
        );

        CallRecording recording = CallRecording.builder()
                .deviceId(deviceId)
                .phoneNumber(phoneNumber)
                .callType(callType)
                .startedAt(System.currentTimeMillis() - ((duration != null ? duration : 0) * 1000L))
                .endedAt(System.currentTimeMillis())
                .duration(duration)
                .fileName(fileName)
                .fileSize(fileSize != null ? fileSize : file.getSize())
                .storageKey(storageKey)
                .status(CallStatus.UPLOADED)
                .uploadAttempts(1)
                .uploadedAt(LocalDateTime.now())
                .build();

        return callRecordingRepository.save(recording);
    }

    @Transactional(readOnly = true)
    public Page<CallRecording> getAllRecordings(Pageable pageable) {
        return callRecordingRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<CallRecording> getRecordingById(Long id) {
        return callRecordingRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<CallRecording> getRecordingsByDevice(
            String deviceId,
            Pageable pageable
    ) {
        return callRecordingRepository.findByDeviceId(deviceId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CallRecording> getRecordingsByPhoneNumber(
            String phoneNumber,
            Pageable pageable
    ) {
        return callRecordingRepository.findByPhoneNumber(phoneNumber, pageable);
    }

    public byte[] getFileContent(String storageKey) throws Exception {
        return storageService.getFile(storageKey);
    }

    public CallRecording deleteRecording(Long id) {
        CallRecording recording = callRecordingRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Recording not found: " + id)
                );

        if (recording.getStorageKey() != null) {
            storageService.deleteFile(recording.getStorageKey());
        }

        recording.setStatus(CallStatus.DELETED);
        recording.setDeletedAt(LocalDateTime.now());

        return callRecordingRepository.save(recording);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", callRecordingRepository.count());
        stats.put(
                "uploaded",
                callRecordingRepository.countByStatus(CallStatus.UPLOADED)
        );
        stats.put(
                "recording",
                callRecordingRepository.countByStatus(CallStatus.RECORDING)
        );
        stats.put(
                "processing",
                callRecordingRepository.countByStatus(CallStatus.PROCESSING)
        );
        stats.put(
                "pendingUpload",
                callRecordingRepository.countByStatus(CallStatus.PENDING_UPLOAD)
        );
        stats.put(
                "uploading",
                callRecordingRepository.countByStatus(CallStatus.UPLOADING)
        );
        stats.put(
                "uploadFailed",
                callRecordingRepository.countByStatus(CallStatus.UPLOAD_FAILED)
        );
        stats.put(
                "deleted",
                callRecordingRepository.countByStatus(CallStatus.DELETED)
        );

        return stats;
    }
}