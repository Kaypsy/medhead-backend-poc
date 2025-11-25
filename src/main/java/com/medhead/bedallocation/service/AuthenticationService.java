package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.UserDTO;
import com.medhead.bedallocation.dto.UserRegistrationDTO;
import com.medhead.bedallocation.security.AuthenticationRequest;
import com.medhead.bedallocation.security.AuthenticationResponse;

/**
 * Service d'authentification et d'inscription des utilisateurs.
 */
public interface AuthenticationService {

    /**
     * Authentifie un utilisateur et génère un token JWT en cas de succès.
     *
     * @param request payload d'authentification (username/password)
     * @return réponse contenant le token JWT et des métadonnées
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);

    /**
     * Enregistre un nouvel utilisateur après validations et hashage du mot de passe.
     *
     * @param dto payload d'inscription
     * @return l'utilisateur créé (DTO sans mot de passe)
     */
    UserDTO register(UserRegistrationDTO dto);
}
