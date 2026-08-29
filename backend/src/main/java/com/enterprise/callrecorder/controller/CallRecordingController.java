package com.enterprise.callrecorder.controller;

import com.enterprise.callrecorder.model.CallRecording;
import com.enterprise.callrecorder.model.CallStatus;
import com.enterprise.callrecorder.model.CallType;
import com.enterprise.callrecorder.service.CallRecordingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

/**
 * 🔴 CRITICAL: REST Controller for Call Recording API
 */
@RestController
@RequestMapping("/api/v1/call-recordings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class CallRecordingController {

    private final CallRecordingService recordingService;

    /**
     * Upload a call recording
     * POST /api/v1/call-recordings/upload
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('DEVICE', 'ADMIN')")
    public ResponseEntity<?> uploadRecording(
            @RequestParam String deviceId,
            @RequestParam String phoneNumber,
            @RequestParam String callType,
            @RequestParam Long duration,
            @RequestParam Long fileSize,
            @RequestPart MultipartFile file) {
        
        try {
            log.info("Upload request from device: {}, phone: {}", deviceId, phoneNumber);

            CallRecording saved = recordingService.processUpload(
                deviceId, phoneNumber, CallType.valueOf(callType), duration, fileSize, file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recordingId", saved.getId());
            response.put("storageKey", saved.getStorageKey());
            response.put("message", "Recording uploaded successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "Invalid callType: " + callType));
        } catch (Exception e) {
            log.error("Upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get all recordings (paginated)
     * GET /api/v1/call-recordings
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<CallRecording>> getAllRecordings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(recordingService.getAllRecordings(pageable));
    }

    /**
     * Get recording by ID
     * GET /api/v1/call-recordings/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getRecording(@PathVariable Long id) {
        return recordingService.getRecordingById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get recordings by device
     * GET /api/v1/call-recordings/device/{deviceId}
     */
    @GetMapping("/device/{deviceId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<CallRecording>> getRecordingsByDevice(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CallRecording> recordings = recordingService.getRecordingsByDevice(deviceId, pageable);
        
        return ResponseEntity.ok(recordings);
    }

    /**
     * Get recordings by phone number
     * GET /api/v1/call-recordings/phone/{phoneNumber}
     */
    @GetMapping("/phone/{phoneNumber}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<CallRecording>> getRecordingsByPhone(
            @PathVariable String phoneNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CallRecording> recordings = recordingService.getRecordingsByPhoneNumber(phoneNumber, pageable);
        
        return ResponseEntity.ok(recordings);
    }

    /**
     * Download recording audio file
     * GET /api/v1/call-recordings/{id}/download
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> downloadRecording(@PathVariable Long id) {
        return recordingService.getRecordingById(id)
            .map(recording -> {
                if (recording.getStorageKey() == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No file available for this recording"));
                }
                try {
                    byte[] fileContent = recordingService.getFileContent(recording.getStorageKey());
                    return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + recording.getFileName() + "\"")
                        .header("Content-Type", "application/octet-stream")
                        .body(fileContent);
                } catch (Exception e) {
                    log.error("Download failed for recording {}", id, e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Failed to read file: " + e.getMessage()));
                }
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete recording
     * DELETE /api/v1/call-recordings/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRecording(@PathVariable Long id) {
        CallRecording deleted = recordingService.deleteRecording(id);
        return ResponseEntity.ok(Map.of("message", "Recording deleted", "id", deleted.getId()));
    }

    /**
     * Get statistics
     * GET /api/v1/call-recordings/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(recordingService.getStats());
    }
}
