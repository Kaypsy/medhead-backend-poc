package com.medhead.bedallocation.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration CORS pour autoriser les frontends locaux.
 */
@Configuration
public class CorsConfig {

    /**
     * Définit les règles CORS globales:
     * - Origines autorisées: localhost:3000 et localhost:4200
     * - Méthodes autorisées: GET, POST, PUT, PATCH, DELETE, OPTIONS
     * - En-têtes autorisés: Authorization, Content-Type
     * - Credentials activés (cookies/authorization header)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:4200"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        // Applique la configuration à toutes les routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
