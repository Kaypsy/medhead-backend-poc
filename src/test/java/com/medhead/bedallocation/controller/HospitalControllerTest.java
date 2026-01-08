package com.medhead.bedallocation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medhead.bedallocation.dto.HospitalCreateDTO;
import com.medhead.bedallocation.dto.HospitalUpdateDTO;
import com.medhead.bedallocation.model.Hospital;
import com.medhead.bedallocation.repository.HospitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.medhead.bedallocation.repository.SpecialtyRepository;
import com.medhead.bedallocation.repository.SpecialtyGroupRepository;
import com.medhead.bedallocation.model.Specialty;
import com.medhead.bedallocation.model.SpecialtyGroup;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties", properties = {
        "spring.profiles.active=test"
})
@Transactional
public class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private SpecialtyGroupRepository specialtyGroupRepository;

    private Long existingHospitalId;

    @BeforeEach
    void setup() {
        if (objectMapper == null) objectMapper = new ObjectMapper();

        // Préparer un hôpital minimal pour les tests GET/PUT/DELETE
        Hospital h = new Hospital();
        h.setName("Test Hospital");
        h.setCity("Paris");
        h.setLatitude(48.8566);
        h.setLongitude(2.3522);
        h.setIsActive(true);
        Hospital saved = hospitalRepository.save(h);
        existingHospitalId = saved.getId();
    }

    @Test
    @DisplayName("GET /api/hospitals - retourne 200 et liste")
    @WithMockUser(roles = {"USER"})
    void getAll_returns200AndList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/hospitals"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/hospitals/{id} - 200 et détails")
    @WithMockUser(roles = {"USER"})
    void getById_returns200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/hospitals/" + existingHospitalId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(existingHospitalId))
                .andExpect(jsonPath("$.name").value("Test Hospital"))
                .andExpect(jsonPath("$.city").value("Paris"));
    }

    @Test
    @DisplayName("GET /api/hospitals/{id} - 404 si inexistant")
    @WithMockUser(roles = {"USER"})
    void getById_returns404_whenNotFound() throws Exception {
        long notExisting = existingHospitalId + 99999;
        mockMvc.perform(MockMvcRequestBuilders.get("/api/hospitals/" + notExisting))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/hospitals - 201 avec ADMIN")
    @WithMockUser(roles = {"ADMIN"})
    void create_returns201_withAdmin() throws Exception {
        HospitalCreateDTO dto = HospitalCreateDTO.builder()
                .name("Created Hospital")
                .city("Lyon")
                .latitude(45.7640)
                .longitude(4.8357)
                .address("1 Rue Exemple")
                .postalCode("69000")
                .phoneNumber("0102030405")
                .totalBeds(100)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Created Hospital"))
                .andExpect(jsonPath("$.city").value("Lyon"));
    }

    @Test
    @DisplayName("POST /api/hospitals - 403 sans ADMIN")
    @WithMockUser(roles = {"USER"})
    void create_returns403_withoutAdmin() throws Exception {
        HospitalCreateDTO dto = HospitalCreateDTO.builder()
                .name("Forbidden Hospital")
                .city("Nice")
                .latitude(43.7102)
                .longitude(7.2620)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/hospitals/{id} - 200 avec ADMIN")
    @WithMockUser(roles = {"ADMIN"})
    void update_returns200_withAdmin() throws Exception {
        HospitalUpdateDTO dto = HospitalUpdateDTO.builder()
                .name("Updated Name")
                .city("Paris")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/hospitals/" + existingHospitalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingHospitalId))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @DisplayName("PUT /api/hospitals/{id} - 403 sans ADMIN")
    @WithMockUser(roles = {"USER"})
    void update_returns403_withoutAdmin() throws Exception {
        HospitalUpdateDTO dto = HospitalUpdateDTO.builder()
                .name("Should Fail")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/hospitals/" + existingHospitalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/hospitals/{id} - 204 avec ADMIN")
    @WithMockUser(roles = {"ADMIN"})
    void delete_returns204_withAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/hospitals/" + existingHospitalId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/hospitals/{id} - 403 sans ADMIN")
    @WithMockUser(roles = {"USER"})
    void delete_returns403_withoutAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/hospitals/" + existingHospitalId))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/hospitals - Reproduction erreur utilisateur")
    @WithMockUser(username = "admin@admin.com", roles = {"ADMIN"})
    void create_reproUserIssue() throws Exception {
        // S'assurer que la spécialité 8 existe pour le test
        SpecialtyGroup sg = new SpecialtyGroup();
        sg.setCode("REPRO_GP");
        sg.setName("Repro Group");
        sg = specialtyGroupRepository.save(sg);

        Specialty s = new Specialty();
        s.setId(8L); // On essaie de forcer l'ID 8, mais H2 IDENTITY pourrait l'ignorer
        s.setCode("REPRO_SPEC");
        s.setName("Repro Specialty");
        s.setSpecialtyGroup(sg);
        s = specialtyRepository.save(s);
        Long specId = s.getId();

        // Payload fourni par l'utilisateur (adapté pour l'ID réel si ID 8 n'est pas possible)
        String payload = """
            {
                "name":"Hôpital Bichat – Claude-Bernard",
                "address":"46 rue Henri Huchard",
                "city":"Paris",
                "postalCode":"75018",
                "totalBeds":500,
                "specialtyIds":[%d],
                "latitude":48.89899,
                "longitude":2.33194,
                "phoneNumber":"0147254094"
            }
            """.formatted(specId);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Hôpital Bichat – Claude-Bernard"))
                .andExpect(jsonPath("$.specialtyIds[0]").value(specId));
    }

    @Test
    @DisplayName("POST /api/hospitals - Reproduction erreur utilisateur V2")
    @WithMockUser(username = "lzephir", roles = {"ADMIN"})
    void create_reproUserIssue_v2() throws Exception {
        // S'assurer que les spécialités 8 et 9 existent pour le test
        SpecialtyGroup sg = new SpecialtyGroup();
        sg.setCode("REPRO_GP_2");
        sg.setName("Repro Group 2");
        sg = specialtyGroupRepository.save(sg);

        Specialty s8 = new Specialty();
        s8.setCode("REPRO_SPEC_8");
        s8.setName("Repro Specialty 8");
        s8.setSpecialtyGroup(sg);
        s8 = specialtyRepository.save(s8);
        Long specId8 = s8.getId();

        Specialty s9 = new Specialty();
        s9.setCode("REPRO_SPEC_9");
        s9.setName("Repro Specialty 9");
        s9.setSpecialtyGroup(sg);
        s9 = specialtyRepository.save(s9);
        Long specId9 = s9.getId();

        // Payload fourni par l'utilisateur
        String payload = """
            {
                "name":"Hôpital Bichat – Claude-Bernard V2",
                "address":"46 rue Henri Huchard",
                "city":"Paris",
                "postalCode":"75018",
                "totalBeds":120,
                "specialtyIds":[%d, %d],
                "latitude":48.89899,
                "longitude":2.33194,
                "phoneNumber":"0147254094"
            }
            """.formatted(specId8, specId9);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Hôpital Bichat – Claude-Bernard V2"))
                .andExpect(jsonPath("$.specialtyIds").isArray())
                .andExpect(jsonPath("$.specialtyIds").value(org.hamcrest.Matchers.hasItems(specId8.intValue(), specId9.intValue())));
    }
}
