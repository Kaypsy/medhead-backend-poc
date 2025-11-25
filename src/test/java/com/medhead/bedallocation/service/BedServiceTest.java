package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.BedAvailabilityDTO;
import com.medhead.bedallocation.dto.BedDTO;
import com.medhead.bedallocation.dto.BedUpdateDTO;
import com.medhead.bedallocation.mapper.BedMapper;
import com.medhead.bedallocation.model.Bed;
import com.medhead.bedallocation.model.Hospital;
import com.medhead.bedallocation.model.Specialty;
import com.medhead.bedallocation.model.enums.BedStatus;
import com.medhead.bedallocation.repository.BedRepository;
import com.medhead.bedallocation.repository.HospitalRepository;
import com.medhead.bedallocation.repository.SpecialtyRepository;
import com.medhead.bedallocation.service.exception.BadRequestException;
import com.medhead.bedallocation.service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BedServiceTest {

    @Mock private BedRepository bedRepository;
    @Mock private HospitalRepository hospitalRepository;
    @Mock private SpecialtyRepository specialtyRepository;
    @Mock private BedMapper bedMapper;

    @InjectMocks
    private BedServiceImpl service;

    private Bed bed;

    @BeforeEach
    void setUp() {
        bed = new Bed();
        bed.setId(100L);
        bed.setStatus(BedStatus.AVAILABLE);
        Hospital h = new Hospital(); h.setId(1L); h.setLatitude(48.8566); h.setLongitude(2.3522);
        bed.setHospital(h);
        Specialty s = new Specialty(); s.setId(9L);
        bed.setSpecialty(s);
    }

    // findAvailableBySpecialty
    @Test
    void findAvailableBySpecialty_validId_returnsAvailabilityList() {
        // Given
        when(bedRepository.findBySpecialtyIdAndStatus(9L, BedStatus.AVAILABLE)).thenReturn(List.of(new Bed(), new Bed()));
        List<BedAvailabilityDTO> mapped = List.of(new BedAvailabilityDTO(), new BedAvailabilityDTO());
        when(bedMapper.toAvailabilityDtoList(anyList())).thenReturn(mapped);

        // When
        List<BedAvailabilityDTO> result = service.findAvailableBySpecialty(9L);

        // Then
        assertThat(result).hasSize(2); // assertion principale
        verify(bedRepository).findBySpecialtyIdAndStatus(9L, BedStatus.AVAILABLE);
        verify(bedMapper).toAvailabilityDtoList(anyList());
    }

    // updateBedStatus validations (succès)
    @Test
    void updateBedStatus_availableToOccupied_updatesAndSaves() {
        // Given
        when(bedRepository.findById(100L)).thenReturn(Optional.of(bed));
        when(bedRepository.save(any(Bed.class))).thenAnswer(inv -> inv.getArgument(0));
        BedDTO dto = BedDTO.builder().id(100L).status(BedStatus.OCCUPIED).build();
        when(bedMapper.toDto(any(Bed.class))).thenReturn(dto);

        // When
        BedDTO result = service.updateBedStatus(100L, BedStatus.OCCUPIED);

        // Then
        assertThat(result.getStatus()).isEqualTo(BedStatus.OCCUPIED); // assertion principale
        verify(bedRepository).save(any(Bed.class));
    }

    // updateBedStatus validations (transition invalide)
    @Test
    void updateBedStatus_occupiedToReserved_throwsBadRequest() {
        // Given
        bed.setStatus(BedStatus.OCCUPIED);
        when(bedRepository.findById(100L)).thenReturn(Optional.of(bed));

        // When-Then
        assertThatThrownBy(() -> service.updateBedStatus(100L, BedStatus.RESERVED))
                .isInstanceOf(BadRequestException.class);
        verify(bedRepository, never()).save(any());
    }

    // reserveBed - success
    @Test
    void reserveBed_fromAvailable_success() {
        // Given
        when(bedRepository.findById(100L)).thenReturn(Optional.of(bed));
        when(bedRepository.save(any(Bed.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bedMapper.toDto(any(Bed.class))).thenReturn(BedDTO.builder().id(100L).status(BedStatus.RESERVED).build());

        // When
        BedDTO result = service.reserveBed(100L);

        // Then
        assertThat(result.getStatus()).isEqualTo(BedStatus.RESERVED); // assertion principale
        verify(bedRepository).save(any(Bed.class));
    }

    // reserveBed - déjà occupé
    @Test
    void reserveBed_fromOccupied_throwsBadRequest() {
        // Given
        bed.setStatus(BedStatus.OCCUPIED);
        when(bedRepository.findById(100L)).thenReturn(Optional.of(bed));

        // When-Then
        assertThatThrownBy(() -> service.reserveBed(100L))
                .isInstanceOf(BadRequestException.class);
        verify(bedRepository, never()).save(any());
    }

    // releaseBed - déjà disponible
    @Test
    void releaseBed_alreadyAvailable_noSave() {
        // Given
        when(bedRepository.findById(100L)).thenReturn(Optional.of(bed));
        when(bedMapper.toDto(bed)).thenReturn(BedDTO.builder().id(100L).status(BedStatus.AVAILABLE).build());

        // When
        BedDTO result = service.releaseBed(100L);

        // Then
        assertThat(result.getStatus()).isEqualTo(BedStatus.AVAILABLE); // assertion principale
        verify(bedRepository, never()).save(any());
    }

    // releaseBed - depuis RESERVED
    @Test
    void releaseBed_fromReserved_savesAvailable() {
        // Given
        bed.setStatus(BedStatus.RESERVED);
        when(bedRepository.findById(100L)).thenReturn(Optional.of(bed));
        when(bedRepository.save(any(Bed.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bedMapper.toDto(any(Bed.class))).thenReturn(BedDTO.builder().id(100L).status(BedStatus.AVAILABLE).build());

        // When
        BedDTO result = service.releaseBed(100L);

        // Then
        assertThat(result.getStatus()).isEqualTo(BedStatus.AVAILABLE); // assertion principale
        verify(bedRepository).save(any(Bed.class));
    }
}
