package com.medhead.bedallocation.controller;

import com.medhead.bedallocation.dto.UserDTO;
import com.medhead.bedallocation.dto.UserRegistrationDTO;
import com.medhead.bedallocation.security.AuthenticationRequest;
import com.medhead.bedallocation.security.AuthenticationResponse;
import com.medhead.bedallocation.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur d'authentification exposant les endpoints de login et d'inscription.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Authentifie un utilisateur et retourne un JWT.
     */
    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user",
        description = "Authenticates user with username/password and returns a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authenticated",
            content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content)
    })
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        log.info("Tentative de connexion: username={}", request.getUsername());
        AuthenticationResponse response = authenticationService.authenticate(request);

        // En-têtes CORS: gérés globalement via CorsConfig; ici on laisse la place à des en-têtes additionnels si besoin
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    /**
     * Inscrit un nouvel utilisateur et retourne ses informations (sans mot de passe).
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register user",
        description = "Registers a new user after validation and returns created user data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content)
    })
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserRegistrationDTO request) {
        log.info("Tentative d'inscription: username={}, email={}", request.getUsername(), request.getEmail());
        UserDTO created = authenticationService.register(request);

        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(created, headers, HttpStatus.CREATED);
    }
}
