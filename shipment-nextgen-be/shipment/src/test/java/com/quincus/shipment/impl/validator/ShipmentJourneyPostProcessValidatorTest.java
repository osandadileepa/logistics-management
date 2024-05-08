package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentJourneyPostProcessValidatorTest {

    @InjectMocks
    private ShipmentJourneyPostProcessValidator validator;

    @Mock
    private PackageJourneySegmentPostProcessValidator segmentValidator;

    @Test
    void isValid_nullJourney_shouldReturnFalse() {
        assertThat(validator.isValid(null)).isFalse();
    }

    @Test
    void isValid_emptyJourney_shouldReturnFalse() {
        ShipmentJourney journey = new ShipmentJourney();
        journey.setPackageJourneySegments(Collections.emptyList());
        assertThat(validator.isValid(journey)).isFalse();
    }

    @Test
    void givenJourneyWithInvalidDeletedSegments_whenValidated_shouldNotIncludeInSegmentValidation() {
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setSequence("1");
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setDeleted(true);
        segment2.setSequence("2");
        ShipmentJourney journey = new ShipmentJourney();
        journey.setPackageJourneySegments(List.of(segment1, segment2));
        when(segmentValidator.isValid(segment1)).thenReturn(true);

        assertThat(validator.isValid(journey)).isTrue();
        verify(segmentValidator, times(1)).isValid(segment1);
        verify(segmentValidator, times(0)).isValid(segment2);

    }

    @Test
    void isValid_invalidSegment_shouldReturnFalse() {
        ShipmentJourney journey = new ShipmentJourney();
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setSegmentId(UUID.randomUUID().toString());
        segment1.setRefId("1");
        segment1.setSequence("1");
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setSegmentId(UUID.randomUUID().toString());
        segment2.setRefId("2");
        segment2.setSequence("2");
        journey.addPackageJourneySegment(segment1);
        journey.addPackageJourneySegment(segment2);

        when(segmentValidator.isValid(any(PackageJourneySegment.class))).thenReturn(true).thenReturn(false);

        assertThat(validator.isValid(journey)).isFalse();
    }

    @Test
    void isValid_validSegments_shouldReturnFalse() {
        ShipmentJourney journey = new ShipmentJourney();
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setSegmentId(UUID.randomUUID().toString());
        segment1.setRefId("1");
        segment1.setSequence("1");
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setSegmentId(UUID.randomUUID().toString());
        segment2.setRefId("2");
        segment2.setSequence("2");
        journey.addPackageJourneySegment(segment1);
        journey.addPackageJourneySegment(segment2);

        when(segmentValidator.isValid(any(PackageJourneySegment.class))).thenReturn(true).thenReturn(true);

        assertThat(validator.isValid(journey)).isTrue();
    }
}
