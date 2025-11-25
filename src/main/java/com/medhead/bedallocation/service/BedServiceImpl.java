package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.*;
import com.medhead.bedallocation.mapper.BedMapper;
import com.medhead.bedallocation.model.Bed;
import com.medhead.bedallocation.model.Hospital;
import com.medhead.bedallocation.model.Specialty;
import com.medhead.bedallocation.model.enums.BedStatus;
import com.medhead.bedallocation.repository.BedRepository;
import com.medhead.bedallocation.repository.HospitalRepository;
import com.medhead.bedallocation.repository.SpecialtyRepository;
import com.medhead.bedallocation.service.exception.BadRequestException;
import com.medhead.bedallocation.service.exception.ResourceNotFoundException;
import com.medhead.bedallocation.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BedServiceImpl implements BedService {

    private final BedRepository bedRepository;
    private final HospitalRepository hospitalRepository;
    private final SpecialtyRepository specialtyRepository;
    private final BedMapper bedMapper;

    // -------- CRUD --------
    @Override
    public List<BedDTO> findAll() {
        log.debug("[BedService] findAll called");
        return bedMapper.toDtoList(bedRepository.findAll());
    }

    @Override
    public BedDTO findById(Long id) {
        if (id == null) throw new BadRequestException("L'identifiant du lit est requis");
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lit introuvable avec id=" + id));
        return bedMapper.toDto(bed);
    }

    @Override
    @Transactional
    public BedDTO create(BedCreateDTO dto) {
        if (dto == null) throw new BadRequestException("Le payload de création est requis");
        if (dto.getHospitalId() == null || dto.getSpecialtyId() == null)
            throw new BadRequestException("hospitalId et specialtyId sont requis");

        // Vérifier l'existence des entités référencées
        Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hôpital introuvable avec id=" + dto.getHospitalId()));
        Specialty specialty = specialtyRepository.findById(dto.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Spécialité introuvable avec id=" + dto.getSpecialtyId()));

        Bed entity = bedMapper.fromCreateDto(dto);
        // Attacher les entités managées pour éviter des entités détachées
        entity.setHospital(hospital);
        entity.setSpecialty(specialty);

        Bed saved = bedRepository.save(entity);
        log.info("Lit créé: id={}, hospitalId={}, specialtyId={}, status={}", saved.getId(), hospital.getId(), specialty.getId(), saved.getStatus());
        return bedMapper.toDto(saved);
    }

    @Override
    @Transactional
    public BedDTO update(Long id, BedUpdateDTO dto) {
        if (id == null) throw new BadRequestException("L'identifiant du lit est requis");
        if (dto == null) throw new BadRequestException("Le payload de mise à jour est requis");
        Bed entity = bedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lit introuvable avec id=" + id));

        // Valider la transition de statut si fournie
        if (dto.getStatus() != null) {
            validateStatusTransition(entity.getStatus(), dto.getStatus());
            if (dto.getStatus() == BedStatus.OCCUPIED && entity.getStatus() != BedStatus.OCCUPIED) {
                entity.setLastOccupiedAt(LocalDateTime.now());
            }
            entity.setStatus(dto.getStatus());
        }

        // Valider l'existence du nouvel hôpital/spécialité si fournis
        if (dto.getHospitalId() != null) {
            Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hôpital introuvable avec id=" + dto.getHospitalId()));
            entity.setHospital(hospital);
        }
        if (dto.getSpecialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(dto.getSpecialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Spécialité introuvable avec id=" + dto.getSpecialtyId()));
            entity.setSpecialty(specialty);
        }

        // Autres champs via mapper
        bedMapper.updateEntity(entity, dto);

        Bed saved = bedRepository.save(entity);
        log.info("Lit mis à jour: id={}", saved.getId());
        return bedMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new BadRequestException("L'identifiant du lit est requis");
        Bed entity = bedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lit introuvable avec id=" + id));
        bedRepository.delete(entity);
        log.warn("Lit supprimé: id={}", id);
    }

    // -------- Recherches / opérations métier --------
    @Override
    public List<BedAvailabilityDTO> findAvailableByHospital(Long hospitalId) {
        if (hospitalId == null) throw new BadRequestException("hospitalId est requis");
        List<Bed> beds = bedRepository.findByHospitalIdAndStatus(hospitalId, BedStatus.AVAILABLE);
        return bedMapper.toAvailabilityDtoList(beds);
    }

    @Override
    public List<BedAvailabilityDTO> findAvailableBySpecialty(Long specialtyId) {
        if (specialtyId == null) throw new BadRequestException("specialtyId est requis");
        List<Bed> beds = bedRepository.findBySpecialtyIdAndStatus(specialtyId, BedStatus.AVAILABLE);
        return bedMapper.toAvailabilityDtoList(beds);
    }

    @Override
    @Transactional
    public BedDTO updateBedStatus(Long bedId, BedStatus newStatus) {
        if (bedId == null) throw new BadRequestException("bedId est requis");
        if (newStatus == null) throw new BadRequestException("newStatus est requis");
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Lit introuvable avec id=" + bedId));

        validateStatusTransition(bed.getStatus(), newStatus);
        if (newStatus == BedStatus.OCCUPIED && bed.getStatus() != BedStatus.OCCUPIED) {
            bed.setLastOccupiedAt(LocalDateTime.now());
        }
        bed.setStatus(newStatus);

        Bed saved = bedRepository.save(bed);
        log.info("Statut du lit mis à jour: id={}, oldStatus={}, newStatus={}", bedId, bed.getStatus(), newStatus);
        return bedMapper.toDto(saved);
    }

    @Override
    public List<BedAvailabilityDTO> findAvailableBedsForEmergency(String specialtyCode, double lat, double lon) {
        if (!StringUtils.hasText(specialtyCode)) throw new BadRequestException("specialtyCode est requis");
        validateCoordinates(lat, lon);

        // Récupérer les lits disponibles pour la spécialité
        List<Bed> beds = bedRepository.findByStatusAndSpecialty_Code(BedStatus.AVAILABLE, specialtyCode);

        // Trier par distance croissante par rapport aux coordonnées fournies
        beds.sort(Comparator.comparingDouble(b -> {
            Hospital h = b.getHospital();
            if (h == null || h.getLatitude() == null || h.getLongitude() == null) return Double.MAX_VALUE;
            return DistanceCalculator.haversineKm(lat, lon, h.getLatitude(), h.getLongitude());
        }));

        return bedMapper.toAvailabilityDtoList(beds);
    }

    @Override
    @Transactional
    public BedDTO reserveBed(Long bedId) {
        return updateBedStatus(bedId, BedStatus.RESERVED);
    }

    @Override
    @Transactional
    public BedDTO releaseBed(Long bedId) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Lit introuvable avec id=" + bedId));
        // Autoriser la remise en disponibilité depuis RESERVED, OCCUPIED, MAINTENANCE
        if (bed.getStatus() == BedStatus.AVAILABLE) {
            log.debug("Lit déjà disponible: id={}", bedId);
            return bedMapper.toDto(bed);
        }
        validateStatusTransition(bed.getStatus(), BedStatus.AVAILABLE);
        bed.setStatus(BedStatus.AVAILABLE);
        Bed saved = bedRepository.save(bed);
        log.info("Lit libéré: id={}", bedId);
        return bedMapper.toDto(saved);
    }

    // -------- Règles / validations --------
    private void validateStatusTransition(BedStatus from, BedStatus to) {
        if (from == null || to == null) return;
        if (from == to) return; // pas de changement

        switch (from) {
            case AVAILABLE -> {
                Set<BedStatus> allowed = Set.of(BedStatus.RESERVED, BedStatus.OCCUPIED, BedStatus.MAINTENANCE);
                if (!allowed.contains(to)) {
                    throw new BadRequestException("Transition de statut invalide: AVAILABLE -> " + to);
                }
            }
            case RESERVED -> {
                Set<BedStatus> allowed = Set.of(BedStatus.OCCUPIED, BedStatus.AVAILABLE);
                if (!allowed.contains(to)) {
                    throw new BadRequestException("Transition de statut invalide: RESERVED -> " + to);
                }
            }
            case OCCUPIED -> {
                Set<BedStatus> allowed = Set.of(BedStatus.AVAILABLE);
                if (!allowed.contains(to)) {
                    throw new BadRequestException("Transition de statut invalide: OCCUPIED -> " + to);
                }
            }
            case MAINTENANCE -> {
                Set<BedStatus> allowed = Set.of(BedStatus.AVAILABLE);
                if (!allowed.contains(to)) {
                    throw new BadRequestException("Transition de statut invalide: MAINTENANCE -> " + to);
                }
            }
            default -> throw new BadRequestException("Statut source inconnu: " + from);
        }
    }

    private void validateCoordinates(Double lat, Double lon) {
        if (lat == null || lon == null) throw new BadRequestException("Coordonnées lat/lon requises");
        if (lat < -90 || lat > 90) throw new BadRequestException("Latitude invalide: " + lat);
        if (lon < -180 || lon > 180) throw new BadRequestException("Longitude invalide: " + lon);
    }
}
