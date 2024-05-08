package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PackageJourneySegmentPostProcessValidatorTest {

    @InjectMocks
    private PackageJourneySegmentPostProcessValidator validator;

    private static Stream<Arguments> provideBaseSegmentMissingRequiredFields() {
        PackageJourneySegment segmentNoTransportType = new PackageJourneySegment();
        segmentNoTransportType.setTransportType(null);

        PackageJourneySegment segmentNoPartner = new PackageJourneySegment();
        segmentNoPartner.setTransportType(TransportType.GROUND);
        segmentNoPartner.setPartner(null);

        PackageJourneySegment segmentNoStartFacility = new PackageJourneySegment();
        segmentNoStartFacility.setTransportType(TransportType.GROUND);
        segmentNoStartFacility.setPartner(new Partner());
        segmentNoStartFacility.setStartFacility(null);

        PackageJourneySegment segmentNoEndFacility = new PackageJourneySegment();
        segmentNoEndFacility.setTransportType(TransportType.GROUND);
        segmentNoEndFacility.setPartner(new Partner());
        segmentNoEndFacility.setStartFacility(new Facility());
        segmentNoEndFacility.setEndFacility(null);

        return Stream.of(
                Arguments.of(segmentNoTransportType),
                Arguments.of(segmentNoPartner),
                Arguments.of(segmentNoStartFacility),
                Arguments.of(segmentNoEndFacility)
        );
    }

    private static Stream<Arguments> provideGroundSegmentMissingRequiredFields() {
        PackageJourneySegment segmentNoPickUpTime = createDummySegment();
        segmentNoPickUpTime.setPickUpTime(null);

        OffsetDateTime refTime = OffsetDateTime.now(Clock.systemUTC());
        PackageJourneySegment segmentNoDropOffTime = createDummySegment();
        segmentNoDropOffTime.setPickUpTime(refTime.toString());
        segmentNoDropOffTime.setDropOffTime(null);

        return Stream.of(
                Arguments.of(segmentNoPickUpTime),
                Arguments.of(segmentNoDropOffTime)
        );
    }

    private static Stream<Arguments> provideAirSegmentMissingRequiredFields() {
        PackageJourneySegment segmentNoDepartureTime = createDummySegment();
        segmentNoDepartureTime.setDepartureTime(null);

        OffsetDateTime refTime = OffsetDateTime.now(Clock.systemUTC());

        PackageJourneySegment segmentNoAirlineCode = createDummySegment();
        segmentNoAirlineCode.setDepartureTime(refTime.toString());
        segmentNoAirlineCode.setAirlineCode(null);

        PackageJourneySegment segmentNoFlightNumber = createDummySegment();
        segmentNoFlightNumber.setDepartureTime(refTime.toString());
        segmentNoFlightNumber.setAirlineCode("AB");
        segmentNoFlightNumber.setFlightNumber(null);

        return Stream.of(
                Arguments.of(segmentNoDepartureTime),
                Arguments.of(segmentNoAirlineCode),
                Arguments.of(segmentNoFlightNumber)
        );
    }

    private static PackageJourneySegment createDummySegment() {
        PackageJourneySegment segment = new PackageJourneySegment();
        Partner partner = new Partner();
        segment.setPartner(partner);
        Facility startFacility = new Facility();
        segment.setStartFacility(startFacility);
        Facility endFacility = new Facility();
        segment.setEndFacility(endFacility);
        return segment;
    }

    @ParameterizedTest
    @MethodSource({"provideBaseSegmentMissingRequiredFields",
            "provideGroundSegmentMissingRequiredFields",
            "provideAirSegmentMissingRequiredFields"})
    void isValid_segmentMissingFields_shouldReturnFalse(PackageJourneySegment segment) {
        assertThat(validator.isValid(segment)).isFalse();
    }

    @Test
    void isValid_groundSegmentRequiredFieldsPresent_shouldReturnTrue() {
        PackageJourneySegment segment = createDummySegment();
        segment.setTransportType(TransportType.GROUND);
        OffsetDateTime pickupTime = OffsetDateTime.now(Clock.systemUTC());
        OffsetDateTime dropOffTime = pickupTime.plusDays(3);
        segment.setPickUpTime(pickupTime.toString());
        segment.setDropOffTime(dropOffTime.toString());

        assertThat(validator.isValid(segment)).isTrue();
    }

    @Test
    void isValid_airSegmentRequiredFieldsPresent_shouldReturnTrue() {
        PackageJourneySegment segment = createDummySegment();
        segment.setTransportType(TransportType.AIR);
        OffsetDateTime departureTime = OffsetDateTime.now(Clock.systemUTC());
        segment.setDepartureTime(departureTime.toString());
        segment.setAirlineCode("AB");
        segment.setFlightNumber("123");

        assertThat(validator.isValid(segment)).isTrue();
    }

    @Test
    void isValid_otherSegmentRequiredFieldsPresent_shouldReturnTrue() {
        PackageJourneySegment segment = createDummySegment();
        segment.setTransportType(TransportType.SEA);

        assertThat(validator.isValid(segment)).isTrue();
    }
}
