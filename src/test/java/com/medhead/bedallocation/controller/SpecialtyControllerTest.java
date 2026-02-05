package com.medhead.bedallocation.controller;

import com.medhead.bedallocation.model.Specialty;
import com.medhead.bedallocation.model.SpecialtyGroup;
import com.medhead.bedallocation.repository.SpecialtyGroupRepository;
import com.medhead.bedallocation.repository.SpecialtyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties", properties = {
        "spring.profiles.active=test"
})
@Transactional
public class SpecialtyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private SpecialtyGroupRepository specialtyGroupRepository;

    @BeforeEach
    void setup() {
        SpecialtyGroup group = new SpecialtyGroup();
        group.setCode("TEST_GRP");
        group.setName("Test Group");
        group.setIsActive(true);
        specialtyGroupRepository.save(group);

        Specialty s1 = new Specialty();
        s1.setCode("SPEC_ACTIVE");
        s1.setName("Active Specialty");
        s1.setIsActive(true);
        s1.setSpecialtyGroup(group);
        specialtyRepository.save(s1);

        Specialty s2 = new Specialty();
        s2.setCode("SPEC_INACTIVE");
        s2.setName("Inactive Specialty");
        s2.setIsActive(false);
        s2.setSpecialtyGroup(group);
        specialtyRepository.save(s2);
    }

    @Test
    @DisplayName("GET /api/specialties - doit inclure le champ isActive")
    @WithMockUser(roles = {"USER"})
    void getAll_shouldIncludeIsActive() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/specialties"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // Vérifie que isActive est présent et correct pour les deux spécialités
                .andExpect(jsonPath("$.content[?(@.code=='SPEC_ACTIVE')].isActive").value(true))
                .andExpect(jsonPath("$.content[?(@.code=='SPEC_INACTIVE')].isActive").value(false));
    }
}
