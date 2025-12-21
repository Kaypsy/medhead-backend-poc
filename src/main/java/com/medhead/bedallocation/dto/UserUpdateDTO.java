package com.medhead.bedallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de mise à jour d'utilisateur (ADMIN). Le mot de passe n'est pas géré ici.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserUpdateDTO", description = "Payload de mise à jour d'un utilisateur (ADMIN)")
public class UserUpdateDTO {

    @Size(min = 3, max = 50)
    @Schema(description = "Nom d'utilisateur", example = "jdoe", minLength = 3, maxLength = 50)
    private String username;

    @Email
    @Schema(description = "Email", example = "jdoe@example.com")
    private String email;

    @Schema(description = "Rôles sous forme de chaîne ex: ROLE_USER,ROLE_ADMIN", example = "ROLE_USER")
    private String roles;

    @Schema(description = "Utilisateur actif")
    private Boolean isActive;
}
