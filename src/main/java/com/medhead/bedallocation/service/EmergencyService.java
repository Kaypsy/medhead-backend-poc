package com.medhead.bedallocation.service;

import com.medhead.bedallocation.dto.EmergencyRequestDTO;
import com.medhead.bedallocation.dto.EmergencyResponseDTO;

/**
 * Service pour la gestion des allocations d'urgence.
 */
public interface EmergencyService {

    /**
     * Recherche et alloue le meilleur hôpital disponible pour une urgence.
     *
     * @param request les détails de l'urgence (GPS, spécialité)
     * @return la réponse structurée avec l'hôpital recommandé
     */
    EmergencyResponseDTO allocate(EmergencyRequestDTO request);
}
