package com.medhead.bedallocation.mapper;

import com.medhead.bedallocation.dto.SpecialtyGroupDTO;
import com.medhead.bedallocation.model.SpecialtyGroup;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpecialtyGroupMapper {

    // Entity -> DTO
    SpecialtyGroupDTO toDto(SpecialtyGroup entity);
    List<SpecialtyGroupDTO> toDtoList(List<SpecialtyGroup> entities);

    // DTO -> Entity (for create)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specialties", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SpecialtyGroup fromDto(SpecialtyGroupDTO dto);

    // Update in-place
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specialties", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget SpecialtyGroup entity, SpecialtyGroupDTO dto);
}
