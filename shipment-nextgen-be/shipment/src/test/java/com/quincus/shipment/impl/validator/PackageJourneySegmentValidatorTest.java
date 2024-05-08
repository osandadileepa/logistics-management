package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.exception.PackageJourneySegmentException;
import com.quincus.shipment.api.exception.SegmentException;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static com.quincus.shipment.impl.test_utils.ShipmentJourneyTestDataFactory.createDummyAirSegment;
import static com.quincus.shipment.impl.test_utils.ShipmentJourneyTestDataFactory.createDummyGroundSegment;
import static com.quincus.shipment.impl.test_utils.ShipmentJourneyTestDataFactory.createDummyJourneyFromSegments;
import static com.quincus.shipment.impl.test_utils.ShipmentJourneyTestDataFactory.createShipmentJourney;
import static com.quincus.shipment.impl.test_utils.ShipmentJourneyTestDataFactory.enrichSegmentWithFacilities;
import static com.quincus.shipment.impl.test_utils.ShipmentJourneyTestDataFactory.enrichSegmentWithPartner;
import static com.quincus.shipment.impl.validator.PackageJourneySegmentValidator.DUPLICATE_SEGMENT_FOUND_ERR_MSG;
import static com.quincus.shipment.impl.validator.PackageJourneySegmentValidator.EMPTY_DEPARTURE_ARRIVAL_TIME;
import static com.quincus.shipment.impl.validator.PackageJourneySegmentValidator.EMPTY_FACILITY_ID;
import static com.quincus.shipment.impl.validator.PackageJourneySegmentValidator.EMPTY_PARTNER_ID;
import static com.quincus.shipment.impl.validator.PackageJourneySegmentValidator.EMPTY_PICKUP_DROP_OFF_TIME;
import static com.quincus.shipment.impl.validator.PackageJourneySegmentValidator.FACILITY_DROP_PICKUP_NOT_MATCH_ERR_MSG;
import static com.quincus.shipment.impl.validator.PackageJourneySegmentValidator.FACILITY_ID_SET_ERR_MSG;
import static com.quincus.shipment.impl.validator.PackageJourneySegmentValidator.PICKUP_DROP_OFF_SAME_ERR_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageJourneySegmentValidatorTest {
    private static final String VALID_PICK_UP_TIME = "2022-12-10T16:27:02+07:00";
    private static final String VALID_DROP_OFF_TIME = "2022-12-11T16:27:02+07:00";
    private static final String VALID_PICK_UP2_TIME = "2022-12-12T16:27:02+07:00";
    private static final String VALID_DROP_OFF2_TIME = "2022-12-13T16:27:02+07:00";
    private static final String VALID_LOCK_OUT_TIME = "2022-12-10T17:28:03+07:00";
    private static final String VALID_DEPARTURE_TIME = "2022-12-11T17:28:03+07:00";
    private static final String VALID_ARRIVAL_TIME = "2022-12-12T17:28:03+07:00";
    private static final String VALID_RECOVERY_TIME = "2022-12-13T17:28:03+07:00";
    @InjectMocks
    private PackageJourneySegmentValidator packageJourneySegmentValidator;
    @Mock
    private UserDetailsProvider userDetailsProvider;

    @ParameterizedTest
    @MethodSource("provideJourneyWithInvalidFacilities")
    void validateFacilityIds_variousInvalidFacilities_shouldThrowException(ShipmentJourney shipmentJourney,
                                                                           int errorCount) {
        assertThatThrownBy(() -> packageJourneySegmentValidator.validatePackageJourneySegments(shipmentJourney))
                .isInstanceOfSatisfying(PackageJourneySegmentException.class, exception -> {
                    assertThat(exception.getErrors()).hasSize(errorCount);
                });
    }

    @Test
    void validateFacilityIds_validFacilities_shouldNotThrowException() {
        assertThatNoException().isThrownBy(() -> packageJourneySegmentValidator.validatePackageJourneySegments(createShipmentJourney()));
    }

    @ParameterizedTest
    @MethodSource("provideJourneyWithValidFieldsAndCombinations")
    void validatePackageJourneySegments_validSegmentCombination_shouldNotThrowException(ShipmentJourney journey) {
        assertThatNoException().isThrownBy(() -> packageJourneySegmentValidator.validatePackageJourneySegments(journey));
    }

    @ParameterizedTest
    @MethodSource("provideJourneyWithInvalidFieldsAndExpectedErrors")
    void validatePackageJourneySegments_invalidSegmentCombination_shouldThrowException(ShipmentJourney journey,
                                                                                       Class<Exception> exceptionClass,
                                                                                       String errorMessage) {

        Throwable thrown = catchThrowable(() -> packageJourneySegmentValidator.validatePackageJourneySegments(journey));

        assertThat(thrown).isInstanceOf(exceptionClass);

        if (thrown instanceof PackageJourneySegmentException packageJourneySegmentException) {
            assertThat(packageJourneySegmentException.getErrors()).anyMatch(error -> error.contains(errorMessage));
        }

    }

    @Test
    void validatePackageJourneySegments_shouldSkipValidationWhenSourceIsFromKafka() {
        ShipmentJourney shipmentJourneyWithInvalidSegments = new ShipmentJourney();

        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setTransportType(TransportType.GROUND);
        packageJourneySegment.setRefId("1");
        packageJourneySegment.setSequence("1");
        packageJourneySegment.setPickUpTime("2016-10-05 08:20:10+05:30");
        packageJourneySegment.setDropOffTime("2016-10-05 08:21:10+05:30");

        Facility startFacility1 = new Facility();
        startFacility1.setExternalId("blankThisIsOrigin");
        Facility endFacility1 = new Facility();
        endFacility1.setExternalId("facilityId1");
        packageJourneySegment.setStartFacility(startFacility1);
        packageJourneySegment.setEndFacility(endFacility1);
        shipmentJourneyWithInvalidSegments.setPackageJourneySegments(List.of(packageJourneySegment));

        when(userDetailsProvider.isFromKafka()).thenReturn(true);

        assertThatNoException().isThrownBy(() -> packageJourneySegmentValidator.validatePackageJourneySegments(shipmentJourneyWithInvalidSegments));
    }

    @Test
    void validatePackageJourneySegments_shouldSkipValidationWhenSourceIsFromOrderAndS2S() {
        ShipmentJourney shipmentJourneyWithInvalidSegments = new ShipmentJourney();

        PackageJourneySegment packageJourneySegment = new PackageJourneySegment();
        packageJourneySegment.setTransportType(TransportType.GROUND);
        packageJourneySegment.setRefId("1");
        packageJourneySegment.setSequence("1");
        packageJourneySegment.setPickUpTime("2016-10-05T08:20:10+05:30");
        packageJourneySegment.setDropOffTime("2016-10-05T08:21:10+05:30");

        Facility startFacility1 = new Facility();
        startFacility1.setExternalId("blankThisIsOrigin");
        Facility endFacility1 = new Facility();
        endFacility1.setExternalId("facilityId1");
        packageJourneySegment.setStartFacility(startFacility1);
        packageJourneySegment.setEndFacility(endFacility1);
        shipmentJourneyWithInvalidSegments.setPackageJourneySegments(List.of(packageJourneySegment));

        when(userDetailsProvider.isFromAllowedSource()).thenReturn(true);

        assertThatNoException().isThrownBy(() -> packageJourneySegmentValidator.validatePackageJourneySegments(shipmentJourneyWithInvalidSegments));
    }

    private static Stream<Arguments> provideJourneyWithValidFieldsAndCombinations() {
        PackageJourneySegment segmentValidCombination = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segmentValidCombination, "partner1");
        enrichSegmentWithFacilities(segmentValidCombination, "A", "B");
        ShipmentJourney validCombination = createDummyJourneyFromSegments(segmentValidCombination);

        PackageJourneySegment segment2ValidCombination = createDummyGroundSegment(2, VALID_PICK_UP2_TIME, VALID_DROP_OFF2_TIME);
        enrichSegmentWithPartner(segment2ValidCombination, "partner2");
        enrichSegmentWithFacilities(segment2ValidCombination, "B", "C");
        ShipmentJourney validCombinationMultiple = createDummyJourneyFromSegments(segmentValidCombination, segment2ValidCombination);

        PackageJourneySegment airValidCombination = createDummyAirSegment(VALID_LOCK_OUT_TIME, VALID_DEPARTURE_TIME, VALID_ARRIVAL_TIME, VALID_RECOVERY_TIME);
        enrichSegmentWithPartner(airValidCombination, "partner1");
        enrichSegmentWithFacilities(airValidCombination, "A", "B");
        ShipmentJourney validAirCombination = createDummyJourneyFromSegments(segmentValidCombination);

        //Considered valid at this level
        PackageJourneySegment segmentPickupEqualDropOff = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_PICK_UP_TIME);
        enrichSegmentWithPartner(segmentPickupEqualDropOff, "partner1");
        enrichSegmentWithFacilities(segmentPickupEqualDropOff, "A", "B");
        ShipmentJourney pickupEqualDropOff = createDummyJourneyFromSegments(segmentPickupEqualDropOff);

        //Considered valid at this level
        PackageJourneySegment segmentPickupAfterDropOff = createDummyGroundSegment(VALID_DROP_OFF_TIME, VALID_PICK_UP_TIME);
        enrichSegmentWithPartner(segmentPickupAfterDropOff, "partner1");
        enrichSegmentWithFacilities(segmentPickupAfterDropOff, "A", "B");
        ShipmentJourney pickupAfterDropOff = createDummyJourneyFromSegments(segmentPickupAfterDropOff);

        //Considered valid at this level
        PackageJourneySegment segmentValidCombinationA = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segmentValidCombinationA, "partner1");
        enrichSegmentWithFacilities(segmentValidCombinationA, "A", "B");
        PackageJourneySegment segmentPickupEqualsPreviousDropOff = createDummyGroundSegment(2, VALID_DROP_OFF_TIME, VALID_DROP_OFF2_TIME);
        enrichSegmentWithPartner(segmentPickupEqualsPreviousDropOff, "partner2");
        enrichSegmentWithFacilities(segmentPickupEqualsPreviousDropOff, "B", "C");
        ShipmentJourney segment1DropOffEqualsSegment2Pickup = createDummyJourneyFromSegments(segmentValidCombinationA, segmentPickupEqualsPreviousDropOff);

        //Considered valid at this level
        PackageJourneySegment segmentValidCombinationA2 = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_PICK_UP2_TIME);
        enrichSegmentWithPartner(segmentValidCombinationA2, "partner1");
        enrichSegmentWithFacilities(segmentValidCombinationA2, "A", "B");
        PackageJourneySegment segmentPickupBeforePreviousDropOff = createDummyGroundSegment(2, VALID_DROP_OFF_TIME, VALID_DROP_OFF2_TIME);
        enrichSegmentWithPartner(segmentPickupBeforePreviousDropOff, "partner2");
        enrichSegmentWithFacilities(segmentPickupBeforePreviousDropOff, "B", "C");
        ShipmentJourney segment1DropOffAfterSegment2Pickup = createDummyJourneyFromSegments(segmentValidCombinationA2, segmentPickupBeforePreviousDropOff);

        PackageJourneySegment segment2ReverseFacilityOrder = createDummyGroundSegment(2, VALID_PICK_UP2_TIME, VALID_DROP_OFF2_TIME);
        enrichSegmentWithPartner(segment2ReverseFacilityOrder, "partner2");
        enrichSegmentWithFacilities(segment2ReverseFacilityOrder, "B", "A");
        ShipmentJourney roundTripJourney = createDummyJourneyFromSegments(segmentValidCombination, segment2ReverseFacilityOrder);

        PackageJourneySegment airSegmentNoLockout = createDummyAirSegment(null, VALID_DEPARTURE_TIME, VALID_ARRIVAL_TIME, VALID_RECOVERY_TIME);
        enrichSegmentWithPartner(airSegmentNoLockout, "partner1");
        enrichSegmentWithFacilities(airSegmentNoLockout, "A", "B");
        ShipmentJourney airNoLockout = createDummyJourneyFromSegments(airSegmentNoLockout);

        PackageJourneySegment airSegmentNoArrival = createDummyAirSegment(VALID_LOCK_OUT_TIME, VALID_DEPARTURE_TIME, null, VALID_RECOVERY_TIME);
        enrichSegmentWithPartner(airSegmentNoArrival, "partner1");
        enrichSegmentWithFacilities(airSegmentNoArrival, "A", "B");
        ShipmentJourney airNoArrival = createDummyJourneyFromSegments(airSegmentNoArrival);

        PackageJourneySegment airSegmentNoRecovery = createDummyAirSegment(VALID_LOCK_OUT_TIME, VALID_DEPARTURE_TIME, VALID_ARRIVAL_TIME, null);
        enrichSegmentWithPartner(airSegmentNoRecovery, "partner1");
        enrichSegmentWithFacilities(airSegmentNoRecovery, "A", "B");
        ShipmentJourney airNoRecovery = createDummyJourneyFromSegments(airSegmentNoRecovery);

        PackageJourneySegment airSegmentBlankLockout = createDummyAirSegment(" ", VALID_DEPARTURE_TIME, VALID_ARRIVAL_TIME, VALID_RECOVERY_TIME);
        enrichSegmentWithPartner(airSegmentBlankLockout, "partner1");
        enrichSegmentWithFacilities(airSegmentBlankLockout, "A", "B");
        ShipmentJourney airBlankLockout = createDummyJourneyFromSegments(airSegmentBlankLockout);

        PackageJourneySegment airSegmentBlankArrival = createDummyAirSegment(VALID_LOCK_OUT_TIME, VALID_DEPARTURE_TIME, " ", VALID_RECOVERY_TIME);
        enrichSegmentWithPartner(airSegmentBlankArrival, "partner1");
        enrichSegmentWithFacilities(airSegmentBlankArrival, "A", "B");
        ShipmentJourney airBlankArrival = createDummyJourneyFromSegments(airSegmentBlankArrival);

        PackageJourneySegment airSegmentBlankRecovery = createDummyAirSegment(VALID_LOCK_OUT_TIME, VALID_DEPARTURE_TIME, VALID_ARRIVAL_TIME, " ");
        enrichSegmentWithPartner(airSegmentBlankRecovery, "partner1");
        enrichSegmentWithFacilities(airSegmentBlankRecovery, "A", "B");
        ShipmentJourney airBlankRecovery = createDummyJourneyFromSegments(airSegmentBlankRecovery);

        //Considered valid at this level
        PackageJourneySegment airSegmentTimingOutOfOrder = createDummyAirSegment(VALID_LOCK_OUT_TIME, VALID_DEPARTURE_TIME, VALID_RECOVERY_TIME, VALID_ARRIVAL_TIME);
        enrichSegmentWithPartner(airSegmentTimingOutOfOrder, "partner1");
        enrichSegmentWithFacilities(airSegmentTimingOutOfOrder, "A", "B");
        ShipmentJourney airTimingOutOfOrder = createDummyJourneyFromSegments(airSegmentTimingOutOfOrder);

        return Stream.of(
                Arguments.of(Named.of("Normal Combination, single segment", validCombination)),
                Arguments.of(Named.of("Normal Combination, multiple segments", validCombinationMultiple)),
                Arguments.of(Named.of("Pick Up time is equal to Drop off time", pickupEqualDropOff)),
                Arguments.of(Named.of("Pick Up time is after to Drop off time", pickupAfterDropOff)),
                Arguments.of(Named.of("Segment 1 drop_off_time and Segment 2 pick_up_time are the same", segment1DropOffEqualsSegment2Pickup)),
                Arguments.of(Named.of("Segment 1 drop_off_time is After Segment 2 pickup time", segment1DropOffAfterSegment2Pickup)),
                Arguments.of(Named.of("Round trip Journey (A -> B -> A)", roundTripJourney)),
                Arguments.of(Named.of("Air segment field lock_out_time is missing", airNoLockout)),
                Arguments.of(Named.of("Air segment field arrival_time is missing", airNoArrival)),
                Arguments.of(Named.of("Air segment field recovery_time is missing", airNoRecovery)),
                Arguments.of(Named.of("Air segment field lock_out_time is blank", airBlankLockout)),
                Arguments.of(Named.of("Air segment field arrival_time is blank", airBlankArrival)),
                Arguments.of(Named.of("Air segment field recovery_time is blank", airBlankRecovery)),
                Arguments.of(Named.of("Air segment fields are in order (lockout < depart < arrival < recovery)", validAirCombination)),
                Arguments.of(Named.of("Air segment fields are out of order", airTimingOutOfOrder))
        );
    }

    private static Stream<Arguments> provideJourneyWithInvalidFacilities() {
        String id1 = "ID1";
        Partner partner = new Partner();
        partner.setId("PartnerId");
        Facility startFacilityOk = new Facility();
        startFacilityOk.setExternalId(id1);
        Facility startFacilityNg = new Facility();
        startFacilityNg.setId(id1);
        Facility endFacilityOk = new Facility();
        endFacilityOk.setExternalId(id1);
        Facility endFacilityNg = new Facility();
        endFacilityNg.setId(id1);
        ShipmentJourney journey1 = new ShipmentJourney();
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setRefId("1");
        segment1.setSequence("1");
        segment1.setStartFacility(startFacilityNg);
        segment1.setEndFacility(endFacilityOk);
        segment1.setPartner(partner);
        journey1.setPackageJourneySegments(List.of(segment1));
        ShipmentJourney journey2 = new ShipmentJourney();
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setRefId("2");
        segment2.setSequence("2");
        segment2.setStartFacility(startFacilityOk);
        segment2.setEndFacility(endFacilityNg);
        segment2.setPartner(partner);
        journey2.setPackageJourneySegments(List.of(segment2));
        ShipmentJourney journey3 = new ShipmentJourney();
        PackageJourneySegment segment3 = new PackageJourneySegment();
        segment3.setRefId("3");
        segment3.setSequence("3");
        segment3.setStartFacility(startFacilityNg);
        segment3.setEndFacility(endFacilityNg);
        segment3.setPartner(partner);
        journey3.setPackageJourneySegments(List.of(segment3));
        ShipmentJourney journey4 = new ShipmentJourney();
        PackageJourneySegment segment4 = new PackageJourneySegment();
        segment4.setRefId("4");
        segment4.setSequence("4");
        segment4.setStartFacility(startFacilityOk);
        segment4.setEndFacility(endFacilityOk);
        segment4.setPartner(partner);
        journey4.setPackageJourneySegments(List.of(segment4, segment3));
        ShipmentJourney journey5 = new ShipmentJourney();
        journey5.setPackageJourneySegments(List.of(segment1, segment2));
        return Stream.of(
                Arguments.of(Named.of("1 Segment, start_facility NG", journey1), 2),
                Arguments.of(Named.of("1 Segment, end_facility NG", journey2), 2),
                Arguments.of(Named.of("1 Segment, start_facility and end_facility NG", journey3), 5),
                Arguments.of(Named.of("2 Segments, segment[1] start_facility and end_facility NG", journey4), 6),
                Arguments.of(Named.of("2 Segments, segment[0].start_facility NG segment[1].end_facility NG", journey5), 4)
        );
    }

    public static Stream<Arguments> provideJourneyWithInvalidFieldsAndExpectedErrors() {
        PackageJourneySegment segment1ValidCombination = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segment1ValidCombination, "partner1");
        enrichSegmentWithFacilities(segment1ValidCombination, "A", "B");

        PackageJourneySegment segment2ValidCombination = createDummyGroundSegment(2, VALID_PICK_UP2_TIME, VALID_DROP_OFF2_TIME);
        enrichSegmentWithPartner(segment2ValidCombination, "partner2");
        enrichSegmentWithFacilities(segment2ValidCombination, "B", "C");

        PackageJourneySegment segment1IdPopulated = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segment1IdPopulated, "partner1");
        enrichSegmentWithFacilities(segment1IdPopulated, "A", "B");
        segment1IdPopulated.getStartFacility().setId("dummy-id");
        ShipmentJourney segmentWithIdDefined = createDummyJourneyFromSegments(segment1IdPopulated);

        PackageJourneySegment segmentGroundNoPickUpTime = createDummyGroundSegment(null, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segmentGroundNoPickUpTime, "partner1");
        enrichSegmentWithFacilities(segmentGroundNoPickUpTime, "A", "B");
        ShipmentJourney groundNoPickUpTime = createDummyJourneyFromSegments(segmentGroundNoPickUpTime);

        PackageJourneySegment segmentGroundEmptyPickUpTime = createDummyGroundSegment("", VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segmentGroundEmptyPickUpTime, "partner1");
        enrichSegmentWithFacilities(segmentGroundEmptyPickUpTime, "A", "B");
        ShipmentJourney groundEmptyPickUpTime = createDummyJourneyFromSegments(segmentGroundEmptyPickUpTime);

        PackageJourneySegment segmentGroundNoDropOffTime = createDummyGroundSegment(VALID_PICK_UP_TIME, null);
        enrichSegmentWithPartner(segmentGroundNoDropOffTime, "partner1");
        enrichSegmentWithFacilities(segmentGroundNoDropOffTime, "A", "B");
        ShipmentJourney groundNoDropOffTime = createDummyJourneyFromSegments(segmentGroundNoDropOffTime);

        PackageJourneySegment segmentGroundEmptyDropOffTime = createDummyGroundSegment(VALID_PICK_UP_TIME, "");
        enrichSegmentWithPartner(segmentGroundEmptyDropOffTime, "partner1");
        enrichSegmentWithFacilities(segmentGroundEmptyDropOffTime, "A", "B");
        ShipmentJourney groundEmptyDropOffTime = createDummyJourneyFromSegments(segmentGroundEmptyDropOffTime);

        PackageJourneySegment segmentIdenticalTimes = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segmentIdenticalTimes, "partner1");
        enrichSegmentWithFacilities(segmentIdenticalTimes, "A", "A");
        ShipmentJourney identicalTimes = createDummyJourneyFromSegments(segmentIdenticalTimes);

        PackageJourneySegment segmentNoPickupFacility = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segmentNoPickupFacility, "partner1");
        enrichSegmentWithFacilities(segmentNoPickupFacility, null, "B");
        ShipmentJourney noPickupFacility = createDummyJourneyFromSegments(segmentNoPickupFacility);

        PackageJourneySegment segmentNoEndFacility = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segmentNoEndFacility, "partner1");
        enrichSegmentWithFacilities(segmentNoEndFacility, "A", null);
        ShipmentJourney noEndFacility = createDummyJourneyFromSegments(segmentNoEndFacility);

        PackageJourneySegment segmentNoBothFacilities = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segmentNoBothFacilities, "partner1");
        enrichSegmentWithFacilities(segmentNoBothFacilities, null, null);
        ShipmentJourney noBothFacilities = createDummyJourneyFromSegments(segmentNoBothFacilities);

        PackageJourneySegment segmentBlankBothFacilities = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segmentBlankBothFacilities, "partner1");
        enrichSegmentWithFacilities(segmentBlankBothFacilities, "", " ");
        ShipmentJourney blankBothFacilities = createDummyJourneyFromSegments(segmentBlankBothFacilities);

        PackageJourneySegment segmentGroundNoPartner = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithFacilities(segmentGroundNoPartner, "A", "B");
        ShipmentJourney groundNoPartner = createDummyJourneyFromSegments(segmentGroundNoPartner);

        PackageJourneySegment identicalSegmentA = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(identicalSegmentA, "partner1");
        enrichSegmentWithFacilities(identicalSegmentA, "A", "B");

        PackageJourneySegment identicalSegmentB = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(identicalSegmentB, "partner1");
        enrichSegmentWithFacilities(identicalSegmentB, "B", "A");
        identicalSegmentB.setSegmentId(identicalSegmentA.getSegmentId());
        identicalSegmentB.setJourneyId(identicalSegmentA.getJourneyId());

        ShipmentJourney identicalSegments = createDummyJourneyFromSegments(identicalSegmentA, identicalSegmentB);

        PackageJourneySegment segment2GroundNoPickUpTime = createDummyGroundSegment(null, VALID_DROP_OFF2_TIME);
        enrichSegmentWithPartner(segment2GroundNoPickUpTime, "partner2");
        enrichSegmentWithFacilities(segment2GroundNoPickUpTime, "B", "C");

        PackageJourneySegment segment2DifferentFacility = createDummyGroundSegment(2, VALID_PICK_UP2_TIME, VALID_DROP_OFF2_TIME);
        enrichSegmentWithPartner(segment2DifferentFacility, "partner2");
        enrichSegmentWithFacilities(segment2DifferentFacility, "Z", "C");

        ShipmentJourney segment1ValidSegment2Invalid = createDummyJourneyFromSegments(segment1ValidCombination, segment2GroundNoPickUpTime);

        ShipmentJourney segment1InvalidSegment2Valid = createDummyJourneyFromSegments(segmentGroundNoDropOffTime, segment2ValidCombination);

        ShipmentJourney segmentsNotConnected = createDummyJourneyFromSegments(segment1ValidCombination, segment2DifferentFacility);

        PackageJourneySegment segment1NoDropOffFacility = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segment1NoDropOffFacility, "partner1");
        enrichSegmentWithFacilities(segment1NoDropOffFacility, "A", null);

        PackageJourneySegment segment1BlankDropOffFacility = createDummyGroundSegment(VALID_PICK_UP_TIME, VALID_DROP_OFF_TIME);
        enrichSegmentWithPartner(segment1BlankDropOffFacility, "partner1");
        enrichSegmentWithFacilities(segment1BlankDropOffFacility, "A", "");

        PackageJourneySegment segment2NoPickUpFacility = createDummyGroundSegment(2, VALID_PICK_UP2_TIME, VALID_DROP_OFF2_TIME);
        enrichSegmentWithPartner(segment2NoPickUpFacility, "partner2");
        enrichSegmentWithFacilities(segment2NoPickUpFacility, null, "C");

        PackageJourneySegment segment2BlankPickUpFacility = createDummyGroundSegment(2, VALID_PICK_UP2_TIME, VALID_DROP_OFF2_TIME);
        enrichSegmentWithPartner(segment2BlankPickUpFacility, "partner2");
        enrichSegmentWithFacilities(segment2BlankPickUpFacility, "", "C");

        ShipmentJourney missingMiddleFacility = createDummyJourneyFromSegments(segment1NoDropOffFacility, segment2NoPickUpFacility);
        ShipmentJourney missingMiddleFacility2 = createDummyJourneyFromSegments(segment1BlankDropOffFacility, segment2NoPickUpFacility);
        ShipmentJourney missingMiddleFacility3 = createDummyJourneyFromSegments(segment1NoDropOffFacility, segment2BlankPickUpFacility);
        ShipmentJourney missingMiddleFacility4 = createDummyJourneyFromSegments(segment1BlankDropOffFacility, segment2BlankPickUpFacility);

        ShipmentJourney partialMiddleFacility = createDummyJourneyFromSegments(segment1ValidCombination, segment2NoPickUpFacility);
        ShipmentJourney partialMiddleFacility2 = createDummyJourneyFromSegments(segment1ValidCombination, segment2BlankPickUpFacility);
        ShipmentJourney partialMiddleFacility3 = createDummyJourneyFromSegments(segment1NoDropOffFacility, segment2ValidCombination);
        ShipmentJourney partialMiddleFacility4 = createDummyJourneyFromSegments(segment1BlankDropOffFacility, segment2ValidCombination);

        PackageJourneySegment airSegmentNoDeparture = createDummyAirSegment(VALID_LOCK_OUT_TIME, null, VALID_ARRIVAL_TIME, VALID_RECOVERY_TIME);
        enrichSegmentWithPartner(airSegmentNoDeparture, "partner1");
        enrichSegmentWithFacilities(airSegmentNoDeparture, "A", "B");
        ShipmentJourney airNoDeparture = createDummyJourneyFromSegments(airSegmentNoDeparture);

        PackageJourneySegment airSegmentBlankDeparture = createDummyAirSegment(VALID_LOCK_OUT_TIME, " ", VALID_ARRIVAL_TIME, VALID_RECOVERY_TIME);
        enrichSegmentWithPartner(airSegmentBlankDeparture, "partner1");
        enrichSegmentWithFacilities(airSegmentBlankDeparture, "A", "B");
        ShipmentJourney airBlankDeparture = createDummyJourneyFromSegments(airSegmentBlankDeparture);

        return Stream.of(
                Arguments.of(Named.of("Single Segment facility ID defined in internal id field",
                        segmentWithIdDefined), PackageJourneySegmentException.class, FACILITY_ID_SET_ERR_MSG),
                Arguments.of(Named.of("Single Segment No Pick Up Time",
                        groundNoPickUpTime), PackageJourneySegmentException.class, EMPTY_PICKUP_DROP_OFF_TIME),
                Arguments.of(Named.of("Single Segment Empty Pick Up Time",
                        groundEmptyPickUpTime), PackageJourneySegmentException.class, EMPTY_PICKUP_DROP_OFF_TIME),
                Arguments.of(Named.of("Single Segment No Drop Off Time",
                        groundNoDropOffTime), PackageJourneySegmentException.class, EMPTY_PICKUP_DROP_OFF_TIME),
                Arguments.of(Named.of("Single Segment Empty Drop Off Time",
                        groundEmptyDropOffTime), PackageJourneySegmentException.class, EMPTY_PICKUP_DROP_OFF_TIME),
                Arguments.of(Named.of("Single Segment Identical Pickup and Drop Off times",
                        identicalTimes), PackageJourneySegmentException.class, PICKUP_DROP_OFF_SAME_ERR_MSG),
                Arguments.of(Named.of("Single Segment No Start Facility",
                        noPickupFacility), PackageJourneySegmentException.class, EMPTY_FACILITY_ID),
                Arguments.of(Named.of("Single Segment No End Facility",
                        noEndFacility), PackageJourneySegmentException.class, EMPTY_FACILITY_ID),
                Arguments.of(Named.of("Single Segment No Start nor End Facilities",
                        noBothFacilities), PackageJourneySegmentException.class, EMPTY_FACILITY_ID),
                Arguments.of(Named.of("Single Segment Blank Start nor End Facilities",
                        blankBothFacilities), PackageJourneySegmentException.class, EMPTY_FACILITY_ID),
                Arguments.of(Named.of("Single Segment No Partner",
                        groundNoPartner), PackageJourneySegmentException.class, EMPTY_PARTNER_ID),
                Arguments.of(Named.of("Journey with Identical Segments",
                        identicalSegments), SegmentException.class, DUPLICATE_SEGMENT_FOUND_ERR_MSG),
                Arguments.of(Named.of("Segment 1 drop_off_time has value and Segment 2 pick_up_time is null",
                        segment1ValidSegment2Invalid), PackageJourneySegmentException.class, EMPTY_PICKUP_DROP_OFF_TIME),
                Arguments.of(Named.of("Segment 1 drop_off_time is empty and Segment 2 pick_up_time has value",
                        segment1InvalidSegment2Valid), PackageJourneySegmentException.class, EMPTY_PICKUP_DROP_OFF_TIME),
                Arguments.of(Named.of("Segment 1 dropOff has value and Segment 2 pickup has value but different",
                        segmentsNotConnected), PackageJourneySegmentException.class, FACILITY_DROP_PICKUP_NOT_MATCH_ERR_MSG),
                Arguments.of(Named.of("Segment 1 drop off facility(null), Segment 2 pick up facility(null)",
                        missingMiddleFacility), PackageJourneySegmentException.class, EMPTY_FACILITY_ID),
                Arguments.of(Named.of("Segment 1 drop off facility(blank), Segment 2 pick up facility(null)",
                        missingMiddleFacility2), PackageJourneySegmentException.class, EMPTY_FACILITY_ID),
                Arguments.of(Named.of("Segment 1 drop off facility(null), Segment 2 pick up facility(blank)",
                        missingMiddleFacility3), PackageJourneySegmentException.class, EMPTY_FACILITY_ID),
                Arguments.of(Named.of("Segment 1 drop off facility(blank), Segment 2 pick up facility(blank)",
                        missingMiddleFacility4), PackageJourneySegmentException.class, EMPTY_FACILITY_ID),
                Arguments.of(Named.of("Segment 1 drop off facility present, Segment 2 pick up facility(null)",
                        partialMiddleFacility), PackageJourneySegmentException.class, EMPTY_FACILITY_ID),
                Arguments.of(Named.of("Segment 1 drop off facility present, Segment 2 pick up facility(blank)",
                        partialMiddleFacility2), PackageJourneySegmentException.class, EMPTY_FACILITY_ID),
                Arguments.of(Named.of("Segment 1 drop off facility(null), Segment 2 pick up facility present",
                        partialMiddleFacility3), PackageJourneySegmentException.class, EMPTY_FACILITY_ID),
                Arguments.of(Named.of("Segment 1 drop off facility(blank), Segment 2 pick up facility present",
                        partialMiddleFacility4), PackageJourneySegmentException.class, EMPTY_FACILITY_ID),
                Arguments.of(Named.of("Air Segment departure time is missing",
                        airNoDeparture), PackageJourneySegmentException.class, EMPTY_DEPARTURE_ARRIVAL_TIME),
                Arguments.of(Named.of("Air Segment departure time is blank",
                        airBlankDeparture), PackageJourneySegmentException.class, EMPTY_DEPARTURE_ARRIVAL_TIME)
        );

    }
}
