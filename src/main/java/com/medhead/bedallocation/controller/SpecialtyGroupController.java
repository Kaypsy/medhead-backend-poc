package com.medhead.bedallocation.controller;

import com.medhead.bedallocation.dto.SpecialtyGroupDTO;
import com.medhead.bedallocation.service.SpecialtyGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/specialty-groups")
@Tag(name = "Specialty Groups", description = "Endpoints de gestion des groupes de spécialités")
public class SpecialtyGroupController {

    private final SpecialtyGroupService specialtyGroupService;

    // GET all
    @GetMapping
    @Operation(summary = "Lister tous les groupes de spécialités")
    public ResponseEntity<List<SpecialtyGroupDTO>> getAll() {
        log.debug("[SpecialtyGroupController] GET /api/specialty-groups");
        return ResponseEntity.ok(specialtyGroupService.findAll());
    }

    // GET by id
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un groupe par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groupe trouvé", content = @Content(schema = @Schema(implementation = SpecialtyGroupDTO.class))),
            @ApiResponse(responseCode = "404", description = "Groupe introuvable", content = @Content)
    })
    public ResponseEntity<SpecialtyGroupDTO> getById(@PathVariable("id") Long id) {
        log.debug("[SpecialtyGroupController] GET /api/specialty-groups/{}", id);
        return ResponseEntity.ok(specialtyGroupService.findById(id));
    }

    // GET by code
    @GetMapping("/code/{code}")
    @Operation(summary = "Obtenir un groupe par code")
    public ResponseEntity<SpecialtyGroupDTO> getByCode(@PathVariable("code") @NotBlank String code) {
        log.debug("[SpecialtyGroupController] GET /api/specialty-groups/code/{}", code);
        return ResponseEntity.ok(specialtyGroupService.findByCode(code));
    }

    // POST create (ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un groupe (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Groupe créé", content = @Content(schema = @Schema(implementation = SpecialtyGroupDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<SpecialtyGroupDTO> create(@Valid @RequestBody SpecialtyGroupDTO request) {
        log.info("[SpecialtyGroupController] POST /api/specialty-groups - création: code={}, name={}", request.getCode(), request.getName());
        SpecialtyGroupDTO created = specialtyGroupService.create(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // PUT update (ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un groupe (ADMIN)")
    public ResponseEntity<SpecialtyGroupDTO> update(@PathVariable("id") Long id,
                                                    @Valid @RequestBody SpecialtyGroupDTO request) {
        log.info("[SpecialtyGroupController] PUT /api/specialty-groups/{} - mise à jour", id);
        return ResponseEntity.ok(specialtyGroupService.update(id, request));
    }

    // DELETE (ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un groupe (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supprimé"),
            @ApiResponse(responseCode = "404", description = "Introuvable", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        log.warn("[SpecialtyGroupController] DELETE /api/specialty-groups/{}", id);
        specialtyGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
