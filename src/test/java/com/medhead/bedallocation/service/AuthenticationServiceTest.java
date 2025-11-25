package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.UserDTO;
import com.medhead.bedallocation.dto.UserRegistrationDTO;
import com.medhead.bedallocation.mapper.UserMapper;
import com.medhead.bedallocation.model.User;
import com.medhead.bedallocation.repository.UserRepository;
import com.medhead.bedallocation.security.AuthenticationRequest;
import com.medhead.bedallocation.security.AuthenticationResponse;
import com.medhead.bedallocation.security.JwtTokenProvider;
import com.medhead.bedallocation.service.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    // Utilisera une vraie instance configurée dans setUp() pour éviter les problèmes de mock avec JDK 23
    private JwtTokenProvider jwtTokenProvider;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;

    private AuthenticationServiceImpl service;

    private AuthenticationRequest authReq;

    @BeforeEach
    void setUp() {
        authReq = new AuthenticationRequest();
        authReq.setUsername("john");
        authReq.setPassword("pass");

        // Crée une vraie instance de JwtTokenProvider avec propriétés simples
        com.medhead.bedallocation.security.JwtProperties props = new com.medhead.bedallocation.security.JwtProperties();
        props.setSecret("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF"); // 64 hex chars ~ 256 bits
        props.setExpiration(3600000L); // 1h
        jwtTokenProvider = new JwtTokenProvider(props);

        // Instancier explicitement le service avec les dépendances (mocks + provider réel)
        service = new AuthenticationServiceImpl(authenticationManager, jwtTokenProvider, userRepository, passwordEncoder, userMapper);
    }

    // authenticate_success
    @Test
    void authenticate_validCredentials_returnsToken() {
        // Given
        org.springframework.security.core.Authentication authentication =
                new UsernamePasswordAuthenticationToken("john", "pass", java.util.Collections.emptyList());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        // Pas de mock: le provider réel générera un token

        // When
        AuthenticationResponse resp = service.authenticate(authReq);

        // Then
        assertThat(resp.getToken()).isNotBlank(); // assertion principale
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    // authenticate_failure
    @Test
    void authenticate_badCredentials_throwsException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        // When-Then
        assertThatThrownBy(() -> service.authenticate(authReq))
                .isInstanceOf(BadCredentialsException.class);
        // Pas d'appel vérifiable du provider réel ici
    }

    // register_success
    @Test
    void register_validPayload_returnsUserDto() {
        // Given
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .username("john")
                .password("pass")
                .email("john@example.com")
                .build();

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        User saved = new User();
        saved.setId(10L);
        saved.setUsername("john");
        saved.setEmail("john@example.com");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDTO mapped = UserDTO.builder().id(10L).username("john").email("john@example.com").build();
        when(userMapper.toDto(saved)).thenReturn(mapped);

        // When
        UserDTO result = service.register(dto);

        // Then
        assertThat(result.getUsername()).isEqualTo("john"); // assertion principale
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(saved);
    }

    // register_duplicate username
    @Test
    void register_existingUsername_throwsBadRequest() {
        // Given
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .username("john")
                .password("pass")
                .email("john@example.com")
                .build();
        when(userRepository.existsByUsername("john")).thenReturn(true);

        // When-Then
        assertThatThrownBy(() -> service.register(dto))
                .isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).save(any());
    }
}
