package com.quincus.shipment.impl.helper.journey;

import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class PackageJourneySegmentTypeAssignerTest {

    private final PackageJourneySegmentTypeAssigner packageJourneySegmentTypeAssigner = new PackageJourneySegmentTypeAssigner();

    @Test
    void givenMultipleNumberOfSegments_generatePackageJourneySegments_ThenCorrectlyAssignTypes() {
        List<PackageJourneySegment> segments = generatePackageJourneySegments(5);
        packageJourneySegmentTypeAssigner.assignSegmentTypes(segments);
        assertThat(segments.get(0).getType()).isEqualTo(SegmentType.FIRST_MILE);
        assertThat(segments.get(1).getType()).isEqualTo(SegmentType.MIDDLE_MILE);
        assertThat(segments.get(2).getType()).isEqualTo(SegmentType.MIDDLE_MILE);
        assertThat(segments.get(3).getType()).isEqualTo(SegmentType.MIDDLE_MILE);
        assertThat(segments.get(4).getType()).isEqualTo(SegmentType.LAST_MILE);
    }

    @Test
    void givenSingleSegment_generatePackageJourneySegments_ThenSegmentTypeIsLastMileOnly() {
        List<PackageJourneySegment> segments = generatePackageJourneySegments(1);
        packageJourneySegmentTypeAssigner.assignSegmentTypes(segments);
        assertThat(segments.get(0).getType()).isEqualTo(SegmentType.LAST_MILE);
    }

    @Test
    void givenTwoSegment_generatePackageJourneySegments_ThenNoMiddleMileIsCreated() {
        List<PackageJourneySegment> segments = generatePackageJourneySegments(2);
        packageJourneySegmentTypeAssigner.assignSegmentTypes(segments);
        assertThat(segments.get(0).getType()).isEqualTo(SegmentType.FIRST_MILE);
        assertThat(segments.get(1).getType()).isEqualTo(SegmentType.LAST_MILE);
    }

    @Test
    void givenEmptySegments_generatePackageJourneySegments_ThenNoExceptionShouldHappen() {
        assertThatNoException().isThrownBy(() -> packageJourneySegmentTypeAssigner.assignSegmentTypes(List.of()));
    }

    private List<PackageJourneySegment> generatePackageJourneySegments(int numberOfSegments) {
        return IntStream.range(0,numberOfSegments).mapToObj(index->{
            PackageJourneySegment segment = new PackageJourneySegment();
            String refIdSequence = String.valueOf(index);
            segment.setSequence(refIdSequence);
            segment.setRefId(refIdSequence);
            return segment;
        }).toList();
    }
}
