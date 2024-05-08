package com.quincus.shipment.impl.attachment.networklane;

import com.quincus.shipment.api.dto.csv.NetworkLaneCsv;
import com.quincus.shipment.api.dto.csv.NetworkLaneSegmentCsv;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class NetworkLaneCsvValidatorTest {
    private static final String INVALID_CALCULATED_MILEAGE_UNIT = "miles";
    private static final String INVALID_DURATION_UNIT = "minutes";
    private static final String INVALID_TRANSPORT_TYPE = "grounds";
    private static final String INVALID_DATE_TIME = "2022-12-20 16:27:02 07:00";

    private final NetworkLaneCsvValidator validator = new NetworkLaneCsvValidator();

    @Test
    void givenBlankDataForNetworkLaneWhenValidateThenTriggerExpectedValidation() {
        // GIVEN:
        NetworkLaneCsv networkLaneCsv = new NetworkLaneCsv();
        NetworkLaneSegmentCsv segmentCsvData = new NetworkLaneSegmentCsv();
        segmentCsvData.setAirline("test");
        networkLaneCsv.setNetworkLaneSegments(List.of(segmentCsvData));

        // WHEN:
        assertThatThrownBy(() -> validator.validate(networkLaneCsv)).isInstanceOf(QuincusValidationException.class);

        String[] errors = networkLaneCsv.getFailedReason().split("\\|");
        assertThat(errors).hasSize(9);
    }

    @Test
    void givenNetworkLaneWithBlankNetworkLaneSegmentPaddingWhenValidateThenShouldNotTriggerValidation() {
        // GIVEN:
        NetworkLaneCsv networkLaneCsv = createNetworkLane();
        networkLaneCsv.setNetworkLaneSegments(List.of(new NetworkLaneSegmentCsv(), createValidNetworkLaneSegmentData()));

        // WHEN:
        assertThatNoException().isThrownBy(() -> validator.validate(networkLaneCsv));
        assertThat(networkLaneCsv.getNetworkLaneSegments().get(0).isIgnoreRecord()).isTrue();
    }

    @Test
    void givenNetworkLaneCsvNoPickUpFacilityOnFirstSegmentAndNoDropOffFacilityOnLastSegment_noValidationThrownByValidate() {
        // GIVEN:
        NetworkLaneCsv networkLaneCsv = createNetworkLane();
        networkLaneCsv.setNetworkLaneSegments(List.of(createNetworkLaneSegmentWithNoPickUpFacility(), createNetworkLaneSegmentWithNoDropOffFacility()));

        // WHEN:
        assertThatNoException().isThrownBy(() -> validator.validate(networkLaneCsv));
    }

    @Test
    void givenNetworkLaneCsvNoPickUpFacilityOnFirstSegmentAndNoDropOffFacilityOnMidSegment_shouldValidateFacilityMandatory() {
        // GIVEN:
        NetworkLaneCsv networkLaneCsv = createNetworkLane();
        networkLaneCsv.setNetworkLaneSegments(List.of(createValidNetworkLaneSegmentData()
                , createNetworkLaneSegmentWithNoPickUpFacility()
                , createNetworkLaneSegmentWithNoDropOffFacility()
                , createValidNetworkLaneSegmentData()));

        // WHEN:
        assertThatThrownBy(() -> validator.validate(networkLaneCsv)).isInstanceOf(QuincusValidationException.class);

        String[] errors = networkLaneCsv.getFailedReason().split("\\|");
        assertThat(errors[0]).isEqualTo("Segment 2: `Pickup Facility Name` is Mandatory ");
        assertThat(errors[1]).isEqualTo(" Segment 3: `Drop Off Facility Name` is Mandatory");
    }

    @Test
    void givenNetworkLaneCsvHasPaddingAndNoDropOffFacilityInLastSegment_shouldHaveNoValidationError() {
        // GIVEN:
        NetworkLaneCsv networkLaneCsv = createNetworkLane();
        networkLaneCsv.setNetworkLaneSegments(List.of(createNetworkLaneSegmentWithNoPickUpFacility()
                , createNetworkLaneSegmentWithNoDropOffFacility()
                , new NetworkLaneSegmentCsv(), new NetworkLaneSegmentCsv()));

        // WHEN:
        assertThatNoException().isThrownBy(() -> validator.validate(networkLaneCsv));
    }

    @Test
    void givenBlankDataForNetworkLaneSegmentsWhenValidateThenTriggerExpectedValidation() {
        // GIVEN:
        NetworkLaneCsv networkLaneCsv = createNetworkLane();
        networkLaneCsv.setNetworkLaneSegments(List.of(createNetworkLaneSegmentCsvWithInvalidEnumValue()));

        // WHEN:
        assertThatThrownBy(() -> validator.validate(networkLaneCsv)).isInstanceOf(QuincusValidationException.class);
        String[] errors = networkLaneCsv.getFailedReason().split("\\|");
        assertThat(errors).hasSize(7);
    }

    @Test
    void givenValidNetworkLaneWithNoSegmentsWhenValidateThenErrorOnNoNetworkLaneSegmentFoundExpected() {
        // GIVEN:
        NetworkLaneCsv networkLaneCsv = createNetworkLane();
        networkLaneCsv.setNetworkLaneSegments(new ArrayList<>());

        // WHEN:
        assertThatThrownBy(() -> validator.validate(networkLaneCsv)).isInstanceOf(QuincusValidationException.class)
                .hasMessage("No Network Network Lane Connection found");
    }

    private NetworkLaneCsv createNetworkLane() {
        NetworkLaneCsv networkLaneCsv = new NetworkLaneCsv();
        networkLaneCsv.setDestinationFacilityId("dest-facility-id");
        networkLaneCsv.setDestinationLocationTreeLevel1("dest-location-tree-1");
        networkLaneCsv.setDestinationLocationTreeLevel2("dest-location-tree-2");
        networkLaneCsv.setDestinationLocationTreeLevel3("dest-location-tree-3");
        networkLaneCsv.setOriginLocationTreeLevel1("origin-location-tree-1");
        networkLaneCsv.setOriginLocationTreeLevel2("origin-location-tree-2");
        networkLaneCsv.setOriginLocationTreeLevel3("origin-location-tree-3");
        networkLaneCsv.setOriginFacilityId("origin-facility-id");
        networkLaneCsv.setServiceType("test-service-type");
        return networkLaneCsv;
    }

    private NetworkLaneSegmentCsv createNetworkLaneSegmentCsvWithInvalidEnumValue() {
        NetworkLaneSegmentCsv networkLaneSegment = new NetworkLaneSegmentCsv();
        networkLaneSegment.setSequenceNumber("1");
        networkLaneSegment.setTransportCategory(INVALID_TRANSPORT_TYPE);
        networkLaneSegment.setPartnerName("test-partner");
        networkLaneSegment.setVehicleInfo("test-vehicle-info");
        networkLaneSegment.setFlightNumber("test-flight-number");
        networkLaneSegment.setAirline("test-airline");
        networkLaneSegment.setAirlineCode("test-airline-code");
        networkLaneSegment.setMasterWaybill("test-master-waybill");
        networkLaneSegment.setPickupFacilityName("test-facility-pickup-name");
        networkLaneSegment.setDropOffFacilityName("test-facility-dropoff-name");
        networkLaneSegment.setPickupInstruction("test-puckup-instruction");
        networkLaneSegment.setDropOffInstruction("test-dropoff-instruction");
        networkLaneSegment.setDuration("123");
        networkLaneSegment.setDurationUnit(INVALID_DURATION_UNIT);
        networkLaneSegment.setPickUpTime(INVALID_DATE_TIME);
        networkLaneSegment.setDropOffTime(INVALID_DATE_TIME);
        networkLaneSegment.setLockOutTime(INVALID_DATE_TIME);
        networkLaneSegment.setDepartureTime(INVALID_DATE_TIME);
        networkLaneSegment.setArrivalTime(INVALID_DATE_TIME);
        networkLaneSegment.setRecoveryTime(INVALID_DATE_TIME);
        networkLaneSegment.setCalculatedMileage("123");
        networkLaneSegment.setCalculatedMileageUnit(INVALID_CALCULATED_MILEAGE_UNIT);
        return networkLaneSegment;
    }

    private NetworkLaneSegmentCsv createValidNetworkLaneSegmentData() {
        NetworkLaneSegmentCsv networkLaneSegment = new NetworkLaneSegmentCsv();
        networkLaneSegment.setSequenceNumber("1");
        networkLaneSegment.setTransportCategory("GROUND");
        networkLaneSegment.setPartnerName("test-partner");
        networkLaneSegment.setVehicleInfo("test-vehicle-info");
        networkLaneSegment.setFlightNumber("test-flight-number");
        networkLaneSegment.setAirline("test-airline");
        networkLaneSegment.setAirlineCode("test-airline-code");
        networkLaneSegment.setMasterWaybill("test-master-waybill");
        networkLaneSegment.setPickupFacilityName("test-facility-pickup-name");
        networkLaneSegment.setDropOffFacilityName("test-facility-dropoff-name");
        networkLaneSegment.setPickupInstruction("test-puckup-instruction");
        networkLaneSegment.setDropOffInstruction("test-dropoff-instruction");
        networkLaneSegment.setDuration("123");
        networkLaneSegment.setDurationUnit("MINUTE");
        networkLaneSegment.setPickUpTime("2022-12-12T16:27:02+07:00");
        networkLaneSegment.setDropOffTime("2022-12-12T16:27:02+07:00");
        networkLaneSegment.setLockOutTime("2022-12-12T16:27:02+07:00");
        networkLaneSegment.setDepartureTime("2022-12-12T16:27:02+07:00");
        networkLaneSegment.setArrivalTime("2022-12-12T16:27:02+07:00");
        networkLaneSegment.setRecoveryTime("2022-12-12T16:27:02+07:00");
        networkLaneSegment.setCalculatedMileage("123");
        networkLaneSegment.setCalculatedMileageUnit("MINUTE");
        return networkLaneSegment;
    }

    private NetworkLaneSegmentCsv createNetworkLaneSegmentWithNoPickUpFacility() {
        NetworkLaneSegmentCsv networkLaneSegment = createValidNetworkLaneSegmentData();
        networkLaneSegment.setPickupFacilityName(null);
        return networkLaneSegment;
    }

    private NetworkLaneSegmentCsv createNetworkLaneSegmentWithNoDropOffFacility() {
        NetworkLaneSegmentCsv networkLaneSegment = createValidNetworkLaneSegmentData();
        networkLaneSegment.setDropOffFacilityName(null);
        return networkLaneSegment;
    }
}
