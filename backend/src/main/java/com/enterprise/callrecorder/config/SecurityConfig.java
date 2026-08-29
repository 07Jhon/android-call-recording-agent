package com.enterprise.callrecorder.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Deux mécanismes d'authentification cohabitent :
 *
 *  1. Les appareils Android s'authentifient avec un token statique
 *     (header "Authorization: Bearer <token>") délivré une fois par
 *     POST /api/v1/devices/register — voir DeviceAuthFilter. Rôle: DEVICE.
 *
 *  2. Le frontend/Dashboard s'authentifie en Basic Auth avec un compte
 *     admin unique, configurable via les variables d'environnement
 *     ADMIN_USERNAME / ADMIN_PASSWORD (voir application.yml). Rôles: USER, ADMIN.
 *
 * ⚠️ Le compte admin par défaut ci-dessous (admin/changeme) est fourni
 * UNIQUEMENT pour pouvoir démarrer et tester le projet immédiatement.
 * Change ADMIN_PASSWORD avant tout déploiement au-delà d'un poste de dev.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final DeviceAuthFilter deviceAuthFilter;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:changeme}")
    private String adminPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(
            User.withUsername(adminUsername)
                .password(encoder.encode(adminPassword))
                .roles("USER", "ADMIN")
                .build()
        );
        return manager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/devices/register").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {})
            .addFilterBefore(deviceAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
