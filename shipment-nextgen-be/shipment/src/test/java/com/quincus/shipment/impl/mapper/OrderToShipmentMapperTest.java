package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quincus.ext.DateTimeUtil;
import com.quincus.order.api.domain.Attachment;
import com.quincus.order.api.domain.CommoditiesPackage;
import com.quincus.order.api.domain.Commodity;
import com.quincus.order.api.domain.Consignee;
import com.quincus.order.api.domain.CustomerReference;
import com.quincus.order.api.domain.Destination;
import com.quincus.order.api.domain.Instruction;
import com.quincus.order.api.domain.Origin;
import com.quincus.order.api.domain.Package;
import com.quincus.order.api.domain.Packaging;
import com.quincus.order.api.domain.Root;
import com.quincus.order.api.domain.SegmentsPayload;
import com.quincus.order.api.domain.Shipper;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.OrderAttachment;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.config.ShipmentJourneyCreationProperties;
import com.quincus.shipment.impl.converter.ShipmentOrderMessageConverter;
import com.quincus.shipment.impl.helper.journey.PackageJourneySegmentTypeAssigner;
import com.quincus.shipment.impl.helper.journey.ShipmentJourneyProvider;
import com.quincus.shipment.impl.helper.journey.generator.OrderMessageShipmentJourneyGenerator;
import com.quincus.shipment.impl.helper.journey.generator.ShipmentJourneyGenerator;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.impl.validator.RootOrderValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.quincus.shipment.impl.mapper.OrderToShipmentMapper.mapOrderMessageToShipmentDomain;
import static com.quincus.shipment.impl.mapper.OrderToShipmentMapper.mapOrderMessageToShipmentOrder;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrderToShipmentMapperTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    private final MapperTestUtil mapperTestUtil = MapperTestUtil.getInstance();
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private ShipmentJourneyCreationProperties shipmentJourneyCreationProperties;
    @Mock
    private RootOrderValidator rootOrderValidator;

    @Test
    void mapOrderMessageToShipmentDomain_validParameters_shouldReturnShipmentDomain() {
        Package shipmentPackageMessage = new Package();
        String orderIdLabel = "123";
        Root orderMessage = new Root();

        orderMessage.setUserId("USERID_TEST");
        orderMessage.setCustomerReferences(new ArrayList<>());
        orderMessage.setOrganisationId("Org 1");
        orderMessage.setInternalOrderId(orderIdLabel);
        orderMessage.setExternalOrderId("456");
        orderMessage.setCustomerOrderId("789");
        orderMessage.setOrderIdLabel(orderIdLabel);
        Origin origin = new Origin();
        origin.setId("originid");
        origin.setCountry("US");
        origin.setState("New York");
        origin.setCity("New York City");
        Destination destination = new Destination();
        destination.setId("destinationid");
        destination.setCountry("Japan");
        destination.setState("Kanto");
        destination.setCity("Tokyo");
        orderMessage.setOrigin(origin);
        orderMessage.setDestination(destination);

        Shipper shipper = new Shipper();
        shipper.setName("SHIPPER NAME");
        shipper.setEmail("shipper@email.com");
        shipper.setPhone("123456789");
        orderMessage.setShipper(shipper);

        Consignee consignee = new Consignee();
        consignee.setName("SHIPPER NAME");
        consignee.setEmail("shipper@email.com");
        consignee.setPhone("123456789");
        orderMessage.setConsignee(consignee);

        shipmentPackageMessage.setMeasurementUnits("metric");
        shipmentPackageMessage.setAllTagsList(Arrays.asList("tag1", "tag2"));

        Shipment shipmentDomain = mapOrderMessageToShipmentDomain(shipmentPackageMessage, orderMessage);

        assertThat(shipmentDomain.getUserId()).isEqualTo(orderMessage.getUserId());

        Origin orderOrigin = orderMessage.getOrigin();
        Address shipmentOrigin = shipmentDomain.getOrigin();

        assertThat(shipmentOrigin.getCountryName()).withFailMessage("Origin Country mismatch").isEqualTo(orderOrigin.getCountry());
        assertThat(shipmentOrigin.getStateName()).withFailMessage("Origin State mismatch").isEqualTo(orderOrigin.getState());
        assertThat(shipmentOrigin.getCityName()).withFailMessage("Origin City mismatch").isEqualTo(orderOrigin.getCity());

        Destination orderDestination = orderMessage.getDestination();
        Address shipmentDestination = shipmentDomain.getDestination();

        assertThat(shipmentDestination.getCountryName()).withFailMessage("Destination Country mismatch").isEqualTo(orderDestination.getCountry());
        assertThat(shipmentDestination.getStateName()).withFailMessage("Destination State mismatch").isEqualTo(orderDestination.getState());
        assertThat(shipmentDestination.getCityName()).withFailMessage("Destination City mismatch").isEqualTo(orderDestination.getCity());

        assertThat(shipmentDomain.getShipmentTags()).containsAll(Arrays.asList("tag1", "tag2"));

        assertThat(shipmentDomain.getInternalOrderId()).isEqualTo(orderMessage.getInternalOrderId());
        assertThat(shipmentDomain.getExternalOrderId()).isEqualTo(orderMessage.getExternalOrderId());
        assertThat(shipmentDomain.getCustomerOrderId()).isEqualTo(orderMessage.getCustomerOrderId());

        assertThat(shipmentDomain.getShipmentPackage().getSource()).isEqualTo(TriggeredFrom.OM);
    }

    @Test
    void mapCustomerReferenceIDsToShipmentDomain_shouldReturnShipmentDomainWithCustomerReferenceIDs() {
        Package shipmentPackageMessage = new Package();
        Root orderMessage = new Root();
        Origin origin = new Origin();
        origin.setId("originid");
        origin.setCountry("US");
        origin.setState("New York");
        origin.setCity("New York City");
        Destination destination = new Destination();
        destination.setId("destinationid");
        destination.setCountry("Japan");
        destination.setState("Kanto");
        destination.setCity("Tokyo");
        orderMessage.setPickupStartTime("2022-12-08 00:00:00 GMT+08:00");
        orderMessage.setPickupCommitTime("2022-12-08 02:00:00 GMT+08:00");
        orderMessage.setDeliveryStartTime("2022-12-10 23:00:00 GMT+01:00");
        orderMessage.setDeliveryCommitTime("2022-12-11 00:00:00 GMT+01:00");
        orderMessage.setOrigin(origin);
        orderMessage.setDestination(destination);
        orderMessage.setUserId("USERID_TEST");
        orderMessage.setId("ID_TEST");
        orderMessage.setStatus("STATUS_TEST");
        orderMessage.setCustomerReferences(new ArrayList<>(Arrays.asList(
                createCustomerReference("CUSTOMER_REF_ID_LABEL_TEST1"),
                createCustomerReference("CUSTOMER_REF_ID_LABEL_TEST2"))));

        Shipper shipper = new Shipper();
        shipper.setName("SHIPPER NAME");
        shipper.setEmail("shipper@email.com");
        shipper.setPhone("123456789");
        orderMessage.setShipper(shipper);

        Consignee consignee = new Consignee();
        consignee.setName("SHIPPER NAME");
        consignee.setEmail("shipper@email.com");
        consignee.setPhone("123456789");
        orderMessage.setConsignee(consignee);
        shipmentPackageMessage.setMeasurementUnits("metric");
        orderMessage.setInstructions(createInstructions());

        Shipment shipmentDomain = mapOrderMessageToShipmentDomain(shipmentPackageMessage, orderMessage);

        assertThat(shipmentDomain.getUserId()).isEqualTo(orderMessage.getUserId());

        Origin orderOrigin = orderMessage.getOrigin();
        Address shipmentOrigin = shipmentDomain.getOrigin();

        assertThat(shipmentOrigin.getCountryName()).withFailMessage("Origin Country mismatch").isEqualTo(orderOrigin.getCountry());
        assertThat(shipmentOrigin.getStateName()).withFailMessage("Origin State mismatch").isEqualTo(orderOrigin.getState());
        assertThat(shipmentOrigin.getCityName()).withFailMessage("Origin City mismatch").isEqualTo(orderOrigin.getCity());

        Destination orderDestination = orderMessage.getDestination();
        Address shipmentDestination = shipmentDomain.getDestination();

        assertThat(shipmentDestination.getCountryName()).withFailMessage("Destination Country mismatch").isEqualTo(orderDestination.getCountry());
        assertThat(shipmentDestination.getStateName()).withFailMessage("Destination State mismatch").isEqualTo(orderDestination.getState());
        assertThat(shipmentDestination.getCityName()).withFailMessage("Destination City mismatch").isEqualTo(orderDestination.getCity());
    }

    @Test
    void mapValueOfGoodsToShipmentDomain_shouldReturnShipmentDomainWithValueOfGoods() {
        Package shipmentPackageMessage = createPackage(BigDecimal.valueOf(1000.75));
        Root orderMessage = new Root();
        Origin origin = new Origin();
        origin.setId("originid");
        origin.setCountry("US");
        origin.setState("New York");
        origin.setCity("New York City");
        Destination destination = new Destination();
        destination.setId("destinationid");
        destination.setCountry("Japan");
        destination.setState("Kanto");
        destination.setCity("Tokyo");
        orderMessage.setOrigin(origin);
        orderMessage.setDestination(destination);
        orderMessage.setUserId("USERID_TEST");
        orderMessage.setId("ID_TEST");
        orderMessage.setPickupStartTime("2022-12-08 00:00:00 GMT+08:00");
        orderMessage.setOrganisationId("Org 1");
        orderMessage.setPickupCommitTime("2022-12-08 00:00:00 GMT+08:00");
        orderMessage.setDeliveryStartTime("2022-12-10 23:00:00 GMT+01:00");
        orderMessage.setDeliveryCommitTime("2022-12-11 00:00:00 GMT+01:00");
        Shipper shipper = new Shipper();
        shipper.setName("SHIPPER NAME");
        shipper.setEmail("shipper@email.com");
        shipper.setPhone("123456789");
        orderMessage.setShipper(shipper);

        Consignee consignee = new Consignee();
        consignee.setName("SHIPPER NAME");
        consignee.setEmail("shipper@email.com");
        consignee.setPhone("123456789");
        orderMessage.setConsignee(consignee);

        List<Package> packages = new ArrayList<>();
        packages.add(shipmentPackageMessage);
        packages.add(createPackage(BigDecimal.valueOf(2000)));
        orderMessage.setShipments(packages);
        shipmentPackageMessage.setMeasurementUnits("metric");
        orderMessage.setInstructions(createInstructions());

        Shipment shipmentDomain = mapOrderMessageToShipmentDomain(shipmentPackageMessage, orderMessage);

        assertThat(shipmentDomain.getUserId()).isEqualTo(orderMessage.getUserId());
        assertThat(shipmentDomain.getShipmentPackage()).isNotNull();

        assertThat(shipmentDomain.getShipmentPackage().getRefId())
                .withFailMessage("Package Ref ID mismatch.")
                .isEqualTo(shipmentPackageMessage.getId());

        assertThat(shipmentDomain.getShipmentPackage().getTypeRefId())
                .withFailMessage("Package Type Ref ID mismatch.")
                .isEqualTo(shipmentPackageMessage.getPackaging().getId());

        assertThat(shipmentDomain.getShipmentPackage().getCode())
                .withFailMessage("Package Code mismatch.")
                .isEqualTo(shipmentPackageMessage.getCode());

        assertThat(shipmentDomain.getShipmentPackage().getTotalItemsCount())
                .withFailMessage("Total Item Count mismatch.")
                .isEqualTo(shipmentPackageMessage.getItemsCount());

        assertThat(shipmentDomain.getShipmentPackage().getTotalValue().doubleValue())
                .withFailMessage("Shipment value of goods mismatch")
                .isEqualTo(orderMessage.getShipments().get(0).getValueOfGoods());

        assertThat(shipmentDomain.getShipmentReferenceId())
                .withFailMessage("Shipment Reference ID mismatch")
                .isEqualTo(Collections.singletonList(orderMessage.getShipments().get(0).getAdditionalData1()));

        Origin orderOrigin = orderMessage.getOrigin();
        Address shipmentOrigin = shipmentDomain.getOrigin();

        assertThat(shipmentOrigin.getCountryName()).withFailMessage("Origin Country mismatch").isEqualTo(orderOrigin.getCountry());
        assertThat(shipmentOrigin.getStateName()).withFailMessage("Origin State mismatch").isEqualTo(orderOrigin.getState());
        assertThat(shipmentOrigin.getCityName()).withFailMessage("Origin City mismatch").isEqualTo(orderOrigin.getCity());

        Destination orderDestination = orderMessage.getDestination();
        Address shipmentDestination = shipmentDomain.getDestination();

        assertThat(shipmentDestination.getCountryName()).withFailMessage("Destination Country mismatch").isEqualTo(orderDestination.getCountry());
        assertThat(shipmentDestination.getStateName()).withFailMessage("Destination State mismatch").isEqualTo(orderDestination.getState());
        assertThat(shipmentDestination.getCityName()).withFailMessage("Destination City mismatch").isEqualTo(orderDestination.getCity());

        assertThat(shipmentDomain.getShipmentPackage().getId()).isEqualTo(orderMessage.getShipments().get(0).getId());
        assertThat(shipmentDomain.getShipmentPackage().getCode()).isEqualTo(orderMessage.getShipments().get(0).getCode());
        assertThat(shipmentDomain.getDescription()).isEqualTo(orderMessage.getShipments().get(0).getDescription());
    }

    @Test
    void mapOrderMessageToShipmentDomain_orderWithSingleSegment_shouldReturnShipmentDomain() throws JsonProcessingException {
        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-1-segment.json");
        List<SegmentsPayload> segments = testUtil.createSegmentListFromOM(data.toString(),
                data.get("is_segment").asText("false"));
        Root root = testUtil.createRootFromOM(data.toString());
        root.setSegmentsPayloads(segments);
        List<Package> omPackageList = root.getShipments();
        Package omPackage = omPackageList.get(0);

        Shipment shipmentDomain = mapOrderMessageToShipmentDomain(omPackage, root);

        assertThat(shipmentDomain).isNotNull();
        ShipmentJourney journeyDomain = shipmentDomain.getShipmentJourney();
        assertThat(journeyDomain).isNull();
        assertThat(shipmentDomain.getOrder()).isNull();
    }

    @Test
    void mapOrderMessageToShipmentDomain_orderWithMultipleSegments_shouldReturnShipmentDomain() {
        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-n-segments.json");
        ObjectNode dataObj = (ObjectNode) data;

        int segmentSize = (dataObj.get("segments_payload") != null) ? dataObj.get("segments_payload").size() : 1;
        List<ShipmentJourneyGenerator> journeyGeneratorList = IntStream.range(0, segmentSize)
                .mapToObj(s -> (ShipmentJourneyGenerator) new OrderMessageShipmentJourneyGenerator(new PackageJourneySegmentTypeAssigner(), shipmentJourneyCreationProperties, userDetailsProvider))
                .toList();

        ShipmentJourneyProvider journeyProvider = new ShipmentJourneyProvider(journeyGeneratorList);

        ShipmentOrderMessageConverter convert = new ShipmentOrderMessageConverter(testUtil.getObjectMapper(), userDetailsProvider, journeyProvider, rootOrderValidator);
        List<Shipment> shipments = convert.convertOrderMessageToShipments(data.toString(), "");
        Shipment shipmentDomain = shipments.get(0);
        assertThat(shipmentDomain).isNotNull();
        ShipmentJourney journeyDomain = shipmentDomain.getShipmentJourney();
        assertThat(journeyDomain).isNotNull();
        List<PackageJourneySegment> segmentDomainList = journeyDomain.getPackageJourneySegments();

        assertThat(shipmentDomain.getOrder().getInstructions()).isNotEmpty();
        assertThat(shipmentDomain.getOrder().getInstructions()).hasSize(3);

        assertThat(segmentDomainList).hasSize(5);
        assertThat(segmentDomainList.get(0).getType()).isEqualTo(SegmentType.FIRST_MILE);
        assertThat(segmentDomainList.get(1).getType()).isEqualTo(SegmentType.MIDDLE_MILE);
        assertThat(segmentDomainList.get(2).getType()).isEqualTo(SegmentType.MIDDLE_MILE);
        assertThat(segmentDomainList.get(3).getType()).isEqualTo(SegmentType.MIDDLE_MILE);
        assertThat(segmentDomainList.get(4).getType()).isEqualTo(SegmentType.LAST_MILE);
        assertThat(shipmentDomain.getDescription()).isNotNull();
    }

    @Test
    void convertCalculatedMileageUomStringToUom_metricArgument_shouldReturnKm() {
        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-n-segments.json");
        ObjectNode dataObj = (ObjectNode) data;
        dataObj.put("distance_uom", Root.DISTANCE_UOM_METRIC);

        int segmentSize = (dataObj.get("segments_payload") != null) ? dataObj.get("segments_payload").size() : 1;
        List<ShipmentJourneyGenerator> journeyGeneratorList = IntStream.range(0, segmentSize)
                .mapToObj(s -> (ShipmentJourneyGenerator) new OrderMessageShipmentJourneyGenerator(new PackageJourneySegmentTypeAssigner(), shipmentJourneyCreationProperties, userDetailsProvider))
                .toList();

        ShipmentJourneyProvider journeyProvider = new ShipmentJourneyProvider(journeyGeneratorList);

        ShipmentOrderMessageConverter convert = new ShipmentOrderMessageConverter(testUtil.getObjectMapper(), userDetailsProvider, journeyProvider, rootOrderValidator);
        List<Shipment> shipments = convert.convertOrderMessageToShipments(data.toString(), "");
        Shipment shipmentDomain = shipments.get(0);
        assertThat(shipmentDomain).isNotNull();
        ShipmentJourney journeyDomain = shipmentDomain.getShipmentJourney();
        assertThat(journeyDomain).isNotNull();
        List<PackageJourneySegment> segmentDomainList = journeyDomain.getPackageJourneySegments();

        assertThat(segmentDomainList).hasSize(5);
        assertThat(segmentDomainList.get(0).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.KM);
        assertThat(segmentDomainList.get(1).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.KM);
        assertThat(segmentDomainList.get(2).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.KM);
        assertThat(segmentDomainList.get(3).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.KM);
        assertThat(segmentDomainList.get(4).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.KM);
        assertThat(shipmentDomain.getDescription()).isNotNull();
    }

    @Test
    void convertCalculatedMileageUomStringToUom_imperialArgument_shouldReturnMile() {
        JsonNode data = testUtil.getDataFromFile("samplepayload/ordermodule-orders-1-package-n-segments.json");
        ObjectNode dataObj = (ObjectNode) data;
        dataObj.put("distance_uom", Root.DISTANCE_UOM_IMPERIAL);

        int segmentSize = (dataObj.get("segments_payload") != null) ? dataObj.get("segments_payload").size() : 1;
        List<ShipmentJourneyGenerator> journeyGeneratorList = IntStream.range(0, segmentSize)
                .mapToObj(s -> (ShipmentJourneyGenerator) new OrderMessageShipmentJourneyGenerator(new PackageJourneySegmentTypeAssigner(), shipmentJourneyCreationProperties, userDetailsProvider))
                .toList();

        ShipmentJourneyProvider journeyProvider = new ShipmentJourneyProvider(journeyGeneratorList);

        ShipmentOrderMessageConverter convert = new ShipmentOrderMessageConverter(testUtil.getObjectMapper(), userDetailsProvider, journeyProvider, rootOrderValidator);
        List<Shipment> shipments = convert.convertOrderMessageToShipments(data.toString(), "");
        Shipment shipmentDomain = shipments.get(0);
        assertThat(shipmentDomain).isNotNull();
        ShipmentJourney journeyDomain = shipmentDomain.getShipmentJourney();
        assertThat(journeyDomain).isNotNull();
        List<PackageJourneySegment> segmentDomainList = journeyDomain.getPackageJourneySegments();

        assertThat(segmentDomainList).hasSize(5);
        assertThat(segmentDomainList.get(0).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.MILE);
        assertThat(segmentDomainList.get(1).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.MILE);
        assertThat(segmentDomainList.get(2).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.MILE);
        assertThat(segmentDomainList.get(3).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.MILE);
        assertThat(segmentDomainList.get(4).getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.MILE);
        assertThat(shipmentDomain.getDescription()).isNotNull();
    }

    @Test
    void mapOrderMessageToShipmentDomain_withCommoditiesPackages_shouldReturnShipmentDomain() {
        Package shipmentPackageMessage = createPackage(BigDecimal.valueOf(2000));
        String orderIdLabel = "123";
        Root orderMessage = new Root();

        orderMessage.setUserId("USERID_TEST");
        orderMessage.setCustomerReferences(new ArrayList<>());
        orderMessage.setOrganisationId("Org 1");
        orderMessage.setInternalOrderId(orderIdLabel);
        orderMessage.setExternalOrderId("456");
        orderMessage.setCustomerOrderId("789");
        orderMessage.setOrderIdLabel(orderIdLabel);
        Origin origin = new Origin();
        origin.setId("originid");
        origin.setCountry("US");
        origin.setState("New York");
        origin.setCity("New York City");
        Destination destination = new Destination();
        destination.setId("destinationid");
        destination.setCountry("Japan");
        destination.setState("Kanto");
        destination.setCity("Tokyo");
        orderMessage.setOrigin(origin);
        orderMessage.setDestination(destination);

        Shipper shipper = new Shipper();
        shipper.setName("SHIPPER NAME");
        shipper.setEmail("shipper@email.com");
        shipper.setPhone("123456789");
        orderMessage.setShipper(shipper);

        Consignee consignee = new Consignee();
        consignee.setName("SHIPPER NAME");
        consignee.setEmail("shipper@email.com");
        consignee.setPhone("123456789");
        orderMessage.setConsignee(consignee);

        shipmentPackageMessage.setMeasurementUnits("metric");

        Shipment shipmentDomain = mapOrderMessageToShipmentDomain(shipmentPackageMessage, orderMessage);

        assertThat(shipmentDomain.getUserId()).isEqualTo(orderMessage.getUserId());

        assertThat(shipmentDomain.getInternalOrderId()).isEqualTo(orderMessage.getInternalOrderId());
        assertThat(shipmentDomain.getExternalOrderId()).isEqualTo(orderMessage.getExternalOrderId());
        assertThat(shipmentDomain.getCustomerOrderId()).isEqualTo(orderMessage.getCustomerOrderId());

        com.quincus.shipment.api.domain.Commodity commodityDomain = shipmentDomain.getShipmentPackage().getCommodities().get(0);
        CommoditiesPackage commoditiesPackage = shipmentPackageMessage.getCommoditiesPackages().get(0);

        assertThat(commodityDomain.getValue()).hasToString(commoditiesPackage.getValueOfGoods());
        assertThat(commodityDomain.getName()).isEqualTo(commoditiesPackage.getCommodityName());
        assertThat(commodityDomain.getDescription()).isEqualTo(commoditiesPackage.getDescription());
        assertThat(commodityDomain.getCode()).isEqualTo(commoditiesPackage.getCode());
        assertThat(commodityDomain.getHsCode()).isEqualTo(commoditiesPackage.getHsCode());
        assertThat(commodityDomain.getNote()).isEqualTo(commoditiesPackage.getNote());
        assertThat(commodityDomain.getPackagingType()).isEqualTo(commoditiesPackage.getPackagingType());
    }

    @Test
    void mapOrderMessageToShipmentDomain_withCompanyAndDepartment_shouldReturnShipmentDomain() {
        Package shipmentPackageMessage = createPackage(BigDecimal.valueOf(2000));
        String orderIdLabel = "123";
        Root orderMessage = new Root();

        orderMessage.setUserId("USERID_TEST");
        orderMessage.setCustomerReferences(new ArrayList<>());
        orderMessage.setOrganisationId("Org 1");
        orderMessage.setInternalOrderId(orderIdLabel);
        orderMessage.setExternalOrderId("456");
        orderMessage.setCustomerOrderId("789");
        orderMessage.setOrderIdLabel(orderIdLabel);
        Origin origin = new Origin();
        origin.setId("originid");
        origin.setCountry("US");
        origin.setState("New York");
        origin.setCity("New York City");
        origin.setCompany("Origin Company");
        origin.setDepartment("Origin Department");
        Destination destination = new Destination();
        destination.setId("destinationid");
        destination.setCountry("Japan");
        destination.setState("Kanto");
        destination.setCity("Tokyo");
        destination.setCompany("Destination Company");
        destination.setDepartment("Destination Department");
        orderMessage.setOrigin(origin);
        orderMessage.setDestination(destination);

        Shipper shipper = new Shipper();
        shipper.setName("SHIPPER NAME");
        shipper.setEmail("shipper@email.com");
        shipper.setPhone("123456789");
        orderMessage.setShipper(shipper);

        Consignee consignee = new Consignee();
        consignee.setName("SHIPPER NAME");
        consignee.setEmail("shipper@email.com");
        consignee.setPhone("123456789");
        orderMessage.setConsignee(consignee);

        shipmentPackageMessage.setMeasurementUnits("metric");

        Shipment shipmentDomain = mapOrderMessageToShipmentDomain(shipmentPackageMessage, orderMessage);

        assertThat(shipmentDomain.getUserId()).isEqualTo(orderMessage.getUserId());

        assertThat(shipmentDomain.getInternalOrderId()).isEqualTo(orderMessage.getInternalOrderId());
        assertThat(shipmentDomain.getExternalOrderId()).isEqualTo(orderMessage.getExternalOrderId());
        assertThat(shipmentDomain.getCustomerOrderId()).isEqualTo(orderMessage.getCustomerOrderId());

        com.quincus.shipment.api.domain.Address originDomain = shipmentDomain.getOrigin();
        com.quincus.shipment.api.domain.Address destinationDomain = shipmentDomain.getDestination();

        assertThat(originDomain.getCompany()).isEqualTo(origin.getCompany());
        assertThat(originDomain.getDepartment()).isEqualTo(origin.getDepartment());
        assertThat(destinationDomain.getCompany()).isEqualTo(destination.getCompany());
        assertThat(destinationDomain.getDepartment()).isEqualTo(destination.getDepartment());
    }

    @Test
    void mapOrderMessageToShipmentDomain_withCommoditiesPackages_andNullValueOfGoods_shouldReturnShipmentDomain() {
        Package shipmentPackageMessage = createPackage(BigDecimal.valueOf(2000));
        String orderIdLabel = "123";
        Root orderMessage = new Root();

        orderMessage.setUserId("USERID_TEST");
        orderMessage.setCustomerReferences(new ArrayList<>());
        orderMessage.setOrganisationId("Org 1");
        orderMessage.setInternalOrderId(orderIdLabel);
        orderMessage.setExternalOrderId("456");
        orderMessage.setCustomerOrderId("789");
        orderMessage.setOrderIdLabel(orderIdLabel);
        Origin origin = new Origin();
        origin.setId("originid");
        origin.setCountry("US");
        origin.setState("New York");
        origin.setCity("New York City");
        Destination destination = new Destination();
        destination.setId("destinationid");
        destination.setCountry("Japan");
        destination.setState("Kanto");
        destination.setCity("Tokyo");
        orderMessage.setOrigin(origin);
        orderMessage.setDestination(destination);

        Shipper shipper = new Shipper();
        shipper.setName("SHIPPER NAME");
        shipper.setEmail("shipper@email.com");
        shipper.setPhone("123456789");
        orderMessage.setShipper(shipper);

        Consignee consignee = new Consignee();
        consignee.setName("SHIPPER NAME");
        consignee.setEmail("shipper@email.com");
        consignee.setPhone("123456789");
        orderMessage.setConsignee(consignee);

        shipmentPackageMessage.setMeasurementUnits("metric");

        CommoditiesPackage commoditiesPackageToUpdate = shipmentPackageMessage.getCommoditiesPackages().get(0);
        commoditiesPackageToUpdate.setValueOfGoods(null);
        shipmentPackageMessage.getCommoditiesPackages().set(0, commoditiesPackageToUpdate);

        Shipment shipmentDomain = mapOrderMessageToShipmentDomain(shipmentPackageMessage, orderMessage);

        assertThat(shipmentDomain.getUserId()).isEqualTo(orderMessage.getUserId());

        assertThat(shipmentDomain.getInternalOrderId()).isEqualTo(orderMessage.getInternalOrderId());
        assertThat(shipmentDomain.getExternalOrderId()).isEqualTo(orderMessage.getExternalOrderId());
        assertThat(shipmentDomain.getCustomerOrderId()).isEqualTo(orderMessage.getCustomerOrderId());

        com.quincus.shipment.api.domain.Commodity commodityDomain = shipmentDomain.getShipmentPackage().getCommodities().get(0);
        CommoditiesPackage commoditiesPackage = shipmentPackageMessage.getCommoditiesPackages().get(0);

        assertThat(commodityDomain.getValue()).isNull();
        assertThat(commodityDomain.getName()).isEqualTo(commoditiesPackage.getCommodityName());
        assertThat(commodityDomain.getDescription()).isEqualTo(commoditiesPackage.getDescription());
        assertThat(commodityDomain.getCode()).isEqualTo(commoditiesPackage.getCode());
        assertThat(commodityDomain.getHsCode()).isEqualTo(commoditiesPackage.getHsCode());
        assertThat(commodityDomain.getNote()).isEqualTo(commoditiesPackage.getNote());
        assertThat(commodityDomain.getPackagingType()).isEqualTo(commoditiesPackage.getPackagingType());
    }

    @Test
    void mapOrderMessageToShipmentOrder_validParameters_shouldReturnOrderDomain() {
        String orderIdLabel = "123";
        Root orderMessage = new Root();
        orderMessage.setUserId("USERID_TEST");
        orderMessage.setId("ID_TEST");
        orderMessage.setStatus("STATUS_TEST");
        orderMessage.setOrderIdLabel(orderIdLabel);
        orderMessage.setInternalOrderId(orderIdLabel);
        orderMessage.setCustomerReferences(new ArrayList<>(Arrays.asList(
                createCustomerReference("CUSTOMER_REF_ID_LABEL_TEST1"),
                createCustomerReference("CUSTOMER_REF_ID_LABEL_TEST2"))));
        orderMessage.setPickupStartTime("2023-02-14 08:00:00 GMT-05:00");
        orderMessage.setPickupCommitTime("2023-02-16 22:59:00 GMT-05:00");
        orderMessage.setPickupTimezone("GMT-05:00");
        orderMessage.setDeliveryStartTime("2023-03-28 10:00:00 GMT+10:00");
        orderMessage.setDeliveryCommitTime("2023-03-29 19:59:00 GMT+10:00");
        orderMessage.setDeliveryTimezone("GMT+10:00");
        orderMessage.setOrganisationId("Org 1");
        orderMessage.setInternalOrderId("123");
        orderMessage.setExternalOrderId("456");
        orderMessage.setCustomerOrderId("789");
        orderMessage.setOrderReferences(mapperTestUtil.createDummyOrderReferences());
        Origin origin = new Origin();
        origin.setId("originid");
        origin.setCountry("US");
        origin.setState("New York");
        origin.setCity("New York City");
        Destination destination = new Destination();
        destination.setId("destinationid");
        destination.setCountry("Japan");
        destination.setState("Kanto");
        destination.setCity("Tokyo");
        orderMessage.setOrigin(origin);
        orderMessage.setDestination(destination);

        Shipper shipper = new Shipper();
        shipper.setName("SHIPPER NAME");
        shipper.setEmail("shipper@email.com");
        shipper.setPhone("123456789");
        orderMessage.setShipper(shipper);

        Consignee consignee = new Consignee();
        consignee.setName("SHIPPER NAME");
        consignee.setEmail("shipper@email.com");
        consignee.setPhone("123456789");
        orderMessage.setConsignee(consignee);

        Attachment omAttachment1 = new Attachment();
        omAttachment1.setId("attachment-1");
        omAttachment1.setFileName("picture.jpg");
        omAttachment1.setFileSize(1234L);
        omAttachment1.setFileUrl("https://example.com");

        Attachment omAttachment2 = new Attachment();
        omAttachment2.setId("attachment-1");
        omAttachment2.setFileName("picture.jpg");
        omAttachment2.setFileSize(1234L);
        omAttachment2.setFileUrl("https://example.com");

        List<Attachment> omAttachments = new ArrayList<>();
        omAttachments.add(omAttachment1);
        omAttachments.add(omAttachment2);
        orderMessage.setAttachments(omAttachments);
        orderMessage.setInstructions(createInstructions());

        Order orderDomain = mapOrderMessageToShipmentOrder(orderMessage);

        assertThat(testUtil.isDateTimeFromString(orderMessage.getPickupStartTime(), DateTimeUtil.convertStringToLocalDateTime(orderDomain.getPickupStartTime())))
                .withFailMessage("Order Pickup Start Time mismatch.")
                .isTrue();

        assertThat(testUtil.isDateTimeFromString(orderMessage.getPickupCommitTime(), DateTimeUtil.convertStringToLocalDateTime(orderDomain.getPickupCommitTime())))
                .withFailMessage("Order Pickup Commit Time mismatch.")
                .isTrue();

        assertThat(orderDomain.getPickupTimezone())
                .withFailMessage("Order Pickup Timezone mismatch.")
                .isEqualTo(orderMessage.getPickupTimezone());

        assertThat(testUtil.isDateTimeFromString(orderMessage.getDeliveryStartTime(), DateTimeUtil.convertStringToLocalDateTime(orderDomain.getDeliveryStartTime())))
                .withFailMessage("Order Delivery Start Time mismatch.")
                .isTrue();

        assertThat(testUtil.isDateTimeFromString(orderMessage.getDeliveryCommitTime(), DateTimeUtil.convertStringToLocalDateTime(orderDomain.getDeliveryCommitTime())))
                .withFailMessage("Order Delivery Commit Time mismatch.")
                .isTrue();

        assertThat(orderDomain.getDeliveryTimezone())
                .withFailMessage("Order Delivery Timezone mismatch.")
                .isEqualTo(orderMessage.getDeliveryTimezone());

        assertThat(orderDomain.getOrderIdLabel()).isEqualTo(orderMessage.getOrderIdLabel());

        assertThat(orderDomain.getCustomerReferenceId()).isNotNull();
        assertThat(orderDomain.getCustomerReferenceId()).hasSameSizeAs(orderMessage.getCustomerReferences());

        List<OrderAttachment> attachments = orderDomain.getAttachments();
        assertThat(attachments).usingRecursiveComparison().isEqualTo(omAttachments);

        assertThat(orderDomain.getInstructions()).isNotEmpty();

        assertThat(orderDomain.getOrderReferences()).isNotEmpty().hasSize(2);
        assertThat(orderDomain.getOrderReferences().get(0).getId()).isEqualTo(orderMessage.getOrderReferences().get(0).getId());
        assertThat(orderDomain.getOrderReferences().get(0).getExternalId()).isEqualTo(orderMessage.getOrderReferences().get(0).getExternalId());
        assertThat(orderDomain.getOrderReferences().get(0).getLabel()).isEqualTo(orderMessage.getOrderReferences().get(0).getLabel());
    }

    private List<Instruction> createInstructions() {
        List<Instruction> instructions = new ArrayList<>();

        Instruction pickUpInstruction = new Instruction();
        pickUpInstruction.setId("2678bdeb-73a5-4679-8d11-b169867cd901");
        pickUpInstruction.setLabel("pickup label");
        pickUpInstruction.setSource("order");
        pickUpInstruction.setApplyTo("pickup");
        pickUpInstruction.setCreatedAt("2023-04-19T09:10:43.614Z");
        pickUpInstruction.setUpdatedAt("2023-04-19T10:10:43.614Z");
        instructions.add(pickUpInstruction);

        Instruction deliveryInstruction = new Instruction();
        deliveryInstruction.setId("2678bdeb-73a5-4679-8d11-b169867cd111");
        deliveryInstruction.setLabel("delivery label");
        deliveryInstruction.setSource("order");
        deliveryInstruction.setApplyTo("delivery");
        deliveryInstruction.setCreatedAt("2023-04-20T11:10:43.614Z");
        deliveryInstruction.setUpdatedAt("2023-04-21T01:10:43.614Z");
        instructions.add(deliveryInstruction);

        Instruction journeyInstruction = new Instruction();
        journeyInstruction.setId("2678bdeb-73a5-4679-8d11-b169867cd111");
        journeyInstruction.setLabel("journey label");
        journeyInstruction.setSource("order");
        journeyInstruction.setApplyTo("journey");
        journeyInstruction.setCreatedAt("2023-04-20T11:10:43.614Z");
        journeyInstruction.setUpdatedAt("2023-04-21T01:10:43.614Z");
        instructions.add(journeyInstruction);

        return instructions;
    }

    private Package createPackage(BigDecimal valueOfGoods) {
        Package pkg = new Package();
        pkg.setId(randomUUID().toString());
        pkg.setCode("000");
        pkg.setValueOfGoods(valueOfGoods.doubleValue());
        pkg.setPackageType("BIG BOX");
        pkg.setVolumeWeight(50);
        pkg.setChargeableWeight(60);
        pkg.setGrossWeight(60);
        pkg.setDescription("This is a valid description");
        List<Commodity> commodities = new ArrayList<>();
        Commodity commodity = new Commodity();
        commodity.setId(randomUUID().toString());
        commodity.setName("FOOD PRODUCTS");
        commodities.add(commodity);
        pkg.setCommodities(commodities);

        List<CommoditiesPackage> commoditiesPackages = new ArrayList<>();
        CommoditiesPackage commoditiesPackage = new CommoditiesPackage();
        commoditiesPackage.setId(randomUUID().toString());
        commoditiesPackage.setValueOfGoods(valueOfGoods.toString());
        commoditiesPackage.setCommodityName("TOYS");
        commoditiesPackage.setDescription("DESC");
        commoditiesPackage.setHsCode("T01");
        commoditiesPackage.setCode("CODE");
        commoditiesPackage.setNote("NOTE");
        commoditiesPackage.setPackagingType("TYPE");
        commoditiesPackages.add(commoditiesPackage);
        pkg.setCommoditiesPackages(commoditiesPackages);

        pkg.setAdditionalData1("UP387429834T");
        pkg.setCode("1000");

        Packaging packagingMessage = new Packaging();
        packagingMessage.setId("packaging-type-1");
        packagingMessage.setCustom(true);
        pkg.setPackaging(packagingMessage);
        pkg.setItemsCount(5);
        return pkg;
    }

    private CustomerReference createCustomerReference(String customerIdLabel) {
        var customerReference = new CustomerReference();
        customerReference.setIdLabel(customerIdLabel);
        return customerReference;
    }
}
