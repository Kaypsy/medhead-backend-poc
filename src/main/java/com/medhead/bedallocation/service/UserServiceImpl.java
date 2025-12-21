package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.UserCreateDTO;
import com.medhead.bedallocation.dto.UserDTO;
import com.medhead.bedallocation.dto.UserUpdateDTO;
import com.medhead.bedallocation.mapper.UserMapper;
import com.medhead.bedallocation.model.User;
import com.medhead.bedallocation.repository.UserRepository;
import com.medhead.bedallocation.service.exception.BadRequestException;
import com.medhead.bedallocation.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserDTO> findAll() {
        log.debug("[UserService] findAll called");
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Override
    public UserDTO findById(Long id) {
        if (id == null) throw new BadRequestException("L'identifiant utilisateur est requis");
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec id=" + id));
        return userMapper.toDto(entity);
    }

    @Override
    @Transactional
    public UserDTO create(UserCreateDTO dto) {
        if (dto == null) throw new BadRequestException("Le payload de création est requis");
        if (!StringUtils.hasText(dto.getUsername())) {
            throw new BadRequestException("Le nom d'utilisateur est requis");
        }
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BadRequestException("Le mot de passe est requis");
        }
        if (!StringUtils.hasText(dto.getEmail())) {
            throw new BadRequestException("L'email est requis");
        }

        // Unicité username/email
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException("Un utilisateur avec ce username existe déjà: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Un utilisateur avec cet email existe déjà: " + dto.getEmail());
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRoles(StringUtils.hasText(dto.getRoles()) ? dto.getRoles() : "ROLE_USER");
        user.setIsActive(dto.getIsActive() == null ? true : dto.getIsActive());

        User saved = userRepository.save(user);
        log.info("Utilisateur créé: id={}, username={}", saved.getId(), saved.getUsername());
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {
        if (id == null) throw new BadRequestException("L'identifiant utilisateur est requis");
        if (dto == null) throw new BadRequestException("Le payload de mise à jour est requis");

        User entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec id=" + id));

        // Unicité si username ou email changent
        if (StringUtils.hasText(dto.getUsername()) && !dto.getUsername().equals(entity.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new BadRequestException("Un utilisateur avec ce username existe déjà: " + dto.getUsername());
            }
            entity.setUsername(dto.getUsername());
        }
        if (StringUtils.hasText(dto.getEmail()) && (entity.getEmail() == null || !dto.getEmail().equals(entity.getEmail()))) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new BadRequestException("Un utilisateur avec cet email existe déjà: " + dto.getEmail());
            }
            entity.setEmail(dto.getEmail());
        }

        if (dto.getRoles() != null) {
            entity.setRoles(dto.getRoles());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }

        User saved = userRepository.save(entity);
        log.info("Utilisateur mis à jour: id={}", saved.getId());
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new BadRequestException("L'identifiant utilisateur est requis");
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec id=" + id));
        userRepository.delete(entity);
        log.warn("Utilisateur supprimé: id={}", id);
    }
}
