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
 * DTO pour l'inscription d'un nouvel utilisateur.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserRegistrationDTO", description = "Payload d'inscription utilisateur")
public class UserRegistrationDTO {

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
}
