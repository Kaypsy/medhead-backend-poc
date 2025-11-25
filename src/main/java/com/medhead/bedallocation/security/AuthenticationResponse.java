package com.medhead.bedallocation.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse après authentification, renvoyant le token JWT et des métadonnées.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {

    private String token;

    /**
     * Type du token (par défaut: "Bearer").
     */
    @Builder.Default
    private String type = "Bearer";

    private String username;
}
