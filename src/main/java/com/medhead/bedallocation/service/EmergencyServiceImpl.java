package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.EmergencyRequestDTO;
import com.medhead.bedallocation.dto.EmergencyResponseDTO;
import com.medhead.bedallocation.dto.HospitalSummaryDTO;
import com.medhead.bedallocation.dto.SpecialtyDTO;
import com.medhead.bedallocation.service.exception.ResourceNotFoundException;
import com.medhead.bedallocation.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmergencyServiceImpl implements EmergencyService {

    private final HospitalService hospitalService;
    private final SpecialtyService specialtyService;

    @Override
    public EmergencyResponseDTO allocate(EmergencyRequestDTO request) {
        log.info("[EmergencyService] Allocation demandée pour la spécialité: {} à [{}, {}]",
                request.getSpecialtyCode(), request.getLatitude(), request.getLongitude());

        // 1. Vérifier que la spécialité existe
        SpecialtyDTO specialty = specialtyService.findByCode(request.getSpecialtyCode());

        // 2. Trouver les hôpitaux les plus proches avec lits disponibles (limite à 1 pour l'allocation directe)
        List<HospitalSummaryDTO> nearest = hospitalService.findNearestHospitalsWithAvailability(
                request.getLatitude(), request.getLongitude(), request.getSpecialtyCode(), 1
        );

        if (nearest.isEmpty()) {
            throw new ResourceNotFoundException("Aucun hôpital disponible pour la spécialité " + request.getSpecialtyCode());
        }

        HospitalSummaryDTO bestHospital = nearest.get(0);

        // 3. Calculer la distance et le temps estimés
        double distance = DistanceCalculator.haversineKm(
                request.getLatitude(), request.getLongitude(),
                bestHospital.getLatitude(), bestHospital.getLongitude()
        );
        int estimatedTime = DistanceCalculator.estimateTravelTimeMinutes(distance);

        log.info("[EmergencyService] Hôpital recommandé: {} ({} km, {} min)",
                bestHospital.getName(), String.format("%.2f", distance), estimatedTime);

        // 4. Construire la réponse
        return EmergencyResponseDTO.builder()
                .hospital(bestHospital)
                .specialty(specialty)
                .availableBeds(bestHospital.getAvailableBeds())
                .distanceKm(Math.round(distance * 100.0) / 100.0) // Arrondi à 2 décimales
                .estimatedTimeMinutes(estimatedTime)
                .build();
    }
}
