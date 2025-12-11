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
import java.text.Normalizer;
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
        // Ne pas quitter si des données existent déjà: l'initialisation est idempotente

        // Mapping complet fourni: Groupe de spécialité => Liste des spécialités
        Map<String, List<String>> mapping = new LinkedHashMap<>();

        mapping.put("Anesthésie", List.of(
                "Anesthésie",
                "Soins intensifs"
        ));

        mapping.put("Oncologie clinique", List.of(
                "Oncologie clinique"
        ));

        mapping.put("Groupe dentaire", List.of(
                "Spécialités dentaires supplémentaires",
                "Radiologie dentaire et maxillo-faciale",
                "Endodontie",
                "Chirurgie buccale et maxillo-faciale",
                "Pathologie buccale et maxillo-faciale",
                "Médecine buccale",
                "Chirurgie buccale",
                "Orthodontie",
                "Dentisterie pédiatrique",
                "Parodontie",
                "Prosthodontie",
                "Dentisterie restauratrice",
                "Dentisterie de soins spéciaux"
        ));

        mapping.put("Médecine d'urgence", List.of(
                "Médecine d'urgence"
        ));

        mapping.put("Groupe de médecine générale", List.of(
                "Médecine interne de soins aigus",
                "Allergie",
                "Médecine audiovestibulaire",
                "Cardiologie",
                "Génétique clinique",
                "Neurophysiologie clinique",
                "Pharmacologie clinique et thérapeutique",
                "Dermatologie",
                "Endocrinologie et diabète sucré",
                "Gastroentérologie",
                "Médecine générale (interne)",
                "Médecine générale",
                "Médecine générale (GP) 6 mois",
                "Médecine génito-urinaire",
                "Médecine gériatrique",
                "Maladies infectieuses",
                "Oncologie médicale",
                "Ophtalmologie médicale",
                "Neurologie",
                "Médecine du travail",
                "Autre",
                "Médecine palliative",
                "Médecine de réadaptation",
                "Médecine rénale",
                "Médecine respiratoire",
                "Rhumatologie",
                "Médecine du sport et de l'exercice"
        ));

        mapping.put("Obstétrique et gynécologie", List.of(
                "Santé publique sexuelle et procréative"
        ));

        mapping.put("Groupe pédiatrique", List.of(
                "Cardiologie pédiatrique",
                "Pédiatrie"
        ));

        mapping.put("Groupe de pathologie", List.of(
                "Pathologie chimique",
                "Neuropathologie diagnostique",
                "Histopathologie médico-légale",
                "Pathologie générale",
                "Hématologie",
                "Histopathologie",
                "Immunologie",
                "Microbiologie médicale",
                "Pathologie pédiatrique et périnatale",
                "Virologie"
        ));

        mapping.put("Groupe Pronostics et gestion de la santé/Santé communautaire", List.of(
                "Service de santé communautaire dentaire",
                "Service de santé communautaire médicale",
                "Santé publique dentaire",
                "Pratique de l’art dentaire",
                "Santé publique"
        ));

        mapping.put("Groupe de psychiatrie", List.of(
                "Psychiatrie infantile et adolescente",
                "Psychiatrie légale",
                "Psychiatrie générale",
                "Psychiatrie de la vieillesse",
                "Psychiatrie des troubles d'apprentissage",
                "Psychothérapie"
        ));

        mapping.put("Groupe de radiologie", List.of(
                "Radiologie clinique",
                "Médecine nucléaire"
        ));

        mapping.put("Groupe chirurgical", List.of(
                "Chirurgie cardiothoracique",
                "Chirurgie générale",
                "Neurochirurgie",
                "Ophtalmologie",
                "Otolaryngologie",
                "Chirurgie pédiatrique",
                "Chirurgie plastique",
                "Traumatologie et chirurgie orthopédique",
                "Urologie",
                "Chirurgie vasculaire"
        ));

        int createdGroups = 0;
        int createdSpecs = 0;

        for (Map.Entry<String, List<String>> entry : mapping.entrySet()) {
            String groupName = entry.getKey();
            SpecialtyGroup group = ensureGroupExists(groupName);
            // Count creation by checking if just created is not straightforward; skip exact count for groups
            for (String specName : entry.getValue()) {
                String specCode = toCode(specName);
                Optional<Specialty> existing = specialtyRepository.findByCode(specCode);
                if (existing.isEmpty()) {
                    Specialty sp = new Specialty();
                    sp.setCode(specCode);
                    sp.setName(specName);
                    sp.setSpecialtyGroup(group);
                    sp.setDescription("Spécialité: " + specName + " (groupe: " + groupName + ")");
                    sp.setIsActive(true);
                    specialtyRepository.save(sp);
                    createdSpecs++;
                }
            }
        }

        log.info("[DataInitializer] Groupes de spécialités préparés: {} (créations nouvelles non comptabilisées); Spécialités créées: {}", mapping.size(), createdSpecs);
    }

    private SpecialtyGroup ensureGroupExists(String groupName) {
        String code = toCode(groupName);
        return specialtyGroupRepository.findByCode(code)
                .orElseGet(() -> {
                    SpecialtyGroup g = new SpecialtyGroup();
                    g.setCode(code);
                    g.setName(groupName);
                    g.setIsActive(true);
                    return specialtyGroupRepository.save(g);
                });
    }

    private String toCode(String value) {
        if (value == null) return null;
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String code = normalized
                .toUpperCase(Locale.ROOT)
                .replace("’", "'")
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        return code;
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
