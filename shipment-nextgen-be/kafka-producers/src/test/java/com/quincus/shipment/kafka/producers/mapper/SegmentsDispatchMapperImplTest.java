package com.quincus.shipment.kafka.producers.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Commodity;
import com.quincus.shipment.api.domain.Consignee;
import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.PricingInfo;
import com.quincus.shipment.api.domain.Sender;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.kafka.producers.message.dispatch.AddressDetailsMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.CommodityMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.ConsigneeMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.CurrencyMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.OrderMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.PackageMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.PricingInfoMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentCancelMessage;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentsDispatchMessage;
import com.quincus.shipment.kafka.producers.message.dispatch.ShipmentMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.ShipperMsgPart;
import com.quincus.shipment.kafka.producers.test_utils.TestUtil;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static com.quincus.shipment.api.constant.InstructionApplyToType.DELIVERY;
import static com.quincus.shipment.api.constant.InstructionApplyToType.JOURNEY;
import static com.quincus.shipment.api.constant.InstructionApplyToType.PICKUP;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SegmentsDispatchMapperImplTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    private final static String PICK_INSTRUCTION = "testPickInstruction";
    private final static String DELIVERY_INSTRUCTION = "testPickInstruction";
    private final static DspSegmentMsgUpdateSource SEGMENT_UPDATE_SHIPMENT_SOURCE = DspSegmentMsgUpdateSource.CLIENT;
    @InjectMocks
    private SegmentsDispatchMapperImpl mapper;
    @Spy
    private ObjectMapper objectMapper = testUtil.getObjectMapper();

    @Test
    void mapJourneyAndShipmentsToSegmentsDispatchMessage_validArguments_shouldReturnSegmentsDispatchMessage() {
        List<Shipment> shipmentList = testUtil.createMultipleShipmentsFromOrderMultiSegmentsJson();
        ShipmentJourney journey = shipmentList.get(0).getShipmentJourney();

        SegmentsDispatchMessage segmentsDispatchMessage = mapper.mapJourneyAndShipmentsToSegmentsDispatchMessage(shipmentList, journey);

        assertThat(segmentsDispatchMessage).isNotNull();
        assertThat(segmentsDispatchMessage.getShpVersion()).isEqualTo(SegmentsDispatchMessage.MSG_SHP_VERSION);

        Shipment refShipment = shipmentList.get(0);
        assertThat(segmentsDispatchMessage.getOrganisationId()).isEqualTo(refShipment.getOrganization().getId());
        assertThat(segmentsDispatchMessage.getUserId()).isEqualTo(refShipment.getUserId());

        OrderMsgPart orderMsgPart = segmentsDispatchMessage.getOrder();
        assertThat(orderMsgPart).isNotNull();
        assertThat(orderMsgPart.getShipper()).isNotNull();
        assertThat(orderMsgPart.getOrigin()).isNotNull();
        assertThat(orderMsgPart.getConsignee()).isNotNull();
        assertThat(orderMsgPart.getDestination()).isNotNull();
        assertThat(orderMsgPart.getPricingInfo()).isNotNull();
        assertThat(orderMsgPart.getServiceTypeId()).isEqualTo(refShipment.getServiceType().getId());
        assertThat(orderMsgPart.getServiceTypeName()).isEqualTo(refShipment.getServiceType().getCode());
        assertThat(orderMsgPart.getNumberOfShipments()).isEqualTo(shipmentList.size());

        assertThat(segmentsDispatchMessage.getJourneyId()).isEqualTo(journey.getJourneyId());

        List<ShipmentMsgPart> shipmentMsgParts = segmentsDispatchMessage.getShipments();
        assertThat(shipmentMsgParts).hasSize(shipmentList.size());

        List<SegmentMsgPart> segmentMsgParts = segmentsDispatchMessage.getSegments();
        assertThat(segmentMsgParts).hasSize(journey.getPackageJourneySegments().size());

        assertThat(segmentsDispatchMessage.getInternalOrderId()).isEqualTo(refShipment.getInternalOrderId());
        assertThat(segmentsDispatchMessage.getExternalOrderId()).isEqualTo(refShipment.getExternalOrderId());
        assertThat(segmentsDispatchMessage.getCustomerOrderId()).isEqualTo(refShipment.getCustomerOrderId());
    }

    @Test
    void mapJourneyAndShipmentsToSegmentsDispatchMessage_deletedSegment_shouldReturnSegmentsDispatchMessage() {
        List<Shipment> shipmentList = testUtil.createMultipleShipmentsFromOrderMultiSegmentsJson();
        ShipmentJourney journey = shipmentList.get(0).getShipmentJourney();
        journey.getPackageJourneySegments().get(0).setDeleted(true);

        SegmentsDispatchMessage segmentsDispatchMessage = mapper.mapJourneyAndShipmentsToSegmentsDispatchMessage(shipmentList, journey);

        assertThat(segmentsDispatchMessage).isNotNull();
        assertThat(segmentsDispatchMessage.getShpVersion()).isEqualTo(SegmentsDispatchMessage.MSG_SHP_VERSION);

        Shipment refShipment = shipmentList.get(0);
        assertThat(segmentsDispatchMessage.getOrganisationId()).isEqualTo(refShipment.getOrganization().getId());
        assertThat(segmentsDispatchMessage.getUserId()).isEqualTo(refShipment.getUserId());

        assertThat(segmentsDispatchMessage.getJourneyId()).isEqualTo(journey.getJourneyId());

        List<SegmentMsgPart> segmentMsgParts = segmentsDispatchMessage.getSegments();
        assertThat(segmentMsgParts).hasSize(journey.getPackageJourneySegments().size() - 1);
    }

    @Test
    void mapJourneyAndShipmentsToSegmentsDispatchMessage_withAdditionalFields() {
        List<Shipment> shipmentList = testUtil.createMultipleShipmentsFromOrderMultiSegmentsJson();
        ShipmentJourney journey = shipmentList.get(0).getShipmentJourney();
        List<Instruction> instructions = new ArrayList<>();
        instructions.add(createPickUpInstruction());
        instructions.add(createDeliveryInstruction());
        List<String> tags = Arrays.asList("tag1", "tag2");
        shipmentList.forEach(shipment -> shipment.setInstructions(instructions));
        shipmentList.forEach(shipment -> shipment.setShipmentTags(tags));

        SegmentsDispatchMessage segmentsDispatchMessage = mapper.mapJourneyAndShipmentsToSegmentsDispatchMessage(shipmentList, journey);

        assertThat(segmentsDispatchMessage).isNotNull();
        assertThat(segmentsDispatchMessage.getShpVersion()).isEqualTo(SegmentsDispatchMessage.MSG_SHP_VERSION);

        Shipment refShipment = shipmentList.get(0);
        assertThat(segmentsDispatchMessage.getOrganisationId()).isEqualTo(refShipment.getOrganization().getId());
        assertThat(segmentsDispatchMessage.getUserId()).isEqualTo(refShipment.getUserId());

        OrderMsgPart orderMsgPart = segmentsDispatchMessage.getOrder();
        assertThat(orderMsgPart).isNotNull();
        assertThat(orderMsgPart.getCode()).isEqualTo(refShipment.getOrder().getOrderIdLabel());
        assertThat(orderMsgPart.getPickupStartTime()).isEqualTo(DateTimeUtil.parseOffsetDateTime(refShipment.getOrder().getPickupStartTime()));
        assertThat(orderMsgPart.getPickupCommitTime()).isEqualTo(DateTimeUtil.parseOffsetDateTime(refShipment.getOrder().getPickupCommitTime()));
        assertThat(orderMsgPart.getDeliveryStartTime()).isEqualTo(DateTimeUtil.parseOffsetDateTime(refShipment.getOrder().getDeliveryStartTime()));
        assertThat(orderMsgPart.getDeliveryCommitTime()).isEqualTo(DateTimeUtil.parseOffsetDateTime(refShipment.getOrder().getDeliveryCommitTime()));

    }

    @Test
    void mapJourneyAndShipmentsToSegmentsDispatchMessage_nullJourney_shouldReturnNull() {
        List<Shipment> shipmentList = testUtil.createMultipleShipmentsFromOrderMultiSegmentsJson();

        assertThat(mapper.mapJourneyAndShipmentsToSegmentsDispatchMessage(shipmentList, null)).isNull();
    }

    @Test
    void mapShipmentDomainToSegmentCancelMessage_validArguments_shouldReturnSegmentCancelMessage() {
        Shipment shipment = testUtil.createSingleShipmentFromOrderMultiPackagesOneSegmentJson();

        SegmentCancelMessage segmentCancelMessage = mapper.mapShipmentDomainToSegmentCancelMessage(shipment);

        assertThat(segmentCancelMessage).isNotNull();
        assertThat(segmentCancelMessage.getShipmentId()).isEqualTo(shipment.getShipmentTrackingId());
        assertThat(segmentCancelMessage.getOrganisationId()).isEqualTo(shipment.getOrganization().getId());
        assertThat(segmentCancelMessage.getOrderId()).isEqualTo(shipment.getOrder().getId());
    }

    @Test
    void mapOrderDomainToOrderMsgPart_orderDomain_shouldReturnOrderMsgPart() {
        Shipment shipment = testUtil.createSingleShipmentFromOrderMultiPackagesMultiSegmentsJson();
        Order order = shipment.getOrder();

        OrderMsgPart orderMsgPart = mapper.mapOrderDomainToOrderMsgPart(order);

        assertThat(orderMsgPart).isNotNull();
        assertThat(orderMsgPart.getId()).isEqualTo(order.getId());
        assertThat(orderMsgPart.getCode()).isEqualTo(order.getOrderIdLabel());
        assertThat(orderMsgPart.getNote()).isEqualTo(order.getNotes());
        assertThat(orderMsgPart.getCustomerReferences()).isEqualTo(order.getCustomerReferenceId());
        assertThat(orderMsgPart.getTagsList()).isEqualTo(order.getTags());
        assertThat(orderMsgPart.getAttachments()).isNotEmpty();
        assertThat(orderMsgPart.getOpsType()).isEqualTo(order.getOpsType());
        assertThat(orderMsgPart.getNumberOfShipments()).isZero();
    }

    @Test
    void mapOrderDomainToOrderMsgPart_nullArguments_shouldReturnNull() {
        assertThat(mapper.mapOrderDomainToOrderMsgPart(null)).isNull();
    }

    @Test
    void mapShipmentDomainToShipmentMsgPart_validArguments_shouldReturnShipmentMsgPart() {
        Shipment shipment = testUtil.createSingleShipmentFromOrderOnePackageMultiSegmentsJson();

        List<String> tags = Arrays.asList("tag1", "tag2");
        shipment.setShipmentTags(tags);

        List<HostedFile> fileAttachments = new ArrayList<>();
        HostedFile hostedFile = new HostedFile();
        hostedFile.setId("1");
        hostedFile.setFileUrl("testUrl");
        hostedFile.setFileName("testFileName");
        fileAttachments.add(hostedFile);

        ShipmentMsgPart shipmentMsgPart = mapper.mapShipmentDomainToShipmentMsgPart(shipment);

        assertShipmentMsgPart(shipment, shipmentMsgPart);
    }

    @Test
    void mapSegmentDomainToSegmentMsgPart_transportCategoryGroundPlanned_shouldReturnSegmentMsgPart() {
        Shipment shipment = testUtil.createSingleShipmentFromOrderOnePackageMultiSegmentsJson();
        ShipmentJourney journey = shipment.getShipmentJourney();
        PackageJourneySegment segment = journey.getPackageJourneySegments().get(0);
        segment.setInstructions(shipment.getInstructions());
        segment.setVehicle(testUtil.createVehicleByNumber("TEST-123"));
        SegmentMsgPart segmentMsgPart = mapper.mapSegmentDomainToSegmentMsgPart(segment, shipment.getOrder());
        Order orderDomain = shipment.getOrder();

        assertSegmentCommon(segment, segmentMsgPart);
        assertSegmentGroundPlanned(segment, segmentMsgPart, orderDomain);
    }

    @Test
    void mapSegmentDomainToSegmentMsgPart_transportCategoryGroundCompleted_shouldReturnSegmentMsgPart() {
        Shipment shipment = testUtil.createSingleShipmentFromOrderOnePackageMultiSegmentsJson();
        ShipmentJourney journey = shipment.getShipmentJourney();
        PackageJourneySegment segment = journey.getPackageJourneySegments().get(0);
        segment.setPickUpActualTime("2023-04-10 16:27:02 +0700");
        segment.setDropOffActualTime("2023-04-11 16:27:02 +0700");
        segment.setInstructions(shipment.getInstructions());
        segment.setVehicle(testUtil.createVehicleByNumber("TEST-123"));
        SegmentMsgPart segmentMsgPart = mapper.mapSegmentDomainToSegmentMsgPart(segment, shipment.getOrder());

        assertSegmentCommon(segment, segmentMsgPart);
        assertSegmentGroundCompleted(segment, segmentMsgPart);
        //TODO added assert correct pickup and assert drop off time
    }

    @Test
    void mapSegmentDomainToSegmentMsgPart_transportCategoryAir_shouldReturnSegmentMsgPart() {
        Shipment shipment = testUtil.createSingleShipmentFromOrderOnePackageMultiSegmentsJson();
        ShipmentJourney journey = shipment.getShipmentJourney();
        PackageJourneySegment segment = journey.getPackageJourneySegments().get(1);
        segment.setInstructions(shipment.getInstructions());
        SegmentMsgPart segmentMsgPart = mapper.mapSegmentDomainToSegmentMsgPart(segment, shipment.getOrder());

        assertSegmentCommon(segment, segmentMsgPart);
        assertSegmentAir(segment, segmentMsgPart);

        segment.setInstructions(null);
        SegmentMsgPart segmentMsgPart2 = mapper.mapSegmentDomainToSegmentMsgPart(segment, shipment.getOrder());
        assertMiddleAirSegment(segment, segmentMsgPart2);
    }

    @Test
    void mapSegmentDomainToSegmentMsgPart_singleSegment_shouldReturnSegmentMsgPart() {
        Shipment shipment = testUtil.createSingleShipmentFromOrderMultiPackagesOneSegmentJson();
        ShipmentJourney journey = shipment.getShipmentJourney();
        PackageJourneySegment segment = journey.getPackageJourneySegments().get(0);
        segment.setInstructions(shipment.getInstructions());
        SegmentMsgPart segmentMsgPart = mapper.mapSegmentDomainToSegmentMsgPart(segment, shipment.getOrder());

        assertSegmentCommon(segment, segmentMsgPart);
        assertSingleSegment(shipment.getOrder(), segmentMsgPart);
    }

    @Test
    void mapSegmentDomainToSegmentMsgPart_firstSegment_shouldReturnSegmentMsgPart() {
        Shipment shipment = testUtil.createSingleShipmentFromOrderOnePackageMultiSegmentsJson();
        ShipmentJourney journey = shipment.getShipmentJourney();
        PackageJourneySegment segment = journey.getPackageJourneySegments().get(0);
        SegmentMsgPart segmentMsgPart = mapper.mapSegmentDomainToSegmentMsgPart(segment, shipment.getOrder());

        assertSegmentCommon(segment, segmentMsgPart);
        assertFirstSegment(segment, shipment.getOrder(), segmentMsgPart);
    }

    @Test
    void mapSegmentDomainToSegmentMsgPart_middleSegment_shouldReturnSegmentMsgPart() {
        Shipment shipment = testUtil.createSingleShipmentFromOrderOnePackageMultiSegmentsJson();
        ShipmentJourney journey = shipment.getShipmentJourney();
        PackageJourneySegment segment = journey.getPackageJourneySegments().get(3);
        segment.setInstructions(shipment.getInstructions());
        SegmentMsgPart segmentMsgPart = mapper.mapSegmentDomainToSegmentMsgPart(segment, shipment.getOrder());
        assertSegmentCommon(segment, segmentMsgPart);

        segment.setInstructions(null);
        SegmentMsgPart segmentMsgPart2 = mapper.mapSegmentDomainToSegmentMsgPart(segment, shipment.getOrder());
        assertMiddleGroundSegment(segment, segmentMsgPart2);
    }

    @Test
    void mapSegmentDomainToSegmentMsgPart_lastSegment_shouldReturnSegmentMsgPart() {
        Shipment shipment = testUtil.createSingleShipmentFromOrderOnePackageMultiSegmentsJson();
        ShipmentJourney journey = shipment.getShipmentJourney();
        int segmentSize = shipment.getShipmentJourney().getPackageJourneySegments().size();
        int pos = segmentSize - 1;
        PackageJourneySegment segment = journey.getPackageJourneySegments().get(pos);

        SegmentMsgPart segmentMsgPart = mapper.mapSegmentDomainToSegmentMsgPart(segment, shipment.getOrder());
        assertSegmentCommon(segment, segmentMsgPart);
        assertLastSegment(segment, shipment.getOrder(), segmentMsgPart);
    }

    @Test
    void mapOrderDomainToAttachmentList_validArguments_shouldReturnAttachmentsMsgPart() {
        Shipment shipmentDomain = testUtil.createSingleShipmentFromOrderMultiPackagesMultiSegmentsJson();
        Order orderDomain = shipmentDomain.getOrder();

        List<JsonNode> attachmentsMsgPart = mapper.mapOrderDomainToAttachmentList(orderDomain);

        assertThat(attachmentsMsgPart).withFailMessage("Order Attachment is null or empty.").isNotEmpty();
    }

    @Test
    void mapOrderDomainToAttachmentList_nullAttachments_shouldReturnEmptyList() {
        ObjectNode dummyOrderJson = objectMapper.createObjectNode();
        dummyOrderJson.put("id", "dummy-id");
        Order orderDomain = new Order();
        orderDomain.setData(dummyOrderJson.toString());

        List<JsonNode> attachmentsMsgPart = mapper.mapOrderDomainToAttachmentList(orderDomain);

        assertThat(attachmentsMsgPart).isEmpty();
    }

    @Test
    void mapOrderDomainToAttachmentList_emptyAttachments_shouldReturnEmptyList() {
        ObjectNode dummyOrderJson = objectMapper.createObjectNode();
        dummyOrderJson.put("id", "dummy-id");
        dummyOrderJson.putArray("attachments");
        Order orderDomain = new Order();
        orderDomain.setData(dummyOrderJson.toString());

        List<JsonNode> attachmentsMsgPart = mapper.mapOrderDomainToAttachmentList(orderDomain);

        assertThat(attachmentsMsgPart).isEmpty();
    }

    @Test
    void mapOrderJsonToPhoneCodeMsgPart_nullSource_shouldReturnPhoneCodeMsgPart() {
        String emptyJsonText = "{}";
        assertThat(mapper.mapOrderJsonToPhoneCodeMsgPart(emptyJsonText, "shipper", "shipper_phone_code")).isNull();
    }

    @Test
    void mapShipmentDomainToShipperMsgPart_validArguments_shouldReturnShipperMsgPart() {
        Shipment shipmentDomain = testUtil.createSingleShipmentFromOrderMultiPackagesMultiSegmentsJson();
        Sender senderDomain = shipmentDomain.getSender();

        ShipperMsgPart shipperMsgPart = mapper.mapShipmentDomainToShipperMsgPart(shipmentDomain);

        assertThat(shipperMsgPart.getName()).isEqualTo(senderDomain.getName());
        assertThat(shipperMsgPart.getEmail()).isEqualTo(senderDomain.getEmail());
        assertThat(shipperMsgPart.getPhone()).isEqualTo(senderDomain.getContactNumber());

        JsonNode orderJson = MapperUtil.readRawJson(shipmentDomain.getOrder().getData(), objectMapper);
        JsonNode shipperPhoneCodeJson = orderJson.get("shipper").get("shipper_phone_code_id");

        assertThat(shipperMsgPart.getPhoneCode().getId()).isEqualTo(shipperPhoneCodeJson.get("id").asText());
        assertThat(shipperMsgPart.getPhoneCode().getCode()).isEqualTo(shipperPhoneCodeJson.get("code").asText());
        assertThat(shipperMsgPart.getPhoneCode().getName()).isEqualTo(shipperPhoneCodeJson.get("name").asText());
    }

    @Test
    void mapShipmentDomainToConsigneeMsgPart_validArguments_shouldReturnConsigneeMsgPart() {
        Shipment shipmentDomain = testUtil.createSingleShipmentFromOrderMultiPackagesMultiSegmentsJson();
        Consignee consigneeDomain = shipmentDomain.getConsignee();

        ConsigneeMsgPart consigneeMsgPart = mapper.mapShipmentDomainToConsigneeMsgPart(shipmentDomain);

        assertThat(consigneeMsgPart.getName()).isEqualTo(consigneeDomain.getName());
        assertThat(consigneeMsgPart.getEmail()).isEqualTo(consigneeDomain.getEmail());
        assertThat(consigneeMsgPart.getPhone()).isEqualTo(consigneeDomain.getContactNumber());

        JsonNode orderJson = MapperUtil.readRawJson(shipmentDomain.getOrder().getData(), objectMapper);
        JsonNode shipperPhoneCodeJson = orderJson.get("consignee").get("consignee_phone_code_id");

        assertThat(consigneeMsgPart.getPhoneCode().getId()).isEqualTo(shipperPhoneCodeJson.get("id").asText());
        assertThat(consigneeMsgPart.getPhoneCode().getCode()).isEqualTo(shipperPhoneCodeJson.get("code").asText());
        assertThat(consigneeMsgPart.getPhoneCode().getName()).isEqualTo(shipperPhoneCodeJson.get("name").asText());
    }

    @Test
    void mapShipmentDomainToOriginMsgPart_validArguments_shouldReturnAddressMsgPart() {
        Shipment shipmentDomain = testUtil.createSingleShipmentFromOrderMultiPackagesMultiSegmentsJson();
        Address originDomain = shipmentDomain.getOrigin();

        AddressDetailsMsgPart originMsgPart = mapper.mapShipmentDomainToOriginMsgPart(shipmentDomain);

        assertThat(originMsgPart.getId()).isEqualTo(originDomain.getExternalId());
        assertThat(originMsgPart.getCity()).isEqualTo(originDomain.getCityName());
        assertThat(originMsgPart.getState()).isEqualTo(originDomain.getStateName());
        assertThat(originMsgPart.getCountry()).isEqualTo(originDomain.getCountryName());
        assertThat(originMsgPart.getAddress()).isEqualTo(originDomain.getFullAddress());
        assertThat(originMsgPart.getCityId()).isEqualTo(originDomain.getCityId());
        assertThat(originMsgPart.getStateId()).isEqualTo(originDomain.getStateId());
        assertThat(originMsgPart.getCountryId()).isEqualTo(originDomain.getCountryId());
        assertThat(originMsgPart.getLatitude()).isEqualTo(originDomain.getLatitude());
        assertThat(originMsgPart.getLongitude()).isEqualTo(originDomain.getLongitude());
        assertThat(originMsgPart.getPostalCode()).isEqualTo(originDomain.getPostalCode());
        assertThat(originMsgPart.getAddressLine1()).isEqualTo(originDomain.getLine1());
        assertThat(originMsgPart.getAddressLine2()).isEqualTo(originDomain.getLine2());
        assertThat(originMsgPart.getAddressLine3()).isEqualTo(originDomain.getLine3());
        assertThat(originMsgPart.getManualCoordinates()).isEqualTo(originDomain.isManualCoordinates());
        assertThat(originMsgPart.getCompany()).isEqualTo(originDomain.getCompany());
        assertThat(originMsgPart.getDepartment()).isEqualTo(originDomain.getDepartment());
    }

    @Test
    void mapShipmentDomainToDestinationMsgPart_validArguments_shouldReturnAddressMsgPart() {
        Shipment shipmentDomain = testUtil.createSingleShipmentFromOrderMultiPackagesMultiSegmentsJson();
        Address destinationDomain = shipmentDomain.getDestination();

        AddressDetailsMsgPart destinationMsgPart = mapper.mapShipmentDomainToDestinationMsgPart(shipmentDomain);

        assertThat(destinationMsgPart.getId()).isEqualTo(destinationDomain.getExternalId());
        assertThat(destinationMsgPart.getCity()).isEqualTo(destinationDomain.getCityName());
        assertThat(destinationMsgPart.getState()).isEqualTo(destinationDomain.getStateName());
        assertThat(destinationMsgPart.getCountry()).isEqualTo(destinationDomain.getCountryName());
        assertThat(destinationMsgPart.getAddress()).isEqualTo(destinationDomain.getFullAddress());
        assertThat(destinationMsgPart.getCityId()).isEqualTo(destinationDomain.getCityId());
        assertThat(destinationMsgPart.getStateId()).isEqualTo(destinationDomain.getStateId());
        assertThat(destinationMsgPart.getCountryId()).isEqualTo(destinationDomain.getCountryId());
        assertThat(destinationMsgPart.getLatitude()).isEqualTo(destinationDomain.getLatitude());
        assertThat(destinationMsgPart.getLongitude()).isEqualTo(destinationDomain.getLongitude());
        assertThat(destinationMsgPart.getPostalCode()).isEqualTo(destinationDomain.getPostalCode());
        assertThat(destinationMsgPart.getAddressLine1()).isEqualTo(destinationDomain.getLine1());
        assertThat(destinationMsgPart.getAddressLine2()).isEqualTo(destinationDomain.getLine2());
        assertThat(destinationMsgPart.getAddressLine3()).isEqualTo(destinationDomain.getLine3());
        assertThat(destinationMsgPart.getManualCoordinates()).isEqualTo(destinationDomain.isManualCoordinates());
        assertThat(destinationMsgPart.getCompany()).isEqualTo(destinationDomain.getCompany());
        assertThat(destinationMsgPart.getDepartment()).isEqualTo(destinationDomain.getDepartment());
    }

    @Test
    void mapShipmentDomainToPricingInfo_validArguments_shouldReturnPricingInfoMsgPart() {
        Shipment shipmentDomain = testUtil.createSingleShipmentFromOrderMultiPackagesMultiSegmentsJson();
        PricingInfo pricingInfoDomain = shipmentDomain.getShipmentPackage().getPricingInfo();

        PricingInfoMsgPart pricingInfoMsgPart = mapper.mapShipmentDomainToPricingInfo(shipmentDomain);
        assertThat(pricingInfoMsgPart.getId()).isEqualTo(pricingInfoDomain.getExternalId());
        assertThat(pricingInfoMsgPart.getCod()).isEqualTo(pricingInfoDomain.getCod());
        assertThat(pricingInfoMsgPart.getTax()).isEqualTo(pricingInfoDomain.getTax());
        assertThat(pricingInfoMsgPart.getDiscount()).isEqualTo(pricingInfoDomain.getDiscount());
        assertThat(pricingInfoMsgPart.getSurcharge()).isEqualTo(pricingInfoDomain.getSurcharge());
        assertThat(pricingInfoMsgPart.getBaseTariff()).isEqualTo(pricingInfoDomain.getBaseTariff());
        assertThat(pricingInfoMsgPart.getInsuranceCharge()).isEqualTo(pricingInfoDomain.getInsuranceCharge());
        assertThat(pricingInfoMsgPart.getServiceTypeCharge()).isEqualTo(pricingInfoDomain.getServiceTypeCharge());
        assertThat(pricingInfoMsgPart.getCurrencyCode()).isEqualTo(pricingInfoDomain.getCurrency());

        JsonNode orderJson = MapperUtil.readRawJson(shipmentDomain.getOrder().getData(), objectMapper);
        JsonNode currencyJson = orderJson.get("pricing_info").get("currency");
        CurrencyMsgPart currencyMsgPart = pricingInfoMsgPart.getCurrency();

        assertThat(currencyMsgPart.getId()).isEqualTo(currencyJson.get("id").asText());
        assertThat(currencyMsgPart.getCode()).isEqualTo(currencyJson.get("code").asText());
        assertThat(currencyMsgPart.getName()).isEqualTo(currencyJson.get("name").asText());
        assertThat(currencyMsgPart.getDeleted()).isEqualTo(currencyJson.get("deleted").asBoolean());
        assertThat(currencyMsgPart.getExchangeRate()).isEqualTo(currencyJson.get("exchange_rate").decimalValue());
        assertThat(currencyMsgPart.getOrganisationId()).isEqualTo(currencyJson.get("organisation_id").asText());
        assertThat(currencyMsgPart.getIsDefaultCurrency()).isEqualTo(currencyJson.get("is_default_currency").asBoolean());
        assertThat(testUtil.isInstantFromString(currencyJson.get("created_at").asText(), currencyMsgPart.getCreatedAt())).isTrue();
        assertThat(testUtil.isInstantFromString(currencyJson.get("updated_at").asText(), currencyMsgPart.getUpdatedAt())).isTrue();
    }

    @Test
    void mapShipmentPackageToPackageMsgPart_packageDomain_shouldReturnPackagesMsgPart() {
        Shipment shipmentDomain = testUtil.createSingleShipmentFromOrderOnePackageMultiSegmentsJson();
        JsonNode orderJson = MapperUtil.readRawJson(shipmentDomain.getOrder().getData(), objectMapper);
        JsonNode packageJson = orderJson.get("packages").get(0);
        Package packageDomain = shipmentDomain.getShipmentPackage();
        PackageDimension dimensionDomain = packageDomain.getDimension();

        PackageMsgPart packageMsgPart = mapper.mapShipmentPackageToPackageMsgPart(shipmentDomain);

        assertThat(packageMsgPart.getId()).isEqualTo(packageDomain.getId());
        assertThat(packageMsgPart.getCode()).isEqualTo(packageDomain.getCode());
        assertThat(packageMsgPart.getRefId()).isEqualTo(packageDomain.getRefId());
        assertThat(packageMsgPart.getTypeRefId()).isEqualTo(packageDomain.getTypeRefId());
        assertThat(packageMsgPart.getType()).isEqualTo(packageDomain.getType());
        assertThat(packageMsgPart.getNote()).isEqualTo(shipmentDomain.getNotes());
        assertThat(packageMsgPart.getValueOfGoods()).isEqualTo(packageDomain.getTotalValue());
        assertThat(packageMsgPart.getItemCount()).isEqualTo(packageJson.get("items_count").intValue());
        assertThat(packageMsgPart.getHeight()).isEqualTo(dimensionDomain.getHeight());
        assertThat(packageMsgPart.getWidth()).isEqualTo(dimensionDomain.getWidth());
        assertThat(packageMsgPart.getLength()).isEqualTo(dimensionDomain.getLength());
        assertThat(packageMsgPart.getGrossWeight()).isEqualTo(dimensionDomain.getGrossWeight());
        assertThat(packageMsgPart.getVolumeWeight()).isEqualTo(dimensionDomain.getVolumeWeight());
        assertThat(packageMsgPart.getChargeableWeight()).isEqualTo(dimensionDomain.getChargeableWeight());
        assertThat(packageMsgPart.getMeasurement()).isEqualTo(dimensionDomain.getMeasurementUnit());
        assertThat(packageMsgPart.getAdditionalData1()).isEqualTo(shipmentDomain.getShipmentReferenceId().get(0));

        List<Commodity> commoditiesDomainList = packageDomain.getCommodities();
        List<CommodityMsgPart> commodityMsgPartList = packageMsgPart.getCommodities();

        assertThat(commodityMsgPartList).isNotEmpty().hasSameSizeAs(commoditiesDomainList);
        for (int j = 0; j < commoditiesDomainList.size(); j++) {
            Commodity commodityDomain = commoditiesDomainList.get(j);
            CommodityMsgPart commodityMsgPart = commodityMsgPartList.get(j);
            assertThat(commodityMsgPart.getId()).isEqualTo(commodityDomain.getExternalId());
            assertThat(commodityMsgPart.getName()).isEqualTo(commodityDomain.getName());
            assertThat(commodityMsgPart.getDescription()).isEqualTo(commodityDomain.getDescription());
            assertThat(commodityMsgPart.getCode()).isEqualTo(commodityDomain.getCode());
            assertThat(commodityMsgPart.getHsCode()).isEqualTo(commodityDomain.getHsCode());
            assertThat(commodityMsgPart.getNote()).isEqualTo(commodityDomain.getNote());
            assertThat(commodityMsgPart.getPackagingType()).isEqualTo(commodityDomain.getPackagingType());
        }
        assertThat(packageMsgPart.getCustom()).isEqualTo(dimensionDomain.isCustom());
    }

    @Test
    void test_compareAndGetLatestTime_segmentPickupTimeIsNull_ShouldReturnOderDeliveryStartTime() {
        String pickupStartTime = "2023-08-18 00:00:00 GMT+05:30";
        String deliveryStartTime = "2023-08-21 00:00:00 GMT+05:30";
        ZonedDateTime orderDeliveryStartTimeZoned = DateTimeUtil.parseZonedDateTime(deliveryStartTime);
        LocalDateTime orderDeliveryStartTime = orderDeliveryStartTimeZoned.toLocalDateTime();
        String timeZone = "GMT+05:30";

        ZonedDateTime result = mapper.compareAndGetLatestTime(pickupStartTime, orderDeliveryStartTime, timeZone);

        ZonedDateTime expected = DateTimeUtil.localeDateTimeToZoneDateTime(orderDeliveryStartTime, timeZone);
        assertThat(result).isEqualTo(expected);

        String resultTimezone = result.getZone().toString();
        assertThat(resultTimezone).isEqualTo(timeZone);
    }

    @Test
    void mapMultiSegments_groundAndLastMileSegments_dropOffStartTimeShouldBeWhicheverIsLatestBetweenSegmentPickupTimeAndOrderDeliveryStartTime() {
        List<Shipment> shipmentList = testUtil.createMultipleShipmentsFromOrderMultiSegmentsJson();
        Shipment shipment = shipmentList.get(0);
        ShipmentJourney journey = shipment.getShipmentJourney();
        PackageJourneySegment segment = journey.getPackageJourneySegments().get(0);
        segment.setInstructions(shipment.getInstructions());
        segment.setVehicle(testUtil.createVehicleByNumber("TEST-123"));
        SegmentMsgPart segmentMsgPart = mapper.mapSegmentDomainToSegmentMsgPart(segment, shipment.getOrder());
        Order order = shipment.getOrder();

        assertSegmentCommon(segment, segmentMsgPart);
        assertSegmentGroundPlanned(segment, segmentMsgPart, order);
    }

    @Test
    void mapSegmentDomainToSegmentMsgPart_segmentDomainWithBookingDetails_ShouldBeMapCorrectlyToBeSentToDispatch() {
        String externalBookingReference = "testExternalBookingReference";
        String internalBookingReference = "testInternalBookingReference";
        String rejectionReason = "testRejectionReason";

        List<Shipment> shipmentList = testUtil.createMultipleShipmentsFromOrderMultiSegmentsJson();
        Shipment shipment = shipmentList.get(0);
        ShipmentJourney journey = shipment.getShipmentJourney();
        PackageJourneySegment segment = journey.getPackageJourneySegments().get(0);
        segment.setInternalBookingReference(internalBookingReference);
        segment.setExternalBookingReference(externalBookingReference);
        segment.setRejectionReason(rejectionReason);
        segment.setAssignmentStatus("Completed");

        SegmentMsgPart segmentMsgPart = mapper.mapSegmentDomainToSegmentMsgPart(segment, shipment.getOrder());
        assertThat(segmentMsgPart.getExternalBookingReference()).isEqualTo(externalBookingReference);
        assertThat(segmentMsgPart.getInternalBookingReference()).isEqualTo(internalBookingReference);
        assertThat(segmentMsgPart.getRejectionReason()).isEqualTo(rejectionReason);
        assertThat(segmentMsgPart.getAssignmentStatus()).isEqualTo("Completed");
    }

    @Test
    void mapMultiSegments_groundAndMidMileSegments_dropOffStartTimeShouldBeTheSameAsTheRespectiveSegmentPickupStartTime() {
        List<Shipment> shipmentList = testUtil.createMultipleShipmentsFromOrderMultiSegmentsJson();
        Shipment shipment = shipmentList.get(0);
        ShipmentJourney journey = shipment.getShipmentJourney();
        PackageJourneySegment segment = journey.getPackageJourneySegments().get(0);
        segment.setInstructions(shipment.getInstructions());
        segment.setVehicle(testUtil.createVehicleByNumber("TEST-123"));
        SegmentMsgPart segmentMsgPart = mapper.mapSegmentDomainToSegmentMsgPart(segment, shipment.getOrder());
        Order order = shipment.getOrder();

        assertSegmentCommon(segment, segmentMsgPart);
        assertSegmentGroundPlanned(segment, segmentMsgPart, order);
    }

    private void assertShipmentMsgPart(Shipment shipmentDomain, ShipmentMsgPart shipmentMessage) {
        assertThat(shipmentMessage.getShipmentId()).isEqualTo(shipmentDomain.getId());
        assertThat(shipmentMessage.getShipmentTrackingId()).isEqualTo(shipmentDomain.getShipmentTrackingId());
        assertThat(shipmentMessage.getShipmentReferenceIds()).isEqualTo(shipmentDomain.getShipmentReferenceId());
        assertThat(shipmentMessage.getPackageVal()).isNotNull();
        assertThat(shipmentMessage.getShipmentTags()).isEqualTo(shipmentDomain.getShipmentTags());
        assertThat(shipmentMessage.getShipmentAttachments()).isEqualTo(shipmentDomain.getShipmentAttachments());
    }

    private void assertSegmentCommon(PackageJourneySegment segmentDomain, SegmentMsgPart segmentMsgPart) {
        assertThat(segmentMsgPart.getId()).isEqualTo(segmentDomain.getSegmentId());
        assertThat(segmentMsgPart.getRefId()).isEqualTo(segmentDomain.getRefId());
        assertThat(segmentMsgPart.getType()).isEqualTo(segmentDomain.getType().toString());
        assertThat(segmentMsgPart.getStatus()).isEqualTo(segmentDomain.getStatus().toString());
        assertThat(segmentMsgPart.getSequenceNo()).isEqualTo(segmentDomain.getSequence());
        assertThat(segmentMsgPart.getTransportCategory()).isEqualTo(segmentDomain.getTransportType().toString());
        assertThat(segmentMsgPart.getPartnerId()).isEqualTo(segmentDomain.getPartner().getId());
        assertThat(segmentMsgPart.getFromFacilityId()).isEqualTo(segmentDomain.getStartFacility().getExternalId());
        assertThat(segmentMsgPart.getToFacilityId()).isEqualTo(segmentDomain.getEndFacility().getExternalId());
        assertThat(segmentMsgPart.getMasterWaybillLabel()).isEqualTo(segmentDomain.getMasterWaybill());
        assertThat(segmentMsgPart.getInstructions()).isNotEmpty();
        assertThat(segmentMsgPart.getCalculatedMileage().getValue().floatValue())
                .isEqualTo(segmentDomain.getCalculatedMileage().floatValue());
        assertThat(segmentMsgPart.getCalculatedMileage().getUom()).isEqualTo(segmentDomain.getCalculatedMileageUnit());
        assertThat(segmentMsgPart.getDuration().getValue().floatValue())
                .isEqualTo(segmentDomain.getDuration().floatValue());
        assertThat(segmentMsgPart.getDuration().getUom()).isEqualTo(segmentDomain.getDurationUnit());
        assertThat(segmentMsgPart.getHubId()).isEqualTo(segmentDomain.getHubId());
    }

    private void assertSegmentGroundPlanned(PackageJourneySegment segmentDomain, SegmentMsgPart segmentMsgPart, Order orderDomain) {
        assertThat(segmentMsgPart.getVehicleNumber()).isEqualTo(segmentDomain.getVehicle().getNumber());
        assertThat(segmentMsgPart.getAirNumber()).isNull();
        assertThat(segmentMsgPart.getAirLine()).isNull();
        assertThat(segmentMsgPart.getAirLineCode()).isNull();
        assertThat(segmentMsgPart.getPickUpActualTime()).isNull();
        assertThat(segmentMsgPart.getDropOffActualTime()).isNull();
        assertThat(segmentMsgPart.getLockoutTime()).withFailMessage("Lockout Time mapped on transport category Ground.").isNull();
        assertThat(segmentMsgPart.getDepartedTime()).withFailMessage("Departed Time mapped on transport category Ground.").isNull();
        assertThat(segmentMsgPart.getArrivalTime()).withFailMessage("Arrival Time mapped on transport category Ground.").isNull();
        assertThat(segmentMsgPart.getRecoverTime()).withFailMessage("Recover Time mapped on transport category Ground.").isNull();
        assertPickUpStartTimeAndDropOffStartTime(segmentDomain, segmentMsgPart, orderDomain);
    }

    private void assertPickUpStartTimeAndDropOffStartTime(PackageJourneySegment segmentDomain, SegmentMsgPart segmentMsgPart, Order orderDomain) {
        if (SegmentType.FIRST_MILE == segmentDomain.getType()) {
            // If First Segment, `Pickup Time` will be from order `Drop off Time` will be from segment
            assertLocaleDateTimeToZoneDateTime(DateTimeUtil.convertStringToLocalDateTime(orderDomain.getPickupStartTime()), orderDomain.getPickupTimezone(), segmentMsgPart.getPickUpStartTime());
            assertLocaleDateTimeToZoneDateTime(DateTimeUtil.convertStringToLocalDateTime(orderDomain.getPickupCommitTime()), orderDomain.getPickupTimezone(), segmentMsgPart.getPickUpCommitTime());
            assertThat(segmentMsgPart.getPickupTimeZone()).isEqualTo(orderDomain.getPickupTimezone());
            assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getDropOffTime(), segmentMsgPart.getDropOffStartTime())).isTrue();
            assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getDropOffTime(), segmentMsgPart.getDropOffCommitTime())).isTrue();
            assertThat(segmentMsgPart.getDropOffTimeZone()).isEqualTo(segmentDomain.getDropOffTimezone());
        } else if (SegmentType.LAST_MILE == segmentDomain.getType()) {
            // If Last Segment, `Pickup Time` will be from segment
            assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getPickUpTime(), segmentMsgPart.getPickUpStartTime())).isTrue();
            assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getPickUpTime(), segmentMsgPart.getPickUpCommitTime())).isTrue();
            assertThat(segmentMsgPart.getPickupTimeZone()).isEqualTo(orderDomain.getPickupTimezone());

            // If Last Segment, `Drop off Time` will be whichever the later time is between the segment pick up time and the order delivery start time
            assertThat(testUtil.isZonedDateTimeFromString(mapper.compareAndGetLatestTime(segmentDomain.getPickUpTime(), DateTimeUtil.convertStringToLocalDateTime(orderDomain.getDeliveryStartTime()), orderDomain.getDeliveryTimezone()).toString(), segmentMsgPart.getDropOffStartTime())).isTrue();
            assertLocaleDateTimeToZoneDateTime(DateTimeUtil.convertStringToLocalDateTime(orderDomain.getDeliveryCommitTime()), orderDomain.getDeliveryTimezone(), segmentMsgPart.getDropOffCommitTime());
            assertThat(segmentMsgPart.getDropOffTimeZone()).isEqualTo(segmentDomain.getDropOffTimezone());
        } else {
            // If Middle Segment, pickup drop off times will be from segments
            assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getPickUpTime(), segmentMsgPart.getPickUpStartTime())).isTrue();
            assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getPickUpTime(), segmentMsgPart.getPickUpCommitTime())).isTrue();
            assertThat(segmentMsgPart.getPickupTimeZone()).isEqualTo(segmentDomain.getPickUpTimezone());

            // If the segment is middle and ground type, the drop off start time should be the same timing as its respective segment’s pick up start time
            assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getPickUpTime(), segmentMsgPart.getDropOffStartTime())).isTrue();
            assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getDropOffTime(), segmentMsgPart.getDropOffCommitTime())).isTrue();
            assertThat(segmentMsgPart.getDropOffTimeZone()).isEqualTo(segmentDomain.getDropOffTimezone());
        }
    }

    private void assertLocaleDateTimeToZoneDateTime(LocalDateTime localDateTime, String localDateTimeZone, ZonedDateTime zonedDateTime) {
        assertThat(zonedDateTime.getZone()).isEqualTo(DateTimeUtil.parseZoneId(localDateTimeZone));
        assertThat(zonedDateTime.getYear()).isEqualTo(localDateTime.getYear());
        assertThat(zonedDateTime.getMonthValue()).isEqualTo(localDateTime.getMonthValue());
        assertThat(zonedDateTime.getDayOfMonth()).isEqualTo(localDateTime.getDayOfMonth());
        assertThat(zonedDateTime.getHour()).isEqualTo(localDateTime.getHour());
        assertThat(zonedDateTime.getMinute()).isEqualTo(localDateTime.getMinute());
        assertThat(zonedDateTime.getSecond()).isEqualTo(localDateTime.getSecond());
        assertThat(zonedDateTime.getNano()).isEqualTo(localDateTime.getNano());

    }

    private void assertSegmentGroundCompleted(PackageJourneySegment segmentDomain, SegmentMsgPart segmentMsgPart) {
        assertThat(segmentMsgPart.getVehicleNumber()).isEqualTo(segmentDomain.getVehicle().getNumber());
        assertThat(segmentMsgPart.getAirNumber()).isNull();
        assertThat(segmentMsgPart.getAirLine()).isNull();
        assertThat(segmentMsgPart.getAirLineCode()).isNull();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getPickUpActualTime(), segmentMsgPart.getPickUpActualTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getDropOffActualTime(), segmentMsgPart.getDropOffActualTime())).isTrue();
        assertThat(segmentMsgPart.getLockoutTime()).withFailMessage("Lockout Time mapped on transport category Ground.").isNull();
        assertThat(segmentMsgPart.getDepartedTime()).withFailMessage("Departed Time mapped on transport category Ground.").isNull();
        assertThat(segmentMsgPart.getArrivalTime()).withFailMessage("Arrival Time mapped on transport category Ground.").isNull();
        assertThat(segmentMsgPart.getRecoverTime()).withFailMessage("Recover Time mapped on transport category Ground.").isNull();
    }

    private void assertSegmentAir(PackageJourneySegment segmentDomain, SegmentMsgPart segmentMsgPart) {
        assertThat(segmentMsgPart.getVehicleNumber())
                .withFailMessage("Vehicle Number mapped on transport category Air.")
                .isNull();

        assertThat(segmentMsgPart.getAirNumber()).isEqualTo(segmentDomain.getFlightNumber());
        assertThat(segmentMsgPart.getAirLine()).isEqualTo(segmentDomain.getAirline());
        assertThat(segmentMsgPart.getAirLineCode()).isEqualTo(segmentDomain.getAirlineCode());
        assertThat(segmentMsgPart.getAirLine()).isEqualTo(segmentDomain.getAirline());
        assertThat(segmentMsgPart.getAirLine()).isEqualTo(segmentDomain.getAirline());
        assertThat(segmentMsgPart.getAirLine()).isEqualTo(segmentDomain.getAirline());
        assertThat(segmentMsgPart.getAirLine()).isEqualTo(segmentDomain.getAirline());
        assertThat(segmentMsgPart.getAirLine()).isEqualTo(segmentDomain.getAirline());
        assertThat(segmentMsgPart.getPickUpStartTime())
                .withFailMessage("Pick Up Start Time mapped on transport category Air.")
                .isNull();
        assertThat(segmentMsgPart.getPickUpCommitTime())
                .withFailMessage("Pick Up Commit Time mapped on transport category Air.")
                .isNull();
        assertThat(segmentMsgPart.getPickUpActualTime())
                .withFailMessage("Pick Up Actual Time mapped on transport category Air.")
                .isNull();
        assertThat(segmentMsgPart.getDropOffStartTime())
                .withFailMessage("Drop Off Start Time mapped on transport category Air.")
                .isNull();
        assertThat(segmentMsgPart.getDropOffCommitTime())
                .withFailMessage("Drop Off Commit Time mapped on transport category Air.")
                .isNull();
        assertThat(segmentMsgPart.getDropOffActualTime())
                .withFailMessage("Drop Off Actual Time mapped on transport category Air.")
                .isNull();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getLockOutTime(), segmentMsgPart.getLockoutTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getDepartureTime(), segmentMsgPart.getDepartedTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getArrivalTime(), segmentMsgPart.getArrivalTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getRecoveryTime(), segmentMsgPart.getRecoverTime())).isTrue();
    }

    private void assertSingleSegment(Order orderDomain, SegmentMsgPart segmentMsgPart) {
        assertThat(segmentMsgPart.getInstructions()).satisfies(hasPickupInstruction());
        assertThat(segmentMsgPart.getInstructions()).satisfies(hasDeliveryInstruction());
        assertThat(segmentMsgPart.getInstructions()).satisfies(hasJourneyInstruction());
        assertThat(testUtil.isEqualDateTime(DateTimeUtil.convertStringToLocalDateTime(orderDomain.getPickupStartTime()), orderDomain.getPickupTimezone(), segmentMsgPart.getPickUpStartTime())).isTrue();
        assertThat(testUtil.isEqualDateTime(DateTimeUtil.convertStringToLocalDateTime(orderDomain.getPickupCommitTime()), orderDomain.getPickupTimezone(), segmentMsgPart.getPickUpCommitTime())).isTrue();
        assertThat(testUtil.isEqualDateTime(DateTimeUtil.convertStringToLocalDateTime(orderDomain.getDeliveryStartTime()), orderDomain.getDeliveryTimezone(), segmentMsgPart.getDropOffStartTime())).isTrue();
        assertThat(testUtil.isEqualDateTime(DateTimeUtil.convertStringToLocalDateTime(orderDomain.getDeliveryCommitTime()), orderDomain.getDeliveryTimezone(), segmentMsgPart.getDropOffCommitTime())).isTrue();
    }

    private void assertFirstSegment(PackageJourneySegment segmentDomain, Order orderDomain, SegmentMsgPart segmentMsgPart) {
        assertThat(segmentMsgPart.getInstructions()).satisfies(hasPickupInstruction());
        assertThat(segmentMsgPart.getInstructions()).satisfies(hasNoDeliveryInstruction());
        assertThat(segmentMsgPart.getInstructions()).satisfies(hasJourneyInstruction());
        assertThat(testUtil.isEqualDateTime(DateTimeUtil.convertStringToLocalDateTime(orderDomain.getPickupStartTime()), orderDomain.getPickupTimezone(), segmentMsgPart.getPickUpStartTime())).isTrue();
        assertThat(testUtil.isEqualDateTime(DateTimeUtil.convertStringToLocalDateTime(orderDomain.getPickupCommitTime()), orderDomain.getPickupTimezone(), segmentMsgPart.getPickUpCommitTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getDropOffTime(), segmentMsgPart.getDropOffStartTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getDropOffTime(), segmentMsgPart.getDropOffCommitTime())).isTrue();
    }

    private void assertMiddleGroundSegment(PackageJourneySegment segmentDomain, SegmentMsgPart segmentMsgPart) {
        assertThat(segmentMsgPart.getInstructions()).isNull();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getPickUpTime(), segmentMsgPart.getPickUpStartTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getPickUpTime(), segmentMsgPart.getPickUpCommitTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getPickUpTime(), segmentMsgPart.getDropOffStartTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getDropOffTime(), segmentMsgPart.getDropOffCommitTime())).isTrue();
        assertThat(segmentMsgPart.getLockoutTime()).isNull();
        assertThat(segmentMsgPart.getDepartedTime()).isNull();
        assertThat(segmentMsgPart.getArrivalTime()).isNull();
        assertThat(segmentMsgPart.getRecoverTime()).isNull();
    }

    private void assertMiddleAirSegment(PackageJourneySegment segmentDomain, SegmentMsgPart segmentMsgPart) {
        assertThat(segmentMsgPart.getInstructions()).isNull();
        assertThat(segmentMsgPart.getAirNumber()).isEqualTo(segmentDomain.getFlightNumber());
        assertThat(segmentMsgPart.getAirLine()).isEqualTo(segmentDomain.getAirline());
        assertThat(segmentMsgPart.getAirLineCode()).isEqualTo(segmentDomain.getAirlineCode());
        assertThat(segmentMsgPart.getPickUpStartTime()).isNull();
        assertThat(segmentMsgPart.getPickUpCommitTime()).isNull();
        assertThat(segmentMsgPart.getDropOffStartTime()).isNull();
        assertThat(segmentMsgPart.getDropOffCommitTime()).isNull();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getLockOutTime(), segmentMsgPart.getLockoutTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getDepartureTime(), segmentMsgPart.getDepartedTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getArrivalTime(), segmentMsgPart.getArrivalTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getRecoveryTime(), segmentMsgPart.getRecoverTime())).isTrue();
    }

    private void assertLastSegment(PackageJourneySegment segmentDomain, Order orderDomain, SegmentMsgPart segmentMsgPart) {
        assertThat(segmentMsgPart.getInstructions()).satisfies(hasNoPickupInstruction());
        assertThat(segmentMsgPart.getInstructions()).satisfies(hasDeliveryInstruction());
        assertThat(segmentMsgPart.getInstructions()).satisfies(hasJourneyInstruction());
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getPickUpTime(), segmentMsgPart.getPickUpStartTime())).isTrue();
        assertThat(testUtil.isZonedDateTimeFromString(segmentDomain.getPickUpTime(), segmentMsgPart.getPickUpCommitTime())).isTrue();
        assertThat(testUtil.isEqualDateTime(DateTimeUtil.convertStringToLocalDateTime(orderDomain.getDeliveryStartTime()), orderDomain.getDeliveryTimezone(), segmentMsgPart.getDropOffStartTime())).isTrue();
        assertThat(testUtil.isEqualDateTime(DateTimeUtil.convertStringToLocalDateTime(orderDomain.getDeliveryCommitTime()), orderDomain.getDeliveryTimezone(), segmentMsgPart.getDropOffCommitTime())).isTrue();
    }

    private Instruction createPickUpInstruction() {
        Instruction pickUpInstruction = new Instruction();
        pickUpInstruction.setApplyTo(PICKUP);
        pickUpInstruction.setValue(PICK_INSTRUCTION);
        return pickUpInstruction;
    }

    private Instruction createDeliveryInstruction() {
        Instruction deliveryInstruction = new Instruction();
        deliveryInstruction.setApplyTo(InstructionApplyToType.DELIVERY);
        deliveryInstruction.setValue(DELIVERY_INSTRUCTION);
        return deliveryInstruction;
    }

    private Condition<List<? extends Instruction>> hasPickupInstruction() {
        return new Condition<>(instructionContainsType(PICKUP),
                "contains pickup instruction");
    }

    private Condition<List<? extends Instruction>> hasNoPickupInstruction() {
        return new Condition<>(Predicate.not(instructionContainsType(PICKUP)),
                "does not contain pickup instruction");
    }

    private Condition<List<? extends Instruction>> hasDeliveryInstruction() {
        return new Condition<>(instructionContainsType(DELIVERY),
                "contains delivery instruction");
    }

    private Condition<List<? extends Instruction>> hasNoDeliveryInstruction() {
        return new Condition<>(Predicate.not(instructionContainsType(DELIVERY)),
                "does not contain delivery instruction");
    }

    private Condition<List<? extends Instruction>> hasJourneyInstruction() {
        return new Condition<>(instructionContainsType(JOURNEY),
                "contains journey instruction");
    }

    private Predicate<List<? extends Instruction>> instructionContainsType(InstructionApplyToType type) {
        return instructions -> instructions.stream()
                .map(Instruction::getApplyTo)
                .toList()
                .contains(type);
    }

}
