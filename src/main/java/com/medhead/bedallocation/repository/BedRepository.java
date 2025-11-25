package com.medhead.bedallocation.repository;

import com.medhead.bedallocation.model.Bed;
import com.medhead.bedallocation.model.enums.BedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository Spring Data JPA pour l'entité {@link Bed}.
 */
public interface BedRepository extends JpaRepository<Bed, Long> {

    // Méthodes dérivées demandées
    List<Bed> findByHospitalIdAndStatus(Long hospitalId, BedStatus status);

    List<Bed> findBySpecialtyIdAndStatus(Long specialtyId, BedStatus status);

    long countByHospitalIdAndStatus(Long hospitalId, BedStatus status);

    /**
     * Projection légère pour retourner un résumé d'hôpitaux par proximité
     * avec nombre de lits disponibles pour une spécialité donnée.
     */
    interface GeoAvailabilityProjection {
        Long getHospitalId();
        String getHospitalName();
        String getCity();
        Double getLatitude();
        Double getLongitude();
        Double getDistanceKm();
        Long getAvailableBeds();
    }

    /**
     * Recherche les hôpitaux actifs triés par proximité géographique par rapport à un point (lat, lon)
     * et renvoie, pour chacun, le nombre de lits disponibles pour la spécialité identifiée par son code.
     *
     * Notes:
     * - Requête native avec formule de Haversine (distance en kilomètres) pour le tri/filtre par proximité.
     * - Utilise une projection pour limiter les colonnes renvoyées et inclure un agrégat.
     * - Filtre sur les lits ayant le statut AVAILABLE.
     */
    @Query(value = """
            SELECT 
              h.id AS hospitalId,
              h.name AS hospitalName,
              h.city AS city,
              h.latitude AS latitude,
              h.longitude AS longitude,
              (6371 * acos(cos(radians(:lat)) * cos(radians(h.latitude)) *
                           cos(radians(h.longitude) - radians(:lng)) +
                           sin(radians(:lat)) * sin(radians(h.latitude)))) AS distanceKm,
              SUM(CASE WHEN b.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS availableBeds
            FROM hospital h
              JOIN bed b ON b.hospital_id = h.id
              JOIN specialty s ON s.id = b.specialty_id
            WHERE h.is_active = true
              AND s.code = :specialtyCode
            GROUP BY h.id, h.name, h.city, h.latitude, h.longitude
            HAVING SUM(CASE WHEN b.status = 'AVAILABLE' THEN 1 ELSE 0 END) > 0
               AND (6371 * acos(cos(radians(:lat)) * cos(radians(h.latitude)) *
                                cos(radians(h.longitude) - radians(:lng)) +
                                sin(radians(:lat)) * sin(radians(h.latitude)))) <= :radiusKm
            ORDER BY distanceKm ASC
            """, nativeQuery = true)
    List<GeoAvailabilityProjection> findAvailableBedsBySpecialtyAndLocation(
            @Param("specialtyCode") String specialtyCode,
            @Param("lat") double latitude,
            @Param("lng") double longitude,
            @Param("radiusKm") double radiusKm
    );
}
