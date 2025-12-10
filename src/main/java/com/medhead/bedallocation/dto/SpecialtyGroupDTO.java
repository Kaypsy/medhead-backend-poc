package com.medhead.bedallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SpecialtyGroupDTO", description = "Représentation d'un groupe de spécialités")
public class SpecialtyGroupDTO {
    @Schema(description = "Identifiant du groupe", example = "3")
    private Long id;

    @Schema(description = "Code unique du groupe", example = "GENERAL_MEDICINE_GROUP")
    private String code;

    @Schema(description = "Nom du groupe", example = "Groupe de médecine générale")
    private String name;

    @Schema(description = "Description du groupe")
    private String description;

    @Schema(description = "Actif ou non", example = "true")
    private Boolean isActive;

    @Schema(description = "Date de création")
    private LocalDateTime createdAt;

    @Schema(description = "Date de mise à jour")
    private LocalDateTime updatedAt;
}
