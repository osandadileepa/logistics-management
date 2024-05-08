package com.quincus.shipment.kafka.producers.test_utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Commodity;
import com.quincus.shipment.api.domain.Consignee;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.FlightDetails;
import com.quincus.shipment.api.domain.FlightStatus;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.PricingInfo;
import com.quincus.shipment.api.domain.Sender;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.domain.Vehicle;
import com.quincus.shipment.api.helper.EnumUtil;
import com.quincus.shipment.kafka.producers.mapper.MapperUtil;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.quincus.shipment.api.constant.InstructionApplyToType.DELIVERY;
import static com.quincus.shipment.api.constant.InstructionApplyToType.JOURNEY;
import static com.quincus.shipment.api.constant.InstructionApplyToType.PICKUP;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Fail.fail;

public class TestUtil {

    private static final TestUtil INSTANCE = new TestUtil();

    private static final Pattern INPUT_DATE_TIME_STR_PATTERN = Pattern.compile(
            "(\\d{4})-(\\d{2})-(\\d{2})\\s*(\\d{2}):(\\d{2}):(\\d{2}).*");

    @Getter
    private final ObjectMapper objectMapper;

    private TestUtil() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper = objectMapper;
    }

    public static TestUtil getInstance() {
        return INSTANCE;
    }

    private static Shipment createDummyShipmentFromOrderJson(JsonNode refOrderJson) {
        return createDummyShipmentFromOrderJson(refOrderJson, 0);
    }

    private static Shipment createDummyShipmentFromOrderJson(JsonNode refOrderJson, int pkgIdx) {
        Shipment shipment = new Shipment();
        shipment.setId("SHIPMENT-TEST-UUID-1");

        var order = new Order();
        order.setId(refOrderJson.get("id").asText());
        order.setOrderIdLabel(refOrderJson.get("order_id_label").asText());

        List<String> customerReferenceIdList = new ArrayList<>();
        var customerRefIdSize = refOrderJson.get("customer_references").size();
        for (int i = 0; i < customerRefIdSize; i++) {
            customerReferenceIdList.add(refOrderJson.get("customer_references").get(i).get("id_label").asText());
        }

        List<String> orderTags = new ArrayList<>();
        var tagSize = refOrderJson.get("tag_list").size();
        for (int i = 0; i < tagSize; i++) {
            orderTags.add(refOrderJson.get("tag_list").get(0).asText());
        }
        order.setTags(orderTags);

        order.setCustomerReferenceId(customerReferenceIdList);
        order.setNotes(refOrderJson.get("note").asText());
        order.setOpsType(refOrderJson.get("ops_type").asText());
        order.setData(refOrderJson.toString());
        order.setPickupStartTime(refOrderJson.get("pickup_start_time").asText());
        order.setPickupTimezone(refOrderJson.get("pickup_timezone").asText());
        order.setPickupCommitTime(refOrderJson.get("pickup_commit_time").asText());
        order.setDeliveryStartTime(refOrderJson.get("delivery_start_time").asText());
        order.setDeliveryTimezone(refOrderJson.get("delivery_timezone").asText());
        order.setDeliveryCommitTime(refOrderJson.get("delivery_commit_time").asText());
        shipment.setOrder(order);

        var organization = new Organization();
        organization.setId(refOrderJson.get("organisation_id").asText());
        shipment.setOrganization(organization);

        var sender = new Sender();
        sender.setName(refOrderJson.get("shipper").get("name").asText());
        sender.setEmail(refOrderJson.get("shipper").get("email").asText());
        sender.setContactNumber(refOrderJson.get("shipper").get("phone").asText());
        shipment.setSender(sender);

        var consignee = new Consignee();
        consignee.setName(refOrderJson.get("consignee").get("name").asText());
        consignee.setEmail(refOrderJson.get("consignee").get("email").asText());
        consignee.setContactNumber(refOrderJson.get("consignee").get("phone").asText());
        shipment.setConsignee(consignee);

        var origin = new Address();
        origin.setId(refOrderJson.get("origin").get("id").asText());
        origin.setCountry(refOrderJson.get("origin").get("country").asText());
        origin.setState(refOrderJson.get("origin").get("state").asText());
        origin.setCity(refOrderJson.get("origin").get("city").asText());
        origin.setCountryId(refOrderJson.get("origin").get("country_id").asText());
        origin.setStateId(refOrderJson.get("origin").get("state_id").asText());
        origin.setCityId(refOrderJson.get("origin").get("city_id").asText());
        origin.setPostalCode(refOrderJson.get("origin").get("postal_code").asText());
        origin.setLine1(refOrderJson.get("origin").get("address_line1").asText());
        origin.setLine2(refOrderJson.get("origin").get("address_line2").asText());
        origin.setLine3(refOrderJson.get("origin").get("address_line3").asText());
        origin.setFullAddress(refOrderJson.get("origin").get("address").asText());
        origin.setLatitude(refOrderJson.get("origin").get("latitude").asText());
        origin.setLongitude(refOrderJson.get("origin").get("longitude").asText());
        origin.setManualCoordinates(refOrderJson.get("origin").get("manual_coordinates").asBoolean(false));
        shipment.setOrigin(origin);

        var destination = new Address();
        destination.setId(refOrderJson.get("destination").get("id").asText());
        destination.setCountry(refOrderJson.get("destination").get("country").asText());
        destination.setState(refOrderJson.get("destination").get("state").asText());
        destination.setCity(refOrderJson.get("destination").get("city").asText());
        destination.setCountryId(refOrderJson.get("destination").get("country_id").asText());
        destination.setStateId(refOrderJson.get("destination").get("state_id").asText());
        destination.setCityId(refOrderJson.get("destination").get("city_id").asText());
        destination.setPostalCode(refOrderJson.get("destination").get("postal_code").asText());
        destination.setLine1(refOrderJson.get("destination").get("address_line1").asText());
        destination.setLine2(refOrderJson.get("destination").get("address_line2").asText());
        destination.setLine3(refOrderJson.get("destination").get("address_line3").asText());
        destination.setFullAddress(refOrderJson.get("destination").get("address").asText());
        destination.setLatitude(refOrderJson.get("destination").get("latitude").asText());
        destination.setLongitude(refOrderJson.get("destination").get("longitude").asText());
        destination.setManualCoordinates(refOrderJson.get("destination").get("manual_coordinates").asBoolean(false));
        shipment.setDestination(destination);

        var serviceType = new ServiceType();
        serviceType.setId(refOrderJson.get("service_type_id").asText());
        serviceType.setCode(refOrderJson.get("service_type").asText());
        shipment.setServiceType(serviceType);

        shipment.setShipmentTrackingId("SHIPMENT-TEST-TRACKING-ID-1");
        shipment.setUserId(refOrderJson.get("user_id").asText());
        shipment.setPartnerId(refOrderJson.get("partner_id").asText());
        shipment.setPickUpLocation(refOrderJson.get("origin").get("id").asText());
        shipment.setDeliveryLocation(refOrderJson.get("destination").get("id").asText());
        shipment.setInternalOrderId(refOrderJson.get("internal_order_id").asText());
        shipment.setExternalOrderId(refOrderJson.get("external_order_id").asText());
        shipment.setCustomerOrderId(refOrderJson.get("customer_order_id").asText());

        JsonNode packageJson = refOrderJson.get("packages").get(pkgIdx);

        var shipmentPackage = new Package();
        shipmentPackage.setId(randomUUID().toString());
        shipmentPackage.setRefId(packageJson.get("id").asText());
        shipmentPackage.setType(packageJson.get("package_type").asText());
        shipmentPackage.setTypeRefId(packageJson.get("packaging").get("id").asText());
        shipmentPackage.setCode(packageJson.get("code").asText());
        shipmentPackage.setTotalItemsCount(packageJson.get("items_count").asLong(1L));
        shipmentPackage.setTotalValue(packageJson.get("value_of_goods").decimalValue());

        var dimension = new PackageDimension();
        dimension.setId("DIMENSION-ID-TEST-1");
        dimension.setMeasurementUnit(EnumUtil.toEnum(MeasurementUnit.class, packageJson.get("measurement_units").asText()));
        dimension.setLength(packageJson.get("length").decimalValue());
        dimension.setWidth(packageJson.get("width").decimalValue());
        dimension.setHeight(packageJson.get("height").decimalValue());
        dimension.setGrossWeight(packageJson.get("gross_weight").decimalValue());
        dimension.setVolumeWeight(packageJson.get("volume_weight").decimalValue());
        dimension.setChargeableWeight(packageJson.get("chargeable_weight").decimalValue());
        dimension.setCustom(packageJson.get("packaging").get("is_custom").booleanValue());
        shipmentPackage.setDimension(dimension);

        var commodity1 = new Commodity();
        commodity1.setName(packageJson.get("commodities").get(0).get("name").asText());
        commodity1.setQuantity(packageJson.get("commodities_packages")
                .get(0).get("items_count").asLong());
        commodity1.setValue(packageJson.get("commodities_packages")
                .get(0).get("value_of_goods").decimalValue());
        shipmentPackage.setCommodities(List.of(commodity1));

        var pricingInfo = new PricingInfo();
        pricingInfo.setCurrency(refOrderJson.get("pricing_info").get("currency_code").asText());
        pricingInfo.setBaseTariff(refOrderJson.get("pricing_info").get("base_tariff").decimalValue());
        pricingInfo.setServiceTypeCharge(refOrderJson.get("pricing_info").get("service_type_charge").decimalValue());
        pricingInfo.setSurcharge(refOrderJson.get("pricing_info").get("surcharge").decimalValue());
        pricingInfo.setInsuranceCharge(refOrderJson.get("pricing_info").get("insurance_charge").decimalValue());
        pricingInfo.setDiscount(refOrderJson.get("pricing_info").get("discount").decimalValue());
        pricingInfo.setTax(refOrderJson.get("pricing_info").get("tax").decimalValue());
        pricingInfo.setCod(refOrderJson.get("pricing_info").get("cod").decimalValue());
        shipmentPackage.setPricingInfo(pricingInfo);
        shipment.setShipmentPackage(shipmentPackage);

        JsonNode instructionsJson = refOrderJson.get("instructions");
        List<Instruction> instructions = new ArrayList<>();
        for (int i = 0; i < instructionsJson.size(); i++) {
            Instruction instruction = new Instruction();
            instruction.setExternalId(instructionsJson.get(i).get("id").asText());
            instruction.setLabel(instructionsJson.get(i).get("label").asText());
            instruction.setSource(instructionsJson.get(i).get("source").asText());
            instruction.setApplyTo(EnumUtil.toEnum(InstructionApplyToType.class, instructionsJson.get(i).get("apply_to").asText()));
            instruction.setCreatedAt(instructionsJson.get(i).get("created_at").asText());
            instruction.setUpdatedAt(instructionsJson.get(i).get("updated_at").asText());
            instructions.add(instruction);
        }
        shipment.setInstructions(instructions);

        var shipmentJourney = new ShipmentJourney();
        shipmentJourney.setJourneyId("TEST-JOURNEY-ID-1");

        var segmentsJson = (ArrayNode) refOrderJson.get("segments_payload");
        var segmentList = new ArrayList<PackageJourneySegment>();

        if (segmentsJson.size() > 0) {
            for (int i = 0; i < segmentsJson.size(); i++) {
                var segment = new PackageJourneySegment();
                segment.setJourneyId(shipmentJourney.getJourneyId());
                segment.setSegmentId("TEST-SEGMENT-ID-" + (i + 1));
                segment.setStatus(SegmentStatus.PLANNED);
                segment.setCost(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("cost")));
                segment.setRefId(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("ref_id")));
                segment.setSequence(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("sequence")));
                segment.setCurrencyId(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("currency_id")));
                segment.setInstruction(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("instruction")));

                segment.setPickUpTime(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("pick_up_time")));
                segment.setDropOffTime(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("drop_off_time")));
                segment.setVehicleInfo(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("vehicle_info")));

                segment.setAirlineCode(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("airline_code")));
                segment.setAirline(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("airline")));
                segment.setFlightNumber(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("flight_number")));
                segment.setLockOutTime(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("lock_out_time")));
                segment.setDepartureTime(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("departure_time")));
                segment.setArrivalTime(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("arrival_time")));
                segment.setRecoveryTime(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("recovery_time")));

                segment.setMasterWaybill(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("master_waybill")));
                String transportCategory = MapperUtil.parseTextFromJson(segmentsJson.get(i).get("transport_category"));
                segment.setTransportType(EnumUtil.toEnum(TransportType.class, transportCategory));

                Facility startFacility = new Facility();
                startFacility.setExternalId(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("pick_up_facility_id")));
                segment.setStartFacility(startFacility);

                Facility endFacility = new Facility();
                endFacility.setExternalId(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("drop_off_facility_id")));
                segment.setEndFacility(endFacility);

                segment.setCalculatedMileage(MapperUtil.parseBigDecimalFromJson(segmentsJson.get(i).get("calculated_mileage")));
                segment.setCalculatedMileageUnit(UnitOfMeasure.KM);
                segment.setDuration(MapperUtil.parseBigDecimalFromJson(segmentsJson.get(i).get("duration")));
                segment.setDurationUnit(UnitOfMeasure.MINUTE);

                Partner partner = new Partner();
                partner.setId(MapperUtil.parseTextFromJson(segmentsJson.get(i).get("partner_id")));
                segment.setPartner(partner);

                if (TransportType.AIR == segment.getTransportType()) {
                    segment.setFlight(createFlight());
                }
                segment.setInstructions(new ArrayList<>());

                segmentList.add(segment);
            }
            segmentList.get(0).setType(SegmentType.FIRST_MILE);
            segmentList.get(0).getInstructions().addAll(instructions.stream().filter(i -> PICKUP == i.getApplyTo()).toList());
            segmentList.get(0).getInstructions().addAll(instructions.stream().filter(i -> JOURNEY == i.getApplyTo()).toList());
            segmentList.get(segmentList.size() - 1).setType(SegmentType.LAST_MILE);
            segmentList.get(segmentList.size() - 1).getInstructions().addAll(instructions.stream().filter(i -> DELIVERY == i.getApplyTo()).toList());
            if (segmentList.size() > 1) {
                segmentList.get(segmentList.size() - 1).getInstructions().addAll(instructions.stream().filter(i -> JOURNEY == i.getApplyTo()).toList());
            }
            if (segmentList.size() > 2) {
                for (int j = 1; j < segmentList.size() - 1; j++) {
                    segmentList.get(j).setType(SegmentType.MIDDLE_MILE);
                    segmentList.get(0).getInstructions().addAll(instructions.stream().filter(i -> JOURNEY == i.getApplyTo()).toList());
                }
            }
        }

        shipmentJourney.setPackageJourneySegments(segmentList);
        shipment.setShipmentJourney(shipmentJourney);

        var milestone = new Milestone();
        shipment.setMilestone(milestone);

        shipment.setNotes(refOrderJson.get("note").asText());

        List<String> shipmentReferenceIds = List.of("SHPX1", "REF2");
        shipment.setShipmentReferenceId(shipmentReferenceIds);

        return shipment;
    }

    public Shipment createSingleShipmentFromOrderOnePackageMultiSegmentsJson() {
        JsonNode refOrderJson = getDataFromFile("samplepayload/ordermodule-orders-1-package-n-segments.json");
        return createDummyShipmentFromOrderJson(refOrderJson);
    }

    public Shipment createSingleShipmentFromOrderMultiPackagesOneSegmentJson() {
        JsonNode refOrderJson = getDataFromFile("samplepayload/ordermodule-orders-n-packages-1-segment.json");
        return createDummyShipmentFromOrderJson(refOrderJson);
    }

    public Shipment createSingleShipmentFromOrderMultiPackagesMultiSegmentsJson() {
        JsonNode refOrderJson = getDataFromFile("samplepayload/ordermodule-orders-n-packages-n-segments.json");
        return createDummyShipmentFromOrderJson(refOrderJson);
    }

    public List<Shipment> createMultipleShipmentsFromOrderMultiSegmentsJson() {
        JsonNode refOrderJson = getDataFromFile("samplepayload/ordermodule-orders-n-packages-n-segments.json");
        List<Shipment> shipments = new ArrayList<>();

        JsonNode packageJson = refOrderJson.get("packages");
        int size = (packageJson != null) ? packageJson.size() : 0;

        for (int i = 0; i < size; i++) {
            shipments.add(createDummyShipmentFromOrderJson(refOrderJson, i));
        }

        return shipments;
    }

    public boolean isInstantFromString(String expectedInstantStr, Instant instant) {
        if (expectedInstantStr == null) {
            return (instant == null);
        }

        Instant expectedInstant = Instant.parse(expectedInstantStr);

        return expectedInstant.equals(instant);
    }

    public boolean isEqualDateTime(LocalDateTime expectedDateTime, String expectedTimeZone, ZonedDateTime zonedDateTime) {
        ZoneId zoneId = ZoneId.of(expectedTimeZone);
        ZonedDateTime expectedZonedDateTime = expectedDateTime.atZone(zoneId);
        Instant expectedInstant = expectedZonedDateTime.toInstant().truncatedTo(ChronoUnit.MILLIS);
        Instant zonedInstant = zonedDateTime.toInstant().truncatedTo(ChronoUnit.MILLIS);

        return expectedInstant.equals(zonedInstant);
    }

    public boolean isZonedDateTimeFromString(String expectedDateTimeStr, ZonedDateTime zonedDateTime) {
        if (expectedDateTimeStr == null) {
            return (zonedDateTime == null);
        }

        var expectedDateTimeStrMatcher = INPUT_DATE_TIME_STR_PATTERN.matcher(expectedDateTimeStr);
        if (!expectedDateTimeStrMatcher.matches()) {
            fail("Expected Date Time String format is not defined for the test case.");
        }

        var expectedYear = Integer.parseInt(expectedDateTimeStrMatcher.group(1));
        var expectedMonth = Integer.parseInt(expectedDateTimeStrMatcher.group(2));
        var expectedDay = Integer.parseInt(expectedDateTimeStrMatcher.group(3));
        var expectedHour = Integer.parseInt(expectedDateTimeStrMatcher.group(4));
        var expectedMinute = Integer.parseInt(expectedDateTimeStrMatcher.group(5));
        var expectedSecond = Integer.parseInt(expectedDateTimeStrMatcher.group(6));

        var year = zonedDateTime.getYear();
        var month = zonedDateTime.getMonthValue();
        var day = zonedDateTime.getDayOfMonth();
        var hour = zonedDateTime.getHour();
        var minute = zonedDateTime.getMinute();
        var second = zonedDateTime.getSecond();

        return (expectedYear == year)
                && (expectedMonth == month)
                && (expectedDay == day)
                && (expectedHour == hour)
                && (expectedMinute == minute)
                && (expectedSecond == second);
    }

    public Vehicle createVehicleByNumber(String number) {
        Vehicle vehicle = new Vehicle();
        vehicle.setNumber(number);
        return vehicle;
    }

    private JsonNode getDataFromFile(String jsonPath) {
        try {
            ClassPathResource path = new ClassPathResource(jsonPath);
            return this.objectMapper.readValue(path.getFile(), JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.objectMapper.createObjectNode();
    }

    private static LocalDateTime convertStringToLocalDateTime(String omDateTimeRawStr) {
        String omDateTimeStr = omDateTimeRawStr.substring(0, 19);
        return DateTimeUtil.parseLocalDateTime(omDateTimeStr);
    }

    private static Flight createFlight() {
        FlightDetails departure = new FlightDetails();
        departure.setActualTime("2022-12-27 14:27:02 +0700");

        FlightDetails arrival = new FlightDetails();
        arrival.setActualTime("2022-12-27 14:27:02 +0700");

        FlightStatus status = new FlightStatus();
        status.setDeparture(departure);
        status.setArrival(arrival);

        Flight flight = new Flight();
        flight.setFlightStatuses(Arrays.asList(status));

        return flight;
    }
}
