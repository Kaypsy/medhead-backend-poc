package com.medhead.bedallocation.mapper;

import com.medhead.bedallocation.dto.*;
import com.medhead.bedallocation.model.Bed;
import com.medhead.bedallocation.model.Hospital;
import com.medhead.bedallocation.model.Specialty;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BedMapper {

    // -------- Entity -> DTO --------
    @Mapping(target = "hospitalId", source = "hospital.id")
    @Mapping(target = "specialtyId", source = "specialty.id")
    BedDTO toDto(Bed entity);

    List<BedDTO> toDtoList(List<Bed> entities);

    @Mapping(target = "hospitalId", source = "hospital.id")
    @Mapping(target = "specialtyId", source = "specialty.id")
    BedAvailabilityDTO toAvailabilityDto(Bed entity);

    List<BedAvailabilityDTO> toAvailabilityDtoList(List<Bed> entities);

    // -------- Create DTO -> Entity --------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hospital", expression = "java(mapHospitalId(dto.getHospitalId()))")
    @Mapping(target = "specialty", expression = "java(mapSpecialtyId(dto.getSpecialtyId()))")
    @Mapping(target = "isAvailable", ignore = true) // sera synchronisé depuis status côté entité
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Bed fromCreateDto(BedCreateDTO dto);

    // -------- Update DTO -> Entity (in-place) --------
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hospital", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    @Mapping(target = "isAvailable", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Bed entity, BedUpdateDTO dto);

    @AfterMapping
    default void afterUpdate(@MappingTarget Bed entity, BedUpdateDTO dto) {
        if (dto.getHospitalId() != null) {
            entity.setHospital(mapHospitalId(dto.getHospitalId()));
        }
        if (dto.getSpecialtyId() != null) {
            entity.setSpecialty(mapSpecialtyId(dto.getSpecialtyId()));
        }
        // La synchronisation de isAvailable avec status est assurée par les callbacks de l'entité
    }

    // -------- Helpers --------
    default Hospital mapHospitalId(Long id) {
        if (id == null) return null;
        Hospital h = new Hospital();
        h.setId(id);
        return h;
    }

    default Specialty mapSpecialtyId(Long id) {
        if (id == null) return null;
        Specialty s = new Specialty();
        s.setId(id);
        return s;
    }
}
