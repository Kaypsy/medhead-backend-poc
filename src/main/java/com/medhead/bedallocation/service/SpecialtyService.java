package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.*;

import java.util.List;

/**
 * Service métier pour la gestion des spécialités médicales.
 */
public interface SpecialtyService {

    // -------- CRUD --------
    List<SpecialtySummaryDTO> findAll();

    SpecialtyDTO findById(Long id);

    SpecialtyDTO create(SpecialtyCreateDTO dto);

    SpecialtyDTO update(Long id, SpecialtyUpdateDTO dto);

    void delete(Long id);

    // -------- Recherches métier --------
    SpecialtyDTO findByCode(String code);

    List<SpecialtySummaryDTO> findByGroup(String group);

    List<SpecialtySummaryDTO> findAllActive();
}
