package com.medhead.bedallocation.dto;

import com.medhead.bedallocation.model.enums.BedStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO détaillé représentant un lit d'hôpital.
 * Les relations Hospital et Specialty sont exposées via leurs identifiants.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BedDTO", description = "Représentation complète d'un lit")
public class BedDTO {

    @Schema(description = "Identifiant du lit", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long id;

    @Schema(description = "Identifiant de l'hôpital propriétaire du lit", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long hospitalId;

    @Schema(description = "Identifiant de la spécialité associée", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long specialtyId;

    @Schema(description = "Numéro du lit", example = "B-12", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String bedNumber;

    @Schema(description = "Numéro de la chambre", example = "203")
    private String roomNumber;

    @Schema(description = "Étage où se situe le lit", example = "2")
    private Integer floor;

    @Schema(description = "Statut du lit", example = "AVAILABLE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private BedStatus status;

    @Schema(description = "Disponibilité du lit", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Boolean isAvailable;

    @Schema(description = "Date/heure de dernière occupation")
    private LocalDateTime lastOccupiedAt;

    @Schema(description = "Date de création (lecture seule)")
    private LocalDateTime createdAt;

    @Schema(description = "Date de mise à jour (lecture seule)")
    private LocalDateTime updatedAt;
}
