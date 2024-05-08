package com.quincus.shipment.api.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.quincus.shipment.api.domain.PackageJourneySegment;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

public class PackageJourneySegmentListSerializer extends JsonSerializer<List<PackageJourneySegment>> {
    @Override
    public void serialize(List<PackageJourneySegment> segments, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        List<PackageJourneySegment> filteredSegments = segments.stream()
                .filter(Predicate.not(PackageJourneySegment::isDeleted)).toList();
        jsonGenerator.writeStartArray();
        for (PackageJourneySegment segment : filteredSegments) {
            jsonGenerator.writeObject(segment);
        }
        jsonGenerator.writeEndArray();
    }
}
