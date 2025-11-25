package com.medhead.bedallocation.repository;

import com.medhead.bedallocation.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité {@link Specialty}.
 */
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    Optional<Specialty> findByCode(String code);

    List<Specialty> findBySpecialtyGroup(String group);

    List<Specialty> findByIsActiveTrue();
}
