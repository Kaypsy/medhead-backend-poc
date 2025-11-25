package com.medhead.bedallocation.dto;

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
public class HospitalDTO {

    @NotNull
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 255)
    private String address;

    @NotBlank
    private String city;

    @Size(max = 20)
    private String postalCode;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @Size(max = 30)
    private String phoneNumber;

    @PositiveOrZero
    private Integer totalBeds;

    /**
     * Valeur calculée par l'entité à partir de la liste des lits.
     * Présente ici à titre informatif, en lecture seule côté API.
     */
    @PositiveOrZero
    private Integer availableBeds;

    @NotNull
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Identifiants des spécialités associées. */
    @NotNull
    private List<Long> specialtyIds;
}
