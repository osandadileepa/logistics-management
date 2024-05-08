package com.quincus.apigateway.test_utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.CostCategory;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Commodity;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.CostSegment;
import com.quincus.shipment.api.domain.CostShipment;
import com.quincus.shipment.api.domain.CostType;
import com.quincus.shipment.api.domain.Currency;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.PricingInfo;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.web.common.config.JacksonConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TestUtil {

    private static final TestUtil INSTANCE = new TestUtil();

    @Getter
    private final ObjectMapper objectMapper;

    private TestUtil() {
        JacksonConfiguration jacksonConfigLoader = new JacksonConfiguration();
        this.objectMapper = jacksonConfigLoader.buildObjectMapper();
    }

    public static TestUtil getInstance() {
        return INSTANCE;
    }

    public JsonNode searchFlightSchedulesJson() {
        return getDataFromFile("samplePayload/request/searchFlightSchedules.json");
    }

    public JsonNode getUpdateOrderProgressResponseJson() {
        return getDataFromFile("samplePayload/response/updateOrderProgress.json");
    }

    public JsonNode getAssignVendorDetailsResponseJson() {
        return getDataFromFile("samplePayload/response/assignVendorDetails.json");
    }

    public JsonNode getUpdateOrderAdditionalChargesResponseJson() {
        return getDataFromFile("samplePayload/response/updateOrderAdditionalCharges.json");
    }

    public JsonNode getCheckInDetailsResponseJson() {
        return getDataFromFile("samplePayload/response/checkIn.json");
    }

    private JsonNode getDataFromFile(String jsonPath) {
        try {
            ClassPathResource path = new ClassPathResource(jsonPath);
            return this.objectMapper.readValue(path.getFile(), JsonNode.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return this.objectMapper.createObjectNode();
    }

    public Milestone createMilestone() {
        Milestone milestone = new Milestone();
        milestone.setOrganizationId("ORG-ID-ONE");

        milestone.setShipmentId("SHIPMENT-ID-ONE");
        milestone.setSegmentId("SEGMENT-ID-ONE");
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setPartnerId("PARTNER-ID-ONE");

        milestone.setDriverId("DRIVER1");
        milestone.setDriverName("DRIVER-ONE");
        milestone.setDriverEmail("driveremail@example.com");
        milestone.setDriverPhoneCode("+1");
        milestone.setDriverPhoneNumber("12345");
        milestone.setVehicleId("VEHICLE1");
        milestone.setVehicleName("CAR-ONE");
        milestone.setVehicleType("Car");
        milestone.setVehicleNumber("V12345");

        return milestone;
    }

    public Shipment createShipment() {
        Shipment shipment = new Shipment();
        shipment.setId("SHP-ID-ONE");

        Organization organization = new Organization();
        organization.setId("ORG-ONE");
        shipment.setOrganization(organization);

        Order order = new Order();
        order.setId("ORDER-ID-ONE");
        order.setOrderIdLabel("ORDER-ID-ONE");
        shipment.setOrder(order);
        shipment.setExternalOrderId("ORDER-ID-ONE");

        PackageJourneySegment pjs = new PackageJourneySegment();
        pjs.setSegmentId("SEGMENT-ID-ONE");
        pjs.setRefId("1");
        pjs.setType(SegmentType.FIRST_MILE);
        pjs.setTransportType(TransportType.GROUND);
        Facility startFacility = new Facility();
        startFacility.setId(UUID.randomUUID().toString());
        Facility endFacility = new Facility();
        endFacility.setId(UUID.randomUUID().toString());
        pjs.setStartFacility(startFacility);
        pjs.setEndFacility(endFacility);
        Partner partner = new Partner();
        partner.setId(UUID.randomUUID().toString());
        pjs.setPartner(partner);

        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setPackageJourneySegments(List.of(pjs));
        shipment.setShipmentJourney(shipmentJourney);

        Package shpPackage = new Package();
        shpPackage.setCode("PACKAGE-CODE");
        shpPackage.setTotalValue(new BigDecimal(500));

        PackageDimension dimension = new PackageDimension();
        dimension.setMeasurementUnit(MeasurementUnit.METRIC);
        dimension.setLength(new BigDecimal(10));
        dimension.setHeight(new BigDecimal(5));
        dimension.setWidth(new BigDecimal(30));
        shpPackage.setDimension(dimension);

        Commodity commodity = new Commodity();
        commodity.setName("Drug");
        commodity.setQuantity(2L);
        commodity.setDescription("DESC");
        commodity.setHsCode("T01");
        commodity.setCode("CODE");
        commodity.setNote("NOTE");
        commodity.setPackagingType("TYPE");
        shpPackage.setCommodities(List.of(commodity));

        PricingInfo pricingInfo = new PricingInfo();
        pricingInfo.setCurrency("PHP");
        pricingInfo.setCod(new BigDecimal(500));
        shpPackage.setPricingInfo(pricingInfo);
        shipment.setShipmentPackage(shpPackage);

        return shipment;

    }

    public Cost createCost() {
        Cost cost = new Cost();
        cost.setOrganizationId("ORG-ID-ONE");

        CostType costType = new CostType();
        costType.setCategory(CostCategory.TIME_BASED);

        cost.setCostType(costType);
        cost.setCostAmount(BigDecimal.TEN);

        Currency currency = new Currency();
        currency.setCode("USD");

        cost.setCurrency(currency);
        cost.setDriverId("driver_001");
        cost.setPartnerId("partner_001");
        cost.setIssuedDate(LocalDateTime.now());

        CostShipment shipment = new CostShipment();
        shipment.setOrderId("order_001");
        shipment.setOrderStatus("IN_PROGRESS");

        CostSegment segment1 = new CostSegment();
        segment1.setSegmentId("segment_001");
        segment1.setFirstSegment(true);
        CostSegment segment2 = new CostSegment();
        segment2.setSegmentId("segment_002");
        segment2.setLastSegment(true);

        shipment.setSegments(List.of(segment1, segment2));
        cost.setShipments(List.of(shipment));

        return cost;
    }
}
