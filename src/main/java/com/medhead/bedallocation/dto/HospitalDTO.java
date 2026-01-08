package com.medhead.bedallocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO détaillé représentant un hôpital avec ses informations complètes.
 * Inclut la liste des IDs de spécialités et le nombre de lits disponibles (calculé côté entité).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Représentation complète d'un hôpital")
public class HospitalDTO {

    @NotNull
    @Schema(description = "Identifiant unique de l'hôpital", example = "1")
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Schema(description = "Nom de l'hôpital", example = "Hôpital Bichat – Claude-Bernard")
    private String name;

    @Size(max = 255)
    @Schema(description = "Adresse", example = "46 rue Henri Huchard")
    private String address;

    @NotBlank
    @Schema(description = "Ville", example = "Paris")
    private String city;

    @Size(max = 20)
    @Schema(description = "Code postal", example = "75018")
    private String postalCode;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    @Schema(description = "Latitude", example = "48.89899")
    private Double latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    @Schema(description = "Longitude", example = "2.33194")
    private Double longitude;

    @Size(max = 30)
    @Schema(description = "Téléphone", example = "0147254094")
    private String phoneNumber;

    @PositiveOrZero
    @Schema(description = "Nombre total de lits", example = "500")
    private Integer totalBeds;

    /**
     * Valeur calculée par l'entité à partir de la liste des lits.
     * Présente ici à titre informatif, en lecture seule côté API.
     */
    @PositiveOrZero
    @Schema(description = "Nombre de lits actuellement disponibles", example = "120")
    private Integer availableBeds;

    @NotNull
    @Schema(description = "Statut d'activation de l'hôpital", example = "true")
    private Boolean isActive;

    @Schema(description = "Date de création de l'enregistrement")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière mise à jour")
    private LocalDateTime updatedAt;

    /** Identifiants des spécialités associées. */
    @NotNull
    @Schema(description = "Liste des identifiants des spécialités médicales")
    private List<Long> specialtyIds;
}
