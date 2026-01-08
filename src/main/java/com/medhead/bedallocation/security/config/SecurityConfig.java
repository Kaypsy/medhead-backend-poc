package com.medhead.bedallocation.security.config;

import com.medhead.bedallocation.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration Spring Security (Spring Security 6+).
 * - API REST stateless (CSRF désactivé, sessions désactivées)
 * - CORS configuré via {@link CorsConfig}
 * - Endpoints publics vs protégés
 * - Filtre JWT ajouté avant {@link UsernamePasswordAuthenticationFilter}
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity // Active les annotations @PreAuthorize/@PostAuthorize au niveau des méthodes
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPoint restAuthenticationEntryPoint;

    /**
     * Chaîne de filtres de sécurité avec le DSL lambda.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // API REST stateless : pas de CSRF
            .csrf(csrf -> csrf.disable())

            // CORS : activé (la configuration détaillée est fournie par CorsConfig)
            .cors(cors -> {})

            // Pas d'authentification par formulaire ni HTTP Basic
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            // Gestion d'erreurs d'authentification: renvoyer du JSON 401
            .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))

            // Sessions désactivées (stateless)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Politique d'autorisation
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics (on prévoit les chemins avec et sans le context-path /api)
                .requestMatchers(
                    "/api/auth/**", "/auth/**",
                    "/api/emergency/allocate", "/emergency/allocate",
                    "/api/docs/**", "/docs/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // Toute autre requête doit être authentifiée
                .anyRequest().authenticated()
            )

            // Ajouter le filtre JWT dans la chaîne avant UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Expose l'AuthenticationManager à partir de la configuration Spring Security.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * PasswordEncoder utilisant BCrypt (recommandé).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
