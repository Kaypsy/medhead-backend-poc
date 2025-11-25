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
 * DTO utilisé pour la création d'un lit.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BedCreateDTO", description = "Payload de création d'un lit")
public class BedCreateDTO {

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

    @Schema(description = "Statut du lit", example = "AVAILABLE")
    private BedStatus status;
}
