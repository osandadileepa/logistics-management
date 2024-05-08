package com.quincus.shipment.impl.helper.segment;

import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SegmentDefaultUpdateCheckerTest {

    @InjectMocks
    private SegmentDefaultUpdateChecker segmentUpdateChecker;

    private static Stream<Arguments> provideMatchingSegments() {
        Facility location1 = new Facility();
        location1.setId("facility-1");
        location1.setExternalId("ext-facility-1");

        Facility location2 = new Facility();
        location2.setId("facility-2");
        location2.setExternalId("ext-facility-2");

        Partner partner1 = new Partner();
        partner1.setId("partner-1");

        LocationHierarchyEntity existingLocation1 = new LocationHierarchyEntity();
        existingLocation1.setId("FACILITY-A");
        existingLocation1.setExternalId("ext-facility-1");

        LocationHierarchyEntity existingLocation2 = new LocationHierarchyEntity();
        existingLocation2.setId("FACILITY-B");
        existingLocation2.setExternalId("ext-facility-2");

        PartnerEntity existingPartner1 = new PartnerEntity();
        existingPartner1.setId("PARTNER-A");
        existingPartner1.setExternalId("partner-1");

        PackageJourneySegment segmentNullAll = new PackageJourneySegment();
        PackageJourneySegmentEntity existingSegmentNullAll = new PackageJourneySegmentEntity();

        PackageJourneySegment segmentBlankAll = new PackageJourneySegment();
        segmentBlankAll.setStartFacility(new Facility());
        segmentBlankAll.setEndFacility(new Facility());
        segmentBlankAll.setPartner(new Partner());
        PackageJourneySegmentEntity existingSegmentBlankAll = new PackageJourneySegmentEntity();
        existingSegmentBlankAll.setStartLocationHierarchy(new LocationHierarchyEntity());
        existingSegmentBlankAll.setEndLocationHierarchy(new LocationHierarchyEntity());
        existingSegmentBlankAll.setPartner(new PartnerEntity());

        PackageJourneySegment segmentStartOnly = new PackageJourneySegment();
        segmentStartOnly.setStartFacility(location1);
        PackageJourneySegmentEntity existingSegmentStartOnly = new PackageJourneySegmentEntity();
        existingSegmentStartOnly.setStartLocationHierarchy(existingLocation1);

        PackageJourneySegment segmentEndOnly = new PackageJourneySegment();
        segmentEndOnly.setEndFacility(location1);
        PackageJourneySegmentEntity existingSegmentEndOnly = new PackageJourneySegmentEntity();
        existingSegmentEndOnly.setEndLocationHierarchy(existingLocation1);

        PackageJourneySegment segmentPartnerOnly = new PackageJourneySegment();
        segmentPartnerOnly.setPartner(partner1);
        PackageJourneySegmentEntity existingSegmentPartnerOnly = new PackageJourneySegmentEntity();
        existingSegmentPartnerOnly.setPartner(existingPartner1);

        PackageJourneySegment segmentStartAndEnd = new PackageJourneySegment();
        segmentStartAndEnd.setStartFacility(location1);
        segmentStartAndEnd.setEndFacility(location2);
        PackageJourneySegmentEntity existingSegmentStartAndEnd = new PackageJourneySegmentEntity();
        existingSegmentStartAndEnd.setStartLocationHierarchy(existingLocation1);
        existingSegmentStartAndEnd.setEndLocationHierarchy(existingLocation2);

        PackageJourneySegment segmentStartAndPartner = new PackageJourneySegment();
        segmentStartAndPartner.setStartFacility(location1);
        segmentStartAndPartner.setPartner(partner1);
        PackageJourneySegmentEntity existingSegmentStartAndPartner = new PackageJourneySegmentEntity();
        existingSegmentStartAndPartner.setStartLocationHierarchy(existingLocation1);
        existingSegmentStartAndPartner.setPartner(existingPartner1);

        PackageJourneySegment segmentEndAndPartner = new PackageJourneySegment();
        segmentEndAndPartner.setEndFacility(location1);
        segmentEndAndPartner.setPartner(partner1);
        PackageJourneySegmentEntity existingSegmentEndAndPartner = new PackageJourneySegmentEntity();
        existingSegmentEndAndPartner.setEndLocationHierarchy(existingLocation1);
        existingSegmentEndAndPartner.setPartner(existingPartner1);

        PackageJourneySegment segmentComplete = new PackageJourneySegment();
        segmentComplete.setStartFacility(location1);
        segmentComplete.setEndFacility(location2);
        segmentComplete.setPartner(partner1);
        PackageJourneySegmentEntity existingSegmentComplete = new PackageJourneySegmentEntity();
        existingSegmentComplete.setStartLocationHierarchy(existingLocation1);
        existingSegmentComplete.setEndLocationHierarchy(existingLocation2);
        existingSegmentComplete.setPartner(existingPartner1);

        PackageJourneySegment segmentComplete2 = new PackageJourneySegment();
        segmentComplete2.setStartFacility(location1);
        segmentComplete2.setEndFacility(location2);
        segmentComplete2.setPartner(new Partner());
        PackageJourneySegmentEntity existingSegmentComplete2 = new PackageJourneySegmentEntity();
        existingSegmentComplete2.setStartLocationHierarchy(existingLocation1);
        existingSegmentComplete2.setEndLocationHierarchy(existingLocation2);
        existingSegmentComplete2.setPartner(new PartnerEntity());

        return Stream.of(
                Arguments.of(segmentNullAll, existingSegmentNullAll),
                Arguments.of(segmentBlankAll, existingSegmentBlankAll),
                Arguments.of(segmentStartOnly, existingSegmentStartOnly),
                Arguments.of(segmentEndOnly, existingSegmentEndOnly),
                Arguments.of(segmentPartnerOnly, existingSegmentPartnerOnly),
                Arguments.of(segmentStartAndEnd, existingSegmentStartAndEnd),
                Arguments.of(segmentStartAndPartner, existingSegmentStartAndPartner),
                Arguments.of(segmentEndAndPartner, existingSegmentEndAndPartner),
                Arguments.of(segmentComplete, existingSegmentComplete),
                Arguments.of(segmentComplete2, existingSegmentComplete2)
        );
    }

    private static Stream<Arguments> provideMismatchedSegments() {
        Facility location1 = new Facility();
        location1.setId("facility-1");
        location1.setExternalId("ext-facility-1");

        Facility location2 = new Facility();
        location2.setId("facility-2");
        location2.setExternalId("ext-facility-2");

        Partner partner1 = new Partner();
        partner1.setId("partner-1");

        Partner partner2 = new Partner();
        partner2.setId("partner-2");

        LocationHierarchyEntity existingLocation1 = new LocationHierarchyEntity();
        existingLocation1.setId("FACILITY-A");
        existingLocation1.setExternalId("ext-facility-1");

        LocationHierarchyEntity existingLocation2 = new LocationHierarchyEntity();
        existingLocation2.setId("FACILITY-B");
        existingLocation2.setExternalId("ext-facility-2");

        PartnerEntity existingPartner1 = new PartnerEntity();
        existingPartner1.setId("PARTNER-A");
        existingPartner1.setExternalId("partner-1");

        PartnerEntity existingPartner2 = new PartnerEntity();
        existingPartner2.setId("PARTNER-B");
        existingPartner2.setExternalId("partner-2");

        PackageJourneySegment segmentNullAll = new PackageJourneySegment();
        PackageJourneySegmentEntity existingSegmentNullAll = new PackageJourneySegmentEntity();

        PackageJourneySegment segmentBlankAll = new PackageJourneySegment();
        segmentBlankAll.setStartFacility(new Facility());
        segmentBlankAll.setEndFacility(new Facility());
        segmentBlankAll.setPartner(new Partner());
        PackageJourneySegmentEntity existingSegmentBlankAll = new PackageJourneySegmentEntity();
        existingSegmentBlankAll.setStartLocationHierarchy(new LocationHierarchyEntity());
        existingSegmentBlankAll.setEndLocationHierarchy(new LocationHierarchyEntity());
        existingSegmentBlankAll.setPartner(new PartnerEntity());

        PackageJourneySegment segmentStartOnly = new PackageJourneySegment();
        segmentStartOnly.setStartFacility(location1);
        PackageJourneySegmentEntity existingSegmentStartOnly = new PackageJourneySegmentEntity();
        existingSegmentStartOnly.setStartLocationHierarchy(existingLocation1);

        PackageJourneySegment segmentEndOnly = new PackageJourneySegment();
        segmentEndOnly.setEndFacility(location1);
        PackageJourneySegmentEntity existingSegmentEndOnly = new PackageJourneySegmentEntity();
        existingSegmentEndOnly.setEndLocationHierarchy(existingLocation1);

        PackageJourneySegment segmentPartnerOnly = new PackageJourneySegment();
        segmentPartnerOnly.setPartner(partner1);
        PackageJourneySegmentEntity existingSegmentPartnerOnly = new PackageJourneySegmentEntity();
        existingSegmentPartnerOnly.setPartner(existingPartner1);

        PackageJourneySegment segmentStartAndEnd = new PackageJourneySegment();
        segmentStartAndEnd.setStartFacility(location1);
        segmentStartAndEnd.setEndFacility(location2);
        PackageJourneySegmentEntity existingSegmentStartAndEnd = new PackageJourneySegmentEntity();
        existingSegmentStartAndEnd.setStartLocationHierarchy(existingLocation1);
        existingSegmentStartAndEnd.setEndLocationHierarchy(existingLocation2);

        PackageJourneySegment segmentStartAndPartner = new PackageJourneySegment();
        segmentStartAndPartner.setStartFacility(location1);
        segmentStartAndPartner.setPartner(partner1);
        PackageJourneySegmentEntity existingSegmentStartAndPartner = new PackageJourneySegmentEntity();
        existingSegmentStartAndPartner.setStartLocationHierarchy(existingLocation1);
        existingSegmentStartAndPartner.setPartner(existingPartner1);

        PackageJourneySegment segmentEndAndPartner = new PackageJourneySegment();
        segmentEndAndPartner.setEndFacility(location1);
        segmentEndAndPartner.setPartner(partner1);
        PackageJourneySegmentEntity existingSegmentEndAndPartner = new PackageJourneySegmentEntity();
        existingSegmentEndAndPartner.setEndLocationHierarchy(existingLocation1);
        existingSegmentEndAndPartner.setPartner(existingPartner1);

        PackageJourneySegment segmentComplete = new PackageJourneySegment();
        segmentComplete.setStartFacility(location1);
        segmentComplete.setEndFacility(location2);
        segmentComplete.setPartner(partner1);
        PackageJourneySegmentEntity existingSegmentComplete = new PackageJourneySegmentEntity();
        existingSegmentComplete.setStartLocationHierarchy(existingLocation1);
        existingSegmentComplete.setEndLocationHierarchy(existingLocation2);
        existingSegmentComplete.setPartner(existingPartner1);

        PackageJourneySegment segmentComplete2 = new PackageJourneySegment();
        segmentComplete2.setStartFacility(location2);
        segmentComplete2.setEndFacility(location1);
        segmentComplete2.setPartner(partner1);
        PackageJourneySegmentEntity existingSegmentComplete2 = new PackageJourneySegmentEntity();
        existingSegmentComplete2.setStartLocationHierarchy(existingLocation2);
        existingSegmentComplete2.setEndLocationHierarchy(existingLocation1);
        existingSegmentComplete2.setPartner(existingPartner1);

        PackageJourneySegment segmentComplete3 = new PackageJourneySegment();
        segmentComplete3.setStartFacility(location1);
        segmentComplete3.setEndFacility(location2);
        segmentComplete3.setPartner(partner2);
        PackageJourneySegmentEntity existingSegmentComplete3 = new PackageJourneySegmentEntity();
        existingSegmentComplete3.setStartLocationHierarchy(existingLocation1);
        existingSegmentComplete3.setEndLocationHierarchy(existingLocation2);
        existingSegmentComplete3.setPartner(existingPartner2);

        PackageJourneySegment segmentComplete4 = new PackageJourneySegment();
        segmentComplete4.setStartFacility(location1);
        segmentComplete4.setEndFacility(location2);
        segmentComplete4.setPartner(new Partner());
        PackageJourneySegmentEntity existingSegmentComplete4 = new PackageJourneySegmentEntity();
        existingSegmentComplete4.setStartLocationHierarchy(existingLocation1);
        existingSegmentComplete4.setEndLocationHierarchy(existingLocation2);
        existingSegmentComplete4.setPartner(new PartnerEntity());

        return Stream.of(
                Arguments.of(segmentNullAll, existingSegmentBlankAll),
                Arguments.of(segmentNullAll, existingSegmentStartOnly),
                Arguments.of(segmentNullAll, existingSegmentEndOnly),
                Arguments.of(segmentNullAll, existingSegmentPartnerOnly),
                Arguments.of(segmentNullAll, existingSegmentStartAndEnd),
                Arguments.of(segmentNullAll, existingSegmentStartAndPartner),
                Arguments.of(segmentNullAll, existingSegmentEndAndPartner),
                Arguments.of(segmentNullAll, existingSegmentComplete),
                Arguments.of(segmentBlankAll, existingSegmentNullAll),
                Arguments.of(segmentBlankAll, existingSegmentStartOnly),
                Arguments.of(segmentBlankAll, existingSegmentEndOnly),
                Arguments.of(segmentBlankAll, existingSegmentPartnerOnly),
                Arguments.of(segmentBlankAll, existingSegmentStartAndEnd),
                Arguments.of(segmentBlankAll, existingSegmentStartAndPartner),
                Arguments.of(segmentBlankAll, existingSegmentEndAndPartner),
                Arguments.of(segmentBlankAll, existingSegmentComplete),
                Arguments.of(segmentStartOnly, existingSegmentNullAll),
                Arguments.of(segmentStartOnly, existingSegmentBlankAll),
                Arguments.of(segmentStartOnly, existingSegmentEndOnly),
                Arguments.of(segmentStartOnly, existingSegmentPartnerOnly),
                Arguments.of(segmentStartOnly, existingSegmentStartAndEnd),
                Arguments.of(segmentStartOnly, existingSegmentStartAndPartner),
                Arguments.of(segmentStartOnly, existingSegmentEndAndPartner),
                Arguments.of(segmentStartOnly, existingSegmentComplete),
                Arguments.of(segmentEndOnly, existingSegmentNullAll),
                Arguments.of(segmentEndOnly, existingSegmentBlankAll),
                Arguments.of(segmentEndOnly, existingSegmentStartOnly),
                Arguments.of(segmentEndOnly, existingSegmentPartnerOnly),
                Arguments.of(segmentEndOnly, existingSegmentStartAndEnd),
                Arguments.of(segmentEndOnly, existingSegmentStartAndPartner),
                Arguments.of(segmentEndOnly, existingSegmentEndAndPartner),
                Arguments.of(segmentEndOnly, existingSegmentComplete),
                Arguments.of(segmentPartnerOnly, existingSegmentNullAll),
                Arguments.of(segmentPartnerOnly, existingSegmentBlankAll),
                Arguments.of(segmentPartnerOnly, existingSegmentStartOnly),
                Arguments.of(segmentPartnerOnly, existingSegmentEndOnly),
                Arguments.of(segmentPartnerOnly, existingSegmentStartAndEnd),
                Arguments.of(segmentPartnerOnly, existingSegmentStartAndPartner),
                Arguments.of(segmentPartnerOnly, existingSegmentEndAndPartner),
                Arguments.of(segmentPartnerOnly, existingSegmentComplete),
                Arguments.of(segmentStartAndEnd, existingSegmentNullAll),
                Arguments.of(segmentStartAndEnd, existingSegmentBlankAll),
                Arguments.of(segmentStartAndEnd, existingSegmentStartOnly),
                Arguments.of(segmentStartAndEnd, existingSegmentEndOnly),
                Arguments.of(segmentStartAndEnd, existingSegmentPartnerOnly),
                Arguments.of(segmentStartAndEnd, existingSegmentStartAndPartner),
                Arguments.of(segmentStartAndEnd, existingSegmentEndAndPartner),
                Arguments.of(segmentStartAndEnd, existingSegmentComplete),
                Arguments.of(segmentStartAndPartner, existingSegmentNullAll),
                Arguments.of(segmentStartAndPartner, existingSegmentBlankAll),
                Arguments.of(segmentStartAndPartner, existingSegmentStartOnly),
                Arguments.of(segmentStartAndPartner, existingSegmentEndOnly),
                Arguments.of(segmentStartAndPartner, existingSegmentPartnerOnly),
                Arguments.of(segmentStartAndPartner, existingSegmentStartAndEnd),
                Arguments.of(segmentStartAndPartner, existingSegmentEndAndPartner),
                Arguments.of(segmentStartAndPartner, existingSegmentComplete),
                Arguments.of(segmentEndAndPartner, existingSegmentNullAll),
                Arguments.of(segmentEndAndPartner, existingSegmentBlankAll),
                Arguments.of(segmentEndAndPartner, existingSegmentStartOnly),
                Arguments.of(segmentEndAndPartner, existingSegmentEndOnly),
                Arguments.of(segmentEndAndPartner, existingSegmentPartnerOnly),
                Arguments.of(segmentEndAndPartner, existingSegmentStartAndEnd),
                Arguments.of(segmentEndAndPartner, existingSegmentStartAndPartner),
                Arguments.of(segmentEndAndPartner, existingSegmentComplete),
                Arguments.of(segmentComplete, existingSegmentNullAll),
                Arguments.of(segmentComplete, existingSegmentBlankAll),
                Arguments.of(segmentComplete, existingSegmentStartOnly),
                Arguments.of(segmentComplete, existingSegmentEndOnly),
                Arguments.of(segmentComplete, existingSegmentPartnerOnly),
                Arguments.of(segmentComplete, existingSegmentStartAndEnd),
                Arguments.of(segmentComplete, existingSegmentStartAndPartner),
                Arguments.of(segmentComplete, existingSegmentEndAndPartner),
                Arguments.of(segmentComplete, existingSegmentComplete2),
                Arguments.of(segmentComplete, existingSegmentComplete3),
                Arguments.of(segmentComplete, existingSegmentComplete4),
                Arguments.of(segmentComplete2, existingSegmentComplete),
                Arguments.of(segmentComplete2, existingSegmentComplete3),
                Arguments.of(segmentComplete2, existingSegmentComplete4),
                Arguments.of(segmentComplete3, existingSegmentComplete),
                Arguments.of(segmentComplete3, existingSegmentComplete2),
                Arguments.of(segmentComplete3, existingSegmentComplete4),
                Arguments.of(segmentComplete4, existingSegmentComplete),
                Arguments.of(segmentComplete4, existingSegmentComplete2),
                Arguments.of(segmentComplete4, existingSegmentComplete3)
        );
    }

    @ParameterizedTest
    @MethodSource("provideMatchingSegments")
    void isSegmentMatch_matchingSegments_shouldReturnTrue(PackageJourneySegment segment,
                                                          PackageJourneySegmentEntity existingSegment) {
        assertThat(segmentUpdateChecker.isSegmentMatch(segment, existingSegment)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideMismatchedSegments")
    void isSegmentMatch_mismatchedSegments_shouldReturnFalse(PackageJourneySegment segment,
                                                             PackageJourneySegmentEntity existingSegment) {
        assertThat(segmentUpdateChecker.isSegmentMatch(segment, existingSegment)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideMatchingSegments")
    void findSegmentForUpdate_segmentFound_shouldReturnEntity(PackageJourneySegment segment,
                                                              PackageJourneySegmentEntity existingSegment) {
        assertThat(segmentUpdateChecker.findSegmentForUpdate(segment, List.of(existingSegment))).isPresent()
                .isEqualTo(Optional.of(existingSegment));
    }

    @ParameterizedTest
    @MethodSource("provideMismatchedSegments")
    void findSegmentForUpdate_segmentNotFound_shouldReturnEmpty(PackageJourneySegment segment,
                                                                PackageJourneySegmentEntity existingSegment) {
        assertThat(segmentUpdateChecker.findSegmentForUpdate(segment, List.of(existingSegment))).isNotPresent();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void findSegmentForUpdate_noExistingSegments_shouldReturnEmpty(List<PackageJourneySegmentEntity> existingSegments) {
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(UUID.randomUUID().toString());
        assertThat(segmentUpdateChecker.findSegmentForUpdate(segment, existingSegments)).isNotPresent();
    }
}
