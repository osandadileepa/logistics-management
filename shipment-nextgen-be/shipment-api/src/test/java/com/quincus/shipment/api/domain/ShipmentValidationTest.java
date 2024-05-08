package com.quincus.shipment.api.domain;

import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.MeasurementUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintViolation;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.quincus.shipment.api.constant.MilestoneCode.OM_BOOKED;
import static org.assertj.core.api.Assertions.assertThat;

class ShipmentValidationTest extends ValidationTest {
    private final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

    private static Stream<Arguments> provideEtaStatusValue() {
        return Arrays.stream(EtaStatus.values()).map(Arguments::of);
    }

    @Test
    void shipment_withMissingMandatoryFields_shouldHaveViolations() {
        Shipment shipment = new Shipment();
        Set<ConstraintViolation<Shipment>> violations = validator.validate(shipment);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shipment_withValidFields_shouldNotHaveViolations() {
        Shipment shipment = new Shipment();
        Order order = new Order();
        order.setId("ID");
        order.setStatus("Status");
        order.setOrderIdLabel("ID-LABEL");
        order.setPickupStartTime(OffsetDateTime.now().format(ISO_FORMATTER));
        order.setPickupCommitTime(OffsetDateTime.now().plusHours(1).format(ISO_FORMATTER));
        order.setPickupTimezone("GMT+08:00");
        order.setDeliveryStartTime(OffsetDateTime.now().plusDays(1).format(ISO_FORMATTER));
        order.setDeliveryCommitTime(OffsetDateTime.now().plusDays(1).plusHours(1).format(ISO_FORMATTER));
        order.setDeliveryTimezone("GMT+08:00");
        OrderAttachment attachment = new OrderAttachment();
        attachment.setId("uuid-attachment-1");
        attachment.setCreatedAt(Instant.now().minusSeconds(100));
        attachment.setUpdatedAt(Instant.now());
        attachment.setFileName("Attachment 1");
        attachment.setFileSize(123456L);
        attachment.setFileUrl("https://example.com");
        order.setAttachments(List.of(attachment));
        shipment.setOrder(order);
        shipment.setShipmentTrackingId("dummy-code");

        Address address = new Address();
        address.setCountry("Country");
        address.setCity("City");
        address.setState("State");

        Consignee consignee = new Consignee();
        consignee.setName("Name");
        consignee.setEmail("test@email.com");
        shipment.setConsignee(consignee);

        Sender sender = new Sender();
        sender.setName("testName");
        sender.setEmail("test@email.com");

        Organization org = new Organization();
        org.setId(UUID.randomUUID().toString());
        org.setName("orgName");
        org.setCode("OrgCode");

        Package shipmentPackage = createPackage();

        shipment.setId(UUID.randomUUID().toString());
        shipment.setUserId(UUID.randomUUID().toString());
        shipment.setPickUpLocation("PickupLocation");
        shipment.setDeliveryLocation("deliveryLocation");

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneName("Description");
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setOrganizationId(org.getId());
        shipment.setMilestone(milestone);
        shipment.setMilestoneEvents(List.of(milestone));

        shipment.setSender(sender);
        shipment.setServiceType(createService());
        shipment.setOrganization(org);
        shipment.setShipmentPackage(shipmentPackage);
        Set<ConstraintViolation<Object>> violations = validateModel(shipment);
        assertThat(violations).isEmpty();
    }

    private ServiceType createService() {
        ServiceType serviceType = new ServiceType();
        serviceType.setId(UUID.randomUUID().toString());
        serviceType.setCode("code");
        serviceType.setName("name");
        return serviceType;
    }

    private Package createPackage() {
        Package shipmentPackage = new Package();
        shipmentPackage.setCurrency("PHP");
        shipmentPackage.setTotalValue(new BigDecimal("1111"));
        shipmentPackage.setTotalItemsCount(1L);
        shipmentPackage.setType("Packaging Type");
        shipmentPackage.setTypeRefId("type-id-1");
        PackageDimension dimensions = new PackageDimension();
        dimensions.setMeasurementUnit(MeasurementUnit.METRIC);
        dimensions.setLength(new BigDecimal("1.1"));
        dimensions.setWidth(new BigDecimal("1.2"));
        dimensions.setHeight(new BigDecimal("1.3"));
        dimensions.setVolumeWeight(new BigDecimal("2.1"));
        dimensions.setGrossWeight(new BigDecimal("2.2"));
        dimensions.setChargeableWeight(new BigDecimal("2.3"));
        shipmentPackage.setDimension(dimensions);
        Commodity commodity = new Commodity();
        commodity.setName("Drugs");
        commodity.setQuantity(100L);
        commodity.setValue(new BigDecimal("2000.50"));
        commodity.setDescription("DESC");
        commodity.setHsCode("T01");
        commodity.setCode("CODE");
        commodity.setNote("NOTE");
        commodity.setPackagingType("TYPE");
        shipmentPackage.setCommodities(List.of(commodity));
        PricingInfo pricingInfo = new PricingInfo();
        pricingInfo.setCurrency("PHP");
        pricingInfo.setBaseTariff(new BigDecimal("10.10"));
        pricingInfo.setServiceTypeCharge(new BigDecimal("11.11"));
        pricingInfo.setSurcharge(new BigDecimal("12.12"));
        pricingInfo.setInsuranceCharge(new BigDecimal("20.20"));
        pricingInfo.setExtraCareCharge(new BigDecimal("21.21"));
        pricingInfo.setDiscount(new BigDecimal("15.69"));
        pricingInfo.setTax(new BigDecimal("2.48"));
        pricingInfo.setCod(new BigDecimal("5.11"));
        pricingInfo.setTotal(new BigDecimal("1.1"));
        shipmentPackage.setPricingInfo(pricingInfo);
        shipmentPackage.setCode("1000");

        return shipmentPackage;
    }

    @Test
    void shipment_withBlankFields_shouldHaveViolations() {
        Shipment shipment = new Shipment();
        Order order = new Order();
        order.setId("ID");
        order.setStatus(" ");
        order.setOrderIdLabel("ID-LABEL");
        order.setPickupStartTime(OffsetDateTime.now().format(ISO_FORMATTER));
        order.setPickupCommitTime(OffsetDateTime.now().plusHours(1).format(ISO_FORMATTER));
        order.setPickupTimezone("GMT+08:00");
        order.setDeliveryStartTime(OffsetDateTime.now().plusDays(1).format(ISO_FORMATTER));
        order.setDeliveryCommitTime(OffsetDateTime.now().plusDays(1).plusHours(1).format(ISO_FORMATTER));
        order.setDeliveryTimezone("GMT+08:00");
        OrderAttachment attachment = new OrderAttachment();
        attachment.setId(" ");
        attachment.setCreatedAt(Instant.now().minusSeconds(100));
        attachment.setUpdatedAt(Instant.now());
        attachment.setFileName(" ");
        attachment.setFileSize(123456L);
        attachment.setFileUrl(" ");
        order.setAttachments(List.of(attachment));
        shipment.setOrder(order);
        shipment.setShipmentTrackingId("dummy-code");

        Address address = new Address();
        address.setCountry("Country");
        address.setCity("City");
        address.setState("State");

        Consignee consignee = new Consignee();
        consignee.setName("Name");
        consignee.setEmail("test@email.com");
        shipment.setConsignee(consignee);

        Sender sender = new Sender();
        sender.setName("testName");
        sender.setEmail("test@email.com");
        shipment.setSender(sender);
        shipment.setServiceType(createService());
        shipment.setUserId(UUID.randomUUID().toString());
        shipment.setId(UUID.randomUUID().toString());
        shipment.setPickUpLocation("PickupLocation");
        shipment.setDeliveryLocation("deliveryLocation");

        Package shipmentPackage = createPackage();
        shipment.setShipmentPackage(shipmentPackage);

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneName(" ");
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setOrganizationId(UUID.randomUUID().toString());
        shipment.setMilestone(milestone);
        shipment.setMilestoneEvents(List.of(milestone));

        Set<ConstraintViolation<Object>> violations = validateModel(shipment);
        assertThat(violations).hasSize(4);
    }

    @Test
    void shipment_withEmptyFields_shouldHaveViolations() {
        Shipment shipment = new Shipment();
        Order order = new Order();
        order.setId("");
        order.setOrderIdLabel("");
        order.setPickupStartTime(OffsetDateTime.now().format(ISO_FORMATTER));
        order.setPickupCommitTime(OffsetDateTime.now().plusHours(1).format(ISO_FORMATTER));
        order.setPickupTimezone("GMT+08:00");
        order.setDeliveryStartTime(OffsetDateTime.now().plusDays(1).format(ISO_FORMATTER));
        order.setDeliveryCommitTime(OffsetDateTime.now().plusDays(1).plusHours(1).format(ISO_FORMATTER));
        order.setDeliveryTimezone("GMT+08:00");
        order.setStatus("");
        OrderAttachment attachment = new OrderAttachment();
        attachment.setId("");
        attachment.setCreatedAt(Instant.now().minusSeconds(100));
        attachment.setUpdatedAt(Instant.now());
        attachment.setFileName("");
        attachment.setFileSize(123456L);
        attachment.setFileUrl("");
        order.setAttachments(List.of(attachment));
        shipment.setOrder(order);
        shipment.setShipmentTrackingId("dummy-code");

        Address address = new Address();
        address.setCountryName("");
        address.setCityName("");
        address.setStateName("");

        Consignee consignee = new Consignee();
        consignee.setName("");
        consignee.setEmail("");
        shipment.setConsignee(consignee);

        Sender sender = new Sender();
        sender.setName("");
        sender.setEmail("");

        shipment.setSender(sender);
        shipment.setServiceType(null);
        shipment.setUserId("");
        shipment.setId(UUID.randomUUID().toString());
        shipment.setPickUpLocation("");
        shipment.setDeliveryLocation("");

        Package shipmentPackage = createPackage();
        shipment.setShipmentPackage(shipmentPackage);

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneName("");
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setOrganizationId(UUID.randomUUID().toString());
        shipment.setMilestone(milestone);
        shipment.setMilestoneEvents(List.of(milestone));

        Set<ConstraintViolation<Object>> violations = validateModel(shipment);
        violations.addAll(validateModel(address));
        assertThat(violations).hasSize(15);
    }

    @Test
    void shipment_notesAndInstructionsExceedLimit_shouldHaveViolations() {
        Shipment shipment = new Shipment();

        shipment.setShipmentTrackingId("TRACKING ID");

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setOrderIdLabel("ID-LABEL");
        order.setPickupStartTime(OffsetDateTime.now().format(ISO_FORMATTER));
        order.setPickupCommitTime(OffsetDateTime.now().plusHours(1).format(ISO_FORMATTER));
        order.setPickupTimezone("GMT+08:00");
        order.setDeliveryStartTime(OffsetDateTime.now().plusDays(1).format(ISO_FORMATTER));
        order.setDeliveryCommitTime(OffsetDateTime.now().plusDays(1).plusHours(1).format(ISO_FORMATTER));
        order.setDeliveryTimezone("GMT+08:00");
        order.setStatus("Status");
        shipment.setOrder(order);

        Address address = new Address();
        address.setCountry("Country");
        address.setCity("City");
        address.setState("State");

        Consignee consignee = new Consignee();
        consignee.setName("Name");
        consignee.setEmail("test@email.com");
        shipment.setConsignee(consignee);

        Sender sender = new Sender();
        sender.setName("testName");
        sender.setEmail("test@email.com");

        Organization org = new Organization();
        org.setId(UUID.randomUUID().toString());
        org.setName("orgName");
        org.setCode("OrgCode");

        Package shipmentPackage = createPackage();

        shipment.setUserId(UUID.randomUUID().toString());
        shipment.setId(UUID.randomUUID().toString());
        shipment.setPickUpLocation("PickupLocation");
        shipment.setDeliveryLocation("deliveryLocation");

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneName("Description");
        shipment.setMilestone(milestone);
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setOrganizationId(org.getId());
        shipment.setMilestoneEvents(List.of(milestone));

        shipment.setSender(sender);
        shipment.setServiceType(createService());
        shipment.setOrganization(org);
        shipment.setShipmentPackage(shipmentPackage);

        int notesLimit = 2000;
        String longNote = RandomStringUtils.randomAlphabetic(notesLimit + 1);
        shipment.setNotes(longNote);
        Set<ConstraintViolation<Object>> violations = validateModel(shipment);
        assertThat(violations).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("provideEtaStatusValue")
    void shipment_etaStatusValid_shouldNotHaveViolations(EtaStatus etaStatus) {
        Shipment shipment = new Shipment();
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setStatus("Status");
        order.setOrderIdLabel("ID-LABEL");
        order.setPickupStartTime(OffsetDateTime.now().format(ISO_FORMATTER));
        order.setPickupCommitTime(OffsetDateTime.now().plusHours(1).format(ISO_FORMATTER));
        order.setPickupTimezone("GMT+08:00");
        order.setDeliveryStartTime(OffsetDateTime.now().plusDays(1).format(ISO_FORMATTER));
        order.setDeliveryCommitTime(OffsetDateTime.now().plusDays(1).plusHours(1).format(ISO_FORMATTER));
        order.setDeliveryTimezone("GMT+08:00");
        shipment.setOrder(order);
        shipment.setShipmentTrackingId("dummy-code");

        Address address = new Address();
        address.setCountry("Country");
        address.setCity("City");
        address.setState("State");

        Consignee consignee = new Consignee();
        consignee.setName("Name");
        consignee.setEmail("test@email.com");
        shipment.setConsignee(consignee);

        Sender sender = new Sender();
        sender.setName("testName");
        sender.setEmail("test@email.com");

        Organization org = new Organization();
        org.setId(UUID.randomUUID().toString());
        org.setName("orgName");
        org.setCode("OrgCode");

        Package shipmentPackage = createPackage();

        shipment.setUserId(UUID.randomUUID().toString());
        shipment.setId(UUID.randomUUID().toString());
        shipment.setPickUpLocation("PickupLocation");
        shipment.setDeliveryLocation("deliveryLocation");

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneName("Description");
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setOrganizationId(org.getId());
        shipment.setMilestone(milestone);
        shipment.setMilestoneEvents(List.of(milestone));

        shipment.setSender(sender);
        shipment.setServiceType(createService());
        shipment.setOrganization(org);
        shipment.setShipmentPackage(shipmentPackage);
        shipment.setEtaStatus(etaStatus);
        Set<ConstraintViolation<Object>> violations = validateModel(shipment);
        assertThat(violations).isEmpty();
    }
}
