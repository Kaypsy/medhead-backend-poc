package com.medhead.bedallocation.config;

import com.medhead.bedallocation.model.Bed;
import com.medhead.bedallocation.model.Hospital;
import com.medhead.bedallocation.model.Specialty;
import com.medhead.bedallocation.model.SpecialtyGroup;
import com.medhead.bedallocation.model.User;
import com.medhead.bedallocation.model.enums.BedStatus;
import com.medhead.bedallocation.repository.BedRepository;
import com.medhead.bedallocation.repository.HospitalRepository;
import com.medhead.bedallocation.repository.SpecialtyRepository;
import com.medhead.bedallocation.repository.SpecialtyGroupRepository;
import com.medhead.bedallocation.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Initialisation des données de référence et de test au démarrage.
 *
 * - Spécialités NHS de base (codes/groupes simplifiés)
 * - Hôpitaux de test avec coordonnées UK réalistes
 * - Lits pour chaque hôpital (20-30+)
 * - Comptes utilisateurs par défaut (admin et user)
 *
 * Chargé uniquement sur le profil "dev".
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyGroupRepository specialtyGroupRepository;
    private final HospitalRepository hospitalRepository;
    private final BedRepository bedRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("[DataInitializer] Démarrage de l'initialisation des données (profil dev)");

        initSpecialties();
        initHospitalsAndBeds();
        initUsers();

        log.info("[DataInitializer] Initialisation terminée");
    }

    private void initSpecialties() {
        if (specialtyRepository.count() > 0) {
            log.info("[DataInitializer] Spécialités déjà présentes: {} — aucune création", specialtyRepository.count());
            return;
        }

        // Jeu de références abrégé basé sur des familles NHS courantes
        record Spec(String code, String name, String group) {}
        List<Spec> base = List.of(
                new Spec("CARD", "Cardiology", "Medicine"),
                new Spec("NEUR", "Neurology", "Medicine"),
                new Spec("ORTH", "Trauma and Orthopaedics", "Surgery"),
                new Spec("ONCO", "Oncology", "Medicine"),
                new Spec("PED", "Paediatrics", "Medicine"),
                new Spec("EMER", "Emergency Medicine", "Acute Care"),
                new Spec("GENM", "General Medicine", "Medicine"),
                new Spec("GSUR", "General Surgery", "Surgery"),
                new Spec("PSYC", "Psychiatry", "Mental Health"),
                new Spec("DERM", "Dermatology", "Medicine")
        );

        // Ensure SpecialtyGroup entities exist for each group in base
        Map<String, SpecialtyGroup> groupByName = base.stream()
                .map(Spec::group)
                .distinct()
                .collect(Collectors.toMap(g -> g, g -> ensureGroupExists(g)));

        List<Specialty> entities = base.stream().map(s -> {
            Specialty sp = new Specialty();
            sp.setCode(s.code());
            sp.setName(s.name());
            sp.setSpecialtyGroup(groupByName.get(s.group()));
            sp.setDescription("Reference NHS specialty: " + s.name());
            sp.setIsActive(true);
            return sp;
        }).collect(Collectors.toList());

        specialtyRepository.saveAll(entities);
        log.info("[DataInitializer] {} spécialités NHS créées", entities.size());
    }

    private SpecialtyGroup ensureGroupExists(String groupName) {
        String code = groupName.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
        return specialtyGroupRepository.findByCode(code)
                .orElseGet(() -> {
                    SpecialtyGroup g = new SpecialtyGroup();
                    g.setCode(code);
                    g.setName(groupName);
                    g.setIsActive(true);
                    return specialtyGroupRepository.save(g);
                });
    }

    private void initHospitalsAndBeds() {
        if (hospitalRepository.count() > 0) {
            long hospitals = hospitalRepository.count();
            long beds = bedRepository.count();
            log.info("[DataInitializer] Hôpitaux ({}) et lits ({}) déjà présents — aucune création", hospitals, beds);
            return;
        }

        Map<String, Specialty> specByCode = specialtyRepository.findAll().stream()
                .collect(Collectors.toMap(Specialty::getCode, s -> s));

        // Définition de 5 hôpitaux UK avec coordonnées réalistes
        class HDef {
            final String name, city, address, postal; final double lat, lon; final String phone; final String[] specCodes;
            HDef(String name, String city, String address, String postal, double lat, double lon, String phone, String... specCodes) {
                this.name = name; this.city = city; this.address = address; this.postal = postal; this.lat = lat; this.lon = lon; this.phone = phone; this.specCodes = specCodes; }
        }

        List<HDef> defs = List.of(
                new HDef("St Thomas' Hospital", "London", "Westminster Bridge Rd", "SE1 7EH", 51.4980, -0.1170, "+44 20 7188 7188", "EMER","CARD","GSUR","GENM"),
                new HDef("Manchester Royal Infirmary", "Manchester", "Oxford Rd", "M13 9WL", 53.4631, -2.2256, "+44 161 276 1234", "EMER","ORTH","NEUR","GENM"),
                new HDef("Queen Elizabeth Hospital Birmingham", "Birmingham", "Mindelsohn Way", "B15 2WB", 52.4527, -1.9430, "+44 121 627 2000", "EMER","ONCO","GSUR","CARD"),
                new HDef("Queen Elizabeth University Hospital", "Glasgow", "1345 Govan Rd", "G51 4TF", 55.8609, -4.3476, "+44 141 201 1100", "EMER","PED","PSYC","GENM"),
                new HDef("St James's University Hospital", "Leeds", "Beckett St", "LS9 7TF", 53.8067, -1.5200, "+44 113 243 3144", "EMER","DERM","ORTH","GENM")
        );

        List<Hospital> hospitals = new ArrayList<>();
        for (HDef d : defs) {
            Hospital h = new Hospital();
            h.setName(d.name);
            h.setAddress(d.address);
            h.setCity(d.city);
            h.setPostalCode(d.postal);
            h.setLatitude(d.lat);
            h.setLongitude(d.lon);
            h.setPhoneNumber(d.phone);
            h.setIsActive(true);
            // Associer les spécialités
            Set<Specialty> specs = Arrays.stream(d.specCodes)
                    .map(code -> {
                        Specialty s = specByCode.get(code);
                        if (s == null) {
                            log.warn("[DataInitializer] Spécialité inconnue {} — ignorée pour {}", code, d.name);
                        }
                        return s;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            h.setSpecialties(specs);
            hospitals.add(h);
        }

        hospitalRepository.saveAll(hospitals);
        log.info("[DataInitializer] {} hôpitaux créés", hospitals.size());

        // Créer 24 à 30 lits par hôpital
        Random rnd = new Random(42);
        int totalBedsCreated = 0;
        List<Bed> toSave = new ArrayList<>();
        for (Hospital h : hospitals) {
            List<Specialty> specs = new ArrayList<>(h.getSpecialties());
            if (specs.isEmpty()) {
                // fallback: utiliser General Medicine si disponible
                Specialty gen = specByCode.get("GENM");
                if (gen != null) specs = List.of(gen);
            }
            int bedCount = 24 + rnd.nextInt(7); // 24..30
            for (int i = 1; i <= bedCount; i++) {
                Bed b = new Bed();
                b.setHospital(h);
                Specialty spec = specs.get((i - 1) % specs.size());
                b.setSpecialty(spec);
                b.setBedNumber(String.format("%03d", i));
                b.setRoomNumber(String.format("R%02d", ((i - 1) / 2) + 1));
                b.setFloor(1 + ((i - 1) / 10));
                // Statut: majoritairement AVAILABLE, quelques OCCUPIED/MAINTENANCE/RESERVED
                int roll = rnd.nextInt(100);
                BedStatus status = (roll < 70) ? BedStatus.AVAILABLE : (roll < 85) ? BedStatus.OCCUPIED : (roll < 95) ? BedStatus.MAINTENANCE : BedStatus.RESERVED;
                b.setStatus(status);
                // isAvailable se synchronise via @PrePersist, mais on fixe par cohérence
                b.setIsAvailable(status == BedStatus.AVAILABLE);
                toSave.add(b);
            }
            totalBedsCreated += bedCount;
            h.setTotalBeds(bedCount);
            // availableBeds sera recalculé par callbacks lors de la persistance des lits
        }

        bedRepository.saveAll(toSave);
        log.info("[DataInitializer] {} lits créés ({} à {} par hôpital)", totalBedsCreated, 24, 30);
    }

    private void initUsers() {
        // Admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // A CHANGER EN PROD
            admin.setEmail("admin@local.dev");
            admin.setRoles("ROLE_ADMIN");
            admin.setIsActive(true);
            userRepository.save(admin);
            log.info("[DataInitializer] Utilisateur ADMIN créé (username=admin)");
        } else {
            log.info("[DataInitializer] Utilisateur ADMIN déjà présent (username=admin)");
        }

        // User standard
        if (!userRepository.existsByUsername("user")) {
            User u = new User();
            u.setUsername("user");
            u.setPassword(passwordEncoder.encode("user123"));
            u.setEmail("user@local.dev");
            u.setRoles("ROLE_USER");
            u.setIsActive(true);
            userRepository.save(u);
            log.info("[DataInitializer] Utilisateur standard créé (username=user)");
        } else {
            log.info("[DataInitializer] Utilisateur standard déjà présent (username=user)");
        }
    }
}
