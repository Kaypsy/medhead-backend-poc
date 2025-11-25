package com.medhead.bedallocation.dto;

import com.medhead.bedallocation.model.enums.BedStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO utilisé pour la mise à jour partielle d'un lit.
 * Tous les champs sont optionnels; les validations s'appliquent si présents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BedUpdateDTO", description = "Payload de mise à jour d'un lit")
public class BedUpdateDTO {

    @Schema(description = "Identifiant de l'hôpital propriétaire du lit", example = "1")
    private Long hospitalId;

    @Schema(description = "Identifiant de la spécialité associée", example = "5")
    private Long specialtyId;

    @Schema(description = "Numéro du lit", example = "B-12")
    private String bedNumber;

    @Schema(description = "Numéro de la chambre", example = "203")
    private String roomNumber;

    @Schema(description = "Étage où se situe le lit", example = "2")
    private Integer floor;

    @Schema(description = "Statut du lit", example = "AVAILABLE")
    private BedStatus status;

    @Schema(description = "Date/heure de dernière occupation")
    private LocalDateTime lastOccupiedAt;
}
