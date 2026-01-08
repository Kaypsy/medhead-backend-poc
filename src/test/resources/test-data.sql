-- Nettoyage des tables (ordre respectant les FK)
DELETE FROM bed;
DELETE FROM hospital_specialty;
DELETE FROM hospital;
DELETE FROM specialty;
DELETE FROM specialty_group;

-- Groupes de spécialités
INSERT INTO specialty_group (id, code, name, is_active)
VALUES
  (1, 'MED',  'Médecine', true),
  (2, 'SURG', 'Chirurgie', true);

-- Spécialités
INSERT INTO specialty (id, code, name, specialty_group, description, is_active)
VALUES 
  (1, 'CARD', 'Cardiologie', 1, 'Cardiology specialty', true),
  (2, 'NEUR', 'Neurologie',  1, 'Neurology specialty',  true),
  (3, 'ORTH', 'Orthopédie',  2, 'Orthopedics specialty',true),
  (8, 'SPEC8', 'Spécialité 8', 1, 'Test Specialty 8', true),
  (9, 'SPEC9', 'Spécialité 9', 1, 'Test Specialty 9', true);

-- Hôpitaux
INSERT INTO hospital (id, name, address, city, postal_code, latitude, longitude, phone_number, total_beds, available_beds, is_active)
VALUES 
  (10, 'Hôpital Saint-Louis', '12 Rue A', 'Paris', '75010', 48.877, 2.370, '0102030405', 200, 0, true),
  (11, 'Hôpital de la Pitié', '34 Rue B', 'Paris', '75013', 48.836, 2.362, '0102030406', 300, 0, true),
  (12, 'Hôpital Lyon Centre', '56 Rue C', 'Lyon',  '69001', 45.764, 4.835, '0102030407', 150, 0, false);

-- Associations hôpital <-> spécialité
INSERT INTO hospital_specialty (hospital_id, specialty_id) VALUES 
  (10, 1), -- Paris, CARD
  (10, 2), -- Paris, NEUR
  (11, 1), -- Paris, CARD
  (11, 3), -- Paris, ORTH
  (12, 2); -- Lyon (inactif), NEUR

-- Lits
-- Hôpital 10 (Paris) - CARD: 2 disponibles, 1 occupé
INSERT INTO bed (id, hospital_id, specialty_id, bed_number, room_number, floor, status, is_available)
VALUES 
  (100, 10, 1, 'A-101', 'A1', 1, 'AVAILABLE',  true),
  (101, 10, 1, 'A-102', 'A1', 1, 'AVAILABLE',  true),
  (102, 10, 1, 'A-103', 'A1', 1, 'OCCUPIED',   false);

-- Hôpital 10 (Paris) - NEUR: 1 maintenance (non disponible)
INSERT INTO bed (id, hospital_id, specialty_id, bed_number, room_number, floor, status, is_available)
VALUES 
  (103, 10, 2, 'N-201', 'N2', 2, 'MAINTENANCE', false);

-- Hôpital 11 (Paris) - CARD: 1 disponible, 1 réservé (non dispo)
INSERT INTO bed (id, hospital_id, specialty_id, bed_number, room_number, floor, status, is_available)
VALUES 
  (110, 11, 1, 'C-301', 'C3', 3, 'AVAILABLE',  true),
  (111, 11, 1, 'C-302', 'C3', 3, 'RESERVED',   false);

-- Hôpital 11 (Paris) - ORTH: 1 occupé
INSERT INTO bed (id, hospital_id, specialty_id, bed_number, room_number, floor, status, is_available)
VALUES 
  (112, 11, 3, 'O-401', 'O4', 4, 'OCCUPIED',   false);

-- Hôpital 12 (Lyon, inactif) - NEUR: 1 disponible (ne doit pas apparaître dans les requêtes filtrant sur hôpitaux actifs)
INSERT INTO bed (id, hospital_id, specialty_id, bed_number, room_number, floor, status, is_available)
VALUES 
  (120, 12, 2, 'L-501', 'L5', 5, 'AVAILABLE',  true);
