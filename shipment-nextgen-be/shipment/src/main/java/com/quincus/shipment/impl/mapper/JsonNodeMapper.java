package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class JsonNodeMapper {
    
    static JsonNode toJsonNode(Object obj, ObjectMapper mapper) {
        return mapper.convertValue(obj, JsonNode.class);
    }

    static <T> T toObject(JsonNode json, Class<T> objClass, ObjectMapper mapper) {
        return mapper.convertValue(json, objClass);
    }
}
