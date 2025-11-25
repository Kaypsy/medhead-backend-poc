package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.*;
import com.medhead.bedallocation.model.enums.BedStatus;

import java.util.List;

/**
 * Service métier pour la gestion des lits d'hôpital.
 */
public interface BedService {

    // -------- CRUD --------
    List<BedDTO> findAll();

    BedDTO findById(Long id);

    BedDTO create(BedCreateDTO dto);

    BedDTO update(Long id, BedUpdateDTO dto);

    void delete(Long id);

    // -------- Recherches / opérations métier --------
    List<BedAvailabilityDTO> findAvailableByHospital(Long hospitalId);

    List<BedAvailabilityDTO> findAvailableBySpecialty(Long specialtyId);

    BedDTO updateBedStatus(Long bedId, BedStatus newStatus);

    List<BedAvailabilityDTO> findAvailableBedsForEmergency(String specialtyCode, double lat, double lon);

    BedDTO reserveBed(Long bedId);

    BedDTO releaseBed(Long bedId);
}
