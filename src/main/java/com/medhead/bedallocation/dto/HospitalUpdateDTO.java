package com.medhead.bedallocation.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO utilisé pour la mise à jour d'un hôpital.
 * Contient uniquement des champs modifiables. Tous les champs sont optionnels
 * afin de permettre des mises à jour partielles (PATCH/PUT). Les contraintes
 * s'appliquent si les valeurs sont fournies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalUpdateDTO {

    @Size(max = 200)
    private String name;

    @Size(max = 255)
    private String address;

    private String city;

    @Size(max = 20)
    private String postalCode;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @Size(max = 30)
    private String phoneNumber;

    @PositiveOrZero
    private Integer totalBeds;

    private Boolean isActive;

    /**
     * Identifiants de spécialités à appliquer lors de la mise à jour.
     * La gestion d'association sera effectuée côté service.
     */
    private List<@NotNull Long> specialtyIds;
}
