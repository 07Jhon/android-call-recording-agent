package com.enterprise.callrecorder.service;

import com.enterprise.callrecorder.model.RegisteredDevice;
import com.enterprise.callrecorder.repository.RegisteredDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final RegisteredDeviceRepository deviceRepository;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Enregistre un nouvel appareil et lui génère un token API.
     * Si le device existe déjà, retourne l'enregistrement existant
     * SANS regénérer de token (pour ne pas invalider un token déjà en
     * usage sur l'appareil).
     */
    public RegisteredDevice registerDevice(String deviceId, String deviceName, Integer androidVersion,
                                            String manufacturer, String model) {

        return deviceRepository.findByDeviceId(deviceId)
            .map(existing -> {
                log.info("Device already registered: {}", deviceId);
                existing.setLastSeenAt(LocalDateTime.now());
                return deviceRepository.save(existing);
            })
            .orElseGet(() -> {
                log.info("Registering new device: {}", deviceId);
                RegisteredDevice device = RegisteredDevice.builder()
                    .deviceId(deviceId)
                    .deviceName(deviceName)
                    .androidVersion(androidVersion)
                    .manufacturer(manufacturer)
                    .model(model)
                    .apiToken(generateToken())
                    .isRecordingSupported(true)
                    .isActive(true)
                    .lastSeenAt(LocalDateTime.now())
                    .registeredAt(LocalDateTime.now())
                    .build();
                return deviceRepository.save(device);
            });
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
