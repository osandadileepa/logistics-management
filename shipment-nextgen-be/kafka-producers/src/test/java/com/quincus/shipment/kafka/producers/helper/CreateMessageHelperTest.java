package com.quincus.shipment.kafka.producers.helper;

import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Commodity;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.PricingInfo;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.FlightStatsRequest;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.kafka.producers.mapper.FlightStatsMessageMapper;
import com.quincus.shipment.kafka.producers.mapper.MapperUtil;
import com.quincus.shipment.kafka.producers.mapper.SegmentsDispatchMapper;
import com.quincus.shipment.kafka.producers.mapper.ShipmentToOrderMapper;
import com.quincus.shipment.kafka.producers.mapper.ShipmentToPackageDimensionsMapper;
import com.quincus.shipment.kafka.producers.mapper.ShipmentToQshipMapper;
import com.quincus.shipment.kafka.producers.message.MilestoneMessage;
import com.quincus.shipment.kafka.producers.message.PackageDimensionsMessage;
import com.quincus.shipment.kafka.producers.message.ShipShipmentPathMessage;
import com.quincus.shipment.kafka.producers.message.ShipmentCancelMessage;
import com.quincus.shipment.kafka.producers.message.ShipmentPathMessage;
import com.quincus.shipment.kafka.producers.message.dispatch.OrderMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.PackageMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentsDispatchMessage;
import com.quincus.shipment.kafka.producers.message.dispatch.ShipmentMsgPart;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightEventPayloadMessage;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightStatsMessage;
import com.quincus.shipment.kafka.producers.message.qship.QshipSegmentMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateMessageHelperTest {
    private static final DspSegmentMsgUpdateSource SEGMENT_UPDATE_SHIPMENT_SOURCE = DspSegmentMsgUpdateSource.CLIENT;
    @InjectMocks
    CreateMessageHelper createMessageHelper;

    @Mock
    ShipmentToOrderMapper shipmentToOrderMapper;
    @Mock
    SegmentsDispatchMapper segmentsDispatchMapper;
    @Mock
    ShipmentToQshipMapper shipmentToQshipMapper;
    @Mock
    FlightStatsMessageMapper flightStatsMessageMapper;

    @Mock
    ShipmentToPackageDimensionsMapper shipmentToPackageDimensionsMapper;

    Shipment shipment = null;

    @BeforeEach
    void init() {
        shipment = createShipment();
    }

    @Test
    void createShipmentPackageDimensions_withValidArguments_shouldReturnPackageDimensions() {
        PackageDimensionsMessage packageDimensionMessage = createPackageDimensionFromShipment();
        when(shipmentToPackageDimensionsMapper.mapShipmentToPackageDimensionsMessage(shipment)).thenReturn(packageDimensionMessage);

        PackageDimensionsMessage result = createMessageHelper.createShipmentPackageDimensions(shipment);
        assertThat(result.getOrgId()).isEqualTo(packageDimensionMessage.getOrgId());
        assertThat(result.getPackageId()).isEqualTo(packageDimensionMessage.getPackageId());
        assertThat(result.getGrossWeight()).isEqualTo(packageDimensionMessage.getGrossWeight());
    }

    @Test
    void createMilestoneMessage_withValidArguments_shouldReturnMilestone() {
        MilestoneMessage milestone = createMilestoneFromShipment();
        when(shipmentToOrderMapper.mapShipmentDomainToMilestoneMessage(shipment)).thenReturn(milestone);

        MilestoneMessage result = createMessageHelper.createMilestoneMessage(shipment);
        assertThat(result.getMilestoneId()).isEqualTo(milestone.getMilestoneId());
        assertThat(result.getOrganizationId()).isEqualTo(milestone.getOrganizationId());
        assertThat(result.getPackageId()).isEqualTo(milestone.getPackageId());
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void createShpShipmentPathMessage_withValidArguments_shouldReturnShipShipmentPath() {
        ShipShipmentPathMessage shipShipmentPath = createShipmentPathFromShipment();
        when(shipmentToOrderMapper.mapShipmentDomainToShipmentPathMessage(shipment)).thenReturn(shipShipmentPath);

        ShipShipmentPathMessage result = createMessageHelper.createShipShipmentPathMessage(shipment);
        assertThat(result.getOrganizationId()).isEqualTo(shipShipmentPath.getOrganizationId());
        assertThat(result.getShipmentPath()).hasSameSizeAs(shipShipmentPath.getShipmentPath());
        assertThat(result.getId()).isEqualTo(shipShipmentPath.getId());
    }

    @Test
    void createQshipSegmentMessage_withValidArguments_shouldReturnQshipSegment() {
        QshipSegmentMessage qshipSegment = mock(QshipSegmentMessage.class);
        when(shipmentToQshipMapper.mapShipmentDomainToQshipSegmentMessageList(any(Shipment.class)))
                .thenReturn(List.of(qshipSegment));

        List<QshipSegmentMessage> result = createMessageHelper.createQshipSegmentMessageList(shipment);

        assertThat(result).isNotNull();

        verify(shipmentToQshipMapper, times(1))
                .mapShipmentDomainToQshipSegmentMessageList(any(Shipment.class));
    }

    @Test
    void createShipmentCancelMessage_withValidArguments_shouldReturnShipmentCancel() {
        ShipmentCancelMessage shipmentCancel = createShipmentCancelFromShipment();
        when(shipmentToOrderMapper.mapShipmentDomainToShipmentCancelMessage(shipment)).thenReturn(shipmentCancel);

        ShipmentCancelMessage result = createMessageHelper.createShipmentCancelMessage(shipment);
        assertThat(result.getOrganisationId()).isEqualTo(shipmentCancel.getOrganisationId());
        assertThat(result.getOrderId()).isEqualTo(shipmentCancel.getOrderId());
        assertThat(result.getShipmentId()).isEqualTo(shipmentCancel.getShipmentId());
    }

    @Test
    void createSegmentsDispatchMessage_orderSinglePackage_shouldReturnSegmentsDispatch() {
        var package1MsgPart = new PackageMsgPart();
        SegmentsDispatchMessage segmentsDispatch = new SegmentsDispatchMessage();
        segmentsDispatch.setOrder(new OrderMsgPart());
        segmentsDispatch.setShipments(new ArrayList<>());
        segmentsDispatch.getShipments().add(new ShipmentMsgPart());
        segmentsDispatch.getShipments().get(0).setPackageVal(package1MsgPart);
        when(segmentsDispatchMapper.mapJourneyAndShipmentsToSegmentsDispatchMessage(anyList(), any(ShipmentJourney.class)))
                .thenReturn(segmentsDispatch);

        SegmentsDispatchMessage result = createMessageHelper.createSegmentsDispatchMessage(List.of(shipment),
                shipment.getShipmentJourney(), SegmentDispatchType.SHIPMENT_CREATED, SEGMENT_UPDATE_SHIPMENT_SOURCE);

        var packagesMsgPart = result.getShipments().get(0).getPackageVal();
        assertThat(result.getUpdateSource()).isEqualTo(SEGMENT_UPDATE_SHIPMENT_SOURCE);
        assertThat(packagesMsgPart).isNotNull();
    }

    @Test
    void createSegmentsDispatchMessage_withSingleSegment_shouldReturnSegmentsDispatch() {
        var package1MsgPart = new PackageMsgPart();
        SegmentsDispatchMessage segmentsDispatch = new SegmentsDispatchMessage();
        segmentsDispatch.setOrder(new OrderMsgPart());
        segmentsDispatch.setShipments(new ArrayList<>());
        segmentsDispatch.getShipments().add(new ShipmentMsgPart());
        segmentsDispatch.getShipments().get(0).setPackageVal(package1MsgPart);
        when(segmentsDispatchMapper.mapSegmentAndShipmentsToSegmentsDispatchMessage(anyList(), any(PackageJourneySegment.class)))
                .thenReturn(segmentsDispatch);

        SegmentsDispatchMessage result = createMessageHelper.createSegmentsDispatchMessage(List.of(shipment),
                shipment.getShipmentJourney().getPackageJourneySegments().get(0), SegmentDispatchType.SHIPMENT_CREATED, SEGMENT_UPDATE_SHIPMENT_SOURCE);

        var packagesMsgPart = result.getShipments().get(0).getPackageVal();
        assertThat(packagesMsgPart).isNotNull();
        assertThat(result.getUpdateSource()).isEqualTo(SEGMENT_UPDATE_SHIPMENT_SOURCE);
    }

    @Test
    void createFlightSubscriptionMessage_withValidRequest_shouldReturnFlightStatsMessage() {
        String uuid = randomUUID().toString();

        FlightStatsRequest request = createFlightEventPayloadRequest();
        FlightEventPayloadMessage message = createFlightEventPayloadMessage();
        FlightStatsMessage flightStatsMessage = createFlightStatsMessage(uuid);

        when(flightStatsMessageMapper.mapFlightToEventPayloadMessage(request)).thenReturn(message);
        when(flightStatsMessageMapper.mapToFlightStatsMessage(message, uuid)).thenReturn(flightStatsMessage);

        FlightStatsMessage result = createMessageHelper.createFlightSubscriptionMessage(request, uuid);

        FlightEventPayloadMessage payload = result.getEventPayload();
        assertThat(payload).isNotNull();

        verify(flightStatsMessageMapper, times(1)).mapToFlightStatsMessage(message, uuid);
    }

    @Test
    void createQshipSegmentMessage_shipmentAndSegmentArguments_shouldCallMapper() {
        ShipmentMessageDto shipmentMessageDto = new ShipmentMessageDto();
        PackageJourneySegment segment = new PackageJourneySegment();

        createMessageHelper.createQshipSegmentMessage(shipmentMessageDto, segment);

        verify(shipmentToQshipMapper, times(1)).mapSegmentDomainToQshipSegmentMessage(segment, shipmentMessageDto);
    }

    private FlightStatsRequest createFlightEventPayloadRequest() {
        FlightStatsRequest request = new FlightStatsRequest();
        request.setCarrier("QF");
        request.setOrigin("SYD");
        request.setDestination("MNL");
        request.setFlightNumber("19");
        request.setDepartureDate(LocalDate.parse("2023-02-18"));
        return request;
    }

    private FlightEventPayloadMessage createFlightEventPayloadMessage() {
        FlightEventPayloadMessage message = new FlightEventPayloadMessage();
        message.setCarrier("QF");
        message.setOrigin("SYD");
        message.setDestination("MNL");
        message.setFlightNumber("19");
        message.setDepartureDate("2023-02-18");
        return message;
    }

    private FlightStatsMessage createFlightStatsMessage(String uuid) {
        FlightStatsMessage message = new FlightStatsMessage();
        message.setEventId(uuid);
        message.setCorrelationId(uuid);
        message.setEventDateUtc("2023-02-16T01:04:30.883Z");
        message.setModule("SHP-NEXTGEN");
        message.setEventType("FLIGHT_SUBSCRIBE_RQ");
        message.setEventPayload(createFlightEventPayloadMessage());
        return message;
    }

    private Shipment createShipment() {
        String orgId = "ORG1";
        var organizationDomain = new Organization();
        organizationDomain.setId(orgId);

        String orderId = "ORDER1";
        var orderDomain = new Order();
        orderDomain.setId(orderId);
        orderDomain.setOrderIdLabel("QC123");
        orderDomain.setNotes("NOTES");

        var shipmentId = "SHP-1";
        var shipmentDomain = new Shipment();
        shipmentDomain.setId(shipmentId);
        shipmentDomain.setOrganization(organizationDomain);
        shipmentDomain.setOrder(orderDomain);

        String segmentId = "SEGMENT-1";
        var packageJourneySegmentDomain = new PackageJourneySegment();
        packageJourneySegmentDomain.setSegmentId(segmentId);
        packageJourneySegmentDomain.setTransportType(TransportType.GROUND);

        String startFacilityId = "FAC-1";
        var startFacilityDomain = new Facility();
        startFacilityDomain.setId(startFacilityId);
        packageJourneySegmentDomain.setStartFacility(startFacilityDomain);

        var packageJourneySegmentDomainList = new ArrayList<PackageJourneySegment>();
        packageJourneySegmentDomainList.add(packageJourneySegmentDomain);
        var shipmentJourneyDomain = new ShipmentJourney();
        shipmentJourneyDomain.setPackageJourneySegments(packageJourneySegmentDomainList);
        shipmentDomain.setShipmentJourney(shipmentJourneyDomain);

        var shipmentPackage = new Package();
        shipmentPackage.setId("SHPV2-PACKAGE-ID");

        var packageDimension = new PackageDimension();
        packageDimension.setMeasurementUnit(MeasurementUnit.METRIC);
        packageDimension.setLength(new BigDecimal("18.01"));
        packageDimension.setWidth(new BigDecimal("18.02"));
        packageDimension.setHeight(new BigDecimal("18.03"));
        packageDimension.setVolumeWeight(new BigDecimal("18.04"));
        packageDimension.setGrossWeight(new BigDecimal("18.05"));
        packageDimension.setChargeableWeight(new BigDecimal("18.06"));
        packageDimension.setCustom(true);
        shipmentPackage.setDimension(packageDimension);

        var packageCommodity = new Commodity();
        packageCommodity.setName("LINGERIE");
        packageCommodity.setName("Boxer Shorts");
        packageCommodity.setQuantity(4L);
        packageCommodity.setValue(new BigDecimal("21.99"));
        shipmentPackage.setCommodities(List.of(packageCommodity));

        var packagePricingInfo = new PricingInfo();
        packagePricingInfo.setCurrency("NZD");
        packagePricingInfo.setBaseTariff(new BigDecimal("9.81"));
        packagePricingInfo.setServiceTypeCharge(new BigDecimal("9.82"));
        packagePricingInfo.setSurcharge(new BigDecimal("9.83"));
        packagePricingInfo.setInsuranceCharge(new BigDecimal("9.84"));
        packagePricingInfo.setExtraCareCharge(new BigDecimal("9.85"));
        packagePricingInfo.setDiscount(new BigDecimal("9.86"));
        packagePricingInfo.setTax(new BigDecimal("9.87"));
        packagePricingInfo.setDiscount(new BigDecimal("9.88"));
        shipmentPackage.setPricingInfo(packagePricingInfo);

        shipmentDomain.setShipmentPackage(shipmentPackage);

        return shipmentDomain;
    }

    private MilestoneMessage createMilestoneFromShipment() {
        MilestoneMessage milestone = new MilestoneMessage();
        milestone.setPackageId(shipment.getShipmentPackage().getId());
        milestone.setOrganizationId(shipment.getOrganization().getId());
        milestone.setActive(true);
        return milestone;
    }

    private ShipShipmentPathMessage createShipmentPathFromShipment() {
        ShipShipmentPathMessage shipmentPathMessage = new ShipShipmentPathMessage();
        shipmentPathMessage.setId(shipment.getId());

        var organizationDomain = shipment.getOrganization();
        shipmentPathMessage.setOrganizationId(organizationDomain.getId());

        var orderDomain = shipment.getOrder();
        shipmentPathMessage.setOrderId(orderDomain.getId());

        var shipmentJourneyDomain = shipment.getShipmentJourney();
        if (shipmentJourneyDomain == null) {
            shipmentPathMessage.setShipmentPath(Collections.emptyList());
            return shipmentPathMessage;
        }

        var shipmentPathList = new ArrayList<ShipmentPathMessage>();
        for (var journeySegmentDomain : shipmentJourneyDomain.getPackageJourneySegments()) {
            var shipmentPath = new ShipmentPathMessage();
            shipmentPath.setId(journeySegmentDomain.getSegmentId());
            var startFacilityDomain = journeySegmentDomain.getStartFacility();
            if (startFacilityDomain != null) {
                shipmentPath.setHubId(startFacilityDomain.getId());
            }
            shipmentPath.setTransportType(MapperUtil.getValueFromEnum(journeySegmentDomain.getTransportType()));
            shipmentPathList.add(shipmentPath);
        }

        shipmentPathMessage.setShipmentPath(shipmentPathList);

        return shipmentPathMessage;
    }

    private PackageDimensionsMessage createPackageDimensionFromShipment() {
        PackageDimensionsMessage packageDimensions = new PackageDimensionsMessage();
        packageDimensions.setPackageId(shipment.getShipmentPackage().getId());
        packageDimensions.setOrgId(shipment.getOrganization().getId());
        packageDimensions.setGrossWeight(shipment.getShipmentPackage().getDimension().getGrossWeight());
        return packageDimensions;
    }

    private ShipmentCancelMessage createShipmentCancelFromShipment() {
        ShipmentCancelMessage shipmentCancel = new ShipmentCancelMessage();
        shipmentCancel.setOrganisationId(shipment.getOrganization().getId());
        shipmentCancel.setOrderId(shipment.getOrder().getId());
        shipmentCancel.setShipmentId(shipment.getId());

        return shipmentCancel;
    }
}
