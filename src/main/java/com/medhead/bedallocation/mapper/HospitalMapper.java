package com.medhead.bedallocation.mapper;

import com.medhead.bedallocation.dto.*;
import com.medhead.bedallocation.model.Hospital;
import com.medhead.bedallocation.model.Specialty;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface HospitalMapper {

    // -------------------- Entity -> DTO --------------------
    @Mapping(target = "specialtyIds", expression = "java(mapSpecialtyIds(entity))")
    HospitalDTO toDto(Hospital entity);

    @Mapping(target = "availableBeds", source = "availableBeds")
    HospitalSummaryDTO toSummaryDto(Hospital entity);

    List<HospitalDTO> toDtoList(List<Hospital> entities);

    List<HospitalSummaryDTO> toSummaryDtoList(List<Hospital> entities);

    // -------------------- Create DTO -> Entity --------------------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "beds", ignore = true)
    @Mapping(target = "specialties", ignore = true)
    @Mapping(target = "availableBeds", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Hospital fromCreateDto(HospitalCreateDTO dto, @Context Set<Specialty> specialties);

    // MapStruct uses this named method for the mapping above
    @ObjectFactory
    default Hospital createHospital(HospitalCreateDTO dto, @Context Set<Specialty> specialties) {
        Hospital h = new Hospital();
        // Fields will be set by MapStruct; specialties handled below in after-mapping
        return h;
    }

    @AfterMapping
    default void afterCreate(@MappingTarget Hospital entity, HospitalCreateDTO dto, @Context Set<Specialty> specialties) {
        if (specialties != null) {
            entity.setSpecialties(specialties);
        }
        // Sync availableBeds from beds list
        entity.recalculateAvailableBeds();
    }

    // -------------------- Update DTO -> Entity (in-place) --------------------
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "beds", ignore = true)
    @Mapping(target = "availableBeds", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Hospital entity, HospitalUpdateDTO dto, @Context Set<Specialty> specialties);

    @AfterMapping
    default void afterUpdate(@MappingTarget Hospital entity, HospitalUpdateDTO dto, @Context Set<Specialty> specialties) {
        if (specialties != null) {
            entity.setSpecialties(specialties);
        }
        entity.recalculateAvailableBeds();
    }

    // -------------------- Helpers --------------------
    default List<Long> mapSpecialtyIds(Hospital entity) {
        if (entity == null || entity.getSpecialties() == null) return List.of();
        return entity.getSpecialties().stream()
                .map(Specialty::getId)
                .collect(Collectors.toList());
    }
}
