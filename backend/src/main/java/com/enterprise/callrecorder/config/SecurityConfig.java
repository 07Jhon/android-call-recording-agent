package com.enterprise.callrecorder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ⚠️ IMPORTANT: Sans cette classe, les annotations @PreAuthorize sur le
 * contrôleur sont silencieusement ignorées (@EnableMethodSecurity n'était
 * jamais activé) et Spring Security tombe sur sa config par défaut
 * (Basic Auth avec un mot de passe généré aléatoirement à chaque démarrage,
 * loggé dans la console).
 *
 * ⚠️ TODO PRODUCTION: Ce fichier active seulement le mécanisme d'autorisation
 * par rôle. Il faut encore brancher une authentification réelle des devices
 * (ex: token API par device, JWT, ou mTLS) — voir README section "Authentification
 * des appareils". En l'état, ce squelette n'authentifie personne.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {});

        return http.build();
    }
}
