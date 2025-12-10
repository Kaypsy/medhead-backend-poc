package com.medhead.bedallocation.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

/**
 * Utilitaire pour la gestion des tokens JWT (génération, validation, extraction d'informations).
 *
 * Implémentation basée sur JJWT 0.12.x et l'algorithme HS256.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties properties;

    private volatile SecretKey cachedKey;

    /**
     * Génère un token JWT signé contenant le {@code username} en tant que subject.
     *
     * @param username identifiant unique de l'utilisateur
     * @return token JWT signé (compact JWS)
     */
    public String generateToken(String username) {
        Objects.requireNonNull(username, "username ne peut pas être null");

        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getExpiration());

        String jws = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();

        if (log.isDebugEnabled()) {
            log.debug("JWT généré pour subject='{}' avec exp={}.", username, expiry);
        }
        return jws;
    }

    /**
     * Valide la structure, la signature et l'expiration du token.
     *
     * @param token JWT compact
     * @return true si le token est valide, false sinon
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expiré: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token JWT mal formé: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT non supporté: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Signature JWT invalide ou compromis de sécurité: {}", e.getMessage());
        } catch (IncorrectClaimException e) {
            log.warn("JWT avec claim incorrect: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token JWT vide ou invalide: {}", e.getMessage());
        } catch (Exception e) {
            // Catch-all pour éviter de divulguer des détails sensibles
            log.error("Erreur inattendue lors de la validation du JWT: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extrait le username (subject) du token JWT.
     *
     * @param token JWT compact
     * @return le username si extraction possible, sinon null
     */
    public String getUsernameFromToken(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            // Même si expiré, on peut récupérer le subject utile pour logs
            return safeSubject(e);
        } catch (Exception e) {
            log.debug("Impossible d'extraire le subject du token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrait la date d'expiration du token.
     *
     * @param token JWT compact
     * @return Date d'expiration ou null si non disponible
     */
    public Date getExpirationDate(String token) {
        try {
            return parseClaims(token).getExpiration();
        } catch (ExpiredJwtException e) {
            return e.getClaims() != null ? e.getClaims().getExpiration() : null;
        } catch (Exception e) {
            log.debug("Impossible d'extraire l'expiration du token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Indique si le token est expiré.
     *
     * @param token JWT compact
     * @return true si expiré, false sinon (ou si indéterminé)
     */
    public boolean isTokenExpired(String token) {
        Date exp = getExpirationDate(token);
        return exp != null && exp.before(new Date());
    }

    // =========================
    // Méthodes utilitaires
    // =========================

    private Claims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Le token est vide");
        }
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(stripPrefix(token))
                .getPayload();
    }

    private String stripPrefix(String token) {
        String prefix = properties.getPrefix();
        if (prefix != null && !prefix.isBlank() && token.startsWith(prefix)) {
            return token.substring(prefix.length()).trim();
        }
        return token.trim();
    }

    /**
     * Construit la clé HMAC à partir du secret configuré.
     * Supporte les secrets bruts (UTF-8) ou encodés en Base64.
     */
    private SecretKey getSigningKey() {
        SecretKey key = cachedKey;
        if (key != null) return key;

        synchronized (this) {
            if (cachedKey == null) {
                String secret = properties.getSecret();
                if (secret == null || secret.isBlank()) {
                    throw new IllegalStateException("Le secret JWT n'est pas configuré (security.jwt.secret)");
                }

                // Essaye d'abord Base64, sinon utilise les bytes UTF-8
                byte[] keyBytes;
                try {
                    keyBytes = Decoders.BASE64.decode(secret);
                    if (keyBytes.length == 0) {
                        // secret n'était pas du Base64 valable, fallback
                        keyBytes = secret.getBytes(StandardCharsets.UTF_8);
                    }
                } catch (IllegalArgumentException | DecodingException e) {
                    // Certains secrets peuvent contenir des caractères non valides pour Base64 (ex: '-')
                    // Dans ce cas, on interprète la chaîne comme une clé brute en UTF-8
                    keyBytes = secret.getBytes(StandardCharsets.UTF_8);
                }

                if (keyBytes.length < 32) { // 256 bits requis pour HS256
                    throw new IllegalStateException("Le secret JWT doit faire au moins 256 bits (32 octets). " +
                            "Fournissez une clé plus longue ou une chaîne Base64 d'au moins 32 octets décodés.");
                }

                cachedKey = Keys.hmacShaKeyFor(keyBytes);
                if (log.isDebugEnabled()) {
                    log.debug("Clé JWT initialisée (alg=HS256, taille={} octets).", keyBytes.length);
                }
            }
            return cachedKey;
        }
    }

    private String safeSubject(ExpiredJwtException e) {
        try {
            return e.getClaims() != null ? e.getClaims().getSubject() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
