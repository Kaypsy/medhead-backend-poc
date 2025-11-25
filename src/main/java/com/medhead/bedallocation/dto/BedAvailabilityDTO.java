package com.medhead.bedallocation.dto;

import com.medhead.bedallocation.model.enums.BedStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO léger pour exposer l'état de disponibilité d'un lit.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BedAvailabilityDTO", description = "DTO léger de disponibilité d'un lit")
public class BedAvailabilityDTO {

    @Schema(description = "Identifiant du lit", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long id;

    @Schema(description = "Numéro du lit", example = "B-12", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String bedNumber;

    @Schema(description = "Statut du lit", example = "AVAILABLE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private BedStatus status;

    @Schema(description = "Identifiant de la spécialité associée", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long specialtyId;

    @Schema(description = "Identifiant de l'hôpital propriétaire du lit", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long hospitalId;
}
