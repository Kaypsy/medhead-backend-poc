package com.medhead.bedallocation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medhead.bedallocation.repository.UserRepository;
import com.medhead.bedallocation.security.AuthenticationRequest;
import com.medhead.bedallocation.dto.UserRegistrationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties", properties = {
        "spring.profiles.active=test"
})
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/auth/register - 201 success")
    void register_success_created201() throws Exception {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .username("john_doe")
                .email("john.doe@test.local")
                .password("StrongP@ssw0rd")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john.doe@test.local"));
    }

    @Test
    @DisplayName("POST /api/auth/register - 409 duplicate")
    void register_duplicate_conflict409() throws Exception {
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .username("dup_user")
                .email("dup@test.local")
                .password("StrongP@ssw0rd")
                .build();

        // 1ère inscription OK
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // 2ème inscription avec même username ou email -> 409
        UserRegistrationDTO duplicate = UserRegistrationDTO.builder()
                .username("dup_user")
                .email("dup2@test.local")
                .password("StrongP@ssw0rd")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/login - 200 avec token")
    void login_success_ok200_returnsToken() throws Exception {
        // Créer un utilisateur via l'endpoint register
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .username("login_user")
                .email("login.user@test.local")
                .password("StrongP@ssw0rd")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        AuthenticationRequest req = AuthenticationRequest.builder()
                .username("login_user")
                .password("StrongP@ssw0rd")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.username").value("login_user"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 401 bad credentials")
    void login_badCredentials_unauthorized401() throws Exception {
        // Créer un utilisateur via register
        UserRegistrationDTO dto = UserRegistrationDTO.builder()
                .username("badcred_user")
                .email("badcred.user@test.local")
                .password("StrongP@ssw0rd")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        AuthenticationRequest bad = AuthenticationRequest.builder()
                .username("badcred_user")
                .password("WrongPassword")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isUnauthorized());
    }
}
