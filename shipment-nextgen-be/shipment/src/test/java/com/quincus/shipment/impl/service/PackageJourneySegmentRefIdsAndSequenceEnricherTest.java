package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.impl.helper.journey.PackageJourneySegmentRefIdsAndSequenceEnricher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PackageJourneySegmentRefIdsAndSequenceEnricherTest {
    @InjectMocks
    private PackageJourneySegmentRefIdsAndSequenceEnricher packageJourneySegmentRefIdsAndSequenceEnricher;


    @ParameterizedTest
    @MethodSource("provideSegmentsWithRefId")
    @DisplayName("Given segments with various refIds, When enriched, Then segments should have correct sequence and refIds")
    void shouldEnrichSegmentsWithCorrectSequenceAndRefIds(List<PackageJourneySegment> segments, List<String> expectedRefIds) {
        //Given
        List<PackageJourneySegment> newlyCreatedList = segments.stream().filter(segment -> StringUtils.isBlank(segment.getRefId())).toList();

        //When
        packageJourneySegmentRefIdsAndSequenceEnricher.enrichSegmentsWithTypesRefIdsAndSequence(segments);

        List<PackageJourneySegment> filteredCreatedList = segments.stream().filter(PackageJourneySegment::isNewlyCreated).toList();
        List<String> refIdList = segments.stream().map(PackageJourneySegment::getRefId).toList();

        //Then
        assertThat(filteredCreatedList).hasSameSizeAs(newlyCreatedList);
        assertThat(refIdList).isEqualTo(expectedRefIds);
        assertThat(new HashSet<>(segments)).hasSameSizeAs(segments);
    }

    private static Stream<Arguments> provideSegmentsWithRefId() {
        return Stream.of(
                Arguments.of(
                        createSegments("1", "2", "3"),
                        List.of("1", "2", "3")
                ),
                Arguments.of(
                        createSegments(null, null, null),
                        List.of("0", "1", "2")
                ),
                Arguments.of(
                        createSegments("1", "2", null),
                        List.of("1", "2", "3")
                ),
                Arguments.of(
                        createSegments("0", "3", null),
                        List.of("0", "3", "4")
                ),
                Arguments.of(
                        createSegments("0", null, "2", "3", null, "5"),
                        List.of("0", "6", "2", "3", "7", "5")
                ),
                Arguments.of(
                        createSegments("a", "2", "3"),
                        List.of("a", "2", "3")
                ),
                Arguments.of(
                        createSegments("a", null, null, "c", null),
                        List.of("a", "0", "1", "c", "2")
                ),
                Arguments.of(
                        createSegments("9", null, "7"),
                        List.of("9", "10", "7")
                ),
                Arguments.of(
                        createSegments(null, "a", "2", "b", null),
                        List.of("3", "a", "2", "b", "4")
                ),
                Arguments.of(
                        createSegments("a", null, "b", null, "c"),
                        List.of("a", "0", "b", "1", "c")
                ),
                Arguments.of(
                        createSegments("1", null, "2"),
                        List.of("1", "3", "2")
                ),
                Arguments.of(
                        createSegments("1", "b", "4", "c", null, "3"),
                        List.of("1", "b", "4", "c", "5", "3")
                ),
                Arguments.of(
                        createSegments("1", "", "", "", null, "3"),
                        List.of("1", "4", "5", "6", "7", "3")
                )
        );
    }

    private static List<PackageJourneySegment> createSegments(String... refIds) {
        return Stream.of(refIds)
                .map(refId -> {
                    PackageJourneySegment segment = new PackageJourneySegment();
                    segment.setRefId(refId);
                    return segment;
                })
                .toList();
    }
}