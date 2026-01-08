package com.medhead.bedallocation.repository;

import com.medhead.bedallocation.model.Specialty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = RepositoryTestConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Sql(scripts = "classpath:test-data.sql")
class SpecialtyRepositoryTest {

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Test
    @DisplayName("findByCode doit retourner la spécialité par code")
    void testFindByCode() {
        Optional<Specialty> opt = specialtyRepository.findByCode("CARD");
        assertThat(opt).isPresent();
        Specialty s = opt.get();
        assertThat(s.getName()).isEqualTo("Cardiologie");
        assertThat(s.getSpecialtyGroup().getCode()).isEqualTo("MED");
    }

    @Test
    @DisplayName("findBySpecialtyGroup doit retourner les spécialités du groupe")
    void testFindBySpecialtyGroup() {
        List<Specialty> meds = specialtyRepository.findBySpecialtyGroup("MED");
        assertThat(meds).hasSize(3);
        assertThat(meds).extracting(Specialty::getCode).containsExactlyInAnyOrder("CARD", "NEUR", "SPEC8");
    }
}
