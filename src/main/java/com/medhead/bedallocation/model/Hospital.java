package com.medhead.bedallocation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "hospital",
       indexes = {
           @Index(name = "idx_hospital_city", columnList = "city"),
           @Index(name = "idx_hospital_is_active", columnList = "is_active")
       })
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200, unique = true)
    private String name;

    private String address;

    @NotBlank
    @Column(nullable = false)
    private String city;

    private String postalCode;

    @NotNull
    @Column(nullable = false)
    private Double latitude;

    @NotNull
    @Column(nullable = false)
    private Double longitude;

    private String phoneNumber;

    private Integer totalBeds;

    private Integer availableBeds;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bed> beds = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "hospital_specialty",
            joinColumns = @JoinColumn(name = "hospital_id"),
            inverseJoinColumns = @JoinColumn(name = "specialty_id")
    )
    private Set<Specialty> specialties = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (isActive == null) {
            isActive = true;
        }
        recalculateAvailableBeds();
    }

    @PreUpdate
    public void preUpdate() {
        recalculateAvailableBeds();
    }

    /**
     * Recalcule le nombre de lits disponibles à partir de la liste des lits associés.
     * Met à jour le champ availableBeds.
     */
    public void recalculateAvailableBeds() {
        if (beds == null) {
            this.availableBeds = 0;
            return;
        }
        this.availableBeds = (int) beds.stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsAvailable()))
                .count();
    }
}
