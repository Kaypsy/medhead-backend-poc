package com.medhead.bedallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserDTO", description = "DTO utilisateur (sans mot de passe)")
public class UserDTO {

    @Schema(description = "Identifiant de l'utilisateur")
    private Long id;

    @Schema(description = "Nom d'utilisateur (unique)")
    private String username;

    @Schema(description = "Email (unique)")
    private String email;

    @Schema(description = "Rôles sous forme de chaîne ex: ROLE_USER,ROLE_ADMIN")
    private String roles;

    @Schema(description = "Utilisateur actif")
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
