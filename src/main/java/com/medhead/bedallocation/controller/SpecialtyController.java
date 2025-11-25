package com.medhead.bedallocation.controller;

import com.medhead.bedallocation.dto.*;
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
 * REST Controller pour la gestion des spécialités médicales.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/specialties")
@Tag(name = "Specialties", description = "Endpoints de gestion et de recherche des spécialités")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    // ------------ Utils: convertir List -> Page ------------
    private Page<SpecialtySummaryDTO> toPage(List<SpecialtySummaryDTO> list, Pageable pageable) {
        if (list == null || list.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        int pageSize = pageable.getPageSize();
        int current = pageable.getPageNumber();
        int start = Math.min(current * pageSize, list.size());
        int end = Math.min(start + pageSize, list.size());
        List<SpecialtySummaryDTO> content = start > end ? Collections.emptyList() : list.subList(start, end);
        return new PageImpl<>(content, pageable, list.size());
    }

    // ------------- GET: liste avec pagination -------------
    @GetMapping
    @Operation(summary = "Lister toutes les spécialités")
    public ResponseEntity<Page<SpecialtySummaryDTO>> getAll(@Parameter(description = "Paramètres de pagination") Pageable pageable) {
        log.debug("[SpecialtyController] GET /api/specialties page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        List<SpecialtySummaryDTO> list = specialtyService.findAll();
        return ResponseEntity.ok(toPage(list, pageable));
    }

    // ------------- GET: détail par id -------------
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une spécialité par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Spécialité trouvée", content = @Content(schema = @Schema(implementation = SpecialtyDTO.class))),
            @ApiResponse(responseCode = "404", description = "Spécialité introuvable", content = @Content)
    })
    public ResponseEntity<SpecialtyDTO> getById(@PathVariable("id") Long id) {
        log.debug("[SpecialtyController] GET /api/specialties/{}", id);
        return ResponseEntity.ok(specialtyService.findById(id));
    }

    // ------------- GET: par code -------------
    @GetMapping("/code/{code}")
    @Operation(summary = "Rechercher une spécialité par code")
    public ResponseEntity<SpecialtyDTO> getByCode(@PathVariable("code") @NotBlank String code) {
        log.debug("[SpecialtyController] GET /api/specialties/code/{}", code);
        return ResponseEntity.ok(specialtyService.findByCode(code));
    }

    // ------------- GET: par groupe -------------
    @GetMapping("/group/{group}")
    @Operation(summary = "Lister les spécialités par groupe")
    public ResponseEntity<Page<SpecialtySummaryDTO>> getByGroup(@PathVariable("group") @NotBlank String group,
                                                                @Parameter(description = "Paramètres de pagination") Pageable pageable) {
        log.debug("[SpecialtyController] GET /api/specialties/group/{} page={} size={}", group, pageable.getPageNumber(), pageable.getPageSize());
        List<SpecialtySummaryDTO> list = specialtyService.findByGroup(group);
        return ResponseEntity.ok(toPage(list, pageable));
    }

    // ------------- POST: créer (ADMIN) -------------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une spécialité (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Spécialité créée", content = @Content(schema = @Schema(implementation = SpecialtyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<SpecialtyDTO> create(@Valid @RequestBody SpecialtyCreateDTO request) {
        log.info("[SpecialtyController] POST /api/specialties - création: code={}, name={}", request.getCode(), request.getName());
        SpecialtyDTO created = specialtyService.create(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // ------------- PUT: mettre à jour (ADMIN) -------------
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour une spécialité (ADMIN)")
    public ResponseEntity<SpecialtyDTO> update(@PathVariable("id") Long id, @Valid @RequestBody SpecialtyUpdateDTO request) {
        log.info("[SpecialtyController] PUT /api/specialties/{} - mise à jour", id);
        return ResponseEntity.ok(specialtyService.update(id, request));
    }

    // ------------- DELETE: supprimer (ADMIN) -------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une spécialité (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supprimé"),
            @ApiResponse(responseCode = "404", description = "Introuvable", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        log.warn("[SpecialtyController] DELETE /api/specialties/{}", id);
        specialtyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
