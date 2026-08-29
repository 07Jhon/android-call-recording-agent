package com.enterprise.callrecorder.config;

import com.enterprise.callrecorder.model.RegisteredDevice;
import com.enterprise.callrecorder.repository.RegisteredDeviceRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Authentifie les appareils Android via un token statique envoyé dans le
 * header "Authorization: Bearer <token>". Le token est généré une seule
 * fois lors de l'enregistrement du device (voir DeviceController) et
 * stocké côté device (SharedPreferences chiffrées côté Android).
 *
 * ⚠️ Limite connue : le token est stocké en clair en base (voir
 * RegisteredDevice.apiToken). Pour une vraie mise en production, remplacer
 * par un hash (ex: BCrypt) comme pour un mot de passe, et comparer le hash
 * plutôt que la valeur brute. Laissé en clair ici pour rester simple à
 * déboguer/démarrer.
 */
@Component
@RequiredArgsConstructor
public class DeviceAuthFilter extends OncePerRequestFilter {

    private final RegisteredDeviceRepository deviceRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();

            Optional<RegisteredDevice> deviceOpt = deviceRepository.findByApiToken(token);

            if (deviceOpt.isPresent() && Boolean.TRUE.equals(deviceOpt.get().getIsActive())) {
                RegisteredDevice device = deviceOpt.get();

                var authToken = new UsernamePasswordAuthenticationToken(
                    device.getDeviceId(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_DEVICE"))
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);

                device.setLastSeenAt(LocalDateTime.now());
                deviceRepository.save(device);
            }
        }

        filterChain.doFilter(request, response);
    }
}
