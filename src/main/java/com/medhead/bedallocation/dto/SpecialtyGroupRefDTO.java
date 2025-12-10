package com.medhead.bedallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SpecialtyGroupRefDTO", description = "Référence vers un groupe de spécialités pour création/mise à jour")
public class SpecialtyGroupRefDTO {

    @Schema(description = "Identifiant du groupe", example = "3")
    private Long id;

    @Schema(description = "Code du groupe", example = "GENERAL_MEDICINE_GROUP")
    private String code;

    @Schema(description = "Nom du groupe (utilisé si création dynamique)", example = "Groupe de médecine générale")
    private String name;
}
