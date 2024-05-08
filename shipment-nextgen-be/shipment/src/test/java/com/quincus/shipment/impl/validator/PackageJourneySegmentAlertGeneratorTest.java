package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.constant.AlertMessage;
import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.ShipmentJourney;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PackageJourneySegmentAlertGeneratorTest {
    @InjectMocks
    PackageJourneySegmentAlertGenerator packageJourneySegmentAlertGenerator;

    @Test
    void validateGroundSegmentAndAddAlert_withMissingFields_shouldHaveAnErrorAlert() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        List<PackageJourneySegment> segments = new ArrayList<>();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setType(SegmentType.FIRST_MILE);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setSequence("1");
        segments.add(segment);
        shipmentJourney.setPackageJourneySegments(segments);

        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, false);

        assertThat(segment.getAlerts().get(0).getType()).isEqualTo(AlertType.ERROR);
        assertThat(segment.getAlerts().get(0).getShortMessage()).isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.toString());
        assertThat(segment.getAlerts().get(0).getFields())
                .hasToString("[ops_type, pick_up_time, drop_off_time, start_facility, end_facility, partner]");
        assertThat(shipmentJourney.getAlerts().get(0).getMessage())
                .isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.getFullMessage() + " [Segment 1]");
    }

    @Test
    void validateGroundSegmentAndAddAlert_withMissingFields_andFromKafka_shouldHaveWarningAlert() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        List<PackageJourneySegment> segments = new ArrayList<>();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setType(SegmentType.FIRST_MILE);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setSequence("1");
        segments.add(segment);
        shipmentJourney.setPackageJourneySegments(segments);

        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, false);

        assertThat(segment.getAlerts().get(0).getType()).isEqualTo(AlertType.ERROR);
        assertThat(segment.getAlerts().get(0).getShortMessage()).isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.toString());
        assertThat(segment.getAlerts().get(0).getFields())
                .hasToString("[ops_type, pick_up_time, drop_off_time, start_facility, end_facility, partner]");
        assertThat(shipmentJourney.getAlerts().get(0).getMessage())
                .isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.getFullMessage() + " [Segment 1]");
    }

    @Test
    void validateAirSegmentAndAddAlert_withMissingFields_shouldHaveAnErrorAlert() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        List<PackageJourneySegment> segments = new ArrayList<>();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setType(SegmentType.LAST_MILE);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.AIR);
        segment.setSequence("1");
        segment.setFlightNumber("163");
        segment.setAirlineCode("PR");
        segment.setDepartureTime("2022-12-17 17:27:02 +0700");
        segments.add(segment);
        shipmentJourney.setPackageJourneySegments(segments);

        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, false);

        assertThat(segment.getAlerts().get(0).getType()).isEqualTo(AlertType.ERROR);
        assertThat(segment.getAlerts().get(0).getShortMessage()).isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.toString());
        assertThat(segment.getAlerts().get(0).getFields())
                .hasToString("[ops_type, start_facility, end_facility, partner]");
        assertThat(shipmentJourney.getAlerts().get(0).getMessage())
                .isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.getFullMessage() + " [Segment 1]");
    }

    @Test
    void validateAirSegmentAndAddAlert_withMissingFields_andFromOM_shouldHaveWarningAlert() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        List<PackageJourneySegment> segments = new ArrayList<>();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setType(SegmentType.LAST_MILE);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.AIR);
        segment.setSequence("1");
        segment.setFlightNumber("163");
        segment.setAirlineCode("PR");
        segment.setDepartureTime("2022-12-17 17:27:02 +0700");
        segments.add(segment);
        shipmentJourney.setPackageJourneySegments(segments);

        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, false);

        assertThat(segment.getAlerts().get(0).getType()).isEqualTo(AlertType.ERROR);
        assertThat(segment.getAlerts().get(0).getShortMessage()).isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.toString());
        assertThat(segment.getAlerts().get(0).getFields())
                .hasToString("[ops_type, start_facility, end_facility, partner]");
        assertThat(shipmentJourney.getAlerts().get(0).getMessage())
                .isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.getFullMessage() + " [Segment 1]");
    }

    @Test
    void validateSegmentsAndAddAlert_withOverlappingSegmentsTime_shouldHaveAWarningAlert() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        List<PackageJourneySegment> segments = new ArrayList<>();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setType(SegmentType.FIRST_MILE);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setRefId("0");
        segment.setSequence("0");
        segment.setPickUpTime("2022-12-12T16:27:02+07:00");
        segment.setPickUpCommitTime("2022-12-12T16:27:02+07:00");
        segment.setDropOffTime("2022-12-16T16:27:02+07:00");
        segment.setDropOffCommitTime("2022-12-16T16:27:02+07:00");
        segment.setOpsType("H&S");
        Partner partner = new Partner();
        partner.setId(UUID.randomUUID().toString());
        segment.setPartner(partner);
        segment.setStartFacility(new Facility());
        segment.setEndFacility(new Facility());

        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setType(SegmentType.LAST_MILE);
        segment2.setStatus(SegmentStatus.PLANNED);
        segment2.setTransportType(TransportType.GROUND);
        segment2.setRefId("1");
        segment2.setSequence("1");
        segment2.setPickUpTime("2022-12-12T16:27:02+07:00");
        segment2.setPickUpCommitTime("2022-12-12T16:27:02+07:00");
        segment2.setDropOffTime("2022-12-16T16:27:02+07:00");
        segment2.setDropOffCommitTime("2022-12-16T16:27:02+07:00");
        segment2.setOpsType("H&S");
        segment2.setPartner(partner);
        segment2.setStartFacility(new Facility());
        segment2.setEndFacility(new Facility());

        segments.add(segment);
        segments.add(segment2);

        shipmentJourney.setPackageJourneySegments(segments);

        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, false);

        assertThat(segment.getAlerts()).isNull();
        assertThat(segment2.getAlerts().get(0).getType()).isEqualTo(AlertType.WARNING);

        assertThat(segment2.getAlerts().get(0).getShortMessage())
                .isEqualTo(AlertMessage.TIME_OVERLAP_ACROSS_SEGMENTS.toString());
        assertThat(shipmentJourney.getAlerts().get(0).getMessage())
                .isEqualTo(AlertMessage.TIME_OVERLAP_ACROSS_SEGMENTS.getFullMessage() + " [Segment 2]");
    }

    @Test
    void validateSegmentsAndAddAlert_withInvalidMAWB_shouldHaveAWarningAlert() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        List<PackageJourneySegment> segments = new ArrayList<>();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setType(SegmentType.FIRST_MILE);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setRefId("0");
        segment.setSequence("0");
        segment.setPickUpTime("2022-12-12T16:27:02+07:00");
        segment.setPickUpCommitTime("2022-12-12T16:27:02+07:00");
        segment.setDropOffTime("2022-12-16T16:27:02+07:00");
        segment.setDropOffCommitTime("2022-12-16T16:27:02+07:00");
        segment.setOpsType("H&S");
        Partner partner = new Partner();
        partner.setId(UUID.randomUUID().toString());
        segment.setPartner(partner);
        segment.setStartFacility(new Facility());
        segment.setEndFacility(new Facility());

        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setType(SegmentType.LAST_MILE);
        segment2.setStatus(SegmentStatus.PLANNED);
        segment2.setTransportType(TransportType.AIR);
        segment2.setRefId("1");
        segment2.setSequence("1");
        segment2.setLockOutTime("2022-12-17T16:27:02+07:00");
        segment2.setDepartureTime("2022-12-17T17:27:02+07:00");
        segment2.setRecoveryTime("2022-12-19T16:27:02+07:00");
        segment2.setOpsType("H&S");
        segment2.setPartner(partner);
        segment2.setFlightNumber("163");
        segment2.setAirlineCode("PR");
        segment2.setStartFacility(new Facility());
        segment2.setEndFacility(new Facility());
        segment2.setMasterWaybill("123-222334");

        segments.add(segment);
        segments.add(segment2);

        shipmentJourney.setPackageJourneySegments(segments);

        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, false);

        assertThat(segment.getAlerts()).isNull();
        assertThat(segment2.getAlerts().get(0).getType()).isEqualTo(AlertType.WARNING);

        assertThat(segment2.getAlerts().get(0).getShortMessage())
                .isEqualTo(AlertMessage.MAWB_NO_CHECKSUM_VALIDATION.toString());
        assertThat(segment2.getAlerts().get(0).getFields())
                .hasToString("[master_waybill]");
        assertThat(shipmentJourney.getAlerts().get(0).getMessage())
                .isEqualTo(AlertMessage.MAWB_NO_CHECKSUM_VALIDATION.getFullMessage() + " [Segment 2]");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void validateSegmentsAndAddAlert_withEmptyOrBlankMAWB_shouldNotHaveAWarningAlert(String masterWayBill) {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        List<PackageJourneySegment> segments = new ArrayList<>();
        PackageJourneySegment groundSegment = new PackageJourneySegment();
        groundSegment.setType(SegmentType.FIRST_MILE);
        groundSegment.setStatus(SegmentStatus.PLANNED);
        groundSegment.setTransportType(TransportType.GROUND);
        groundSegment.setRefId("0");
        groundSegment.setSequence("0");
        groundSegment.setPickUpTime("2022-12-12T16:27:02+07:00");
        groundSegment.setPickUpCommitTime("2022-12-12T16:27:02+07:00");
        groundSegment.setDropOffTime("2022-12-16T16:27:02+07:00");
        groundSegment.setDropOffCommitTime("2022-12-16T16:27:02+07:00");
        groundSegment.setOpsType("H&S");
        Partner partner = new Partner();
        partner.setId(UUID.randomUUID().toString());
        groundSegment.setPartner(partner);
        groundSegment.setStartFacility(new Facility());
        groundSegment.setEndFacility(new Facility());

        PackageJourneySegment airSegment = new PackageJourneySegment();
        airSegment.setType(SegmentType.LAST_MILE);
        airSegment.setStatus(SegmentStatus.PLANNED);
        airSegment.setTransportType(TransportType.AIR);
        airSegment.setRefId("1");
        airSegment.setSequence("1");
        airSegment.setLockOutTime("2022-12-17T16:27:02+07:00");
        airSegment.setDepartureTime("2022-12-17T17:27:02+07:00");
        airSegment.setRecoveryTime("2022-12-19T16:27:02+07:00");
        airSegment.setOpsType("H&S");
        airSegment.setPartner(partner);
        airSegment.setFlightNumber("163");
        airSegment.setAirlineCode("PR");
        airSegment.setStartFacility(new Facility());
        airSegment.setEndFacility(new Facility());
        airSegment.setMasterWaybill(masterWayBill);

        segments.add(groundSegment);
        segments.add(airSegment);

        shipmentJourney.setPackageJourneySegments(segments);

        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, false);

        assertThat(groundSegment.getAlerts()).isNull();
        assertThat(airSegment.getAlerts()).isNull();
    }

    @Test
    void validateSegmentsAndAddAlert_withValidData_shouldHaveNoAlert() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        List<PackageJourneySegment> segments = new ArrayList<>();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setType(SegmentType.FIRST_MILE);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setRefId("0");
        segment.setSequence("0");
        segment.setPickUpTime("2022-12-12T16:27:02+07:00");
        segment.setPickUpCommitTime("2022-12-12T16:27:02+07:00");
        segment.setDropOffTime("2022-12-16T16:27:02+07:00");
        segment.setDropOffCommitTime("2022-12-16T16:27:02+07:00");
        segment.setOpsType("H&S");
        Partner partner = new Partner();
        partner.setId(UUID.randomUUID().toString());
        segment.setPartner(partner);
        segment.setStartFacility(new Facility());
        segment.setEndFacility(new Facility());

        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setType(SegmentType.LAST_MILE);
        segment2.setStatus(SegmentStatus.PLANNED);
        segment2.setTransportType(TransportType.AIR);
        segment2.setRefId("1");
        segment2.setSequence("1");
        segment2.setLockOutTime("2022-12-17T16:27:02+07:00");
        segment2.setDepartureTime("2022-12-17T17:27:02+07:00");
        segment2.setRecoveryTime("2022-12-19T16:27:02+07:00");
        segment2.setOpsType("H&S");
        segment2.setPartner(partner);
        segment2.setFlightNumber("163");
        segment2.setAirlineCode("PR");
        segment2.setStartFacility(new Facility());
        segment2.setEndFacility(new Facility());
        segment2.setMasterWaybill("526-50135212");

        segments.add(segment);
        segments.add(segment2);

        shipmentJourney.setPackageJourneySegments(segments);

        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, false);

        assertThat(segment.getAlerts()).isNull();
        assertThat(segment2.getAlerts()).isNull();
        assertThat(shipmentJourney.getAlerts()).isEmpty();
    }

    @Test
    void validateAirSegmentAndAddAlert_withFlightNumber_shouldHaveAnErrorAlert() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        List<PackageJourneySegment> segments = new ArrayList<>();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setType(SegmentType.LAST_MILE);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.AIR);
        segment.setSequence("1");
        segment.setAirlineCode("PR");
        segments.add(segment);
        shipmentJourney.setPackageJourneySegments(segments);

        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, false);

        assertThat(segment.getAlerts().get(0).getType()).isEqualTo(AlertType.ERROR);
        assertThat(segment.getAlerts().get(0).getShortMessage()).isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.toString());
        assertThat(segment.getAlerts().get(0).getFields())
                .hasToString("[ops_type, start_facility, end_facility, flight_number, departure_time, partner]");
        assertThat(shipmentJourney.getAlerts().get(0).getMessage())
                .isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.getFullMessage() + " [Segment 1]");
    }

    @Test
    void validateAirSegmentAndAddAlert_withMissingAirlineCode_shouldHaveAnErrorAlert() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        List<PackageJourneySegment> segments = new ArrayList<>();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setType(SegmentType.LAST_MILE);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.AIR);
        segment.setSequence("1");
        segment.setFlightNumber("123");
        segment.setDepartureTime("2022-12-17 17:27:02 +0700");
        segments.add(segment);
        shipmentJourney.setPackageJourneySegments(segments);

        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, false);

        assertThat(segment.getAlerts().get(0).getType()).isEqualTo(AlertType.ERROR);
        assertThat(segment.getAlerts().get(0).getShortMessage()).isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.toString());
        assertThat(segment.getAlerts().get(0).getFields())
                .hasToString("[ops_type, start_facility, end_facility, airline_code, partner]");
        assertThat(shipmentJourney.getAlerts().get(0).getMessage())
                .isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.getFullMessage() + " [Segment 1]");
    }

    @Test
    void validateAirSegmentAndAddAlert_withMissingDepartureTime_shouldHaveAnErrorAlert() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        List<PackageJourneySegment> segments = new ArrayList<>();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setType(SegmentType.LAST_MILE);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.AIR);
        segment.setSequence("1");
        segment.setAirlineCode("PR");
        segment.setFlightNumber("123");
        segments.add(segment);
        shipmentJourney.setPackageJourneySegments(segments);

        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, false);

        assertThat(segment.getAlerts().get(0).getType()).isEqualTo(AlertType.ERROR);
        assertThat(segment.getAlerts().get(0).getShortMessage()).isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.toString());
        assertThat(segment.getAlerts().get(0).getFields())
                .hasToString("[ops_type, start_facility, end_facility, departure_time, partner]");
        assertThat(shipmentJourney.getAlerts().get(0).getMessage())
                .isEqualTo(AlertMessage.MISSING_MANDATORY_FIELDS.getFullMessage() + " [Segment 1]");
    }

    @Test
    void validateSegmentAndAddAlert_withMissingRequiredForOmButNotRequiredForShipmentJourneyUpdate_shouldHaveNoAlert() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        List<PackageJourneySegment> segments = new ArrayList<>();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setType(SegmentType.FIRST_MILE);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setRefId("0");
        segment.setSequence("0");
        segment.setPickUpTime("2022-12-12T16:27:02+07:00");
        segment.setDropOffTime("2022-12-16T16:27:02+07:00");
        //segment.setPickUpCommitTime("2022-12-12 16:27:02+07:00"); remove PickUpCommitTime
        //segment.setDropOffCommitTime("2022-12-16 16:27:02+07:00"); remove DropOffCommitTime
        //segment.setOpsType("H&S"); remove opstype
        Partner partner = new Partner();
        partner.setId(UUID.randomUUID().toString());
        segment.setPartner(partner);
        segment.setStartFacility(new Facility());
        segment.setEndFacility(new Facility());

        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setType(SegmentType.LAST_MILE);
        segment2.setStatus(SegmentStatus.PLANNED);
        segment2.setTransportType(TransportType.AIR);
        segment2.setRefId("1");
        segment2.setSequence("1");
        segment2.setLockOutTime("2022-12-17T16:27:02+07:00");
        segment2.setDepartureTime("2022-12-17T17:27:02+07:00");
        segment2.setRecoveryTime("2022-12-19T16:27:02+07:00");
        segment2.setOpsType("H&S");
        segment2.setPartner(partner);
        segment2.setFlightNumber("163");
        segment2.setAirlineCode("PR");
        segment2.setStartFacility(new Facility());
        segment2.setEndFacility(new Facility());
        segment2.setMasterWaybill("526-50135212");

        segments.add(segment);
        segments.add(segment2);

        shipmentJourney.setPackageJourneySegments(segments);

        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, true);

        assertThat(segment.getAlerts()).isNull();
        assertThat(segment2.getAlerts()).isNull();
        assertThat(shipmentJourney.getAlerts()).isEmpty();
    }
}