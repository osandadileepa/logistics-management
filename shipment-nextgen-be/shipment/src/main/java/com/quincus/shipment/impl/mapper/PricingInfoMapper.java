package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.domain.PricingInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class PricingInfoMapper {

    public static JsonNode mapDomainToEntity(PricingInfo domain, ObjectMapper mapper) {
        return JsonNodeMapper.toJsonNode(domain, mapper);
    }

    public static PricingInfo mapEntityToDomain(JsonNode entity, ObjectMapper mapper) {
        return JsonNodeMapper.toObject(entity, PricingInfo.class, mapper);
    }
}
