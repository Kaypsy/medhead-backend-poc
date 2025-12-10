package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.SpecialtyGroupDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface SpecialtyGroupService {
    List<SpecialtyGroupDTO> findAll();
    SpecialtyGroupDTO findById(Long id);
    SpecialtyGroupDTO findByCode(String code);
    SpecialtyGroupDTO create(@Valid SpecialtyGroupDTO dto);
    SpecialtyGroupDTO update(Long id, @Valid SpecialtyGroupDTO dto);
    void delete(Long id);
}
