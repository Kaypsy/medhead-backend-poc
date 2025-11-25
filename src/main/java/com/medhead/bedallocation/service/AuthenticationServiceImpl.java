package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.UserDTO;
import com.medhead.bedallocation.dto.UserRegistrationDTO;
import com.medhead.bedallocation.mapper.UserMapper;
import com.medhead.bedallocation.exception.DuplicateResourceException;
import com.medhead.bedallocation.model.User;
import com.medhead.bedallocation.repository.UserRepository;
import com.medhead.bedallocation.security.AuthenticationRequest;
import com.medhead.bedallocation.security.AuthenticationResponse;
import com.medhead.bedallocation.security.JwtTokenProvider;
import com.medhead.bedallocation.service.exception.BadRequestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new BadRequestException("Username et mot de passe sont requis");
        }

        String username = request.getUsername();
        log.info("Tentative d'authentification pour l'utilisateur: {}", username);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );

            if (authentication.isAuthenticated()) {
                String token = jwtTokenProvider.generateToken(username);
                log.info("Authentification réussie pour l'utilisateur: {}", username);
                return AuthenticationResponse.builder()
                        .token(token)
                        .username(username)
                        .build();
            } else {
                log.warn("Authentification échouée (non authentifié) pour l'utilisateur: {}", username);
                throw new BadCredentialsException("Identifiants invalides");
            }
        } catch (BadCredentialsException e) {
            log.warn("Mauvais identifiants pour l'utilisateur: {}", username);
            throw e;
        }
    }

    @Override
    @Transactional
    public UserDTO register(UserRegistrationDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Payload d'inscription manquant");
        }
        if (!StringUtils.hasText(dto.getUsername())) {
            throw new BadRequestException("Le nom d'utilisateur est requis");
        }
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BadRequestException("Le mot de passe est requis");
        }
        if (!StringUtils.hasText(dto.getEmail())) {
            throw new BadRequestException("L'email est requis");
        }

        String username = dto.getUsername().trim();
        String email = dto.getEmail().trim().toLowerCase();

        // Contraintes d'unicité
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Le nom d'utilisateur existe déjà");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("L'email est déjà utilisé");
        }

        // Création de l'entité utilisateur
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles("ROLE_USER");
        user.setIsActive(true);

        user = userRepository.save(user);
        log.info("Nouvel utilisateur enregistré: {} (id={})", user.getUsername(), user.getId());

        return userMapper.toDto(user);
    }
}
