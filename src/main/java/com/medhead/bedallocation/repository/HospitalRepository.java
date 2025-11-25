package com.medhead.bedallocation.repository;

import com.medhead.bedallocation.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository Spring Data JPA pour l'entité {@link Hospital}.
 */
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    // Méthodes dérivées demandées
    List<Hospital> findByCity(String city);

    List<Hospital> findByIsActiveTrue();

    List<Hospital> findBySpecialties_Code(String specialtyCode);

    List<Hospital> findByCityAndIsActiveTrue(String city);

    /**
     * Projection légère pour retourner seulement les informations utiles à une recherche
     * de lits disponibles par spécialité. Utilisée pour réduire la quantité de données
     * chargées et optimiser les transferts.
     */
    interface HospitalAvailabilityProjection {
        Long getId();
        String getName();
        String getCity();
        Double getLatitude();
        Double getLongitude();
        /** Nombre de lits disponibles pour la spécialité considérée (agrégé). */
        Long getAvailableBeds();
    }

    /**
     * Recherche les hôpitaux actifs ayant au moins un lit disponible pour la spécialité indiquée.
     * Utilise une projection pour ne renvoyer qu'un sous-ensemble des colonnes et un agrégat.
     *
     * Détails de performance:
     * - Jointure directe sur les lits afin d'éviter un N+1 sur la collection "beds".
     * - Filtre par statut et spécialité pour ne compter que les lits réellement disponibles.
     */
    @Query("""
            select 
              h.id as id,
              h.name as name,
              h.city as city,
              h.latitude as latitude,
              h.longitude as longitude,
              sum(case when b.status = com.medhead.bedallocation.model.enums.BedStatus.AVAILABLE then 1 else 0 end) as availableBeds
            from Hospital h
              join h.beds b
            where h.isActive = true
              and b.specialty.code = :specialtyCode
            group by h.id, h.name, h.city, h.latitude, h.longitude
            having sum(case when b.status = com.medhead.bedallocation.model.enums.BedStatus.AVAILABLE then 1 else 0 end) > 0
            """)
    List<HospitalAvailabilityProjection> findActiveHospitalsWithAvailableBedsBySpecialty(@Param("specialtyCode") String specialtyCode);
}
