package com.medhead.bedallocation.repository;

import com.medhead.bedallocation.model.Bed;
import com.medhead.bedallocation.model.enums.BedStatus;
import com.medhead.bedallocation.repository.BedRepository.GeoAvailabilityProjection;
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
class BedRepositoryTest {

    @Autowired
    private BedRepository bedRepository;

    @Test
    @DisplayName("findByHospitalIdAndStatus doit retourner les lits par hôpital et statut")
    void testFindByHospitalIdAndStatus() {
        List<Bed> beds = bedRepository.findByHospitalIdAndStatus(10L, BedStatus.AVAILABLE);
        assertThat(beds).hasSize(2);
        assertThat(beds).extracting(Bed::getBedNumber).containsExactlyInAnyOrder("A-101", "A-102");
    }

    @Test
    @DisplayName("countByHospitalIdAndStatus doit compter les lits par hôpital et statut")
    void testCountByHospitalIdAndStatus() {
        long count = bedRepository.countByHospitalIdAndStatus(11L, BedStatus.AVAILABLE);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("findByStatusAndSpecialty_Code doit retourner les lits via jointures")
    void testFindByStatusAndSpecialtyCode() {
        List<Bed> beds = bedRepository.findByStatusAndSpecialty_Code(BedStatus.AVAILABLE, "CARD");
        assertThat(beds).hasSize(3);
        Set<String> numbers = beds.stream().map(Bed::getBedNumber).collect(Collectors.toSet());
        assertThat(numbers).containsExactlyInAnyOrder("A-101", "A-102", "C-301");
    }

    @Test
    @DisplayName("Requête native proximité: lits disponibles par spécialité et localisation")
    void testFindAvailableBedsBySpecialtyAndLocation() {
        double latParis = 48.8566;
        double lonParis = 2.3522;
        double radiusKm = 6.0; // couvre H10 et H11
        List<GeoAvailabilityProjection> res = bedRepository.findAvailableBedsBySpecialtyAndLocation("CARD", latParis, lonParis, radiusKm);
        assertThat(res).isNotEmpty();
        // Devrait contenir les deux hôpitaux parisiens actifs
        assertThat(res).extracting(GeoAvailabilityProjection::getHospitalId).contains(10L, 11L);
        // Vérifier les agrégats
        assertThat(res).anySatisfy(p -> {
            if (p.getHospitalId() == 10L) {
                assertThat(p.getAvailableBeds()).isEqualTo(2L);
            }
        });
        assertThat(res).anySatisfy(p -> {
            if (p.getHospitalId() == 11L) {
                assertThat(p.getAvailableBeds()).isEqualTo(1L);
            }
        });
        // Distances positives et tri croissant
        double prev = -1;
        for (GeoAvailabilityProjection p : res) {
            assertThat(p.getDistanceKm()).isNotNull();
            assertThat(p.getDistanceKm()).isGreaterThanOrEqualTo(0.0);
            if (prev >= 0) {
                assertThat(p.getDistanceKm()).isGreaterThanOrEqualTo(prev);
            }
            prev = p.getDistanceKm();
        }
    }
}
