package com.medhead.bedallocation.repository;

import com.medhead.bedallocation.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité {@link Specialty}.
 */
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    Optional<Specialty> findByCode(String code);

    List<Specialty> findBySpecialtyGroup_Code(String code);

    @Query("select s from Specialty s where s.specialtyGroup.code = :code")
    List<Specialty> findBySpecialtyGroup(@Param("code") String code);

    long countBySpecialtyGroup_Id(Long groupId);

    List<Specialty> findByIsActiveTrue();
}
