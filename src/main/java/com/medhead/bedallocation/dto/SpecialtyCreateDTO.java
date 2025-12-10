package com.medhead.bedallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilisé pour créer une nouvelle spécialité.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SpecialtyCreateDTO", description = "Payload de création d'une spécialité")
public class SpecialtyCreateDTO {

    @Schema(description = "Code NHS unique de la spécialité", example = "CARDIO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 50)
    private String code;

    @Schema(description = "Nom officiel de la spécialité", example = "Cardiologie", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 150)
    private String name;

    @Schema(description = "Référence vers le groupe de spécialité (id ou code; si code inconnu et name fourni, le groupe sera créé)",
            example = "{\n  'id': 3\n}")
    private SpecialtyGroupRefDTO specialtyGroup;

    @Schema(description = "Description fonctionnelle")
    private String description;
}
