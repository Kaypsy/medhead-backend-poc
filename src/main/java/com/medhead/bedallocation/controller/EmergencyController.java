package com.medhead.bedallocation.controller;

import com.medhead.bedallocation.dto.EmergencyRequestDTO;
import com.medhead.bedallocation.dto.EmergencyResponseDTO;
import com.medhead.bedallocation.service.EmergencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller pour la gestion des allocations d'urgence.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emergency")
@Tag(name = "Emergency", description = "Endpoints pour l'allocation d'urgence des hôpitaux")
public class EmergencyController {

    private final EmergencyService emergencyService;

    @PostMapping("/allocate")
    @Operation(summary = "Allouer le meilleur hôpital disponible",
            description = "Recherche l'hôpital le plus proche avec des lits disponibles pour la spécialité demandée")
    public ResponseEntity<EmergencyResponseDTO> allocate(@Valid @RequestBody EmergencyRequestDTO request) {
        log.info("[EmergencyController] POST /api/emergency/allocate - specialtyCode={}", request.getSpecialtyCode());
        EmergencyResponseDTO response = emergencyService.allocate(request);
        return ResponseEntity.ok(response);
    }
}
