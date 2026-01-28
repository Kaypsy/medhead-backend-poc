package com.medhead.bedallocation.dto.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Désérialiseur personnalisé pour les champs Double.
 * Permet de traiter les nombres fournis sous forme de chaînes avec une virgule (format français)
 * ou un point (format anglophone).
 */
@Slf4j
public class CustomDoubleDeserializer extends JsonDeserializer<Double> {

    @Override
    public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // Nettoyage et remplacement de la virgule par un point
        String normalizedValue = value.trim().replace(',', '.');

        try {
            return Double.parseDouble(normalizedValue);
        } catch (NumberFormatException e) {
            log.warn("Impossible de désérialiser la valeur Double : '{}'", value);
            return null; // On laisse la validation Jakarta s'en occuper via @NotNull si nécessaire
        }
    }
}
