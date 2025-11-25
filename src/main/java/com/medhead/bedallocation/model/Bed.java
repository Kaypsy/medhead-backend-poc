package com.medhead.bedallocation.model;

import com.medhead.bedallocation.model.enums.BedStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "bed",
        indexes = {
                @Index(name = "idx_bed_status", columnList = "status"),
                @Index(name = "idx_bed_is_available", columnList = "is_available")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_bed_hospital_bed_number", columnNames = {"hospital_id", "bed_number"})
        }
)
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @NotBlank
    @Column(name = "bed_number", nullable = false)
    private String bedNumber;

    @Column(name = "room_number")
    private String roomNumber;

    private Integer floor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BedStatus status;

    @NotNull
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "last_occupied_at")
    private LocalDateTime lastOccupiedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        // Défaut: si aucun statut défini, considérer le lit comme disponible
        if (this.status == null) {
            this.status = BedStatus.AVAILABLE;
        }
        syncAvailabilityWithStatus();
    }

    @PreUpdate
    public void preUpdate() {
        syncAvailabilityWithStatus();
    }

    private void syncAvailabilityWithStatus() {
        // Règle: un lit est disponible uniquement si son statut est AVAILABLE
        this.isAvailable = (this.status == BedStatus.AVAILABLE);
    }
}
