package com.medhead.bedallocation.controller;

import com.medhead.bedallocation.dto.*;
import com.medhead.bedallocation.model.enums.BedStatus;
import com.medhead.bedallocation.service.BedService;
import com.medhead.bedallocation.service.SpecialtyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * REST Controller pour la gestion des lits d'hôpital.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beds")
@Tag(name = "Beds", description = "Endpoints de gestion et de recherche des lits")
public class BedController {

    private final BedService bedService;
    private final SpecialtyService specialtyService;

    // ------------- Utils -------------
    private Page<BedDTO> toPage(List<BedDTO> all, Pageable pageable) {
        if (all == null || all.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        int pageSize = pageable.getPageSize();
        int current = pageable.getPageNumber();
        int start = Math.min(current * pageSize, all.size());
        int end = Math.min(start + pageSize, all.size());
        List<BedDTO> content = start > end ? Collections.emptyList() : all.subList(start, end);
        return new PageImpl<>(content, pageable, all.size());
    }

    // ------------- GET: liste avec pagination -------------
    @GetMapping
    @Operation(summary = "Lister tous les lits")
    public ResponseEntity<Page<BedDTO>> getAll(@Parameter(description = "Paramètres de pagination") Pageable pageable) {
        log.debug("[BedController] GET /api/beds page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        List<BedDTO> list = bedService.findAll();
        return ResponseEntity.ok(toPage(list, pageable));
    }

    // ------------- GET: détail par id -------------
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un lit par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lit trouvé", content = @Content(schema = @Schema(implementation = BedDTO.class))),
            @ApiResponse(responseCode = "404", description = "Lit introuvable", content = @Content)
    })
    public ResponseEntity<BedDTO> getById(@PathVariable("id") Long id) {
        log.debug("[BedController] GET /api/beds/{}", id);
        return ResponseEntity.ok(bedService.findById(id));
    }

    // ------------- GET: lits disponibles d'un hôpital -------------
    @GetMapping("/hospital/{hospitalId}/available")
    @Operation(summary = "Lister les lits disponibles d'un hôpital")
    public ResponseEntity<List<BedAvailabilityDTO>> getAvailableByHospital(@PathVariable("hospitalId") Long hospitalId) {
        log.debug("[BedController] GET /api/beds/hospital/{}/available", hospitalId);
        return ResponseEntity.ok(bedService.findAvailableByHospital(hospitalId));
    }

    // ------------- GET: lits disponibles par spécialité (code) -------------
    @GetMapping("/specialty/{specialtyCode}/available")
    @Operation(summary = "Lister les lits disponibles par code de spécialité")
    public ResponseEntity<List<BedAvailabilityDTO>> getAvailableBySpecialty(@PathVariable("specialtyCode") @NotBlank String specialtyCode) {
        log.debug("[BedController] GET /api/beds/specialty/{}/available", specialtyCode);
        Long specialtyId = specialtyService.findByCode(specialtyCode).getId();
        return ResponseEntity.ok(bedService.findAvailableBySpecialty(specialtyId));
    }

    // ------------- GET: recherche urgence -------------
    @GetMapping("/emergency/search")
    @Operation(summary = "Recherche de lits disponibles pour une urgence",
            description = "Recherche par coordonnées (lat, lon) et code de spécialité")
    public ResponseEntity<List<BedAvailabilityDTO>> searchForEmergency(
            @RequestParam("lat") @Parameter(description = "Latitude", example = "48.8566") @NotNull Double lat,
            @RequestParam("lon") @Parameter(description = "Longitude", example = "2.3522") @NotNull Double lon,
            @RequestParam("specialtyCode") @Parameter(description = "Code de la spécialité", example = "CARD") @NotBlank String specialtyCode
    ) {
        log.info("[BedController] GET /api/beds/emergency/search lat={}, lon={}, specialtyCode={}", lat, lon, specialtyCode);
        List<BedAvailabilityDTO> list = bedService.findAvailableBedsForEmergency(specialtyCode, lat, lon);
        return ResponseEntity.ok(list);
    }

    // ------------- POST: créer (ADMIN) -------------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un lit (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lit créé", content = @Content(schema = @Schema(implementation = BedDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<BedDTO> create(@Valid @RequestBody BedCreateDTO request) {
        log.info("[BedController] POST /api/beds - création: hospitalId={}, specialtyId={}", request.getHospitalId(), request.getSpecialtyId());
        BedDTO created = bedService.create(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // ------------- PUT: mettre à jour (ADMIN) -------------
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un lit (ADMIN)")
    public ResponseEntity<BedDTO> update(@PathVariable("id") Long id, @Valid @RequestBody BedUpdateDTO request) {
        log.info("[BedController] PUT /api/beds/{} - mise à jour", id);
        BedDTO updated = bedService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    // ------------- PATCH: changer le statut (USER) -------------
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Changer le statut d'un lit (USER)")
    public ResponseEntity<BedDTO> changeStatus(@PathVariable("id") Long id, @Valid @RequestBody BedStatusUpdateDTO request) {
        log.info("[BedController] PATCH /api/beds/{}/status - newStatus={}", id, request.getStatus());
        BedDTO updated = bedService.updateBedStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    // ------------- POST: réserver (USER) -------------
    @PostMapping("/{id}/reserve")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Réserver un lit (USER)")
    public ResponseEntity<BedDTO> reserve(@PathVariable("id") Long id) {
        log.info("[BedController] POST /api/beds/{}/reserve", id);
        BedDTO updated = bedService.reserveBed(id);
        return ResponseEntity.ok(updated);
    }

    // ------------- POST: libérer (USER) -------------
    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Libérer un lit (USER)")
    public ResponseEntity<BedDTO> release(@PathVariable("id") Long id) {
        log.info("[BedController] POST /api/beds/{}/release", id);
        BedDTO updated = bedService.releaseBed(id);
        return ResponseEntity.ok(updated);
    }

    // ------------- DELETE: supprimer (ADMIN) -------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un lit (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supprimé"),
            @ApiResponse(responseCode = "404", description = "Introuvable", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        log.warn("[BedController] DELETE /api/beds/{}", id);
        bedService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
