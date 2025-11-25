package com.medhead.bedallocation.repository;

import com.medhead.bedallocation.repository.HospitalRepository.HospitalAvailabilityProjection;
import com.medhead.bedallocation.model.Hospital;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = RepositoryTestConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Sql(scripts = "classpath:test-data.sql")
class HospitalRepositoryTest {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Test
    @DisplayName("findByCity doit retourner les hôpitaux d'une ville")
    void testFindByCity() {
        List<Hospital> paris = hospitalRepository.findByCity("Paris");
        assertThat(paris).hasSize(2);
        Set<Long> ids = paris.stream().map(Hospital::getId).collect(Collectors.toSet());
        assertThat(ids).containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    @DisplayName("findByIsActiveTrue doit retourner uniquement les hôpitaux actifs")
    void testFindByIsActiveTrue() {
        List<Hospital> actives = hospitalRepository.findByIsActiveTrue();
        assertThat(actives).extracting(Hospital::getId).containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    @DisplayName("findBySpecialties_Code doit retourner les hôpitaux ayant la spécialité par code")
    void testFindBySpecialtiesCode() {
        List<Hospital> card = hospitalRepository.findBySpecialties_Code("CARD");
        assertThat(card).extracting(Hospital::getId).containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    @DisplayName("Projection: hôpitaux actifs avec lits disponibles par spécialité (CARD)")
    void testCustomProjectionAvailableBedsBySpecialty_CARD() {
        List<HospitalAvailabilityProjection> res = hospitalRepository.findActiveHospitalsWithAvailableBedsBySpecialty("CARD");
        assertThat(res).hasSize(2);
        // H10 => 2 beds, H11 => 1 bed
        assertThat(res).anySatisfy(p -> {
            if (p.getId() == 10L) {
                assertThat(p.getAvailableBeds()).isEqualTo(2L);
                assertThat(p.getCity()).isEqualTo("Paris");
            }
        });
        assertThat(res).anySatisfy(p -> {
            if (p.getId() == 11L) {
                assertThat(p.getAvailableBeds()).isEqualTo(1L);
                assertThat(p.getCity()).isEqualTo("Paris");
            }
        });
    }

    @Test
    @DisplayName("Projection: aucune remontée si aucune dispo (NEUR seulement maintenance)")
    void testCustomProjectionAvailableBedsBySpecialty_NEUR() {
        List<HospitalAvailabilityProjection> res = hospitalRepository.findActiveHospitalsWithAvailableBedsBySpecialty("NEUR");
        // H10 NEUR: maintenance -> 0 dispo; H12 NEUR: dispo mais hôpital inactif -> exclu
        assertThat(res).isEmpty();
    }
}
