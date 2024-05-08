package com.quincus.shipment.kafka.producers.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.kafka.producers.message.qship.QshipSegmentMessage;
import com.quincus.shipment.kafka.producers.test_utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentToQshipMapperImplTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    private ShipmentToQshipMapperImpl mapper;
    @Spy
    private ObjectMapper objectMapper = testUtil.getObjectMapper();

    @Test
    void mapShipmentDomainToQshipSegmentMessageList_validArguments_shouldReturnQshipSegmentMessageList() {
        var shipmentDomain = testUtil.createSingleShipmentFromOrderOnePackageMultiSegmentsJson();
        var segmentDomainList = shipmentDomain.getShipmentJourney().getPackageJourneySegments();
        var qshipSegmentMsgList = mapper.mapShipmentDomainToQshipSegmentMessageList(shipmentDomain);

        assertThat(qshipSegmentMsgList).hasSameSizeAs(segmentDomainList);
    }

    @Test
    void mapShipmentDomainToQshipSegmentMessageList_emptySegmentList_shouldEmptyList() {
        var shipmentDomain = new Shipment();
        shipmentDomain.setShipmentJourney(new ShipmentJourney());
        shipmentDomain.getShipmentJourney().setPackageJourneySegments(Collections.emptyList());

        var qshipSegmentMsgList = mapper.mapShipmentDomainToQshipSegmentMessageList(shipmentDomain);

        assertThat(qshipSegmentMsgList).isEmpty();
    }

    @Test
    void mapShipmentDomainToQshipSegmentMessage_singleSegment_shouldReturnQshipSegmentMessage() {
        int pos = 0;
        var shipmentDomain = testUtil.createSingleShipmentFromOrderMultiPackagesOneSegmentJson();
        var segmentDomain = shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(pos);
        segmentDomain.setInstructions(shipmentDomain.getInstructions());

        var qshipSegmentMsgList = mapper.mapShipmentDomainToQshipSegmentMessageList(shipmentDomain);
        var qshipSegmentMsg = qshipSegmentMsgList.get(pos);

        assertSegmentCommon(segmentDomain, shipmentDomain, qshipSegmentMsg);
        assertSingleSegment(shipmentDomain, qshipSegmentMsg);
    }

    @Test
    void mapShipmentDomainToQshipSegmentMessage_firstSegment_shouldReturnQshipSegmentMessage() {
        int pos = 0;
        var shipmentDomain = testUtil.createSingleShipmentFromOrderMultiPackagesMultiSegmentsJson();
        var segmentDomain = shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(pos);
        segmentDomain.setInstructions(shipmentDomain.getInstructions());

        var qshipSegmentMsgList = mapper.mapShipmentDomainToQshipSegmentMessageList(shipmentDomain);
        var qshipSegmentMsg = qshipSegmentMsgList.get(pos);

        assertSegmentCommon(segmentDomain, shipmentDomain, qshipSegmentMsg);
        assertFirstSegment(shipmentDomain, qshipSegmentMsg);
    }

    @Test
    void mapShipmentDomainToQshipSegmentMessage_middleSegment_shouldReturnQshipSegmentMessage() {
        int pos = 1;
        var shipmentDomain = testUtil.createSingleShipmentFromOrderMultiPackagesMultiSegmentsJson();
        var segmentDomain = shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(pos);

        var qshipSegmentMsgList = mapper.mapShipmentDomainToQshipSegmentMessageList(shipmentDomain);
        var qshipSegmentMsg = qshipSegmentMsgList.get(pos);

        assertSegmentCommon(segmentDomain, shipmentDomain, qshipSegmentMsg);
        assertMiddleSegment(qshipSegmentMsg);
    }

    @Test
    void mapShipmentDomainToQshipSegmentMessage_lastSegment_shouldReturnQshipSegmentMessage() {
        var shipmentDomain = testUtil.createSingleShipmentFromOrderMultiPackagesMultiSegmentsJson();
        var segmentsDomain = shipmentDomain.getShipmentJourney().getPackageJourneySegments();
        int pos = segmentsDomain.size() - 1;
        var segmentDomain = segmentsDomain.get(pos);
        segmentDomain.setInstructions(shipmentDomain.getInstructions());

        var qshipSegmentMsgList = mapper.mapShipmentDomainToQshipSegmentMessageList(shipmentDomain);
        var qshipSegmentMsg = qshipSegmentMsgList.get(pos);

        assertSegmentCommon(segmentDomain, shipmentDomain, qshipSegmentMsg);
        assertLastSegment(shipmentDomain, qshipSegmentMsg);
    }

    @Test
    void mapShipmentDomainToQshipSegmentMessageList_singleSegment_shouldReturnQshipSegmentMessage() {
        int pos = 0;
        Shipment shipmentDomain = testUtil.createSingleShipmentFromOrderMultiPackagesOneSegmentJson();
        ShipmentJourney journeyDomain = shipmentDomain.getShipmentJourney();
        PackageJourneySegment segmentDomain = shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(pos);
        segmentDomain.setInstructions(shipmentDomain.getInstructions());

        List<QshipSegmentMessage> qshipSegmentMsgList = mapper.mapShipmentDomainToQshipSegmentMessageList(shipmentDomain, journeyDomain);
        QshipSegmentMessage qshipSegmentMsg = qshipSegmentMsgList.get(pos);

        assertSegmentCommon(segmentDomain, shipmentDomain, qshipSegmentMsg);
        assertSingleSegment(shipmentDomain, qshipSegmentMsg);
    }

    @Test
    void mapShipmentDomainToQshipSegmentMessageList_shouldIncludeBookingDetailsThatWasSet() {
        int pos = 0;
        String internalBookingReference = "test external booking id";
        String externalBookingReference = "test internal booking Id";
        String rejectionReason = "test rejection reason";
        Shipment shipmentDomain = testUtil.createSingleShipmentFromOrderMultiPackagesOneSegmentJson();
        ShipmentJourney journeyDomain = shipmentDomain.getShipmentJourney();
        PackageJourneySegment segmentDomain = shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(pos);
        segmentDomain.setInstructions(shipmentDomain.getInstructions());
        segmentDomain.setExternalBookingReference(externalBookingReference);
        segmentDomain.setInternalBookingReference(internalBookingReference);
        segmentDomain.setRejectionReason(rejectionReason);
        segmentDomain.setAssignmentStatus("Completed");

        List<QshipSegmentMessage> qshipSegmentMsgList = mapper.mapShipmentDomainToQshipSegmentMessageList(shipmentDomain, journeyDomain);
        QshipSegmentMessage qshipSegmentMsg = qshipSegmentMsgList.get(pos);

        assertThat(qshipSegmentMsg.getInternalBookingReference()).isEqualTo(internalBookingReference);
        assertThat(qshipSegmentMsg.getExternalBookingReference()).isEqualTo(externalBookingReference);
        assertThat(qshipSegmentMsg.getRejectionReason()).isEqualTo(rejectionReason);
        assertThat(qshipSegmentMsg.getAssignmentStatus()).isEqualTo("Completed");

    }

    @Test
    void testMapModifyTime_WithNonNullModifyTimeAndNotDeleted() {
        PackageJourneySegment segmentDomain = mock(PackageJourneySegment.class);
        QshipSegmentMessage qshipSegmentMessage = new QshipSegmentMessage();

        Instant modifyTime = Instant.parse("2023-06-20T10:00:00Z");
        when(segmentDomain.getModifyTime()).thenReturn(modifyTime);
        when(segmentDomain.isDeleted()).thenReturn(false);

        ShipmentToQshipMapperImpl shipmentToQshipMapper = new ShipmentToQshipMapperImpl();
        shipmentToQshipMapper.mapModifyTime(segmentDomain, qshipSegmentMessage);

        ZonedDateTime expectedModifyTime = ZonedDateTime.ofInstant(modifyTime, ZoneId.systemDefault());
        assertThat(qshipSegmentMessage.getUpdatedAt()).isEqualTo(expectedModifyTime);
        assertThat(qshipSegmentMessage.getDeletedAt()).isNull();
    }

    @Test
    void testMapModifyTime_WithNonNullModifyTimeAndDeleted() {
        PackageJourneySegment segmentDomain = mock(PackageJourneySegment.class);
        QshipSegmentMessage qshipSegmentMessage = new QshipSegmentMessage();

        Instant modifyTime = Instant.parse("2023-06-20T10:00:00Z");
        when(segmentDomain.getModifyTime()).thenReturn(modifyTime);
        when(segmentDomain.isDeleted()).thenReturn(true);

        ShipmentToQshipMapperImpl shipmentToQshipMapper = new ShipmentToQshipMapperImpl();
        shipmentToQshipMapper.mapModifyTime(segmentDomain, qshipSegmentMessage);

        ZonedDateTime expectedModifyTime = ZonedDateTime.ofInstant(modifyTime, ZoneId.systemDefault());
        assertThat(qshipSegmentMessage.getUpdatedAt()).isEqualTo(expectedModifyTime);
        assertThat(qshipSegmentMessage.getDeletedAt()).isEqualTo(expectedModifyTime);
    }

    @Test
    void testMapModifyTime_WithNullModifyTime() {
        PackageJourneySegment segmentDomain = mock(PackageJourneySegment.class);
        QshipSegmentMessage qshipSegmentMessage = mock(QshipSegmentMessage.class);

        when(segmentDomain.getModifyTime()).thenReturn(null);

        ShipmentToQshipMapperImpl shipmentToQshipMapper = new ShipmentToQshipMapperImpl();
        shipmentToQshipMapper.mapModifyTime(segmentDomain, qshipSegmentMessage);

        verify(segmentDomain).getModifyTime();
        verify(qshipSegmentMessage, never()).setUpdatedAt(any());
        verify(qshipSegmentMessage, never()).setDeletedAt(any());
        verifyNoMoreInteractions(segmentDomain, qshipSegmentMessage);
    }

    void assertSegmentCommon(PackageJourneySegment segmentDomain, Shipment shipmentDomain,
                             QshipSegmentMessage qshipSegmentMsg) {

        assertThat(qshipSegmentMsg).isNotNull();
        assertThat(qshipSegmentMsg.getId()).isEqualTo(segmentDomain.getSegmentId());
        assertThat(qshipSegmentMsg.getRefId()).isEqualTo(segmentDomain.getRefId());
        assertThat(qshipSegmentMsg.getJourneyId()).isEqualTo(segmentDomain.getJourneyId());
        assertThat(qshipSegmentMsg.getOrderId()).isEqualTo(shipmentDomain.getOrder().getId());
        assertThat(qshipSegmentMsg.getOrganisationId()).isEqualTo(shipmentDomain.getOrganization().getId());
        assertThat(qshipSegmentMsg.getType()).isEqualTo(segmentDomain.getType().getLabel());
        assertThat(qshipSegmentMsg.getStatus()).isEqualTo(segmentDomain.getStatus().getLabel());
        assertThat(qshipSegmentMsg.getSequenceNo()).isEqualTo(segmentDomain.getSequence());
        assertThat(qshipSegmentMsg.getTransportCategory()).isEqualTo(segmentDomain.getTransportType().toString());
        assertThat(qshipSegmentMsg.getPartnerId()).isEqualTo(segmentDomain.getPartner().getId());
        assertThat(qshipSegmentMsg.getVehicleInfo()).isEqualTo(segmentDomain.getVehicleInfo());
        assertThat(qshipSegmentMsg.getFlightNumber()).isEqualTo(segmentDomain.getFlightNumber());
        assertThat(qshipSegmentMsg.getAirline()).isEqualTo(segmentDomain.getAirline());
        assertThat(qshipSegmentMsg.getAirlineCode()).isEqualTo(segmentDomain.getAirlineCode());
        assertThat(qshipSegmentMsg.getMasterWaybill()).isEqualTo(segmentDomain.getMasterWaybill());
        assertThat(qshipSegmentMsg.getPickUpFacilityId()).isEqualTo(segmentDomain.getStartFacility().getExternalId());
        assertThat(qshipSegmentMsg.getDropOffFacilityId()).isEqualTo(segmentDomain.getEndFacility().getExternalId());

        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getPickUpTime(), qshipSegmentMsg.getPickUpTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getLockOutTime(), qshipSegmentMsg.getLockoutTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getDepartureTime(), qshipSegmentMsg.getDepartureTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getArrivalTime(), qshipSegmentMsg.getArrivalTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getDropOffTime(), qshipSegmentMsg.getDropOffTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getRecoveryTime(), qshipSegmentMsg.getRecoveryTime())).isTrue();
        //TODO: add assertion for field `lat`
        //TODO: add assertion for field `lon`

        assertThat(qshipSegmentMsg.getCalculatedMileage().getValue()).isEqualTo(segmentDomain.getCalculatedMileage());
        assertThat(qshipSegmentMsg.getCalculatedMileage().getUom()).isEqualTo(segmentDomain.getCalculatedMileageUnit());
        assertThat(qshipSegmentMsg.getDuration().getValue()).isEqualTo(segmentDomain.getDuration());
        assertThat(qshipSegmentMsg.getDuration().getUom()).isEqualTo(segmentDomain.getDurationUnit());
        assertThat(qshipSegmentMsg.getPackages()).hasSize(1);
        var qshipSegmentPackage = qshipSegmentMsg.getPackages().get(0);

        assertThat(qshipSegmentPackage.getId()).isEqualTo(shipmentDomain.getShipmentPackage().getId());
        assertThat(qshipSegmentPackage.getRefId()).isEqualTo(shipmentDomain.getShipmentPackage().getRefId());
        assertThat(qshipSegmentPackage.getAdditionalData1()).isEqualTo(shipmentDomain.getShipmentReferenceId().get(0));

        assertThat(qshipSegmentMsg.getExternalOrderId()).isEqualTo(shipmentDomain.getExternalOrderId());
        assertThat(qshipSegmentMsg.getInternalOrderId()).isEqualTo(shipmentDomain.getInternalOrderId());
        assertThat(qshipSegmentMsg.getCustomerOrderId()).isEqualTo(shipmentDomain.getCustomerOrderId());
    }

    void assertSingleSegment(Shipment shipmentDomain, QshipSegmentMessage qshipSegmentMsg) {
        assertThat(qshipSegmentMsg.getInstructions()).isNotEmpty();
    }

    void assertFirstSegment(Shipment shipmentDomain, QshipSegmentMessage qshipSegmentMsg) {
        assertThat(qshipSegmentMsg.getInstructions()).isNotEmpty();
    }

    void assertMiddleSegment(QshipSegmentMessage qshipSegmentMsg) {
        assertThat(qshipSegmentMsg.getInstructions()).isEmpty();
    }

    void assertLastSegment(Shipment shipmentDomain, QshipSegmentMessage qshipSegmentMsg) {
        assertThat(qshipSegmentMsg.getInstructions()).isNotEmpty();
    }
}
