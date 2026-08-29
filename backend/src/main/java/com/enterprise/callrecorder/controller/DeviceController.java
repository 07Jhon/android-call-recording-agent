package com.enterprise.callrecorder.controller;

import com.enterprise.callrecorder.model.RegisteredDevice;
import com.enterprise.callrecorder.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint d'enregistrement des devices. Volontairement PUBLIC (pas de
 * @PreAuthorize) puisqu'un device n'a pas encore de token la première fois
 * qu'il appelle cette route — c'est justement elle qui lui en délivre un.
 *
 * ⚠️ En production : ce endpoint devrait être protégé par un secret partagé
 * distinct (ex: clé d'inscription fournie hors-bande à l'app avant
 * déploiement), sans quoi n'importe qui connaissant l'URL du backend peut
 * enregistrer un device et obtenir un token valide. Non implémenté ici pour
 * rester simple à tester.
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String deviceId,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Integer androidVersion,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String model) {

        RegisteredDevice device = deviceService.registerDevice(
            deviceId, deviceName, androidVersion, manufacturer, model);

        Map<String, Object> response = new HashMap<>();
        response.put("deviceId", device.getDeviceId());
        response.put("apiToken", device.getApiToken());
        response.put("message", "Device registered. Store this token securely, it will not be shown again in full.");

        return ResponseEntity.ok(response);
    }
}
