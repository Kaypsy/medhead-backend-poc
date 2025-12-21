package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.UserCreateDTO;
import com.medhead.bedallocation.dto.UserDTO;
import com.medhead.bedallocation.dto.UserUpdateDTO;

import java.util.List;

public interface UserService {

    List<UserDTO> findAll();

    UserDTO findById(Long id);

    UserDTO create(UserCreateDTO dto);

    UserDTO update(Long id, UserUpdateDTO dto);

    void delete(Long id);
}
