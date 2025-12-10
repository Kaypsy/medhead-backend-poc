package com.medhead.bedallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO léger pour lister/afficher rapidement une spécialité.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SpecialtySummaryDTO", description = "Résumé d'une spécialité")
public class SpecialtySummaryDTO {

    @Schema(description = "Identifiant unique", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long id;

    @Schema(description = "Code NHS unique", example = "CARDIO", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 50)
    private String code;

    @Schema(description = "Nom de la spécialité", example = "Cardiologie", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 150)
    private String name;

    @Schema(description = "Groupe de spécialité")
    private SpecialtyGroupDTO specialtyGroup;
}
