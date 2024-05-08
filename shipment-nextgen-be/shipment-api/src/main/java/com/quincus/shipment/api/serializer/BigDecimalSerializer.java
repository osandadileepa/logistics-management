package com.quincus.shipment.api.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal bigDecimal, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        if (isInteger(bigDecimal)) {
            jsonGenerator.writeNumber(bigDecimal.toBigInteger());
        } else {
            jsonGenerator.writeNumber(bigDecimal);
        }
    }

    private boolean isInteger(BigDecimal bd) {
        return bd.stripTrailingZeros().scale() <= 0;
    }
}
