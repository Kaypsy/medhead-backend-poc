package com.medhead.bedallocation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medhead.bedallocation.dto.EmergencyRequestDTO;
import com.medhead.bedallocation.dto.EmergencyResponseDTO;
import com.medhead.bedallocation.dto.HospitalSummaryDTO;
import com.medhead.bedallocation.dto.SpecialtyDTO;
import com.medhead.bedallocation.service.EmergencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmergencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmergencyService emergencyService;

    @Test
    void testAllocate_Success() throws Exception {
        // Given
        EmergencyRequestDTO request = EmergencyRequestDTO.builder()
                .latitude(48.8906)
                .longitude(2.1578)
                .specialtyCode("CARDIO")
                .build();

        EmergencyResponseDTO response = EmergencyResponseDTO.builder()
                .hospital(HospitalSummaryDTO.builder()
                        .id(1L)
                        .name("Hôpital Henri Mondor")
                        .city("Créteil")
                        .latitude(48.8027)
                        .longitude(2.4442)
                        .availableBeds(3)
                        .build())
                .specialty(SpecialtyDTO.builder().code("CARDIO").name("Cardiologie").build())
                .availableBeds(3)
                .distanceKm(15.2)
                .estimatedTimeMinutes(18)
                .build();

        when(emergencyService.allocate(any(EmergencyRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/emergency/allocate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hospital.name").value("Hôpital Henri Mondor"))
                .andExpect(jsonPath("$.distanceKm").value(15.2))
                .andExpect(jsonPath("$.estimatedTimeMinutes").value(18));
    }

    @Test
    void testAllocate_InvalidRequest() throws Exception {
        // Given (missing specialtyCode)
        EmergencyRequestDTO request = EmergencyRequestDTO.builder()
                .latitude(48.8906)
                .longitude(2.1578)
                .build();

        // When & Then
        mockMvc.perform(post("/api/emergency/allocate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
