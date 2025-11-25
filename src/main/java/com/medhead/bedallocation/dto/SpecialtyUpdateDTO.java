package com.medhead.bedallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilisé pour la mise à jour partielle d'une spécialité.
 * Tous les champs sont optionnels; les validations s'appliquent si présents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SpecialtyUpdateDTO", description = "Payload de mise à jour d'une spécialité")
public class SpecialtyUpdateDTO {

    @Schema(description = "Code NHS unique de la spécialité", example = "CARDIO")
    @Size(max = 50)
    private String code;

    @Schema(description = "Nom officiel de la spécialité", example = "Cardiologie")
    @Size(max = 150)
    private String name;

    @Schema(description = "Groupe de spécialité", example = "Médecine")
    @Size(max = 150)
    private String specialtyGroup;

    @Schema(description = "Description fonctionnelle")
    private String description;

    @Schema(description = "Indique si la spécialité est active")
    private Boolean isActive;
}
