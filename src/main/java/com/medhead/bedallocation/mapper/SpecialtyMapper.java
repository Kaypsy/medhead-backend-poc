package com.medhead.bedallocation.mapper;

import com.medhead.bedallocation.dto.*;
import com.medhead.bedallocation.model.Bed;
import com.medhead.bedallocation.model.Hospital;
import com.medhead.bedallocation.model.Specialty;
import com.medhead.bedallocation.model.SpecialtyGroup;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SpecialtyMapper {

    // -------- Entity -> DTO --------
    @Mapping(target = "hospitalIds", expression = "java(mapHospitalIds(entity.getHospitals()))")
    @Mapping(target = "bedIds", expression = "java(mapBedIds(entity.getBeds()))")
    @Mapping(target = "specialtyGroup", expression = "java(mapGroup(entity.getSpecialtyGroup()))")
    SpecialtyDTO toDto(Specialty entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "specialtyGroup", expression = "java(mapGroup(entity.getSpecialtyGroup()))")
    SpecialtySummaryDTO toSummaryDto(Specialty entity);

    List<SpecialtyDTO> toDtoList(List<Specialty> entities);
    List<SpecialtySummaryDTO> toSummaryDtoList(List<Specialty> entities);

    // -------- Create DTO -> Entity --------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hospitals", ignore = true)
    @Mapping(target = "beds", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "specialtyGroup", ignore = true)
    Specialty fromCreateDto(SpecialtyCreateDTO dto);

    // -------- Update DTO -> Entity (in-place) --------
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hospitals", ignore = true)
    @Mapping(target = "beds", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "specialtyGroup", ignore = true)
    void updateEntity(@MappingTarget Specialty entity, SpecialtyUpdateDTO dto);

    // -------- Helpers --------
    default List<Long> mapHospitalIds(Set<Hospital> hospitals) {
        if (hospitals == null) return List.of();
        return hospitals.stream().map(Hospital::getId).collect(Collectors.toList());
    }

    default List<Long> mapBedIds(List<Bed> beds) {
        if (beds == null) return List.of();
        return beds.stream().map(Bed::getId).collect(Collectors.toList());
    }

    // Group mapping helper
    default SpecialtyGroupDTO mapGroup(SpecialtyGroup group) {
        if (group == null) return null;
        SpecialtyGroupDTO dto = new SpecialtyGroupDTO();
        dto.setId(group.getId());
        dto.setCode(group.getCode());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setIsActive(group.getIsActive());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());
        return dto;
    }
}
