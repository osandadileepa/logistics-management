package com.quincus.networkmanagement.api.data;

import com.quincus.networkmanagement.api.constant.DimensionUnit;
import com.quincus.networkmanagement.api.constant.NodeType;
import com.quincus.networkmanagement.api.constant.TimeUnit;
import com.quincus.networkmanagement.api.constant.TransportType;
import com.quincus.networkmanagement.api.constant.VolumeUnit;
import com.quincus.networkmanagement.api.constant.WeightUnit;
import com.quincus.networkmanagement.api.domain.CapacityProfile;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Currency;
import com.quincus.networkmanagement.api.domain.Facility;
import com.quincus.networkmanagement.api.domain.MeasurementUnits;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.domain.OperatingHours;
import com.quincus.networkmanagement.api.domain.Partner;
import com.quincus.networkmanagement.api.domain.ShipmentProfile;
import com.quincus.networkmanagement.api.domain.ShipmentProfileExtension;
import com.quincus.networkmanagement.api.domain.VehicleType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class NetworkManagementTestData {

    public static Node dummyNode() {
        Node node = new Node();
        node.setNodeType(NodeType.BAGGAGE_CLAIM);
        node.setNodeCode(UUID.randomUUID().toString());
        node.setDescription("Dummy node description");
        node.setActive(true);
        node.setTags(List.of("refrigerated", "handle with care", "fragile"));
        node.setAddressLine1("Dummy address line 1");
        node.setAddressLine2("Dummy address line 2");
        node.setAddressLine3("Dummy address line 3");
        node.setTimezone("GMT");
        node.setFacility(dummyFacility());
        node.setOperatingHours(dummyOperatingHours());
        node.setShipmentProfile(dummyShipmentProfile());
        return node;
    }

    public static Partner dummyVendor() {
        Partner vendor = new Partner();
        vendor.setId(UUID.randomUUID().toString());
        vendor.setName("Dummy Vendor");
        return vendor;
    }

    public static Currency dummyCurrency() {
        Currency currency = new Currency();
        currency.setId(UUID.randomUUID().toString());
        currency.setName("Dummy Currency");
        return currency;
    }

    public static VehicleType dummyVehicleType() {
        VehicleType vehicleType = new VehicleType();
        vehicleType.setId(UUID.randomUUID().toString());
        vehicleType.setName("Dummy Vehicle Type");
        return vehicleType;
    }

    public static Facility dummyFacility() {
        Facility facility = new Facility();
        facility.setId(UUID.randomUUID().toString());
        facility.setName("Dummy Facility");
        return facility;
    }

    public static OperatingHours dummyOperatingHours() {
        OperatingHours operatingHours = new OperatingHours();
        operatingHours.setMonStartTime(LocalTime.of(10, 30));
        operatingHours.setMonEndTime(LocalTime.of(17, 30));
        operatingHours.setMonProcessingTime(2);
        operatingHours.setFriStartTime(LocalTime.of(10, 30));
        operatingHours.setFriEndTime(LocalTime.of(17, 30));
        operatingHours.setFriProcessingTime(2);
        operatingHours.setProcessingTimeUnit(TimeUnit.HOURS);
        return operatingHours;
    }

    public static ShipmentProfile dummyShipmentProfile() {
        ShipmentProfile shipmentProfile = new ShipmentProfile();
        shipmentProfile.setMaxLength(BigDecimal.valueOf(999999.0));
        shipmentProfile.setMinLength(BigDecimal.valueOf(0.0));
        shipmentProfile.setMaxWidth(BigDecimal.valueOf(999999.0));
        shipmentProfile.setMinWidth(BigDecimal.valueOf(0.0));
        shipmentProfile.setMaxHeight(BigDecimal.valueOf(999999.0));
        shipmentProfile.setMinHeight(BigDecimal.valueOf(0.0));
        shipmentProfile.setMaxWeight(BigDecimal.valueOf(999999.0));
        shipmentProfile.setMinWeight(BigDecimal.valueOf(0.0));
        return shipmentProfile;
    }

    public static ShipmentProfileExtension dummyShipmentProfileExtension() {
        ShipmentProfileExtension shipmentProfile = new ShipmentProfileExtension();
        shipmentProfile.setMaxLength(BigDecimal.valueOf(999999.0));
        shipmentProfile.setMinLength(BigDecimal.valueOf(0.0));
        shipmentProfile.setMaxWidth(BigDecimal.valueOf(999999.0));
        shipmentProfile.setMinWidth(BigDecimal.valueOf(0.0));
        shipmentProfile.setMaxHeight(BigDecimal.valueOf(999999.0));
        shipmentProfile.setMinHeight(BigDecimal.valueOf(0.0));
        shipmentProfile.setMaxWeight(BigDecimal.valueOf(999999.0));
        shipmentProfile.setMinWeight(BigDecimal.valueOf(0.0));
        shipmentProfile.setMaxSingleSide(BigDecimal.valueOf(999999.0));
        shipmentProfile.setMinSingleSide(BigDecimal.valueOf(0.0));
        shipmentProfile.setMaxLinearDim(BigDecimal.valueOf(999999.0));
        shipmentProfile.setMinLinearDim(BigDecimal.valueOf(0.0));
        shipmentProfile.setMaxVolume(BigDecimal.valueOf(999999.0));
        shipmentProfile.setMinVolume(BigDecimal.valueOf(0.0));
        return shipmentProfile;
    }

    public static CapacityProfile dummyCapacityProfile() {
        CapacityProfile capacityProfile = new CapacityProfile();
        capacityProfile.setMaxShipmentCount(999999);
        capacityProfile.setMaxWeight(BigDecimal.valueOf(999999.0));
        capacityProfile.setMaxVolume(BigDecimal.valueOf(999999.0));
        return capacityProfile;
    }

    public static MeasurementUnits dummyMeasurementUnits() {
        MeasurementUnits measurementUnits = new MeasurementUnits();
        measurementUnits.setWeightUnit(WeightUnit.KILOGRAMS);
        measurementUnits.setVolumeUnit(VolumeUnit.CUBIC_METERS);
        measurementUnits.setDimensionUnit(DimensionUnit.METERS);
        return measurementUnits;
    }

    public static Connection dummyGroundConnection() {
        Connection connection = new Connection();
        connection.setConnectionCode(UUID.randomUUID().toString());
        connection.setDepartureNode(dummyNode(UUID.randomUUID().toString()));
        connection.setArrivalNode(dummyNode(UUID.randomUUID().toString()));
        connection.setActive(true);
        connection.setTags(List.of("refrigerated", "handle with care", "fragile"));
        connection.setVendor(dummyVendor());
        connection.setVehicleType(dummyVehicleType());
        connection.setCurrency(dummyCurrency());
        connection.setCost(BigDecimal.valueOf(3000));
        connection.setShipmentProfile(dummyShipmentProfileExtension());
        connection.setTransportType(TransportType.GROUND);
        connection.setSchedules(List.of(
                "RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=MO,WE,FR;BYHOUR=10;BYMINUTE=30"
        ));
        connection.setDuration(60);
        return connection;
    }

    public static Connection dummyAirConnection() {
        Connection connection = new Connection();
        connection.setConnectionCode(UUID.randomUUID().toString());
        connection.setDepartureNode(dummyNode(UUID.randomUUID().toString()));
        connection.setArrivalNode(dummyNode(UUID.randomUUID().toString()));
        connection.setActive(true);
        connection.setTags(List.of("refrigerated", "handle with care", "fragile"));
        connection.setVendor(dummyVendor());
        connection.setVehicleType(dummyVehicleType());
        connection.setCurrency(dummyCurrency());
        connection.setCost(BigDecimal.valueOf(3000));
        connection.setShipmentProfile(dummyShipmentProfileExtension());
        connection.setTransportType(TransportType.AIR);
        connection.setAirLockoutDuration(60);
        connection.setAirRecoveryDuration(30);
        connection.setSchedules(List.of(
                "RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=MO,TU,WE,TH,FR;BYHOUR=7;BYMINUTE=30",
                "RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=SA,SU;BYHOUR=10;BYMINUTE=15"
        ));
        connection.setDuration(120);
        return connection;
    }

    public static Node dummyNode(String nodeId) {
        Node node = new Node();
        node.setId(nodeId);
        return node;
    }

}
