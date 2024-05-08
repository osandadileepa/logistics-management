package com.quincus.shipment.kafka.consumers.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@UtilityClass
public class KakfaPayloadFieldExtractor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String extractField(String json, String fieldName) {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(json);
            JsonNode rootOrganizationIdNode = jsonNode.get(fieldName);
            if (rootOrganizationIdNode != null) {
                return rootOrganizationIdNode.asText();
            }

            return findFieldValue(jsonNode, fieldName);
        } catch (Exception e) {
            log.trace("Parsing error '{}'", e.getMessage());
            return null;
        }
    }

    private static String findFieldValue(JsonNode node, String fieldName) {
        if (node.isObject()) {
            JsonNode fieldValue = node.get(fieldName);
            if (fieldValue != null) {
                return fieldValue.asText();
            }

            for (JsonNode child : node) {
                String result = findFieldValue(child, fieldName);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

}
