package com.medhead.bedallocation.repository;

import com.medhead.bedallocation.model.SpecialtyGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecialtyGroupRepository extends JpaRepository<SpecialtyGroup, Long> {
    Optional<SpecialtyGroup> findByCode(String code);
    boolean existsByCode(String code);
}
