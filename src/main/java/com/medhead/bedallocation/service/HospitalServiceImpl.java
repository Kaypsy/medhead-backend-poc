package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.HospitalCreateDTO;
import com.medhead.bedallocation.dto.HospitalDTO;
import com.medhead.bedallocation.dto.HospitalSummaryDTO;
import com.medhead.bedallocation.dto.HospitalUpdateDTO;
import com.medhead.bedallocation.mapper.HospitalMapper;
import com.medhead.bedallocation.model.Hospital;
import com.medhead.bedallocation.model.Specialty;
import com.medhead.bedallocation.model.enums.BedStatus;
import com.medhead.bedallocation.repository.BedRepository;
import com.medhead.bedallocation.repository.HospitalRepository;
import com.medhead.bedallocation.repository.SpecialtyRepository;
import com.medhead.bedallocation.service.exception.BadRequestException;
import com.medhead.bedallocation.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implémentation du service métier Hospital.
 *
 * Rappels de conception:
 * - Utilise MapStruct pour la conversion Entity/DTO.
 * - Les lectures sont en readOnly; les opérations d'écriture sont transactionnelles.
 * - Les validations des paramètres entrées sont réalisées avant appel repository.
 * - Les règles de disponibilité sont basées sur le statut des lits (AVAILABLE).
 * - Calcul de distance géographique via la formule de Haversine (cf. méthode privée).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final SpecialtyRepository specialtyRepository;
    private final BedRepository bedRepository;
    private final HospitalMapper hospitalMapper;

    // --------------------- CRUD ---------------------
    @Override
    public List<HospitalSummaryDTO> findAll() {
        log.debug("[HospitalService] findAll called");
        List<Hospital> hospitals = hospitalRepository.findAll();
        return hospitalMapper.toSummaryDtoList(hospitals);
    }

    @Override
    public HospitalDTO findById(Long id) {
        if (id == null) throw new BadRequestException("L'identifiant hôpital est requis");
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hôpital introuvable avec id=" + id));
        return hospitalMapper.toDto(hospital);
    }

    @Override
    @Transactional
    public HospitalDTO create(HospitalCreateDTO dto) {
        if (dto == null) throw new BadRequestException("Le payload de création hospital est requis");
        validateCoordinates(dto.getLatitude(), dto.getLongitude());

        Set<Specialty> specialties = resolveSpecialties(dto.getSpecialtyIds());
        Hospital entity = hospitalMapper.fromCreateDto(dto, specialties);
        entity.recalculateAvailableBeds();
        Hospital saved = hospitalRepository.save(entity);
        log.info("Hôpital créé: id={}, name={}", saved.getId(), saved.getName());
        return hospitalMapper.toDto(saved);
    }

    @Override
    @Transactional
    public HospitalDTO update(Long id, HospitalUpdateDTO dto) {
        if (id == null) throw new BadRequestException("L'identifiant hôpital est requis");
        if (dto == null) throw new BadRequestException("Le payload de mise à jour est requis");
        Hospital entity = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hôpital introuvable avec id=" + id));

        if (dto.getLatitude() != null || dto.getLongitude() != null) {
            Double lat = dto.getLatitude() != null ? dto.getLatitude() : entity.getLatitude();
            Double lon = dto.getLongitude() != null ? dto.getLongitude() : entity.getLongitude();
            validateCoordinates(lat, lon);
        }

        Set<Specialty> specialties = resolveSpecialties(dto.getSpecialtyIds());
        hospitalMapper.updateEntity(entity, dto, specialties);
        entity.recalculateAvailableBeds();
        Hospital saved = hospitalRepository.save(entity);
        log.info("Hôpital mis à jour: id={}", saved.getId());
        return hospitalMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new BadRequestException("L'identifiant hôpital est requis");
        Hospital entity = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hôpital introuvable avec id=" + id));
        hospitalRepository.delete(entity);
        log.warn("Hôpital supprimé: id={}", id);
    }

    // --------------------- Recherches métier ---------------------
    @Override
    public List<HospitalSummaryDTO> findAllAvailable() {
        log.debug("[HospitalService] findAllAvailable called");
        List<Hospital> actives = hospitalRepository.findByIsActiveTrue();
        List<Hospital> withAvailability = actives.stream()
                .filter(h -> h.getAvailableBeds() != null && h.getAvailableBeds() > 0)
                .collect(Collectors.toList());
        return hospitalMapper.toSummaryDtoList(withAvailability);
    }

    @Override
    public List<HospitalSummaryDTO> findByCity(String city) {
        if (!StringUtils.hasText(city)) throw new BadRequestException("La ville est requise");
        List<Hospital> hospitals = hospitalRepository.findByCityAndIsActiveTrue(city);
        return hospitalMapper.toSummaryDtoList(hospitals);
    }

    @Override
    public List<HospitalSummaryDTO> findBySpecialtyCode(String specialtyCode) {
        if (!StringUtils.hasText(specialtyCode)) throw new BadRequestException("Le code spécialité est requis");
        List<Hospital> hospitals = hospitalRepository.findBySpecialties_Code(specialtyCode);
        // Filtrer aux hôpitaux actifs uniquement par sûreté
        hospitals = hospitals.stream().filter(h -> Boolean.TRUE.equals(h.getIsActive())).collect(Collectors.toList());
        return hospitalMapper.toSummaryDtoList(hospitals);
    }

    @Override
    public List<HospitalSummaryDTO> findNearestHospitalsWithAvailability(double lat, double lon, String specialtyCode, int limit) {
        validateCoordinates(lat, lon);
        if (!StringUtils.hasText(specialtyCode)) throw new BadRequestException("Le code spécialité est requis");
        if (limit <= 0) throw new BadRequestException("Le paramètre limit doit être > 0");

        // On récupère une projection des hôpitaux actifs ayant au moins un lit disponible pour la spécialité,
        // puis on calcule/ordonne localement par distance (Haversine) pour limiter à 'limit'.
        List<HospitalRepository.HospitalAvailabilityProjection> projections =
                hospitalRepository.findActiveHospitalsWithAvailableBedsBySpecialty(specialtyCode);

        List<HospitalSummaryDTO> sorted = projections.stream()
                .map(p -> HospitalSummaryDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .city(p.getCity())
                        .availableBeds(p.getAvailableBeds() != null ? p.getAvailableBeds().intValue() : 0)
                        .latitude(p.getLatitude())
                        .longitude(p.getLongitude())
                        .build())
                .sorted(Comparator.comparingDouble(h -> haversineKm(lat, lon, h.getLatitude(), h.getLongitude())))
                .limit(limit)
                .collect(Collectors.toList());

        return sorted;
    }

    // --------------------- Associations spécialités ---------------------
    @Override
    @Transactional
    public HospitalDTO addSpecialtyToHospital(Long hospitalId, Long specialtyId) {
        if (hospitalId == null || specialtyId == null)
            throw new BadRequestException("hospitalId et specialtyId sont requis");
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hôpital introuvable avec id=" + hospitalId));
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Spécialité introuvable avec id=" + specialtyId));

        Set<Specialty> set = hospital.getSpecialties();
        if (set == null) set = new HashSet<>();
        boolean added = set.add(specialty);
        hospital.setSpecialties(set);
        if (added) {
            Hospital saved = hospitalRepository.save(hospital);
            log.info("Spécialité id={} ajoutée à l'hôpital id={}", specialtyId, hospitalId);
            return hospitalMapper.toDto(saved);
        }
        log.debug("Association déjà existante entre hôpital id={} et spécialité id={}", hospitalId, specialtyId);
        return hospitalMapper.toDto(hospital);
    }

    @Override
    @Transactional
    public HospitalDTO removeSpecialtyFromHospital(Long hospitalId, Long specialtyId) {
        if (hospitalId == null || specialtyId == null)
            throw new BadRequestException("hospitalId et specialtyId sont requis");
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hôpital introuvable avec id=" + hospitalId));
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Spécialité introuvable avec id=" + specialtyId));

        Set<Specialty> set = hospital.getSpecialties();
        if (set != null && set.remove(specialty)) {
            Hospital saved = hospitalRepository.save(hospital);
            log.info("Spécialité id={} retirée de l'hôpital id={}", specialtyId, hospitalId);
            return hospitalMapper.toDto(saved);
        }
        log.debug("Aucune association à retirer entre hôpital id={} et spécialité id={}", hospitalId, specialtyId);
        return hospitalMapper.toDto(hospital);
    }

    // --------------------- Disponibilités ---------------------
    @Override
    @Transactional
    public HospitalDTO updateAvailableBeds(Long hospitalId) {
        if (hospitalId == null) throw new BadRequestException("hospitalId est requis");
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hôpital introuvable avec id=" + hospitalId));

        long availableCount = bedRepository.countByHospitalIdAndStatus(hospitalId, BedStatus.AVAILABLE);
        hospital.setAvailableBeds((int) availableCount);
        Hospital saved = hospitalRepository.save(hospital);
        log.info("Disponibilités recalculées pour l'hôpital id={} => {} lits dispo", hospitalId, availableCount);
        return hospitalMapper.toDto(saved);
    }

    // --------------------- Utilitaires ---------------------
    private void validateCoordinates(Double lat, Double lon) {
        if (lat == null || lon == null)
            throw new BadRequestException("Latitude et longitude sont requis");
        if (lat < -90.0 || lat > 90.0)
            throw new BadRequestException("Latitude hors bornes [-90;90]");
        if (lon < -180.0 || lon > 180.0)
            throw new BadRequestException("Longitude hors bornes [-180;180]");
    }

    private Set<Specialty> resolveSpecialties(List<Long> specialtyIds) {
        if (specialtyIds == null || specialtyIds.isEmpty()) return null;
        Set<Specialty> set = specialtyIds.stream()
                .map(id -> specialtyRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Spécialité introuvable avec id=" + id)))
                .collect(Collectors.toSet());
        return set;
    }

    /**
     * Calcule la distance en kilomètres entre deux points géographiques à l'aide de la formule de Haversine.
     * Cette formule suppose la Terre comme une sphère (R=6371km) et fournit une approximation suffisante
     * pour des distances intra-pays. Complexité O(1).
     */
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Rayon moyen de la Terre en kilomètres
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
