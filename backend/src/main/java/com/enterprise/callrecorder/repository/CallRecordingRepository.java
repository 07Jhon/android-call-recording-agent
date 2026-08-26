package com.enterprise.callrecorder.repository;

import com.enterprise.callrecorder.model.CallRecording;
import com.enterprise.callrecorder.model.CallStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CallRecording entity
 */
@Repository
public interface CallRecordingRepository extends JpaRepository<CallRecording, Long> {

    Page<CallRecording> findByDeviceId(String deviceId, Pageable pageable);

    Page<CallRecording> findByPhoneNumber(String phoneNumber, Pageable pageable);

    Page<CallRecording> findByStatus(CallStatus status, Pageable pageable);

    List<CallRecording> findByStatusIn(List<CallStatus> statuses);

    Page<CallRecording> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    long countByStatus(CallStatus status);

    long countByDeviceId(String deviceId);

    Optional<CallRecording> findByFileName(String fileName);
}
