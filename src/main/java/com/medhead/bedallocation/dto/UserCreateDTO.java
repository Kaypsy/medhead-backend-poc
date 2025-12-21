package com.medhead.bedallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de création d'utilisateur (ADMIN).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserCreateDTO", description = "Payload de création d'un utilisateur (ADMIN)")
public class UserCreateDTO {

    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Nom d'utilisateur", example = "jdoe", minLength = 3, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank
    @Size(min = 8)
    @Schema(description = "Mot de passe (au moins 8 caractères)", example = "P@ssw0rd!", minLength = 8, requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Email
    @NotBlank
    @Schema(description = "Email", example = "jdoe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Rôles sous forme de chaîne ex: ROLE_USER,ROLE_ADMIN", example = "ROLE_USER")
    private String roles;

    @Schema(description = "Utilisateur actif (par défaut: true)")
    private Boolean isActive;
}
