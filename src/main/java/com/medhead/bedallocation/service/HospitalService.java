package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.HospitalCreateDTO;
import com.medhead.bedallocation.dto.HospitalDTO;
import com.medhead.bedallocation.dto.HospitalSummaryDTO;
import com.medhead.bedallocation.dto.HospitalUpdateDTO;

import java.util.List;

/**
 * Service métier pour la gestion des hôpitaux et leurs disponibilités.
 */
public interface HospitalService {

    // -------- CRUD --------
    List<HospitalSummaryDTO> findAll();

    HospitalDTO findById(Long id);

    HospitalDTO create(HospitalCreateDTO dto);

    HospitalDTO update(Long id, HospitalUpdateDTO dto);

    void delete(Long id);

    // -------- Recherches métier --------
    List<HospitalSummaryDTO> findAllAvailable();

    List<HospitalSummaryDTO> findByCity(String city);

    List<HospitalSummaryDTO> findBySpecialtyCode(String specialtyCode);

    List<HospitalSummaryDTO> findNearestHospitalsWithAvailability(double lat, double lon, String specialtyCode, int limit);

    // -------- Associations spécialités --------
    HospitalDTO addSpecialtyToHospital(Long hospitalId, Long specialtyId);

    HospitalDTO removeSpecialtyFromHospital(Long hospitalId, Long specialtyId);

    // -------- Disponibilités --------
    HospitalDTO updateAvailableBeds(Long hospitalId);
}
