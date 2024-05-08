package com.quincus.networkmanagement.api.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.quincus.web.common.exception.model.InvalidFieldTypeException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String fieldName = jsonParser.getCurrentName();
        try {
            return new BigDecimal(jsonParser.getValueAsString()).setScale(2, RoundingMode.DOWN);
        } catch (Exception e) {
            throw new InvalidFieldTypeException("Invalid value", fieldName);
        }
    }
}
