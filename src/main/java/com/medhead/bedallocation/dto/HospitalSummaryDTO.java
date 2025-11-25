package com.medhead.bedallocation.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO léger et optimisé pour les listes et recherches.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalSummaryDTO {

    @NotNull
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    private String city;

    @PositiveOrZero
    private Integer availableBeds;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;
}
