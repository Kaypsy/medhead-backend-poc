package com.medhead.bedallocation.mapper;

import com.medhead.bedallocation.dto.SpecialtySummaryDTO;
import com.medhead.bedallocation.model.Specialty;
import com.medhead.bedallocation.model.SpecialtyGroup;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class SpecialtyMapperTest {

    private final SpecialtyMapper mapper = Mappers.getMapper(SpecialtyMapper.class);

    @Test
    void shouldMapSpecialtyToSummaryDtoWithIsActive() {
        // Given
        SpecialtyGroup group = new SpecialtyGroup();
        group.setId(1L);
        group.setCode("GRP");
        group.setName("Group");

        Specialty specialty = new Specialty();
        specialty.setId(10L);
        specialty.setCode("SPEC");
        specialty.setName("Specialty Name");
        specialty.setIsActive(true);
        specialty.setSpecialtyGroup(group);

        // When
        SpecialtySummaryDTO dto = mapper.toSummaryDto(specialty);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(specialty.getId());
        assertThat(dto.getCode()).isEqualTo(specialty.getCode());
        assertThat(dto.getName()).isEqualTo(specialty.getName());
        assertThat(dto.getIsActive()).isTrue();
        assertThat(dto.getSpecialtyGroup()).isNotNull();
        assertThat(dto.getSpecialtyGroup().getCode()).isEqualTo(group.getCode());
    }

    @Test
    void shouldMapSpecialtyToSummaryDtoWithIsActiveFalse() {
        // Given
        Specialty specialty = new Specialty();
        specialty.setIsActive(false);
        // Minimal fields for summary
        specialty.setId(11L);
        specialty.setSpecialtyGroup(new SpecialtyGroup());

        // When
        SpecialtySummaryDTO dto = mapper.toSummaryDto(specialty);

        // Then
        assertThat(dto.getIsActive()).isFalse();
    }
}
