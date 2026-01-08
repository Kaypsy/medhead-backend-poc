package com.medhead.bedallocation.controller;

import com.medhead.bedallocation.dto.HospitalCreateDTO;
import com.medhead.bedallocation.dto.HospitalDTO;
import com.medhead.bedallocation.dto.HospitalSummaryDTO;
import com.medhead.bedallocation.dto.HospitalUpdateDTO;
import com.medhead.bedallocation.exception.ErrorResponse;
import com.medhead.bedallocation.service.HospitalService;
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
 * REST Controller pour la gestion des hôpitaux.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hospitals")
@Tag(name = "Hospitals", description = "Endpoints de gestion et de recherche des hôpitaux")
public class HospitalController {

    private final HospitalService hospitalService;

    // ------------- Utils -------------
    private Page<HospitalSummaryDTO> toPage(List<HospitalSummaryDTO> all, Pageable pageable) {
        if (all == null || all.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        int pageSize = pageable.getPageSize();
        int current = pageable.getPageNumber();
        int start = (int) Math.min((long) current * pageSize, all.size());
        int end = (int) Math.min(start + pageSize, all.size());
        List<HospitalSummaryDTO> content = start > end ? Collections.emptyList() : all.subList(start, end);
        return new PageImpl<>(content, pageable, all.size());
    }

    // ------------- GET: liste avec pagination -------------
    @GetMapping
    @Operation(
        summary = "Lister tous les hôpitaux",
        description = "Retourne la liste paginée des hôpitaux (résumé). Pagination standard Spring via Pageable"
    )
    public ResponseEntity<Page<HospitalSummaryDTO>> getAll(@Parameter(description = "Paramètres de pagination") Pageable pageable) {
        log.debug("[HospitalController] GET /api/hospitals page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        List<HospitalSummaryDTO> list = hospitalService.findAll();
        return ResponseEntity.ok(toPage(list, pageable));
    }

    // ------------- GET: détail par id -------------
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un hôpital par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Hôpital trouvé", content = @Content(schema = @Schema(implementation = HospitalDTO.class))),
        @ApiResponse(responseCode = "404", description = "Hôpital introuvable", content = @Content)
    })
    public ResponseEntity<HospitalDTO> getById(@PathVariable("id") Long id) {
        log.debug("[HospitalController] GET /api/hospitals/{}", id);
        HospitalDTO dto = hospitalService.findById(id);
        return ResponseEntity.ok(dto);
    }

    // ------------- GET: par ville -------------
    @GetMapping("/city/{city}")
    @Operation(summary = "Lister les hôpitaux par ville")
    public ResponseEntity<Page<HospitalSummaryDTO>> getByCity(@PathVariable("city") @NotBlank String city,
                                                              @Parameter(description = "Paramètres de pagination") Pageable pageable) {
        log.debug("[HospitalController] GET /api/hospitals/city/{} page={} size={}", city, pageable.getPageNumber(), pageable.getPageSize());
        List<HospitalSummaryDTO> list = hospitalService.findByCity(city);
        return ResponseEntity.ok(toPage(list, pageable));
    }

    // ------------- GET: par spécialité -------------
    @GetMapping("/specialty/{code}")
    @Operation(summary = "Lister les hôpitaux par code de spécialité")
    public ResponseEntity<Page<HospitalSummaryDTO>> getBySpecialty(@PathVariable("code") @NotBlank String code,
                                                                   @Parameter(description = "Paramètres de pagination") Pageable pageable) {
        log.debug("[HospitalController] GET /api/hospitals/specialty/{} page={} size={}", code, pageable.getPageNumber(), pageable.getPageSize());
        List<HospitalSummaryDTO> list = hospitalService.findBySpecialtyCode(code);
        return ResponseEntity.ok(toPage(list, pageable));
    }

    // ------------- GET: plus proches avec disponibilité -------------
    @GetMapping("/search/nearest")
    @Operation(summary = "Rechercher les hôpitaux les plus proches avec disponibilité",
        description = "Recherche par coordonnées (lat, lon) et spécialité. Paramètre limit optionnel (par défaut 10)")
    public ResponseEntity<List<HospitalSummaryDTO>> searchNearest(
            @RequestParam("lat") @Parameter(description = "Latitude", example = "48.8566") @NotNull Double lat,
            @RequestParam("lon") @Parameter(description = "Longitude", example = "2.3522") @NotNull Double lon,
            @RequestParam("specialtyCode") @Parameter(description = "Code de la spécialité", example = "CARD") @NotBlank String specialtyCode,
            @RequestParam(value = "limit", required = false) @Parameter(description = "Nombre maximum de résultats", example = "5") @Positive Integer limit
    ) {
        int effectiveLimit = (limit == null || limit <= 0) ? 10 : limit;
        log.info("[HospitalController] GET /api/hospitals/search/nearest lat={}, lon={}, specialtyCode={}, limit={}", lat, lon, specialtyCode, effectiveLimit);
        List<HospitalSummaryDTO> list = hospitalService.findNearestHospitalsWithAvailability(lat, lon, specialtyCode, effectiveLimit);
        return ResponseEntity.ok(list);
    }

    // ------------- POST: créer (ADMIN) -------------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un hôpital (ADMIN)", 
               description = "Permet de créer un nouvel hôpital. Nécessite le rôle ROLE_ADMIN. " +
                             "Vérifie l'unicité du nom et la validité des coordonnées géographiques.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Hôpital créé avec succès", 
                     content = @Content(schema = @Schema(implementation = HospitalDTO.class))),
        @ApiResponse(responseCode = "400", description = "Données d'entrée invalides (ex: coordonnées hors bornes, champs obligatoires manquants)", 
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentification requise", 
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Privilèges insuffisants (ADMIN requis)", 
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflit : un hôpital avec ce nom existe déjà", 
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", 
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<HospitalDTO> create(@Valid @RequestBody HospitalCreateDTO request) {
        log.info("[HospitalController] POST /api/hospitals - création: name={}, city={}", request.getName(), request.getCity());
        HospitalDTO created = hospitalService.create(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // ------------- PUT: mettre à jour (ADMIN) -------------
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un hôpital (ADMIN)")
    public ResponseEntity<HospitalDTO> update(@PathVariable("id") Long id, @Valid @RequestBody HospitalUpdateDTO request) {
        log.info("[HospitalController] PUT /api/hospitals/{} - mise à jour", id);
        HospitalDTO updated = hospitalService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    // ------------- DELETE: supprimer (ADMIN) -------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un hôpital (ADMIN)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Supprimé"),
        @ApiResponse(responseCode = "404", description = "Introuvable", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        log.warn("[HospitalController] DELETE /api/hospitals/{}", id);
        hospitalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ------------- POST: ajouter spécialité (ADMIN) -------------
    @PostMapping("/{id}/specialties/{specialtyId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ajouter une spécialité à un hôpital (ADMIN)")
    public ResponseEntity<HospitalDTO> addSpecialty(@PathVariable("id") Long hospitalId,
                                                    @PathVariable("specialtyId") Long specialtyId) {
        log.info("[HospitalController] POST /api/hospitals/{}/specialties/{} - ajout", hospitalId, specialtyId);
        HospitalDTO dto = hospitalService.addSpecialtyToHospital(hospitalId, specialtyId);
        return ResponseEntity.ok(dto);
    }

    // ------------- DELETE: retirer spécialité (ADMIN) -------------
    @DeleteMapping("/{id}/specialties/{specialtyId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retirer une spécialité d'un hôpital (ADMIN)")
    public ResponseEntity<HospitalDTO> removeSpecialty(@PathVariable("id") Long hospitalId,
                                                       @PathVariable("specialtyId") Long specialtyId) {
        log.info("[HospitalController] DELETE /api/hospitals/{}/specialties/{} - retrait", hospitalId, specialtyId);
        HospitalDTO dto = hospitalService.removeSpecialtyFromHospital(hospitalId, specialtyId);
        return ResponseEntity.ok(dto);
    }
}
