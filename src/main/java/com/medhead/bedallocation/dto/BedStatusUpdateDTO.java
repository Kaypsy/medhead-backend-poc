package com.medhead.bedallocation.dto;

import com.medhead.bedallocation.model.enums.BedStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload minimal pour mettre à jour le statut d'un lit via PATCH.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BedStatusUpdateDTO", description = "Payload pour changer le statut d'un lit")
public class BedStatusUpdateDTO {

    @Schema(description = "Nouveau statut du lit", example = "AVAILABLE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private BedStatus status;
}
