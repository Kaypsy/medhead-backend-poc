package com.medhead.bedallocation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Représente une spécialité médicale NHS.
 *
 * Remarque importante: les valeurs de {@code code} et {@code name} doivent
 * correspondre aux données de référence officielles NHS (codes et libellés
 * normalisés). Cela garantit la cohérence des échanges et des rapports.
 */
@Data
@Entity
@Table(
        name = "specialty",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_specialty_code", columnNames = {"code"})
        },
        indexes = {
                @Index(name = "idx_specialty_code", columnList = "code"),
                @Index(name = "idx_specialty_group", columnList = "specialty_group")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Code NHS unique (ex: "CARDIO"). */
    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    /** Nom officiel NHS (ex: "Cardiologie"). */
    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Groupe de spécialité (ex: "Groupe de médecine générale"). */
    @NotBlank
    @Size(max = 150)
    @Column(name = "specialty_group", nullable = false, length = 150)
    private String specialtyGroup;

    /** Description fonctionnelle, libre (stockée en TEXT). */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Relation ManyToMany avec les hôpitaux. */
    @ManyToMany(mappedBy = "specialties")
    private Set<Hospital> hospitals = new HashSet<>();

    /** Relation OneToMany avec les lits. */
    @OneToMany(mappedBy = "specialty", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Bed> beds = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (isActive == null) {
            isActive = true;
        }
    }
}
