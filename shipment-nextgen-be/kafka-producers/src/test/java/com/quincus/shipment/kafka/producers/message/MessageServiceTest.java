package com.quincus.shipment.kafka.producers.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.TriggeredFrom;
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
import com.quincus.shipment.kafka.connection.properties.KafkaDebugProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaOutboundTopicProperties;
import com.quincus.shipment.kafka.producers.MessageService;
import com.quincus.shipment.kafka.producers.helper.CreateMessageHelper;
import com.quincus.shipment.kafka.producers.mapper.MapperUtil;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentsDispatchMessage;
import com.quincus.shipment.kafka.producers.message.qship.PackageMsgPart;
import com.quincus.shipment.kafka.producers.message.qship.QshipSegmentMessage;
import com.quincus.shipment.kafka.producers.test_utils.TestUtil;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {
    private final TestUtil testUtil = TestUtil.getInstance();
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private KafkaOutboundTopicProperties kafkaOutboundTopicProperties;
    @Mock
    private KafkaDebugProperties debugProperties;
    @InjectMocks
    private MessageService messageService;
    @Spy
    private ObjectMapper mapper = testUtil.getObjectMapper();
    @Mock
    private CreateMessageHelper messageHelper;
    private Shipment shipment = null;

    @BeforeEach
    void init() {
        shipment = createShipment();
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        lenient().when(debugProperties.isWriteResultEnabled()).thenReturn(false);
    }

    @Test
    void sendMilestoneMessage_withValidMessage_shouldHaveNoErrors() {
        MilestoneMessage milestone = createMilestoneFromShipment();
        when(messageHelper.createMilestoneMessage(shipment)).thenReturn(milestone);

        assertThatNoException().isThrownBy(() -> messageService.sendMilestoneMessage(shipment, null, TriggeredFrom.SHP));
    }

    @Test
    void sendShipmentPath_withValidMessage_shouldHaveNoErrors() {
        ShipShipmentPathMessage shipmentPath = createShipmentPathFromShipment();
        when(messageHelper.createShipShipmentPathMessage(shipment)).thenReturn(shipmentPath);

        assertThatNoException().isThrownBy(() -> messageService.sendShipmentPath(shipment));
    }

    @Test
    void sendSegmentsDispatch_withValidMessage_shouldHaveNoErrors() {
        SegmentsDispatchMessage segmentsDispatch = new SegmentsDispatchMessage();
        when(messageHelper.createSegmentsDispatchMessage(anyList(), any(ShipmentJourney.class), any(SegmentDispatchType.class), any()))
                .thenReturn(segmentsDispatch);
        doReturn("").when(kafkaOutboundTopicProperties).getSegmentsDispatchTopic();
        mockKafkaTemplate();

        assertThatNoException().isThrownBy(() -> messageService.sendSegmentsDispatch(shipment,
                SegmentDispatchType.SHIPMENT_CREATED, DspSegmentMsgUpdateSource.CLIENT));

        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    void sendSegmentsDispatch_journeyAndListOfShipments_shouldHaveNoErrors() {
        SegmentsDispatchMessage segmentsDispatch = new SegmentsDispatchMessage();
        when(messageHelper.createSegmentsDispatchMessage(anyList(), any(ShipmentJourney.class), any(SegmentDispatchType.class), any()))
                .thenReturn(segmentsDispatch);
        doReturn("").when(kafkaOutboundTopicProperties).getSegmentsDispatchTopic();
        mockKafkaTemplate();

        assertThatNoException().isThrownBy(() -> messageService.sendSegmentsDispatch(List.of(shipment),
                shipment.getShipmentJourney(), SegmentDispatchType.SHIPMENT_CREATED, DspSegmentMsgUpdateSource.CLIENT));

        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    void sendQshipSegment_withValidMessage_shouldHaveNoErrors() {
        ShipmentJourney shipmentJourneyDomain = shipment.getShipmentJourney();
        if (shipmentJourneyDomain == null) {
            fail("Test Setup Error. Input Shipment must contain a Shipment Journey.");
        }

        List<PackageJourneySegment> segments = shipmentJourneyDomain.getPackageJourneySegments();
        if (CollectionUtils.isEmpty(segments)) {
            fail("Test Setup Error. Shipment Journey must contain 1 or more Package Journey Segments.");
        }

        QshipSegmentMessage qshipSegment = createQShipSegmentFromShipment();
        when(messageHelper.createQshipSegmentMessageList(shipment)).thenReturn(List.of(qshipSegment));
        doReturn("").when(kafkaOutboundTopicProperties).getQShipSegmentTopic();
        mockKafkaTemplate();

        try {
            messageService.sendQshipSegment(shipment);
        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }

        verify(kafkaTemplate, times(segments.size())).send(any(ProducerRecord.class));
    }

    @Test
    void sendPackageDimensions_withValidMessage_shouldHaveNoErrors() {
        PackageDimensionsMessage packageDimensions = createPackageDimensionsFromShipment();
        when(messageHelper.createShipmentPackageDimensions(shipment)).thenReturn(packageDimensions);

        assertThatNoException().isThrownBy(() -> messageService.sendPackageDimensions(shipment));
    }

    @Test
    void sendShipmentCancelMessage_withValidMessage_shouldHaveNoErrors() {
        ShipmentCancelMessage shipmentCancel = createShipmentCancelFromShipment();
        when(messageHelper.createShipmentCancelMessage(shipment)).thenReturn(shipmentCancel);

        assertThatNoException().isThrownBy(() -> messageService.sendShipmentCancelMessage(shipment));
    }

    @Test
    void subscribeFlight_withValidRequest_shouldHaveNoErrors() {
        FlightStatsRequest request = createFlightEventPayloadRequest();

        assertThatNoException().isThrownBy(() -> messageService.subscribeFlight(request));
    }

    @Test
    void getFlightSubscriptionMessage_withValidRequest_shouldHaveNoErrors() {
        FlightStatsRequest request = createFlightEventPayloadRequest();
        assertThatNoException().isThrownBy(() -> messageService.getFlightSubscriptionMessage(request, randomUUID().toString()));
    }

    @Test
    void sendUpdatedSegmentFromShipment_withFoundUpdatedSegmentInShipment_ShouldSendKafkaMessage() {
        //GIVEN:
        String segmentId = UUID.randomUUID().toString();
        Shipment mockShipment = mock(Shipment.class);

        List<QshipSegmentMessage> qshipSegmentMessageList = new ArrayList<>();
        QshipSegmentMessage message1 = new QshipSegmentMessage();
        message1.setId(UUID.randomUUID().toString());
        QshipSegmentMessage message2 = new QshipSegmentMessage();
        message2.setId(UUID.randomUUID().toString());
        QshipSegmentMessage message3 = new QshipSegmentMessage();
        message3.setId(segmentId);
        qshipSegmentMessageList.add(message1);
        qshipSegmentMessageList.add(message2);
        qshipSegmentMessageList.add(message3);

        when(messageHelper.createQshipSegmentMessageList(mockShipment)).thenReturn(qshipSegmentMessageList);
        //WHEN:
        messageService.sendUpdatedSegmentFromShipment(mockShipment, segmentId);
        //THEN:
        verify(kafkaTemplate, times(1)).send(any(), any());
    }

    @Test
    void sendUpdatedSegmentFromShipment_withNotFoundSegmentInShipment_ShouldNotSendKafkaMessage() {
        //GIVEN:
        Shipment mockShipment = mock(Shipment.class);

        List<QshipSegmentMessage> qshipSegmentMessageList = new ArrayList<>();
        QshipSegmentMessage message1 = new QshipSegmentMessage();
        message1.setId(UUID.randomUUID().toString());
        QshipSegmentMessage message2 = new QshipSegmentMessage();
        message2.setId(UUID.randomUUID().toString());
        QshipSegmentMessage message3 = new QshipSegmentMessage();
        message3.setId(UUID.randomUUID().toString());
        qshipSegmentMessageList.add(message1);
        qshipSegmentMessageList.add(message2);
        qshipSegmentMessageList.add(message3);

        when(messageHelper.createQshipSegmentMessageList(mockShipment)).thenReturn(qshipSegmentMessageList);
        //WHEN:
        messageService.sendUpdatedSegmentFromShipment(mockShipment, "randomSegmentId");
        //THEN:
        verifyNoInteractions(kafkaTemplate);
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

    private Shipment createShipment() {
        String orgId = "ORG1";
        Organization organizationDomain = new Organization();
        organizationDomain.setId(orgId);

        String orderId = "ORDER1";
        Order orderDomain = new Order();
        orderDomain.setId(orderId);


        String shipmentId = "SHP-1";
        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setId(shipmentId);
        shipmentDomain.setOrganization(organizationDomain);
        shipmentDomain.setOrder(orderDomain);
        shipmentDomain.setOrderId(orderId);

        String segmentId = "SEGMENT-1";
        PackageJourneySegment packageJourneySegmentDomain = new PackageJourneySegment();
        packageJourneySegmentDomain.setSegmentId(segmentId);
        packageJourneySegmentDomain.setTransportType(TransportType.GROUND);

        String startFacilityId = "FAC-1";
        Facility startFacilityDomain = new Facility();
        startFacilityDomain.setId(startFacilityId);
        packageJourneySegmentDomain.setStartFacility(startFacilityDomain);

        List<PackageJourneySegment> packageJourneySegmentDomainList = new ArrayList<>();
        packageJourneySegmentDomainList.add(packageJourneySegmentDomain);
        ShipmentJourney shipmentJourneyDomain = new ShipmentJourney();
        shipmentJourneyDomain.setPackageJourneySegments(packageJourneySegmentDomainList);
        shipmentDomain.setShipmentJourney(shipmentJourneyDomain);

        Package shipmentPackage = new Package();
        shipmentPackage.setId("SHPV2-PACKAGE-ID");

        PackageDimension packageDimension = new PackageDimension();
        packageDimension.setMeasurementUnit(MeasurementUnit.METRIC);
        packageDimension.setLength(new BigDecimal("18.01"));
        packageDimension.setWidth(new BigDecimal("18.02"));
        packageDimension.setHeight(new BigDecimal("18.03"));
        packageDimension.setVolumeWeight(new BigDecimal("18.04"));
        packageDimension.setGrossWeight(new BigDecimal("18.05"));
        packageDimension.setChargeableWeight(new BigDecimal("18.06"));
        packageDimension.setCustom(true);
        shipmentPackage.setDimension(packageDimension);

        Commodity packageCommodity = new Commodity();
        packageCommodity.setName("LINGERIE");
        packageCommodity.setName("Boxer Shorts");
        packageCommodity.setQuantity(4L);
        packageCommodity.setValue(new BigDecimal("21.99"));
        shipmentPackage.setCommodities(List.of(packageCommodity));

        PricingInfo packagePricingInfo = new PricingInfo();
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

        Organization organizationDomain = shipment.getOrganization();
        shipmentPathMessage.setOrganizationId(organizationDomain.getId());

        Order orderDomain = shipment.getOrder();
        shipmentPathMessage.setOrderId(orderDomain.getId());

        ShipmentJourney shipmentJourneyDomain = shipment.getShipmentJourney();
        if (shipmentJourneyDomain == null) {
            shipmentPathMessage.setShipmentPath(Collections.emptyList());
            return shipmentPathMessage;
        }

        List<ShipmentPathMessage> shipmentPathList = new ArrayList<>();
        for (PackageJourneySegment journeySegmentDomain : shipmentJourneyDomain.getPackageJourneySegments()) {
            ShipmentPathMessage shipmentPath = new ShipmentPathMessage();
            shipmentPath.setId(journeySegmentDomain.getSegmentId());
            Facility startFacilityDomain = journeySegmentDomain.getStartFacility();
            if (startFacilityDomain != null) {
                shipmentPath.setHubId(startFacilityDomain.getId());
            }
            shipmentPath.setTransportType(MapperUtil.getValueFromEnum(journeySegmentDomain.getTransportType()));
            shipmentPathList.add(shipmentPath);
        }

        shipmentPathMessage.setShipmentPath(shipmentPathList);

        return shipmentPathMessage;
    }

    private QshipSegmentMessage createQShipSegmentFromShipment() {
        QshipSegmentMessage qshipSegmentMessage = new QshipSegmentMessage();

        Organization organizationDomain = shipment.getOrganization();
        qshipSegmentMessage.setOrganisationId(organizationDomain.getId());

        Order orderDomain = shipment.getOrder();
        qshipSegmentMessage.setOrderId(orderDomain.getId());

        ShipmentJourney shipmentJourneyDomain = shipment.getShipmentJourney();
        if (shipmentJourneyDomain != null) {
            List<PackageJourneySegment> packageJourneySegments = shipmentJourneyDomain.getPackageJourneySegments();
            if (!CollectionUtils.isEmpty(packageJourneySegments)) {
                qshipSegmentMessage.setStatus(MapperUtil.getValueFromEnum(packageJourneySegments.get(0).getStatus()));
            }
        }

        Package packageDomain = shipment.getShipmentPackage();
        PackageMsgPart qshipSegmentPackage = new PackageMsgPart();
        if (packageDomain == null) {
            qshipSegmentMessage.setPackages(Collections.emptyList());
        } else {
            qshipSegmentPackage.setId(packageDomain.getId());
            qshipSegmentMessage.setPackages(List.of(qshipSegmentPackage));
        }

        return qshipSegmentMessage;
    }

    private PackageDimensionsMessage createPackageDimensionsFromShipment() {
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

    private void mockKafkaTemplate() {
        long timestamp = Instant.now(Clock.systemUTC()).getEpochSecond();
        TopicPartition topicPartition = new TopicPartition("", 1);
        RecordMetadata dummyRecord = new RecordMetadata(topicPartition, 0, 0, timestamp, 1, 1);
        SendResult<String, String> kafkaDummyResult = new SendResult<>(null, dummyRecord);

        SettableListenableFuture<SendResult<String, String>> dummyFuture = new SettableListenableFuture();
        dummyFuture.set(kafkaDummyResult);

        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(dummyFuture);
    }

}
