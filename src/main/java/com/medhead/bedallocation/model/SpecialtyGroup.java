package com.medhead.bedallocation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Groupe de spécialités médicales (ex: Groupe dentaire, Groupe de médecine générale).
 */
import lombok.ToString;

@Data
@Entity
@ToString(exclude = "specialties")
@Table(
        name = "specialty_group",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_specialty_group_code", columnNames = {"code"})
        },
        indexes = {
                @Index(name = "idx_specialty_group_code", columnList = "code"),
                @Index(name = "idx_specialty_group_name", columnList = "name")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SpecialtyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "specialtyGroup", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Specialty> specialties = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (isActive == null) {
            isActive = true;
        }
    }
}
