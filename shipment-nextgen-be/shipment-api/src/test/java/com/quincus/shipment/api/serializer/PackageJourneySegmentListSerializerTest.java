package com.quincus.shipment.api.serializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import lombok.Setter;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PackageJourneySegmentListSerializerTest {

    private static final ObjectMapper mapper = initializeObjectMapper();

    private static ObjectMapper initializeObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return objectMapper;
    }

    private static Stream<Arguments> provideDummiesWithSegmentList() {
        PackageJourneySegment activeSegment1 = new PackageJourneySegment();
        activeSegment1.setRefId("0");
        PackageJourneySegment activeSegment2 = new PackageJourneySegment();
        activeSegment2.setRefId("1");
        activeSegment2.setDeleted(false);

        PackageJourneySegment deletedSegment1 = new PackageJourneySegment();
        deletedSegment1.setRefId("0");
        deletedSegment1.setDeleted(true);
        PackageJourneySegment deletedSegment2 = new PackageJourneySegment();
        deletedSegment2.setRefId("1");
        deletedSegment2.setDeleted(true);

        List<PackageJourneySegment> segmentsAllActive = new ArrayList<>(List.of(activeSegment1, activeSegment2));
        List<PackageJourneySegment> segmentsSomeActive = new ArrayList<>(List.of(activeSegment1, deletedSegment1));
        List<PackageJourneySegment> segmentsAllDeleted = new ArrayList<>(List.of(deletedSegment1, deletedSegment2));

        DummyDomain withAllSegmentsActive = new DummyDomain();
        withAllSegmentsActive.setSegments(segmentsAllActive);

        DummyDomain withSomeSegmentsActive = new DummyDomain();
        withSomeSegmentsActive.setSegments(segmentsSomeActive);

        DummyDomain withAllSegmentsDeleted = new DummyDomain();
        withAllSegmentsDeleted.setSegments(segmentsAllDeleted);

        String activeSegment1Str = getJsonString(activeSegment1);
        String activeSegment2Str = getJsonString(activeSegment2);

        return Stream.of(
                Arguments.of(Named.of("All Segments are active", withAllSegmentsActive),
                        "{\"segments\":[" + activeSegment1Str + "," + activeSegment2Str + "]}"),
                Arguments.of(Named.of("Some Segments are active", withSomeSegmentsActive),
                        "{\"segments\":[" + activeSegment1Str + "]}"),
                Arguments.of(Named.of("All Segments are deleted", withAllSegmentsDeleted),
                        "{\"segments\":[]}")
        );
    }

    private static String getJsonString(PackageJourneySegment segment) {
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(segment);
        } catch (JsonProcessingException e) {
            jsonString = "";
        }
        return jsonString;
    }

    @ParameterizedTest
    @MethodSource("provideDummiesWithSegmentList")
    void serialize_variousSegmentListInputs_shouldFilterAccordingly(DummyDomain dummy, String expectedJsonLine) throws JsonProcessingException {
        assertThat(mapper.writeValueAsString(dummy)).isEqualTo(expectedJsonLine);
    }

    @Setter
    public static class DummyDomain {
        @JsonSerialize(using = PackageJourneySegmentListSerializer.class)
        private List<PackageJourneySegment> segments;
    }
}
