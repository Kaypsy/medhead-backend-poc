package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.SpecialtyGroupDTO;
import com.medhead.bedallocation.mapper.SpecialtyGroupMapper;
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
public class SpecialtyGroupServiceImpl implements SpecialtyGroupService {

    private final SpecialtyGroupRepository specialtyGroupRepository;
    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyGroupMapper mapper;

    @Override
    public List<SpecialtyGroupDTO> findAll() {
        log.debug("[SpecialtyGroupService] findAll called");
        return mapper.toDtoList(specialtyGroupRepository.findAll());
    }

    @Override
    public SpecialtyGroupDTO findById(Long id) {
        if (id == null) throw new BadRequestException("L'identifiant du groupe est requis");
        SpecialtyGroup entity = specialtyGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Groupe introuvable avec id=" + id));
        return mapper.toDto(entity);
    }

    @Override
    public SpecialtyGroupDTO findByCode(String code) {
        if (!StringUtils.hasText(code)) throw new BadRequestException("Le code du groupe est requis");
        SpecialtyGroup entity = specialtyGroupRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Groupe introuvable avec code=" + code));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional
    public SpecialtyGroupDTO create(SpecialtyGroupDTO dto) {
        if (dto == null) throw new BadRequestException("Le payload de création de groupe est requis");
        if (!StringUtils.hasText(dto.getCode())) throw new BadRequestException("Le code est requis");
        if (!StringUtils.hasText(dto.getName())) throw new BadRequestException("Le nom est requis");
        if (specialtyGroupRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Un groupe avec ce code existe déjà: " + dto.getCode());
        }
        SpecialtyGroup toSave = mapper.fromDto(dto);
        if (toSave.getIsActive() == null) toSave.setIsActive(true);
        SpecialtyGroup saved = specialtyGroupRepository.save(toSave);
        log.info("Groupe créé: id={}, code={}", saved.getId(), saved.getCode());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SpecialtyGroupDTO update(Long id, SpecialtyGroupDTO dto) {
        if (id == null) throw new BadRequestException("L'identifiant du groupe est requis");
        if (dto == null) throw new BadRequestException("Le payload de mise à jour est requis");
        SpecialtyGroup entity = specialtyGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Groupe introuvable avec id=" + id));

        // Si le code change, vérifier l'unicité
        if (StringUtils.hasText(dto.getCode()) && !dto.getCode().equals(entity.getCode())) {
            if (specialtyGroupRepository.existsByCode(dto.getCode())) {
                throw new BadRequestException("Un groupe avec ce code existe déjà: " + dto.getCode());
            }
        }

        mapper.updateEntity(entity, dto);
        SpecialtyGroup saved = specialtyGroupRepository.save(entity);
        log.info("Groupe mis à jour: id={}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new BadRequestException("L'identifiant du groupe est requis");
        SpecialtyGroup entity = specialtyGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Groupe introuvable avec id=" + id));
        long usage = specialtyRepository.countBySpecialtyGroup_Id(id);
        if (usage > 0) {
            throw new BadRequestException("Impossible de supprimer: " + usage + " spécialité(s) liées à ce groupe");
        }
        specialtyGroupRepository.delete(entity);
        log.warn("Groupe supprimé: id={}", id);
    }
}
