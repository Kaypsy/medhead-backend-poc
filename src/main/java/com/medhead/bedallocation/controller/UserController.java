package com.medhead.bedallocation.controller;

import com.medhead.bedallocation.dto.UserCreateDTO;
import com.medhead.bedallocation.dto.UserDTO;
import com.medhead.bedallocation.dto.UserUpdateDTO;
import com.medhead.bedallocation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints de gestion des utilisateurs")
public class UserController {

    private final UserService userService;

    // Utils: List -> Page (même pattern que SpecialtyController)
    private Page<UserDTO> toPage(List<UserDTO> list, Pageable pageable) {
        if (list == null || list.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        int pageSize = pageable.getPageSize();
        int current = pageable.getPageNumber();
        int start = Math.min(current * pageSize, list.size());
        int end = Math.min(start + pageSize, list.size());
        List<UserDTO> content = start > end ? Collections.emptyList() : list.subList(start, end);
        return new PageImpl<>(content, pageable, list.size());
    }

    // GET: list
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister tous les utilisateurs (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste paginée des utilisateurs",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    public ResponseEntity<Page<UserDTO>> getAll(@Parameter(description = "Paramètres de pagination") Pageable pageable) {
        log.debug("[UserController] GET /api/users page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        List<UserDTO> list = userService.findAll();
        return ResponseEntity.ok(toPage(list, pageable));
    }

    // GET: by id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtenir un utilisateur par ID (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    public ResponseEntity<UserDTO> getById(@Parameter(description = "Identifiant technique de l'utilisateur", required = true)
                                           @PathVariable("id") Long id) {
        log.debug("[UserController] GET /api/users/{}", id);
        return ResponseEntity.ok(userService.findById(id));
    }

    // POST: create
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un utilisateur (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur créé", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserCreateDTO request) {
        log.info("[UserController] POST /api/users - création: username={}, email={}", request.getUsername(), request.getEmail());
        UserDTO created = userService.create(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // PUT: update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un utilisateur (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    public ResponseEntity<UserDTO> update(@Parameter(description = "Identifiant technique de l'utilisateur", required = true)
                                          @PathVariable("id") Long id,
                                          @Valid @RequestBody UserUpdateDTO request) {
        log.info("[UserController] PUT /api/users/{} - mise à jour", id);
        return ResponseEntity.ok(userService.update(id, request));
    }

    // DELETE: delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un utilisateur (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supprimé"),
            @ApiResponse(responseCode = "404", description = "Introuvable", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    public ResponseEntity<Void> delete(@Parameter(description = "Identifiant technique de l'utilisateur", required = true)
                                       @PathVariable("id") Long id) {
        log.warn("[UserController] DELETE /api/users/{}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
