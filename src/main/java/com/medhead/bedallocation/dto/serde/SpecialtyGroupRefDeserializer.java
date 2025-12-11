package com.medhead.bedallocation.dto.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.medhead.bedallocation.dto.SpecialtyGroupRefDTO;

import java.io.IOException;

/**
 * Custom deserializer allowing the field "specialtyGroup" to be provided either as:
 * - a numeric id (e.g. 3)
 * - a full object { id, code, name }
 */
public class SpecialtyGroupRefDeserializer extends JsonDeserializer<SpecialtyGroupRefDTO> {

    @Override
    public SpecialtyGroupRefDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_NUMBER_INT) {
            Long id = p.getLongValue();
            SpecialtyGroupRefDTO ref = new SpecialtyGroupRefDTO();
            ref.setId(id);
            return ref;
        }

        if (token == JsonToken.START_OBJECT) {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            ObjectNode node = mapper.readTree(p);
            return mapper.treeToValue(node, SpecialtyGroupRefDTO.class);
        }

        // For other types (e.g., null or string), return null to let validation handle it
        return null;
    }
}
