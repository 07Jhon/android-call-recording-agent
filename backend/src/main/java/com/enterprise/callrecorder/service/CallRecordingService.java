package com.enterprise.callrecorder.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registered_devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallRecordingService{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "android_version")
    private Integer androidVersion;

    private String manufacturer;

    private String model;

    /**
     * Token secret utilisé par le device pour s'authentifier (header
     * Authorization: Bearer <token>). Généré une seule fois à l'enregistrement,
     * jamais renvoyé en clair après coup — seul un hash pourrait être stocké
     * pour plus de sécurité, mais on garde le token en clair ici pour la
     * simplicité (⚠️ acceptable en interne, pas pour de la donnée exposée
     * publiquement — voir note dans DeviceAuthFilter).
     */
    @Column(name = "api_token", unique = true)
    private String apiToken;

    @Column(name = "is_recording_supported")
    private Boolean isRecordingSupported;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;
}