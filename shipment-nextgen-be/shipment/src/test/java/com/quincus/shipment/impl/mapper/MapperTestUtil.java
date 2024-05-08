package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Commodity;
import com.quincus.shipment.api.domain.Consignee;
import com.quincus.shipment.api.domain.Customer;
import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.OrderAttachment;
import com.quincus.shipment.api.domain.OrderReference;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.PricingInfo;
import com.quincus.shipment.api.domain.Sender;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.CommodityEntity;
import com.quincus.shipment.impl.repository.entity.CustomerEntity;
import com.quincus.shipment.impl.repository.entity.LocationEntity;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageDimensionEntity;
import com.quincus.shipment.impl.repository.entity.PackageEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ServiceTypeEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.test_utils.TestUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.quincus.shipment.api.constant.MilestoneCode.OM_BOOKED;
import static com.quincus.shipment.impl.mapper.LocalDateMapper.toLocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

public class MapperTestUtil {

    private static final MapperTestUtil INSTANCE = new MapperTestUtil();

    private final TestUtil testUtil = TestUtil.getInstance();

    public static MapperTestUtil getInstance() {
        return INSTANCE;
    }

    PackageJourneySegmentEntity createSamplePackageJourneySegment() {
        PackageJourneySegmentEntity packageJourneySegment = new PackageJourneySegmentEntity();
        packageJourneySegment.setId("SEGMENT-1");
        packageJourneySegment.setOpsType("OPS1");
        packageJourneySegment.setType(SegmentType.LAST_MILE);
        packageJourneySegment.setStatus(SegmentStatus.PLANNED);
        packageJourneySegment.setTransportType(TransportType.GROUND);
        packageJourneySegment.setServicedBy("SERVICE-PROV-1");

        return packageJourneySegment;
    }

    ShipmentEntity createSampleShipmentEntity() {
        ShipmentEntity shipmentEntity = new ShipmentEntity();

        shipmentEntity.setId("SHP-V2-Q-001");
        shipmentEntity.setUserId("user-id-1");

        shipmentEntity.setPickUpLocation("Address - 1");
        shipmentEntity.setDeliveryLocation("Address - 2");
        shipmentEntity.setReturnLocation("Address - 3");

        shipmentEntity.setExtraCareInfo(List.of("CARE-1"));

        shipmentEntity.setInsuranceInfo(List.of("INSURANCE-1"));

        shipmentEntity.setUserId("USER-ID-1");

        shipmentEntity.setShipmentTags(List.of("SHP-TAG1", "SHP_TAG2"));

        shipmentEntity.setSender(new Sender("Sender1", "sender@example.com", "12345", "1"));

        shipmentEntity.setConsignee(new Consignee("", "Consignee9", "consignee@example.com", "67890", "1"));

        AddressEntity originAddressEntity = new AddressEntity();
        originAddressEntity.setId("ADDR-TEST-1");
        originAddressEntity.setLine1("1-1");
        originAddressEntity.setLine2("1-2");
        originAddressEntity.setLine3("1-3");
        LocationHierarchyEntity originLocationHierarchy = new LocationHierarchyEntity();
        LocationEntity originCountryLocation = new LocationEntity();
        originCountryLocation.setCode("US");
        originLocationHierarchy.setCountry(originCountryLocation);
        LocationEntity originStateLocation = new LocationEntity();
        originStateLocation.setCode("USA");
        originLocationHierarchy.setState(originStateLocation);
        LocationEntity originCityLocation = new LocationEntity();
        originCityLocation.setCode("USAA");
        originLocationHierarchy.setCity(originCityLocation);
        originAddressEntity.setLocationHierarchy(originLocationHierarchy);
        shipmentEntity.setOrigin(originAddressEntity);

        AddressEntity destinationAddressEntity = new AddressEntity();
        destinationAddressEntity.setId("ADDR-TEST-2");
        destinationAddressEntity.setLine1("2-1");
        destinationAddressEntity.setLine2("2-2");
        destinationAddressEntity.setLine3("2-3");
        LocationHierarchyEntity destinationLocationHierarchy = new LocationHierarchyEntity();
        LocationEntity destinationCountryLocation = new LocationEntity();
        destinationCountryLocation.setCode("CN");
        destinationLocationHierarchy.setCountry(destinationCountryLocation);
        LocationEntity destinationStateLocation = new LocationEntity();
        destinationStateLocation.setCode("CN");
        destinationLocationHierarchy.setState(destinationCountryLocation);
        LocationEntity destinationCityLocation = new LocationEntity();
        destinationCityLocation.setCode("CN");
        destinationLocationHierarchy.setCity(destinationCityLocation);
        destinationAddressEntity.setLocationHierarchy(destinationLocationHierarchy);
        shipmentEntity.setDestination(destinationAddressEntity);

        MilestoneEntity milestoneEvent = new MilestoneEntity();
        milestoneEvent.setId("0001");
        milestoneEvent.setMilestoneCode(OM_BOOKED);
        milestoneEvent.setMilestoneName("MILESTONE-DUMMY");
        shipmentEntity.setMilestoneEvents(Set.of(milestoneEvent));

        PackageEntity shipmentPackage = new PackageEntity();
        shipmentPackage.setId("PCK-1");
        shipmentPackage.setTotalValue(new BigDecimal("1.11"));
        shipmentPackage.setCurrency("USD");
        shipmentPackage.setType("P-TYPE1");
        shipmentPackage.setValue("12.34");
        shipmentPackage.setReadyTime(LocalDateTime.now());

        PackageDimensionEntity dimension = new PackageDimensionEntity();
        dimension.setId("DIM-01");
        dimension.setMeasurementUnit(MeasurementUnit.METRIC);
        dimension.setLength(new BigDecimal("3.01"));
        dimension.setWidth(new BigDecimal("4.02"));
        dimension.setHeight(new BigDecimal("5.03"));
        dimension.setVolumeWeight(new BigDecimal("0.11"));
        dimension.setGrossWeight(new BigDecimal("0.22"));
        dimension.setChargeableWeight(new BigDecimal("0.33"));
        dimension.setCustom(true);
        shipmentPackage.setDimension(dimension);

        CommodityEntity commodity = new CommodityEntity();
        commodity.setId("COMMODITY-1");
        commodity.setName("Perishable");
        commodity.setQuantity(5L);
        commodity.setValue(new BigDecimal("3.99"));
        shipmentPackage.setCommodities(List.of(commodity));

        PricingInfo pricingInfo = new PricingInfo();
        pricingInfo.setCurrency("PHP");
        pricingInfo.setBaseTariff(BigDecimal.valueOf(20.01));
        pricingInfo.setServiceTypeCharge(BigDecimal.valueOf(20.02));
        pricingInfo.setSurcharge(BigDecimal.valueOf(20.03));
        pricingInfo.setInsuranceCharge(BigDecimal.valueOf(20.04));
        pricingInfo.setExtraCareCharge(BigDecimal.valueOf(20.05));
        pricingInfo.setDiscount(BigDecimal.valueOf(20.06));
        pricingInfo.setTax(BigDecimal.valueOf(20.07));
        pricingInfo.setCod(BigDecimal.valueOf(20.08));

        shipmentPackage.setPricingInfo(pricingInfo);
        shipmentEntity.setShipmentPackage(shipmentPackage);

        ShipmentJourneyEntity shipmentJourney = new ShipmentJourneyEntity();
        shipmentJourney.setId("JOURNEY-1");
        shipmentJourney.setStatus(JourneyStatus.PLANNED);

        PackageJourneySegmentEntity packageJourneySegment = createSamplePackageJourneySegment();
        shipmentJourney.addAllPackageJourneySegments(List.of(packageJourneySegment));
        shipmentEntity.setShipmentJourney(shipmentJourney);

        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setId("ORG-001");
        organizationEntity.setCode("ORG-CODE1");
        organizationEntity.setName("Organization Name");
        shipmentEntity.setOrganization(organizationEntity);

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId("ORDER-0001");
        orderEntity.setGroup("ORDER-GRP1");
        orderEntity.setOrderIdLabel("ORDER-ID-LABEL1");
        orderEntity.setCustomerReferenceId(List.of("SHP-REF1"));
        orderEntity.setNotes("ORDER NOTES1");
        orderEntity.setTags(List.of("TAG1", "TAG2"));
        orderEntity.setTrackingUrl("tracking-url");
        orderEntity.setOrderReferences(createDummyOrderReferences());
        ArrayNode attachmentsJson = testUtil.getObjectMapper().createArrayNode();
        ObjectNode attachmentJson = testUtil.getObjectMapper().createObjectNode();
        OrderAttachment attachment = new OrderAttachment();
        attachment.setFileName("ATTACHMENT1.PNG");
        attachment.setFileUrl("http://example.com/attachment1.png");
        orderEntity.setAttachments(List.of(attachment));
        String orderData = "THIS IS A LONG STRING";
        orderEntity.setData(testUtil.getObjectMapper().convertValue(orderData, JsonNode.class));
        shipmentEntity.setOrder(orderEntity);

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setId("CUST-001");
        customerEntity.setCode("CUST-CODE1");
        customerEntity.setName("Customer Name");
        customerEntity.setOrganizationId(organizationEntity.getId());
        shipmentEntity.setCustomer(customerEntity);

        ServiceTypeEntity serviceTypeEntity = new ServiceTypeEntity();
        serviceTypeEntity.setId("SVC-001");
        serviceTypeEntity.setCode("SVC-CODE1");
        serviceTypeEntity.setName("Service Name");
        serviceTypeEntity.setDescription("Service Description");
        serviceTypeEntity.setOrganizationId(organizationEntity.getId());
        shipmentEntity.setServiceType(serviceTypeEntity);

        shipmentEntity.setStatus(ShipmentStatus.CREATED);
        shipmentEntity.setEtaStatus(EtaStatus.DELAYED);

        shipmentEntity.setNotes("SHIPMENT-NOTE1");

        Instruction pickUpInstruction = new Instruction();
        pickUpInstruction.setApplyTo(InstructionApplyToType.PICKUP);
        pickUpInstruction.setValue("test");

        Instruction deliveryInstruction = new Instruction();
        pickUpInstruction.setApplyTo(InstructionApplyToType.DELIVERY);
        pickUpInstruction.setValue("test");

        shipmentEntity.setInstructions(List.of(pickUpInstruction, deliveryInstruction));
        shipmentEntity.setShipmentReferenceId(List.of("UP387429834T", "FED12573336G"));

        shipmentEntity.setShipmentAttachments(List.of(new HostedFile("1",
                "Attachment_1.pdf", "https://file-attachment.example.com", null, 1000L, null)));

        shipmentEntity.setDescription("This is a description");

        shipmentEntity.setCreateTime(Instant.now());
        shipmentEntity.setModifyTime(Instant.now());

        return shipmentEntity;
    }

    ShipmentMessageDto createDummyShipmentDto() {
        ShipmentMessageDto shipmentMessageDto = new ShipmentMessageDto();

        shipmentMessageDto.setId("SHM123456");
        shipmentMessageDto.setOrderId("ORD123456");
        shipmentMessageDto.setOrganizationId("ORG12345");
        shipmentMessageDto.setShipmentReferenceId(List.of("REF123", "REF456"));
        shipmentMessageDto.setExternalOrderId("EXTORD123456");
        shipmentMessageDto.setInternalOrderId("INTORD123456");
        shipmentMessageDto.setCustomerOrderId("CUSORD123456");
        shipmentMessageDto.setOrderReferences(createDummyOrderReferences());
        shipmentMessageDto.setPackageId("PKG123456");
        shipmentMessageDto.setPackageRefId("PKGREF123456");
        shipmentMessageDto.setUserId("USR12345");
        shipmentMessageDto.setPartnerId("PRT12345");

        Sender sender = new Sender();
        shipmentMessageDto.setSender(sender);

        Consignee consignee = new Consignee();
        shipmentMessageDto.setConsignee(consignee);

        shipmentMessageDto.setOrderIdLabel("Order Label");
        shipmentMessageDto.setOrderTrackingUrl("http://track.yourorder.com/ORD123456");
        return shipmentMessageDto;
    }

    public List<OrderReference> createDummyOrderReferences() {
        OrderReference orderReference1 = new OrderReference("id1", "orderId1", "value1", "label1",
                "externalId1", "createdAt1", "updatedAt1", "deletedAt1");
        OrderReference orderReference2 = new OrderReference("id2", "orderId2", "value2", "label2",
                "externalId2", "createdAt2", "updatedAt2", "deletedAt2");
        return List.of(orderReference1, orderReference2);
    }

    void packageJourneySegmentDomainListToEntityList_commonAsserts(List<PackageJourneySegment> domainList,
                                                                   List<PackageJourneySegmentEntity> entityList) {
        assertThat(entityList).hasSameSizeAs(domainList);
        for (int i = 0; i < domainList.size(); i++) {
            PackageJourneySegment domainPjs = domainList.get(i);
            PackageJourneySegmentEntity entityPjs = entityList.get(i);
            assertThat(entityPjs.getId()).withFailMessage(String.format("Package Journey Segment [%d] ID mismatch.", i)).isEqualTo(domainPjs.getSegmentId());
            assertThat(entityPjs.getOpsType()).withFailMessage(String.format("Package Journey Segment [%d] Ops Type mismatch.", i)).isEqualTo(domainPjs.getOpsType());
            assertThat(entityPjs.getStatus()).withFailMessage(String.format("Package Journey Segment [%d] Status mismatch.", i)).isEqualTo(domainPjs.getStatus());
            assertThat(entityPjs.getTransportType()).withFailMessage(String.format("Package Journey Segment [%d] Transport Type mismatch.", i)).isEqualTo(domainPjs.getTransportType());
            assertThat(entityPjs.getServicedBy()).withFailMessage(String.format("Package Journey Segment [%d] Serviced By mismatch.", i)).isEqualTo(domainPjs.getServicedBy());
        }
    }

    void packageJourneySegmentEntityListToDomainList_commonAsserts(List<PackageJourneySegmentEntity> entityList,
                                                                   List<PackageJourneySegment> domainList) {
        assertThat(domainList).hasSameSizeAs(entityList);
        for (int i = 0; i < entityList.size(); i++) {
            PackageJourneySegmentEntity entityPjs = entityList.get(i);
            PackageJourneySegment domainPjs = domainList.get(i);
            assertThat(domainPjs.getOrganizationId()).withFailMessage(String.format("Organization [%d] ID mismatch.", i)).isEqualTo(entityPjs.getOrganizationId());
            assertThat(domainPjs.getSegmentId()).withFailMessage(String.format("Package Journey Segment [%d] ID mismatch.", i)).isEqualTo(entityPjs.getId());
            assertThat(domainPjs.getOpsType()).withFailMessage(String.format("Package Journey Segment [%d] Ops Type mismatch.", i)).isEqualTo(entityPjs.getOpsType());
            assertThat(domainPjs.getStatus()).withFailMessage(String.format("Package Journey Segment [%d] Status mismatch.", i)).isEqualTo(entityPjs.getStatus());
            assertThat(domainPjs.getTransportType()).withFailMessage(String.format("Package Journey Segment [%d] Transport Type mismatch.", i)).isEqualTo(entityPjs.getTransportType());
            assertThat(domainPjs.getServicedBy()).withFailMessage(String.format("Package Journey Segment [%d] Serviced By mismatch.", i)).isEqualTo(entityPjs.getServicedBy());
        }
    }

    public void shipmentDomainToEntity_commonAsserts(Shipment domain, ShipmentEntity entity) {

        assertThat(entity.getId())
                .withFailMessage("Shipment ID mismatch.")
                .isEqualTo(domain.getId());

        Order domainOrder = domain.getOrder();
        OrderEntity entityOrder = entity.getOrder();

        assertThat(entity.getId())
                .withFailMessage("Order ID mismatch.")
                .isEqualTo(domain.getId());

        assertThat(entityOrder.getId())
                .withFailMessage("Order ID mismatch.")
                .isEqualTo(domainOrder.getId());
        assertThat(entityOrder.getGroup())
                .withFailMessage("Order Group mismatch.")
                .isEqualTo(domainOrder.getGroup());
        assertThat(entityOrder.getOrderIdLabel())
                .withFailMessage("Order ID Label mismatch.")
                .isEqualTo(domainOrder.getOrderIdLabel());
        assertThat(entityOrder.getTrackingUrl())
                .withFailMessage("Tracking URL mismatch.")
                .isEqualTo(domainOrder.getTrackingUrl());
        assertThat(entityOrder.getNotes())
                .withFailMessage("Order Notes mismatch.")
                .isEqualTo(domainOrder.getNotes());


        String[] entityOrderTags = testUtil.getObjectMapper().convertValue(entityOrder.getTags(), String[].class);
        List<String> entityOrderTagList = Arrays.asList(entityOrderTags);
        assertThat(domainOrder.getTags())
                .withFailMessage("Order Tags mismatch.")
                .isEqualTo(entityOrderTagList);

        List<OrderAttachment> entityAttachmentsJson = entityOrder.getAttachments();
        assertThat(entityAttachmentsJson)
                .withFailMessage("Order Attachments not mapped.")
                .isNotEmpty();
        for (int i = 0; i < entityAttachmentsJson.size(); i++) {
            OrderAttachment domainAttachment = domainOrder.getAttachments().get(i);
            OrderAttachment entityAttachment = entityAttachmentsJson.get(i);

            assertThat(domainAttachment.getId())
                    .withFailMessage(String.format("Order Attachment [%d] ID mismatch.", i))
                    .isEqualTo(entityAttachment.getId());
            assertThat(domainAttachment.getFileName())
                    .withFailMessage(String.format("Order Attachment [%d] File Name mismatch.", i))
                    .isEqualTo(entityAttachment.getFileName());
            assertThat(domainAttachment.getFileSize())
                    .withFailMessage(String.format("Order Attachment [%d] File Size mismatch.", i))
                    .isEqualTo(entityAttachment.getFileSize());
            assertThat(domainAttachment.getFileUrl())
                    .withFailMessage(String.format("Order Attachment [%d] File Url mismatch.", i))
                    .isEqualTo(entityAttachment.getFileUrl());
        }

        OrderAttachment domainAttachment = domainOrder.getAttachments().get(0);
        OrderAttachment entityAttachmentJson = entityAttachmentsJson.get(0);
        assertThat(entityAttachmentJson.getFileName())
                .withFailMessage("Order Attachment File Name mismatch.")
                .isEqualTo(domainAttachment.getFileName());

        assertThat(entityAttachmentJson.getFileUrl())
                .withFailMessage("Order Attachment File Url mismatch.")
                .isEqualTo(domainAttachment.getFileUrl());

        String domainOrderCustomerRefId = domainOrder.getCustomerReferenceId().get(0);
        String entityOrderCustomerRefId = entityOrder.getCustomerReferenceId().get(0);

        assertThat(entityOrderCustomerRefId)
                .withFailMessage("Order Customer Reference ID mismatch.")
                .isEqualTo(domainOrderCustomerRefId);


        Sender domainSender = domain.getSender();
        Sender entitySender = entity.getSender();

        assertThat(entitySender.getName())
                .withFailMessage("Sender Name mismatch.")
                .isEqualTo(domainSender.getName());
        assertThat(entitySender.getEmail())
                .withFailMessage("Sender Email mismatch.")
                .isEqualTo(domainSender.getEmail());
        assertThat(entitySender.getContactNumber())
                .withFailMessage("Sender Contact Number mismatch.")
                .isEqualTo(domainSender.getContactNumber());

        Consignee domainConsignee = domain.getConsignee();
        Consignee entityConsignee = entity.getConsignee();

        assertThat(entityConsignee.getName())
                .withFailMessage("Consignee Name mismatch.")
                .isEqualTo(domainConsignee.getName());

        assertThat(entityConsignee.getEmail())
                .withFailMessage("Consignee Email mismatch.")
                .isEqualTo(domainConsignee.getEmail());

        assertThat(entityConsignee.getContactNumber())
                .withFailMessage("Consignee Contact Number mismatch.")
                .isEqualTo(domainConsignee.getContactNumber());

        assertThat(entity.getPickUpLocation())
                .withFailMessage("Pick-up Location mismatch.")
                .isEqualTo(domain.getPickUpLocation());

        assertThat(entity.getDeliveryLocation())
                .withFailMessage("Delivery Location mismatch.")
                .isEqualTo(domain.getDeliveryLocation());


        ServiceType domainServiceType = domain.getServiceType();
        ServiceTypeEntity entityServiceType = entity.getServiceType();

        assertThat(entityServiceType.getId())
                .withFailMessage("Service Type ID mismatch.")
                .isEqualTo(domainServiceType.getId());

        assertThat(entityServiceType.getCode())
                .withFailMessage("Service Type Code mismatch.")
                .isEqualTo(domainServiceType.getCode());

        assertThat(entityServiceType.getName())
                .withFailMessage("Service Type Name mismatch.")
                .isEqualTo(domainServiceType.getName());

        assertThat(entityServiceType.getOrganizationId()).isNotNull();

        assertThat(entity.getUserId())
                .withFailMessage("User ID mismatch.")
                .isEqualTo(domain.getUserId());

        Organization domainOrganization = domain.getOrganization();
        OrganizationEntity entityOrganization = entity.getOrganization();

        assertThat(entityOrganization.getId()).isEqualTo(domainOrganization.getId());
        assertThat(entityOrganization.getName()).isEqualTo(domainOrganization.getName());
        assertThat(entityOrganization.getCode()).isEqualTo(domainOrganization.getCode());

        Package domainPackage = domain.getShipmentPackage();
        PackageEntity entityPackage = entity.getShipmentPackage();

        assertThat(entityPackage.getId()).isEqualTo(domainPackage.getId());
        assertThat(entityPackage.getType()).isEqualTo(domainPackage.getType());
        assertThat(entityPackage.getCurrency()).isEqualTo(domainPackage.getCurrency());
        assertThat(entityPackage.getTotalValue()).isEqualTo(domainPackage.getTotalValue());
        assertThat(entityPackage.getValue()).isEqualTo(domainPackage.getValue());
        assertThat(entityPackage.getReadyTime()).isEqualTo(domainPackage.getReadyTime());

        PackageDimension domainDimension = domainPackage.getDimension();
        PackageDimensionEntity entityDimension = entityPackage.getDimension();
        assertThat(entityDimension.getMeasurementUnit()).isEqualTo(domainDimension.getMeasurementUnit());
        assertThat(entityDimension.getLength()).isEqualTo(domainDimension.getLength());
        assertThat(entityDimension.getWidth()).isEqualTo(domainDimension.getWidth());
        assertThat(entityDimension.getHeight()).isEqualTo(domainDimension.getHeight());
        assertThat(entityDimension.getVolumeWeight()).isEqualTo(domainDimension.getVolumeWeight());
        assertThat(entityDimension.getGrossWeight()).isEqualTo(domainDimension.getGrossWeight());
        assertThat(entityDimension.getChargeableWeight()).isEqualTo(domainDimension.getChargeableWeight());
        assertThat(entityDimension.isCustom()).isEqualTo(domainDimension.isCustom());

        List<Commodity> domainCommodityList = domainPackage.getCommodities();
        List<CommodityEntity> entityCommodityList = entityPackage.getCommodities();
        assertThat(entityCommodityList).hasSameSizeAs(domainCommodityList);
        for (int i = 0; i < domainCommodityList.size(); i++) {
            Commodity domainCommodity = domainCommodityList.get(i);
            CommodityEntity entityCommodity = entityCommodityList.get(i);
            assertThat(entityCommodity.getName()).withFailMessage(String.format("Package Commodity [%d] Name mismatch.", i)).isEqualTo(domainCommodity.getName());
            assertThat(entityCommodity.getQuantity()).withFailMessage(String.format("Package Commodity [%d] Quantity mismatch.", i)).isEqualTo(domainCommodity.getQuantity());
            assertThat(entityCommodity.getValue()).withFailMessage(String.format("Package Commodity [%d] Value mismatch.", i)).isEqualTo(domainCommodity.getValue());
        }

        PricingInfo domainPricingInfo = domainPackage.getPricingInfo();
        PricingInfo entityPricingInfoJson = entityPackage.getPricingInfo();
        assertThat(domainPricingInfo.getBaseTariff())
                .withFailMessage("Pricing Info Base Tariff mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getBaseTariff());

        assertThat(domainPricingInfo.getServiceTypeCharge())
                .withFailMessage("Pricing Info Service Type Charge mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getServiceTypeCharge());

        assertThat(domainPricingInfo.getSurcharge())
                .withFailMessage("Pricing Info Surcharge mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getSurcharge());

        assertThat(domainPricingInfo.getExtraCareCharge())
                .withFailMessage("Pricing Info Extra Care Charge mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getExtraCareCharge());

        assertThat(domainPricingInfo.getDiscount())
                .withFailMessage("Pricing Info Discount mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getDiscount());

        assertThat(domainPricingInfo.getTax())
                .withFailMessage("Pricing Info Tax mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getTax());

        assertThat(domainPricingInfo.getCod())
                .withFailMessage("Pricing Info C.O.D mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getCod());

        assertThat(entity.getReturnLocation())
                .withFailMessage("Return Location mismatch.")
                .isEqualTo(domain.getReturnLocation());


        List<String> domainExtraCareInfoList = domain.getExtraCareInfo();
        List<String> entityExtraCareInfoList = entity.getExtraCareInfo();
        assertThat(entityExtraCareInfoList).withFailMessage("Extra Care Info Sizes mismatch.").hasSameSizeAs(domainExtraCareInfoList);
        for (int i = 0; i < domainExtraCareInfoList.size(); i++) {
            assertThat(entityExtraCareInfoList.get(i))
                    .withFailMessage(String.format("Extra Care Info [%d] mismatch.", i))
                    .isEqualTo(domainExtraCareInfoList.get(i));
        }

        List<String> domainInsuranceInfoList = domain.getInsuranceInfo();
        List<String> entityInsuranceInfoList = entity.getInsuranceInfo();
        assertThat(entityInsuranceInfoList).withFailMessage("Insurance Info Sizes mismatch.").hasSameSizeAs(domainInsuranceInfoList);
        for (int i = 0; i < domainInsuranceInfoList.size(); i++) {
            assertThat(entityInsuranceInfoList.get(i))
                    .withFailMessage(String.format("Extra Care Info [%d] mismatch.", i))
                    .isEqualTo(domainInsuranceInfoList.get(i));
        }

        ShipmentJourney domainJourney = domain.getShipmentJourney();
        ShipmentJourneyEntity entityJourney = entity.getShipmentJourney();
        assertThat(entityJourney.getId()).withFailMessage("Journey ID mismatch.").isEqualTo(domainJourney.getJourneyId());
        assertThat(entityJourney.getStatus()).withFailMessage("Journey Status mismatch.").isEqualTo(domainJourney.getStatus());

        List<PackageJourneySegment> domainPjsList = domainJourney.getPackageJourneySegments();
        List<PackageJourneySegmentEntity> entityPjsList = entityJourney.getPackageJourneySegments();
        packageJourneySegmentDomainListToEntityList_commonAsserts(domainPjsList, entityPjsList);

        Address domainOrigin = domain.getOrigin();
        AddressEntity entityOrigin = entity.getOrigin();

        assertThat(entityOrigin.getId()).withFailMessage("Origin ID was also mapped.").isNull();
        assertThat(entityOrigin.getFullAddress()).withFailMessage("Origin full address mismatch.").isEqualTo(domainOrigin.getFullAddress());
        assertThat(entityOrigin.getCompany()).withFailMessage("Origin company mismatch.").isEqualTo(domainOrigin.getCompany());
        assertThat(entityOrigin.getDepartment()).withFailMessage("Origin department mismatch.").isEqualTo(domainOrigin.getDepartment());
        assertThat(entityOrigin.getPostalCode()).withFailMessage("Origin postal code mismatch.").isEqualTo(domainOrigin.getPostalCode());
        assertThat(entityOrigin.getLatitude()).withFailMessage("Origin latitude mismatch.").isEqualTo(domainOrigin.getLatitude());
        assertThat(entityOrigin.getLongitude()).withFailMessage("Origin longitude mismatch.").isEqualTo(domainOrigin.getLongitude());
        assertThat(entityOrigin.getManualCoordinates()).withFailMessage("Origin manual coordinates mismatch.").isEqualTo(domainOrigin.isManualCoordinates());
        assertThat(entityOrigin.getLine1()).withFailMessage("Origin Line 1 mismatch.").isEqualTo(domainOrigin.getLine1());
        assertThat(entityOrigin.getLine2()).withFailMessage("Origin Line 2 mismatch.").isEqualTo(domainOrigin.getLine2());
        assertThat(entityOrigin.getLine3()).withFailMessage("Origin Line 3 mismatch.").isEqualTo(domainOrigin.getLine3());

        Address domainDestination = domain.getDestination();
        AddressEntity entityDestination = entity.getDestination();

        assertThat(entityDestination.getId()).withFailMessage("Destination ID was also mapped.").isNull();
        assertThat(entityDestination.getFullAddress()).withFailMessage("Destination full address mismatch.").isEqualTo(domainDestination.getFullAddress());
        assertThat(entityDestination.getCompany()).withFailMessage("Destination company mismatch.").isEqualTo(domainDestination.getCompany());
        assertThat(entityDestination.getDepartment()).withFailMessage("Destination department mismatch.").isEqualTo(domainDestination.getDepartment());
        assertThat(entityDestination.getPostalCode()).withFailMessage("Destination postal code mismatch.").isEqualTo(domainDestination.getPostalCode());
        assertThat(entityDestination.getLatitude()).withFailMessage("Destination latitude mismatch.").isEqualTo(domainDestination.getLatitude());
        assertThat(entityDestination.getLongitude()).withFailMessage("Destination longitude mismatch.").isEqualTo(domainDestination.getLongitude());
        assertThat(entityDestination.getManualCoordinates()).withFailMessage("Destination manual coordinates mismatch.").isEqualTo(domainDestination.isManualCoordinates());
        assertThat(entityDestination.getLine1()).withFailMessage("Destination Line 1 mismatch.").isEqualTo(domainDestination.getLine1());
        assertThat(entityDestination.getLine2()).withFailMessage("Destination Line 2 mismatch.").isEqualTo(domainDestination.getLine2());
        assertThat(entityDestination.getLine3()).withFailMessage("Destination Line 3 mismatch.").isEqualTo(domainDestination.getLine3());

        Customer domainCustomer = domain.getCustomer();
        CustomerEntity entityCustomer = entity.getCustomer();
        assertThat(entityCustomer.getId()).withFailMessage("Customer ID mismatch.").isEqualTo(domainCustomer.getId());
        assertThat(entityCustomer.getCode()).withFailMessage("Customer Code mismatch.").isEqualTo(domainCustomer.getCode());
        assertThat(entityCustomer.getName()).withFailMessage("Customer Name mismatch.").isEqualTo(domainCustomer.getName());
        assertThat(entityCustomer.getOrganizationId()).withFailMessage("Customer Org ID mismatch.").isEqualTo(domainCustomer.getOrganizationId());

        assertThat(entity.getStatus()).withFailMessage("Shipment Status mismatch.").isEqualTo(domain.getStatus());
        assertThat(entity.getEtaStatus()).withFailMessage("Shipment ETA Status mismatch.").isEqualTo(domain.getEtaStatus());
        assertThat(entity.getPartnerId()).withFailMessage("Shipment Partner Id mismatch.").isEqualTo(domain.getPartnerId());
        assertThat(entity.getNotes()).withFailMessage("Shipment Notes mismatch.").isEqualTo(domain.getNotes());
        assertThat(entity.getInstructions()).withFailMessage("Instruction mismatch.").hasSameSizeAs(domain.getInstructions());
        assertThat(entity.getShipmentReferenceId()).isEqualTo(domain.getShipmentReferenceId());
        assertThat(entity.getDescription()).isEqualTo(domain.getDescription());
    }

    void shipmentEntityToDomain_commonAsserts(ShipmentEntity entity, Shipment domain) {

        assertThat(domain.getShipmentTrackingId()).isEqualTo(entity.getShipmentTrackingId());
        assertThat(domain.getPickUpLocation()).isEqualTo(entity.getPickUpLocation());
        assertThat(domain.getDeliveryLocation()).isEqualTo(entity.getDeliveryLocation());
        assertThat(domain.getReturnLocation()).isEqualTo(entity.getReturnLocation());

        List<String> entityExtraCareInfoList = entity.getExtraCareInfo();
        List<String> domainExtraCareInfoList = domain.getExtraCareInfo();

        assertThat(domainExtraCareInfoList).hasSameSizeAs(entityExtraCareInfoList);
        for (int i = 0; i < entityExtraCareInfoList.size(); i++) {
            assertThat(domainExtraCareInfoList.get(i))
                    .withFailMessage(String.format("Extra Care Info [%d] mismatch.", i))
                    .isEqualTo(entityExtraCareInfoList.get(i));
        }

        List<String> entityInsuranceInfoList = entity.getInsuranceInfo();
        List<String> domainInsuranceInfoList = domain.getInsuranceInfo();
        assertThat(domainInsuranceInfoList).hasSameSizeAs(entityInsuranceInfoList);
        for (int i = 0; i < entityInsuranceInfoList.size(); i++) {
            assertThat(domainInsuranceInfoList.get(i))
                    .withFailMessage(String.format("Insurance Info [%d] mismatch.", i))
                    .isEqualTo(entityInsuranceInfoList.get(i));
        }

        ServiceTypeEntity entityServiceType = entity.getServiceType();
        ServiceType domainServiceType = domain.getServiceType();
        assertThat(domainServiceType.getId()).isEqualTo(entityServiceType.getId());
        assertThat(domainServiceType.getCode()).isEqualTo(entityServiceType.getCode());
        assertThat(domainServiceType.getName()).isEqualTo(entityServiceType.getName());
        assertThat(domainServiceType.getOrganizationId()).isNotNull();

        assertThat(domain.getUserId()).isEqualTo(entity.getUserId());

        Sender entitySender = entity.getSender();
        Sender domainSender = domain.getSender();

        assertThat(domainSender.getName()).isEqualTo(entitySender.getName());
        assertThat(domainSender.getEmail()).isEqualTo(entitySender.getEmail());
        assertThat(domainSender.getContactNumber()).isEqualTo(entitySender.getContactNumber());

        Consignee entityConsignee = entity.getConsignee();
        Consignee domainConsignee = domain.getConsignee();

        assertThat(domainConsignee.getName()).isEqualTo(entityConsignee.getName());
        assertThat(domainConsignee.getEmail()).isEqualTo(entityConsignee.getEmail());
        assertThat(domainConsignee.getContactNumber()).isEqualTo(entityConsignee.getContactNumber());

        AddressEntity entityOrigin = entity.getOrigin();
        Address domainOrigin = domain.getOrigin();
        assertThat(domainOrigin.getId()).withFailMessage("Origin ID was also mapped.").isNull();
        assertThat(domainOrigin.getLine1()).isEqualTo(entityOrigin.getLine1());
        assertThat(domainOrigin.getLine2()).isEqualTo(entityOrigin.getLine2());
        assertThat(domainOrigin.getLine3()).isEqualTo(entityOrigin.getLine3());

        AddressEntity entityDestination = entity.getDestination();
        Address domainDestination = domain.getDestination();
        assertThat(domainDestination.getId()).withFailMessage("Destination ID was also mapped.").isNull();
        assertThat(domainDestination.getLine1()).isEqualTo(entityDestination.getLine1());
        assertThat(domainDestination.getLine2()).isEqualTo(entityDestination.getLine2());
        assertThat(domainDestination.getLine3()).isEqualTo(entityDestination.getLine3());

        PackageEntity entityPackage = entity.getShipmentPackage();
        Package domainPackage = domain.getShipmentPackage();
        assertThat(domainPackage.getId()).isEqualTo(entityPackage.getId());
        assertThat(domainPackage.getTotalValue()).isEqualTo(entityPackage.getTotalValue());
        assertThat(domainPackage.getCurrency()).isEqualTo(entityPackage.getCurrency());
        assertThat(domainPackage.getType()).isEqualTo(entityPackage.getType());
        assertThat(domainPackage.getValue()).isEqualTo(entityPackage.getValue());
        assertThat(domainPackage.getReadyTime()).isEqualTo(entityPackage.getReadyTime());

        PackageDimensionEntity entityDimension = entityPackage.getDimension();
        PackageDimension domainDimension = domainPackage.getDimension();

        assertThat(entityDimension.getMeasurementUnit()).isEqualTo(domainDimension.getMeasurementUnit());
        assertThat(entityDimension.getLength()).isEqualTo(domainDimension.getLength());
        assertThat(entityDimension.getWidth()).isEqualTo(domainDimension.getWidth());
        assertThat(entityDimension.getHeight()).isEqualTo(domainDimension.getHeight());
        assertThat(entityDimension.getVolumeWeight()).isEqualTo(domainDimension.getVolumeWeight());
        assertThat(entityDimension.getGrossWeight()).isEqualTo(domainDimension.getGrossWeight());
        assertThat(entityDimension.getChargeableWeight()).isEqualTo(domainDimension.getChargeableWeight());
        assertThat(entityDimension.isCustom()).isEqualTo(domainDimension.isCustom());

        List<CommodityEntity> entityCommodityList = entityPackage.getCommodities();
        List<Commodity> domainCommodityList = domainPackage.getCommodities();
        assertThat(domainCommodityList).hasSameSizeAs(entityCommodityList);
        for (int i = 0; i < entityCommodityList.size(); i++) {
            CommodityEntity entityCommodity = entityCommodityList.get(i);
            Commodity domainCommodity = domainCommodityList.get(i);

            assertThat(entityCommodity.getName())
                    .withFailMessage(String.format("Package Commodity [%d] Name mismatch.", i))
                    .isEqualTo(domainCommodity.getName());
            assertThat(entityCommodity.getQuantity())
                    .withFailMessage(String.format("Package Commodity [%d] Quantity mismatch.", i))
                    .isEqualTo(domainCommodity.getQuantity());
            assertThat(entityCommodity.getValue())
                    .withFailMessage(String.format("Package Commodity [%d] Value mismatch.", i))
                    .isEqualTo(domainCommodity.getValue());
        }

        PricingInfo domainPricingInfo = domainPackage.getPricingInfo();
        PricingInfo entityPricingInfoJson = entityPackage.getPricingInfo();

        assertThat(domainPricingInfo.getBaseTariff())
                .withFailMessage("Pricing Info Base Tariff mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getBaseTariff());

        assertThat(domainPricingInfo.getServiceTypeCharge())
                .withFailMessage("Pricing Info Service Type Charge mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getServiceTypeCharge());

        assertThat(domainPricingInfo.getSurcharge())
                .withFailMessage("Pricing Info Surcharge mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getSurcharge());

        assertThat(domainPricingInfo.getInsuranceCharge())
                .withFailMessage("Pricing Info Insurance Charge mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getInsuranceCharge());

        assertThat(domainPricingInfo.getExtraCareCharge())
                .withFailMessage("Pricing Info Extra Care Charge mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getExtraCareCharge());

        assertThat(domainPricingInfo.getDiscount())
                .withFailMessage("Pricing Info Discount mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getDiscount());

        assertThat(domainPricingInfo.getTax())
                .withFailMessage("Pricing Info Tax mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getTax());

        assertThat(domainPricingInfo.getCod())
                .withFailMessage("Pricing Info C.O.D mismatch.")
                .isEqualByComparingTo(entityPricingInfoJson.getCod());

        ShipmentJourneyEntity entityJourney = entity.getShipmentJourney();
        ShipmentJourney domainJourney = domain.getShipmentJourney();
        assertThat(domainJourney.getJourneyId()).isEqualTo(entityJourney.getId());
        assertThat(domainJourney.getStatus()).isEqualTo(entityJourney.getStatus());

        List<PackageJourneySegmentEntity> entityPjsList = entityJourney.getPackageJourneySegments();
        List<PackageJourneySegment> domainPjsList = domainJourney.getPackageJourneySegments();
        packageJourneySegmentEntityListToDomainList_commonAsserts(entityPjsList, domainPjsList);

        OrganizationEntity entityOrg = entity.getOrganization();
        Organization domainOrg = domain.getOrganization();
        assertThat(domainOrg.getId()).isEqualTo(entityOrg.getId());
        assertThat(domainOrg.getCode()).isEqualTo(entityOrg.getCode());
        assertThat(domainOrg.getName()).isEqualTo(entityOrg.getName());

        OrderEntity entityOrder = entity.getOrder();
        Order domainOrder = domain.getOrder();
        assertThat(domainOrder.getId()).isEqualTo(entityOrder.getId());
        assertThat(domainOrder.getGroup()).isEqualTo(entityOrder.getGroup());
        assertThat(domainOrder.getOrderIdLabel()).isEqualTo(entityOrder.getOrderIdLabel());
        assertThat(domainOrder.getTrackingUrl()).isEqualTo(entityOrder.getTrackingUrl());
        assertThat(domainOrder.getCustomerReferenceId().get(0)).isEqualTo(entityOrder.getCustomerReferenceId().get(0));
        assertThat(domainOrder.getNotes()).isEqualTo(entityOrder.getNotes());

        String[] entityOrderTags = testUtil.getObjectMapper().convertValue(entityOrder.getTags(), String[].class);
        List<String> entityOrderTagList = Arrays.asList(entityOrderTags);
        assertThat(domainOrder.getTags()).containsAll(entityOrderTagList);

        List<OrderAttachment> entityAttachmentsJson = entityOrder.getAttachments();
        for (int i = 0; i < entityAttachmentsJson.size(); i++) {
            OrderAttachment entityAttachment = entityAttachmentsJson.get(i);
            OrderAttachment domainAttachment = domainOrder.getAttachments().get(i);
            assertThat(domainAttachment.getFileName()).withFailMessage(String.format("Order Attachment [%d] File Name mismatch.", i))
                    .isEqualTo(entityAttachment.getFileName());
            assertThat(domainAttachment.getFileUrl()).withFailMessage(String.format("Order Attachment [%d] File Url mismatch.", i))
                    .isEqualTo(entityAttachment.getFileUrl());
        }

        CustomerEntity entityCustomer = entity.getCustomer();
        Customer domainCustomer = domain.getCustomer();

        assertThat(domainCustomer.getId()).isEqualTo(entityCustomer.getId());
        assertThat(domainCustomer.getCode()).isEqualTo(entityCustomer.getCode());
        assertThat(domainCustomer.getName()).isEqualTo(entityCustomer.getName());
        assertThat(domainCustomer.getOrganizationId()).isEqualTo(entityCustomer.getOrganizationId());

        assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        assertThat(domain.getEtaStatus()).isEqualTo(entity.getEtaStatus());
        assertThat(domain.getPartnerId()).isEqualTo(entity.getPartnerId());
        assertThat(domain.getNotes()).isEqualTo(entity.getNotes());
        assertThat(domain.getInstructions()).isEqualTo(entity.getInstructions());
        assertThat(domain.getShipmentReferenceId()).isEqualTo(entity.getShipmentReferenceId());
        assertThat(domain.getDescription()).isEqualTo(entity.getDescription());

        assertThat(domain.getCreatedTime()).isEqualTo(toLocalDateTime(entity.getCreateTime()));
        assertThat(domain.getLastUpdatedTime()).isEqualTo(toLocalDateTime(entity.getModifyTime()));
    }
}
