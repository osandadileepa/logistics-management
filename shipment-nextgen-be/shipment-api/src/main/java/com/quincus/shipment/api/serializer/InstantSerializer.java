package com.quincus.shipment.api.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class InstantSerializer extends JsonSerializer<Instant> {
    @Override
    public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(DateTimeFormatter.ISO_INSTANT.format(instant));
    }
}
