package com.medhead.bedallocation.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Propriétés de configuration pour JWT.
 *
 * Les valeurs sont chargées depuis le fichier application.properties (prefixe: security.jwt).
 * - secret: clé utilisée pour signer les tokens (doit être >= 256 bits pour HS256).
 * - expiration: durée d'expiration en millisecondes.
 * - header: nom de l'en-tête HTTP transportant le token (par défaut: Authorization).
 * - prefix: préfixe du token dans l'en-tête (par défaut: "Bearer ").
 */
@Getter
@Setter
@ToString(exclude = "secret")
@Validated
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * Clé secrète utilisée pour signer les JWT (HS256).
     * Conseil: la fournir via une variable d'environnement (ex: JWT_SECRET).
     */
    @NotBlank
    private String secret;

    /**
     * Durée d'expiration en millisecondes.
     */
    @NotNull
    private Long expiration;

    /**
     * Support de compatibilité: mappe security.jwt.expiration-ms -> expiration.
     * Permet d'éviter de modifier les fichiers de propriétés existants.
     */
    public void setExpirationMs(Long expirationMs) {
        this.expiration = expirationMs;
    }

    /**
     * Nom de l'en-tête HTTP où le token est attendu.
     */
    private String header = "Authorization";

    /**
     * Préfixe du token dans l'en-tête HTTP.
     */
    private String prefix = "Bearer ";
}
