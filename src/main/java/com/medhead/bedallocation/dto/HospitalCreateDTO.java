package com.medhead.bedallocation.dto;

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
public class HospitalCreateDTO {

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
     * Identifiants des spécialités à associer lors de la création.
     */
    private List<@NotNull Long> specialtyIds;
}
