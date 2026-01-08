package com.medhead.bedallocation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyResponseDTO {

    private HospitalSummaryDTO hospital;
    private SpecialtyDTO specialty;
    private Integer availableBeds;
    private Double distanceKm;
    private Integer estimatedTimeMinutes;
}
