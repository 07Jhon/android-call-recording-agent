package com.enterprise.callrecorder.repository;

import com.enterprise.callrecorder.model.RegisteredDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisteredDeviceRepository extends JpaRepository<RegisteredDevice, Long> {

    Optional<RegisteredDevice> findByApiToken(String apiToken);

    Optional<RegisteredDevice> findByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);
}
