package com.medhead.bedallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO détaillé représentant une spécialité médicale.
 * Contient les informations principales ainsi que les identifiants
 * des hôpitaux et des lits associés.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SpecialtyDTO", description = "Représentation complète d'une spécialité")
public class SpecialtyDTO {

    @Schema(description = "Identifiant unique de la spécialité", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long id;

    @Schema(description = "Code NHS unique de la spécialité", example = "CARDIO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 50)
    private String code;

    @Schema(description = "Nom officiel de la spécialité", example = "Cardiologie", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 150)
    private String name;

    @Schema(description = "Groupe de spécialité", example = "Médecine")
    @NotBlank
    @Size(max = 150)
    private String specialtyGroup;

    @Schema(description = "Description fonctionnelle")
    private String description;

    @Schema(description = "Indique si la spécialité est active", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Boolean isActive;

    @Schema(description = "Date de création de l'enregistrement")
    private LocalDateTime createdAt;

    @Schema(description = "Identifiants des hôpitaux liés à cette spécialité")
    private List<Long> hospitalIds;

    @Schema(description = "Identifiants des lits liés à cette spécialité")
    private List<Long> bedIds;
}
