package com.medhead.bedallocation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO utilisé pour la création d'un hôpital.
 * Ne contient pas d'identifiant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objet de transfert de données pour la création d'un hôpital")
public class HospitalCreateDTO {

    @NotBlank
    @Size(max = 200)
    @Schema(description = "Nom complet de l'hôpital", example = "Hôpital Bichat – Claude-Bernard", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 255)
    @Schema(description = "Adresse postale complète", example = "46 rue Henri Huchard")
    private String address;

    @NotBlank
    @Schema(description = "Ville", example = "Paris", requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    @Size(max = 20)
    @Schema(description = "Code postal", example = "75018")
    private String postalCode;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    @JsonProperty("latitude")
    @Schema(description = "Latitude géographique", example = "48.89899", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    @JsonProperty("longitude")
    @Schema(description = "Longitude géographique", example = "2.33194", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double longitude;

    @Size(max = 30)
    @Schema(description = "Numéro de téléphone de contact", example = "0147254094")
    private String phoneNumber;

    @PositiveOrZero
    @Schema(description = "Nombre total de lits dans l'établissement", example = "500")
    private Integer totalBeds;

    /**
     * Identifiants des spécialités à associer lors de la création.
     */
    @Schema(description = "Liste des IDs des spécialités médicales supportées par l'hôpital", example = "[8, 9]")
    private List<@NotNull Long> specialtyIds;
}
