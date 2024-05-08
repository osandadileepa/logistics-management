package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.constant.MeasurementUnit;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ShipmentToOrderMapperImplTest {

    @Test
    void mapShipmentDomainToShipmentPathMessage_validArguments_shouldReturnShipmentPathMessage() {
        Shipment shipmentDomain = createShipmentDomain();

        ShipmentToOrderMapper shipmentToOrderMapper = new ShipmentToOrderMapperImpl();
        var shipmentPathMessage = shipmentToOrderMapper.mapShipmentDomainToShipmentPathMessage(shipmentDomain);

        assertThat(shipmentPathMessage.getId())
                .withFailMessage("shipment ID mismatch.")
                .isEqualTo(shipmentDomain.getId());
        assertThat(shipmentPathMessage.getOrganizationId())
                .withFailMessage("organization ID mismatch.")
                .isEqualTo(shipmentDomain.getOrganization().getId());
        assertThat(shipmentPathMessage.getOrderId())
                .withFailMessage("order ID mismatch.")
                .isEqualTo(shipmentDomain.getOrder().getId());

        var shipmentPathList = shipmentPathMessage.getShipmentPath();
        assertThat(shipmentPathList).hasSize(1);

        assertThat(shipmentPathList.get(0).getId())
                .withFailMessage("shipment path ID mismatch.")
                .isEqualTo(shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(0).getSegmentId());

        assertThat(shipmentPathList.get(0).getTransportType())
                .withFailMessage("shipment path transport type mismatch.")
                .isEqualTo(shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(0).getTransportType().toString());

        assertThat(shipmentPathList.get(0).getHubId())
                .withFailMessage("shipment path hub id mismatch.")
                .isEqualTo(shipmentDomain.getShipmentJourney().getPackageJourneySegments().get(0).getStartFacility().getId());
    }

    @Test
    void mapShipmentDomainToShipmentPathMessage_argumentNullSubObjects_shouldReturnShipmentPathMessage() {
        var shipmentId = "SHP-1";
        var shipmentDomain = new Shipment();
        shipmentDomain.setId(shipmentId);
        var order = new Order();
        order.setId("order");
        shipmentDomain.setOrder(order);
        ShipmentToOrderMapper shipmentToOrderMapper = new ShipmentToOrderMapperImpl();
        var org = new Organization();
        org.setId("org");
        shipmentDomain.setOrganization(org);
        var shipmentPathMessage = shipmentToOrderMapper.mapShipmentDomainToShipmentPathMessage(shipmentDomain);

        assertThat(shipmentPathMessage).isNotNull();

        assertThat(shipmentPathMessage.getId()).isEqualTo(shipmentId);
        assertThat(shipmentPathMessage.getOrganizationId()).isNotNull();
        assertThat(shipmentPathMessage.getOrderId()).isNotNull();

        var shipmentPathList = shipmentPathMessage.getShipmentPath();
        assertThat(shipmentPathList).isEmpty();
    }

    @Test
    void mapShipmentDomainToShipmentPathMessage_nullArguments_shouldReturnNull() {
        ShipmentToOrderMapper shipmentToOrderMapper = new ShipmentToOrderMapperImpl();
        var shipmentPathMessage = shipmentToOrderMapper.mapShipmentDomainToShipmentPathMessage(null);

        assertThat(shipmentPathMessage).isNull();
    }

    @Test
    void mapShipmentDomainToPackageDimensions_validArguments_shouldReturnPackageDimensionsMessage() {
        Shipment shipmentDomain = createShipmentDomain();

        ShipmentToOrderMapper shipmentToOrderMapper = new ShipmentToOrderMapperImpl();
        var packageDimensions = shipmentToOrderMapper.mapShipmentDomainToPackageDimensions(shipmentDomain);

        assertThat(packageDimensions.getPackageId()).isEqualTo(shipmentDomain.getShipmentPackage().getId());
        assertThat(packageDimensions.getOrgId()).isEqualTo(shipmentDomain.getOrganization().getId());
        assertThat(packageDimensions.getGrossWeight()).isEqualTo(shipmentDomain.getShipmentPackage().getDimension().getGrossWeight());
    }

    @Test
    void mapShipmentDomainToMilestoneMessage_validArguments_shouldReturnMilestoneMessage() {
        Shipment shipmentDomain = createShipmentDomain();

        ShipmentToOrderMapper shipmentToOrderMapper = new ShipmentToOrderMapperImpl();
        var milestoneMessage = shipmentToOrderMapper.mapShipmentDomainToMilestoneMessage(shipmentDomain);

        assertThat(milestoneMessage.getPackageId()).isEqualTo(shipmentDomain.getShipmentPackage().getId());
        assertThat(milestoneMessage.getOrganizationId()).isEqualTo(shipmentDomain.getOrganization().getId());
        assertThat(milestoneMessage.isActive()).isTrue();
    }

    @Test
    void mapShipmentDomainToShipmentCancelMessage_validArguments_shouldReturnShipmentCancelMessage() {
        Shipment shipmentDomain = createShipmentDomain();

        ShipmentToOrderMapper shipmentToOrderMapper = new ShipmentToOrderMapperImpl();
        var shipmentCancelMessage = shipmentToOrderMapper.mapShipmentDomainToShipmentCancelMessage(shipmentDomain);

        assertThat(shipmentCancelMessage.getShipmentId()).isEqualTo(shipmentDomain.getId());
        assertThat(shipmentCancelMessage.getOrganisationId()).isEqualTo(shipmentDomain.getOrganization().getId());
        assertThat(shipmentCancelMessage.getOrderId()).isEqualTo(shipmentDomain.getOrder().getId());
    }

    private Shipment createShipmentDomain() {
        String orgId = "ORG1";
        var organizationDomain = new Organization();
        organizationDomain.setId(orgId);

        String orderId = "ORDER1";
        var orderDomain = new Order();
        orderDomain.setId(orderId);

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
}