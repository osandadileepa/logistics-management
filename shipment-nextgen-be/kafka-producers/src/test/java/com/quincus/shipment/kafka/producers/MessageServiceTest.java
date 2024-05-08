package com.quincus.shipment.kafka.producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Consignee;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.MilestoneError;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.PricingInfo;
import com.quincus.shipment.api.domain.Sender;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.kafka.connection.properties.KafkaDebugProperties;
import com.quincus.shipment.kafka.connection.properties.KafkaOutboundTopicProperties;
import com.quincus.shipment.kafka.producers.helper.CreateMessageHelper;
import com.quincus.shipment.kafka.producers.mapper.FlightStatsMessageMapperImpl;
import com.quincus.shipment.kafka.producers.mapper.SegmentsDispatchMapperImpl;
import com.quincus.shipment.kafka.producers.mapper.ShipmentToOrderMapperImpl;
import com.quincus.shipment.kafka.producers.mapper.ShipmentToPackageDimensionsMapperImpl;
import com.quincus.shipment.kafka.producers.mapper.ShipmentToQshipMapperImpl;
import com.quincus.shipment.kafka.producers.message.MilestoneMessage;
import com.quincus.shipment.kafka.producers.test_utils.TestUtil;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper = TestUtil.getInstance().getObjectMapper();

    @Mock
    private KafkaOutboundTopicProperties kafkaOutboundTopicProperties;

    @Mock
    private KafkaDebugProperties debugProperties;

    private MessageService messageService;

    @BeforeEach
    void initialize() {
        CreateMessageHelper messageHelper = new CreateMessageHelper(new ShipmentToOrderMapperImpl(),
                new ShipmentToQshipMapperImpl(), new ShipmentToPackageDimensionsMapperImpl(),
                new SegmentsDispatchMapperImpl(mapper), new FlightStatsMessageMapperImpl());
        lenient().when(debugProperties.isWriteResultEnabled()).thenReturn(false);
        messageService = new MessageService(kafkaTemplate, mapper, messageHelper, kafkaOutboundTopicProperties, debugProperties);
    }

    @Test
    void sendShipmentToQShip_validShipment_shouldSendToKafka() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");
        shipment.setShipmentTrackingId("QC001-001");

        Order order = new Order();
        order.setId("order1");
        shipment.setOrder(order);

        Organization organization = new Organization();
        organization.setId("organization1");
        shipment.setOrganization(organization);

        Package shipmentPackage = new Package();
        shipmentPackage.setId("package1");
        shipment.setShipmentPackage(shipmentPackage);

        String journeyId = "journey1";
        String segmentId = "segment1";
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId(journeyId);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setJourneyId(journeyId);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setType(SegmentType.LAST_MILE);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        when(kafkaOutboundTopicProperties.getQShipSegmentTopic()).thenReturn("qship-segment");

        messageService.sendShipmentToQShip(shipment);

        verify(kafkaOutboundTopicProperties, times(1)).getQShipSegmentTopic();
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    void sendSegmentDispatch_validParams_shouldSendToKafka() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");
        shipment.setShipmentTrackingId("QC001-001");

        Order order = new Order();
        order.setId("order1");
        order.setData("");
        shipment.setOrder(order);

        Organization organization = new Organization();
        organization.setId("organization1");
        shipment.setOrganization(organization);

        Package shipmentPackage = new Package();
        shipmentPackage.setId("package1");
        shipmentPackage.setTotalItemsCount(1L);

        PackageDimension dimension = new PackageDimension();
        shipmentPackage.setDimension(dimension);

        PricingInfo pricingInfo = new PricingInfo();
        shipmentPackage.setPricingInfo(pricingInfo);

        shipmentPackage.setCommodities(Collections.emptyList());
        shipment.setShipmentPackage(shipmentPackage);

        Sender sender = new Sender("John", "john@email.com", "123", "+1");
        shipment.setSender(sender);
        Consignee consignee = new Consignee("c1", "Mark", "mark@email.com", "456", "+1");
        shipment.setConsignee(consignee);

        Address origin = new Address();
        shipment.setOrigin(origin);

        Address destination = new Address();
        shipment.setDestination(destination);

        ServiceType serviceType = new ServiceType();
        shipment.setServiceType(serviceType);

        String journeyId = "journey1";
        String segmentId = "segment1";
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId(journeyId);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setJourneyId(journeyId);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setType(SegmentType.LAST_MILE);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        when(kafkaOutboundTopicProperties.getSegmentsDispatchTopic()).thenReturn("segment-dispatch");

        messageService.sendSegmentDispatch(List.of(shipment), shipment.getShipmentJourney(), SegmentDispatchType.SHIPMENT_CREATED, DspSegmentMsgUpdateSource.CLIENT);

        verify(kafkaOutboundTopicProperties, times(1)).getSegmentsDispatchTopic();
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    void sendMilestoneMessage_validParams_shouldSendToKafka() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");
        shipment.setShipmentTrackingId("QC001-001");

        Order order = new Order();
        order.setId("order1");
        order.setData("");
        shipment.setOrder(order);

        Organization organization = new Organization();
        organization.setId("organization1");
        shipment.setOrganization(organization);

        Package shipmentPackage = new Package();
        shipmentPackage.setId("package1");
        shipmentPackage.setTotalItemsCount(1L);

        PackageDimension dimension = new PackageDimension();
        shipmentPackage.setDimension(dimension);

        PricingInfo pricingInfo = new PricingInfo();
        shipmentPackage.setPricingInfo(pricingInfo);

        shipmentPackage.setCommodities(Collections.emptyList());
        shipment.setShipmentPackage(shipmentPackage);

        Sender sender = new Sender("John", "john@email.com", "123", "+1");
        shipment.setSender(sender);
        Consignee consignee = new Consignee("c1", "Mark", "mark@email.com", "456", "+1");
        shipment.setConsignee(consignee);

        Address origin = new Address();
        shipment.setOrigin(origin);

        Address destination = new Address();
        shipment.setDestination(destination);

        ServiceType serviceType = new ServiceType();
        shipment.setServiceType(serviceType);

        String journeyId = "journey1";
        String segmentId = "segment1";
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId(journeyId);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setJourneyId(journeyId);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setType(SegmentType.LAST_MILE);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        MilestoneMessage milestoneMessage = new MilestoneMessage();
        milestoneMessage.setMilestoneId("milestone1");

        when(kafkaOutboundTopicProperties.getShipmentMilestoneTopic()).thenReturn("shipment-milestone");

        messageService.sendMilestoneMessage(shipment, milestoneMessage, TriggeredFrom.SHP);

        verify(kafkaOutboundTopicProperties, times(1)).getShipmentMilestoneTopic();
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }

    @Test
    void sendMilestoneMessage_noMilestoneMessage_shouldSendToKafka() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");
        shipment.setShipmentTrackingId("QC001-001");

        Order order = new Order();
        order.setId("order1");
        order.setData("");
        shipment.setOrder(order);

        Organization organization = new Organization();
        organization.setId("organization1");
        shipment.setOrganization(organization);

        Package shipmentPackage = new Package();
        shipmentPackage.setId("package1");
        shipmentPackage.setTotalItemsCount(1L);

        PackageDimension dimension = new PackageDimension();
        shipmentPackage.setDimension(dimension);

        PricingInfo pricingInfo = new PricingInfo();
        shipmentPackage.setPricingInfo(pricingInfo);

        shipmentPackage.setCommodities(Collections.emptyList());
        shipment.setShipmentPackage(shipmentPackage);

        Sender sender = new Sender("John", "john@email.com", "123", "+1");
        shipment.setSender(sender);
        Consignee consignee = new Consignee("c1", "Mark", "mark@email.com", "456", "+1");
        shipment.setConsignee(consignee);

        Address origin = new Address();
        shipment.setOrigin(origin);

        Address destination = new Address();
        shipment.setDestination(destination);

        ServiceType serviceType = new ServiceType();
        shipment.setServiceType(serviceType);

        String journeyId = "journey1";
        String segmentId = "segment1";
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId(journeyId);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setJourneyId(journeyId);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setType(SegmentType.LAST_MILE);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        when(kafkaOutboundTopicProperties.getShipmentMilestoneTopic()).thenReturn("shipment-milestone");

        messageService.sendMilestoneMessage(shipment, null, TriggeredFrom.SHP);

        verify(kafkaOutboundTopicProperties, times(1)).getShipmentMilestoneTopic();
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }

    @Test
    void sendMilestoneError_validParams_shouldSendToKafka() {
        MilestoneError milestoneError = new MilestoneError();

        when(kafkaOutboundTopicProperties.getDispatchMilestoneErrorTopic()).thenReturn("milestone-error");

        messageService.sendMilestoneError(milestoneError);

        verify(kafkaOutboundTopicProperties, times(1)).getDispatchMilestoneErrorTopic();
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }

    @Test
    void sendShipmentPath_validParams_shouldSendToKafka() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");
        shipment.setShipmentTrackingId("QC001-001");

        Order order = new Order();
        order.setId("order1");
        order.setData("");
        shipment.setOrder(order);

        Organization organization = new Organization();
        organization.setId("organization1");
        shipment.setOrganization(organization);

        Package shipmentPackage = new Package();
        shipmentPackage.setId("package1");
        shipmentPackage.setTotalItemsCount(1L);

        PackageDimension dimension = new PackageDimension();
        shipmentPackage.setDimension(dimension);

        PricingInfo pricingInfo = new PricingInfo();
        shipmentPackage.setPricingInfo(pricingInfo);

        shipmentPackage.setCommodities(Collections.emptyList());
        shipment.setShipmentPackage(shipmentPackage);

        Sender sender = new Sender("John", "john@email.com", "123", "+1");
        shipment.setSender(sender);
        Consignee consignee = new Consignee("c1", "Mark", "mark@email.com", "456", "+1");
        shipment.setConsignee(consignee);

        Address origin = new Address();
        shipment.setOrigin(origin);

        Address destination = new Address();
        shipment.setDestination(destination);

        ServiceType serviceType = new ServiceType();
        shipment.setServiceType(serviceType);

        String journeyId = "journey1";
        String segmentId = "segment1";
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId(journeyId);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setJourneyId(journeyId);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setType(SegmentType.LAST_MILE);
        Facility startFacility = new Facility();
        segment.setStartFacility(startFacility);
        Facility endFacility = new Facility();
        segment.setEndFacility(endFacility);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        when(kafkaOutboundTopicProperties.getShipmentPathTopic()).thenReturn("shipment-path");

        messageService.sendShipmentPath(shipment);

        verify(kafkaOutboundTopicProperties, times(1)).getShipmentPathTopic();
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }

    @Test
    void sendSegmentsDispatch_validParams_shouldSendToKafka() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");
        shipment.setShipmentTrackingId("QC001-001");

        Order order = new Order();
        order.setId("order1");
        order.setData("");
        shipment.setOrder(order);

        Organization organization = new Organization();
        organization.setId("organization1");
        shipment.setOrganization(organization);

        Package shipmentPackage = new Package();
        shipmentPackage.setId("package1");
        shipmentPackage.setTotalItemsCount(1L);

        PackageDimension dimension = new PackageDimension();
        shipmentPackage.setDimension(dimension);

        PricingInfo pricingInfo = new PricingInfo();
        shipmentPackage.setPricingInfo(pricingInfo);

        shipmentPackage.setCommodities(Collections.emptyList());
        shipment.setShipmentPackage(shipmentPackage);

        Sender sender = new Sender("John", "john@email.com", "123", "+1");
        shipment.setSender(sender);
        Consignee consignee = new Consignee("c1", "Mark", "mark@email.com", "456", "+1");
        shipment.setConsignee(consignee);

        Address origin = new Address();
        shipment.setOrigin(origin);

        Address destination = new Address();
        shipment.setDestination(destination);

        ServiceType serviceType = new ServiceType();
        shipment.setServiceType(serviceType);

        String journeyId = "journey1";
        String segmentId = "segment1";
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId(journeyId);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setJourneyId(journeyId);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setType(SegmentType.LAST_MILE);
        Facility startFacility = new Facility();
        segment.setStartFacility(startFacility);
        Facility endFacility = new Facility();
        segment.setEndFacility(endFacility);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        when(kafkaOutboundTopicProperties.getSegmentsDispatchTopic()).thenReturn("segment-dispatch");
        mockKafkaTemplate();

        messageService.sendSegmentsDispatch(shipment, SegmentDispatchType.SHIPMENT_CREATED, DspSegmentMsgUpdateSource.CLIENT);

        verify(kafkaOutboundTopicProperties, times(1)).getSegmentsDispatchTopic();
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    void sendSegmentsDispatch_withSingleSegment_shouldSendToKafka() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");
        shipment.setShipmentTrackingId("QC001-001");

        Order order = new Order();
        order.setId("order1");
        order.setData("");
        shipment.setOrder(order);

        Organization organization = new Organization();
        organization.setId("organization1");
        shipment.setOrganization(organization);

        Package shipmentPackage = new Package();
        shipmentPackage.setId("package1");
        shipmentPackage.setTotalItemsCount(1L);

        PackageDimension dimension = new PackageDimension();
        shipmentPackage.setDimension(dimension);

        PricingInfo pricingInfo = new PricingInfo();
        shipmentPackage.setPricingInfo(pricingInfo);

        shipmentPackage.setCommodities(Collections.emptyList());
        shipment.setShipmentPackage(shipmentPackage);

        Sender sender = new Sender("John", "john@email.com", "123", "+1");
        shipment.setSender(sender);
        Consignee consignee = new Consignee("c1", "Mark", "mark@email.com", "456", "+1");
        shipment.setConsignee(consignee);

        Address origin = new Address();
        shipment.setOrigin(origin);

        Address destination = new Address();
        shipment.setDestination(destination);

        ServiceType serviceType = new ServiceType();
        shipment.setServiceType(serviceType);

        String journeyId = "journey1";
        String segmentId = "segment1";
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId(journeyId);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setJourneyId(journeyId);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setType(SegmentType.LAST_MILE);
        Facility startFacility = new Facility();
        segment.setStartFacility(startFacility);
        Facility endFacility = new Facility();
        segment.setEndFacility(endFacility);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        when(kafkaOutboundTopicProperties.getSegmentsDispatchTopic()).thenReturn("segment-dispatch");
        mockKafkaTemplate();

        messageService.sendSegmentsDispatch(shipment, shipment.getShipmentJourney().getPackageJourneySegments().get(0),
                SegmentDispatchType.SHIPMENT_CREATED, DspSegmentMsgUpdateSource.CLIENT);

        verify(kafkaOutboundTopicProperties, times(1)).getSegmentsDispatchTopic();
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    void sendSegmentsDispatch_withJourneyParam_shouldSendToKafka() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");
        shipment.setShipmentTrackingId("QC001-001");

        Order order = new Order();
        order.setId("order1");
        order.setData("");
        shipment.setOrder(order);

        Organization organization = new Organization();
        organization.setId("organization1");
        shipment.setOrganization(organization);

        Package shipmentPackage = new Package();
        shipmentPackage.setId("package1");
        shipmentPackage.setTotalItemsCount(1L);

        PackageDimension dimension = new PackageDimension();
        shipmentPackage.setDimension(dimension);

        PricingInfo pricingInfo = new PricingInfo();
        shipmentPackage.setPricingInfo(pricingInfo);

        shipmentPackage.setCommodities(Collections.emptyList());
        shipment.setShipmentPackage(shipmentPackage);

        Sender sender = new Sender("John", "john@email.com", "123", "+1");
        shipment.setSender(sender);
        Consignee consignee = new Consignee("c1", "Mark", "mark@email.com", "456", "+1");
        shipment.setConsignee(consignee);

        Address origin = new Address();
        shipment.setOrigin(origin);

        Address destination = new Address();
        shipment.setDestination(destination);

        ServiceType serviceType = new ServiceType();
        shipment.setServiceType(serviceType);

        String journeyId = "journey1";
        String segmentId = "segment1";
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId(journeyId);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setJourneyId(journeyId);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setType(SegmentType.LAST_MILE);
        Facility startFacility = new Facility();
        segment.setStartFacility(startFacility);
        Facility endFacility = new Facility();
        segment.setEndFacility(endFacility);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        when(kafkaOutboundTopicProperties.getSegmentsDispatchTopic()).thenReturn("segment-dispatch");
        mockKafkaTemplate();

        messageService.sendSegmentsDispatch(List.of(shipment), shipment.getShipmentJourney(),
                SegmentDispatchType.SHIPMENT_CREATED, DspSegmentMsgUpdateSource.CLIENT);

        verify(kafkaOutboundTopicProperties, times(1)).getSegmentsDispatchTopic();
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    void sendSegmentsDispatch_journeyParam_shouldSendToKafka() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");
        shipment.setShipmentTrackingId("QC001-001");

        Order order = new Order();
        order.setId("order1");
        order.setData("");
        shipment.setOrder(order);

        Organization organization = new Organization();
        organization.setId("organization1");
        shipment.setOrganization(organization);

        Package shipmentPackage = new Package();
        shipmentPackage.setId("package1");
        shipmentPackage.setTotalItemsCount(1L);

        PackageDimension dimension = new PackageDimension();
        shipmentPackage.setDimension(dimension);

        PricingInfo pricingInfo = new PricingInfo();
        shipmentPackage.setPricingInfo(pricingInfo);

        shipmentPackage.setCommodities(Collections.emptyList());
        shipment.setShipmentPackage(shipmentPackage);

        Sender sender = new Sender("John", "john@email.com", "123", "+1");
        shipment.setSender(sender);
        Consignee consignee = new Consignee("c1", "Mark", "mark@email.com", "456", "+1");
        shipment.setConsignee(consignee);

        Address origin = new Address();
        shipment.setOrigin(origin);

        Address destination = new Address();
        shipment.setDestination(destination);

        ServiceType serviceType = new ServiceType();
        shipment.setServiceType(serviceType);

        String journeyId = "journey1";
        String segmentId = "segment1";
        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId(journeyId);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        segment.setJourneyId(journeyId);
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setType(SegmentType.LAST_MILE);
        journey.addPackageJourneySegment(segment);
        shipment.setShipmentJourney(journey);

        when(kafkaOutboundTopicProperties.getSegmentsDispatchTopic()).thenReturn("segment-dispatch");
        mockKafkaTemplate();

        messageService.sendSegmentsDispatch(List.of(shipment), shipment.getShipmentJourney(), SegmentDispatchType.SHIPMENT_CREATED, DspSegmentMsgUpdateSource.CLIENT);

        verify(kafkaOutboundTopicProperties, times(1)).getSegmentsDispatchTopic();
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    void sendUpdatedSegmentFromShipment_segmentArgument_shouldSendToKafka() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");
        shipment.setShipmentTrackingId("QC001-001");
        shipment.setOrder(new Order());
        shipment.setOrganization(new Organization());
        shipment.setShipmentPackage(new Package());

        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId("segment1");
        segment.setJourneyId("journey1");
        segment.setStatus(SegmentStatus.PLANNED);
        segment.setTransportType(TransportType.GROUND);
        segment.setType(SegmentType.LAST_MILE);

        when(kafkaOutboundTopicProperties.getQShipSegmentTopic()).thenReturn("qship-segment");

        messageService.sendUpdatedSegmentFromShipment(shipment, segment);

        verify(kafkaOutboundTopicProperties, times(1)).getQShipSegmentTopic();
        verify(kafkaTemplate, times(1)).send(eq("qship-segment"), anyString());
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
