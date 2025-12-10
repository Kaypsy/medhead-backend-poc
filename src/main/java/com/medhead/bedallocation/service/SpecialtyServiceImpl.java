package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.*;
import com.medhead.bedallocation.mapper.SpecialtyMapper;
import com.medhead.bedallocation.model.Specialty;
import com.medhead.bedallocation.model.SpecialtyGroup;
import com.medhead.bedallocation.repository.SpecialtyGroupRepository;
import com.medhead.bedallocation.repository.SpecialtyRepository;
import com.medhead.bedallocation.service.exception.BadRequestException;
import com.medhead.bedallocation.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyGroupRepository specialtyGroupRepository;
    private final SpecialtyMapper specialtyMapper;

    // -------- CRUD --------
    @Override
    public List<SpecialtySummaryDTO> findAll() {
        log.debug("[SpecialtyService] findAll called");
        return specialtyMapper.toSummaryDtoList(specialtyRepository.findAll());
    }

    @Override
    public SpecialtyDTO findById(Long id) {
        if (id == null) throw new BadRequestException("L'identifiant spécialité est requis");
        Specialty entity = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spécialité introuvable avec id=" + id));
        return specialtyMapper.toDto(entity);
    }

    @Override
    @Transactional
    public SpecialtyDTO create(SpecialtyCreateDTO dto) {
        if (dto == null) throw new BadRequestException("Le payload de création est requis");
        if (!StringUtils.hasText(dto.getCode())) {
            throw new BadRequestException("Le code spécialité est requis");
        }
        // Validation d'unicité du code
        specialtyRepository.findByCode(dto.getCode()).ifPresent(s -> {
            throw new BadRequestException("Une spécialité avec ce code existe déjà: " + dto.getCode());
        });

        Specialty toSave = specialtyMapper.fromCreateDto(dto);
        // Resolve group
        SpecialtyGroup group = resolveGroupForRequest(dto.getSpecialtyGroup());
        if (group == null) {
            throw new BadRequestException("Le groupe de spécialité est requis (id ou code)");
        }
        toSave.setSpecialtyGroup(group);
        Specialty saved = specialtyRepository.save(toSave);
        log.info("Spécialité créée: id={}, code={}", saved.getId(), saved.getCode());
        return specialtyMapper.toDto(saved);
    }

    @Override
    @Transactional
    public SpecialtyDTO update(Long id, SpecialtyUpdateDTO dto) {
        if (id == null) throw new BadRequestException("L'identifiant spécialité est requis");
        if (dto == null) throw new BadRequestException("Le payload de mise à jour est requis");
        Specialty entity = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spécialité introuvable avec id=" + id));

        // Si le code change, vérifier l'unicité
        if (StringUtils.hasText(dto.getCode()) && !dto.getCode().equals(entity.getCode())) {
            specialtyRepository.findByCode(dto.getCode()).ifPresent(existing -> {
                throw new BadRequestException("Une spécialité avec ce code existe déjà: " + dto.getCode());
            });
        }

        specialtyMapper.updateEntity(entity, dto);
        if (dto.getSpecialtyGroup() != null) {
            SpecialtyGroup group = resolveGroupForRequest(dto.getSpecialtyGroup());
            if (group == null) {
                throw new BadRequestException("Le groupe de spécialité spécifié est invalide");
            }
            entity.setSpecialtyGroup(group);
        }
        Specialty saved = specialtyRepository.save(entity);
        log.info("Spécialité mise à jour: id={}", saved.getId());
        return specialtyMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new BadRequestException("L'identifiant spécialité est requis");
        Specialty entity = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spécialité introuvable avec id=" + id));
        specialtyRepository.delete(entity);
        log.warn("Spécialité supprimée: id={}", id);
    }

    // -------- Recherches métier --------
    @Override
    public SpecialtyDTO findByCode(String code) {
        if (!StringUtils.hasText(code)) throw new BadRequestException("Le code est requis");
        Specialty entity = specialtyRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Spécialité introuvable avec code=" + code));
        return specialtyMapper.toDto(entity);
    }

    @Override
    public List<SpecialtySummaryDTO> findByGroup(String group) {
        if (!StringUtils.hasText(group)) throw new BadRequestException("Le groupe est requis");
        return specialtyMapper.toSummaryDtoList(specialtyRepository.findBySpecialtyGroup_Code(group));
    }

    @Override
    public List<SpecialtySummaryDTO> findAllActive() {
        return specialtyMapper.toSummaryDtoList(specialtyRepository.findByIsActiveTrue());
    }
    // -------- Private helpers --------
    @Transactional
    protected SpecialtyGroup resolveGroupForRequest(SpecialtyGroupRefDTO ref) {
        if (ref == null) return null;
        if (ref.getId() != null) {
            return specialtyGroupRepository.findById(ref.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Groupe introuvable avec id=" + ref.getId()));
        }
        if (StringUtils.hasText(ref.getCode())) {
            return specialtyGroupRepository.findByCode(ref.getCode())
                    .orElseGet(() -> {
                        if (StringUtils.hasText(ref.getName())) {
                            // create dynamically
                            SpecialtyGroup g = new SpecialtyGroup();
                            g.setCode(ref.getCode());
                            g.setName(ref.getName());
                            g.setIsActive(true);
                            return specialtyGroupRepository.save(g);
                        }
                        throw new ResourceNotFoundException("Groupe introuvable avec code=" + ref.getCode());
                    });
        }
        return null;
    }
}
