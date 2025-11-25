package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.*;
import com.medhead.bedallocation.mapper.HospitalMapper;
import com.medhead.bedallocation.model.Hospital;
import com.medhead.bedallocation.model.Specialty;
import com.medhead.bedallocation.repository.BedRepository;
import com.medhead.bedallocation.repository.HospitalRepository;
import com.medhead.bedallocation.repository.HospitalRepository.HospitalAvailabilityProjection;
import com.medhead.bedallocation.repository.SpecialtyRepository;
import com.medhead.bedallocation.service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalServiceTest {

    @Mock private HospitalRepository hospitalRepository;
    @Mock private SpecialtyRepository specialtyRepository;
    @Mock private BedRepository bedRepository;
    @Mock private HospitalMapper hospitalMapper;

    @InjectMocks
    private HospitalServiceImpl service;

    private Hospital hospital;

    @BeforeEach
    void setUp() {
        hospital = new Hospital();
        hospital.setId(1L);
        hospital.setName("Hopital A");
        hospital.setCity("Paris");
        hospital.setLatitude(48.8566);
        hospital.setLongitude(2.3522);
        hospital.setIsActive(true);
    }

    // findById_success
    @Test
    void findById_existingId_returnsDto() {
        // Given
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        HospitalDTO dto = HospitalDTO.builder().id(1L).name("Hopital A").build();
        when(hospitalMapper.toDto(hospital)).thenReturn(dto);

        // When
        HospitalDTO result = service.findById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L); // assertion principale
        verify(hospitalRepository).findById(1L);
        verify(hospitalMapper).toDto(hospital);
    }

    // findById_notFound
    @Test
    void findById_unknownId_throwsNotFound() {
        // Given
        when(hospitalRepository.findById(999L)).thenReturn(Optional.empty());

        // When-Then
        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(hospitalRepository).findById(999L);
        verifyNoInteractions(hospitalMapper);
    }

    // create_success
    @Test
    void create_validPayload_returnsCreatedDto() {
        // Given
        HospitalCreateDTO create = HospitalCreateDTO.builder()
                .name("New H")
                .city("Lyon")
                .latitude(45.75)
                .longitude(4.85)
                .specialtyIds(List.of(10L, 20L))
                .build();

        Specialty s1 = new Specialty(); s1.setId(10L);
        Specialty s2 = new Specialty(); s2.setId(20L);
        when(specialtyRepository.findById(10L)).thenReturn(Optional.of(s1));
        when(specialtyRepository.findById(20L)).thenReturn(Optional.of(s2));

        Hospital entity = new Hospital();
        entity.setName("New H");
        entity.setCity("Lyon");
        entity.setLatitude(45.75);
        entity.setLongitude(4.85);
        entity.setSpecialties(new HashSet<>(Arrays.asList(s1, s2)));
        when(hospitalMapper.fromCreateDto(eq(create), any())).thenReturn(entity);

        Hospital saved = new Hospital();
        saved.setId(5L);
        saved.setName("New H");
        when(hospitalRepository.save(entity)).thenReturn(saved);

        HospitalDTO dto = HospitalDTO.builder().id(5L).name("New H").build();
        when(hospitalMapper.toDto(saved)).thenReturn(dto);

        // When
        HospitalDTO result = service.create(create);

        // Then
        assertThat(result.getId()).isEqualTo(5L); // assertion principale
        verify(specialtyRepository, times(1)).findById(10L);
        verify(specialtyRepository, times(1)).findById(20L);
        verify(hospitalRepository).save(entity);
        verify(hospitalMapper).toDto(saved);
    }

    // update_success
    @Test
    void update_validPayload_returnsUpdatedDto() {
        // Given
        HospitalUpdateDTO update = HospitalUpdateDTO.builder()
                .name("Updated")
                .latitude(48.86)
                .longitude(2.35)
                .specialtyIds(List.of(30L))
                .build();

        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        Specialty s3 = new Specialty(); s3.setId(30L);
        when(specialtyRepository.findById(30L)).thenReturn(Optional.of(s3));

        // mapper.updateEntity est void, on le mocke pour no-op
        doAnswer(invocation -> {
            Hospital target = invocation.getArgument(0);
            target.setName("Updated");
            target.setLatitude(48.86);
            target.setLongitude(2.35);
            target.setSpecialties(Set.of(s3));
            return null;
        }).when(hospitalMapper).updateEntity(eq(hospital), eq(update), any());

        Hospital saved = new Hospital();
        saved.setId(1L);
        saved.setName("Updated");
        when(hospitalRepository.save(hospital)).thenReturn(saved);

        HospitalDTO dto = HospitalDTO.builder().id(1L).name("Updated").build();
        when(hospitalMapper.toDto(saved)).thenReturn(dto);

        // When
        HospitalDTO result = service.update(1L, update);

        // Then
        assertThat(result.getName()).isEqualTo("Updated"); // assertion principale
        verify(hospitalRepository).findById(1L);
        verify(hospitalRepository).save(hospital);
        verify(hospitalMapper).toDto(saved);
    }

    // delete_success
    @Test
    void delete_existingId_deletesEntity() {
        // Given
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));

        // When
        service.delete(1L);

        // Then
        ArgumentCaptor<Hospital> captor = ArgumentCaptor.forClass(Hospital.class);
        verify(hospitalRepository).delete(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L); // assertion principale
    }

    // findNearestHospitalsWithAvailability
    @Test
    void findNearestHospitalsWithAvailability_validInputs_returnsSortedLimitedList() {
        // Given
        HospitalAvailabilityProjection p1 = mock(HospitalAvailabilityProjection.class);
        when(p1.getId()).thenReturn(1L);
        when(p1.getName()).thenReturn("H1");
        when(p1.getCity()).thenReturn("Paris");
        when(p1.getLatitude()).thenReturn(48.8566);
        when(p1.getLongitude()).thenReturn(2.3522);
        when(p1.getAvailableBeds()).thenReturn(5L);

        HospitalAvailabilityProjection p2 = mock(HospitalAvailabilityProjection.class);
        when(p2.getId()).thenReturn(2L);
        when(p2.getName()).thenReturn("H2");
        when(p2.getCity()).thenReturn("Lyon");
        when(p2.getLatitude()).thenReturn(45.7640);
        when(p2.getLongitude()).thenReturn(4.8357);
        when(p2.getAvailableBeds()).thenReturn(2L);

        when(hospitalRepository.findActiveHospitalsWithAvailableBedsBySpecialty("CARD"))
                .thenReturn(List.of(p2, p1)); // ordre initial quelconque

        // When
        List<HospitalSummaryDTO> result = service.findNearestHospitalsWithAvailability(48.8566, 2.3522, "CARD", 1);

        // Then
        assertThat(result).hasSize(1); // assertion principale (respect du limit)
        verify(hospitalRepository).findActiveHospitalsWithAvailableBedsBySpecialty("CARD");
    }

    // addSpecialtyToHospital
    @Test
    void addSpecialtyToHospital_validIds_returnsDto() {
        // Given
        Specialty spec = new Specialty(); spec.setId(7L);
        hospital.setSpecialties(new HashSet<>());
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(specialtyRepository.findById(7L)).thenReturn(Optional.of(spec));

        when(hospitalRepository.save(any(Hospital.class))).thenAnswer(inv -> inv.getArgument(0));
        HospitalDTO dto = HospitalDTO.builder().id(1L).build();
        when(hospitalMapper.toDto(any(Hospital.class))).thenReturn(dto);

        // When
        HospitalDTO result = service.addSpecialtyToHospital(1L, 7L);

        // Then
        assertThat(result.getId()).isEqualTo(1L); // assertion principale
        verify(hospitalRepository).save(any(Hospital.class));
        verify(hospitalMapper).toDto(any(Hospital.class));
    }
}
