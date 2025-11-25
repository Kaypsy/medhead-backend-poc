package com.medhead.bedallocation.mapper;

import com.medhead.bedallocation.dto.UserDTO;
import com.medhead.bedallocation.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entity -> DTO
    UserDTO toDto(User entity);

    List<UserDTO> toDtoList(List<User> entities);

    // DTO -> Entity (ne mappe pas le mot de passe)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User fromDto(UserDTO dto);

    // Update in-place (ne touche pas au mot de passe ni aux champs d'audit)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget User entity, UserDTO dto);
}
