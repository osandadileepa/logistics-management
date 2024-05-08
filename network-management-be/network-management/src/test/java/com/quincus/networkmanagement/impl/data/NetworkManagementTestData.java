package com.quincus.networkmanagement.impl.data;

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
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;
import com.quincus.networkmanagement.impl.repository.entity.ConnectionEntity;
import com.quincus.networkmanagement.impl.repository.entity.NodeEntity;
import com.quincus.qportal.model.QPortalFacility;
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
        node.setId(UUID.randomUUID().toString());
        node.setNodeType(NodeType.BAGGAGE_CLAIM);
        node.setNodeCode(UUID.randomUUID().toString());
        node.setDescription("Dummy node description");
        node.setActive(true);
        node.setTags(List.of("refrigerated", "handle with care", "fragile"));
        node.setAddressLine1("Dummy address line 1");
        node.setAddressLine2("Dummy address line 2");
        node.setAddressLine3("Dummy address line 3");
        node.setTimezone("Asia/Manila UTC+08:00");
        node.setFacility(dummyFacility());
        node.setVendor(dummyVendor());
        node.setOperatingHours(dummyOperatingHours());
        node.setShipmentProfile(dummyShipmentProfile());
        return node;
    }

    public static Node dummyNode(String nodeId) {
        Node node = new Node();
        node.setId(nodeId);
        return node;
    }

    public static Node dummyNode(BigDecimal lat, BigDecimal lon) {
        Node node = dummyNode();
        node.getFacility().setLat(lat);
        node.getFacility().setLon(lon);
        return node;
    }

    public static Partner dummyVendor() {
        Partner vendor = new Partner();
        vendor.setName("Dummy Vendor");
        return vendor;
    }

    public static Currency dummyCurrency() {
        Currency currency = new Currency();
        currency.setId(UUID.randomUUID().toString());
        currency.setCode("USD");
        currency.setExchangeRate(BigDecimal.valueOf(1.0));
        currency.setName("US Dollar");
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
        facility.setCode("Dummy Code");
        facility.setLat(new BigDecimal("-2.223222"));
        facility.setLon(new BigDecimal("1.223322"));
        facility.setTimezone("Asia/Manila UTC+08:00");
        return facility;
    }

    public static QPortalFacility dummyQPortalFacility() {
        QPortalFacility qPortalFacility = new QPortalFacility();
        qPortalFacility.setId(UUID.randomUUID().toString());
        qPortalFacility.setName("Dummy QPortal Facility");
        qPortalFacility.setCode("Dummy Code");
        qPortalFacility.setLat(-2.223222);
        qPortalFacility.setLon(1.223322);
        qPortalFacility.setTimezoneTimeInGmt("Asia/Manila UTC+08:00");
        return qPortalFacility;
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

    public static Connection dummyConnection(Node departure, Node arrival) {
        Connection connection = new Connection();
        connection.setDepartureNode(departure);
        connection.setArrivalNode(arrival);
        return connection;
    }

    public static Connection dummyGroundConnection() {
        Connection connection = new Connection();
        connection.setId(UUID.randomUUID().toString());
        connection.setConnectionCode(UUID.randomUUID().toString());
        connection.setDepartureNode(dummyNode());
        connection.setArrivalNode(dummyNode());
        connection.setActive(true);
        connection.setTags(List.of("refrigerated", "handle with care", "fragile"));
        connection.setVendor(dummyVendor());
        connection.setVehicleType(dummyVehicleType());
        connection.setCurrency(dummyCurrency());
        connection.setCost(BigDecimal.valueOf(2000));
        connection.setShipmentProfile(dummyShipmentProfileExtension());
        connection.setTransportType(TransportType.GROUND);
        connection.setSchedules(List.of(
                "RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=MO,WE,FR;BYHOUR=10;BYMINUTE=30"
        ));
        connection.setDuration(60);
        connection.setCapacityProfile(dummyCapacityProfile());
        connection.setMeasurementUnits(dummyMeasurementUnits());
        return connection;
    }

    public static Connection dummyAirConnection() {
        Connection connection = new Connection();
        connection.setId(UUID.randomUUID().toString());
        connection.setConnectionCode(UUID.randomUUID().toString());
        connection.setDepartureNode(dummyNode());
        connection.setArrivalNode(dummyNode());
        connection.setActive(true);
        connection.setTags(List.of("refrigerated", "handle with care", "fragile"));
        connection.setVendor(dummyVendor());
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
        connection.setDuration(240);
        connection.setCapacityProfile(dummyCapacityProfile());
        connection.setMeasurementUnits(dummyMeasurementUnits());
        return connection;
    }

    public static NodeEntity dummyNodeEntity(Node node) {
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setId(node.getId());
        nodeEntity.setNodeCode(node.getNodeCode());
        return nodeEntity;
    }

    public static ConnectionEntity dummyConnectionEntity(Connection connection) {
        ConnectionEntity connectionEntity = new ConnectionEntity();
        connectionEntity.setId(connection.getId());
        connectionEntity.setConnectionCode(connection.getConnectionCode());
        return connectionEntity;
    }

    public static Edge dummyEdge() {
        Edge edge = new Edge();
        edge.setFlightNumber(UUID.randomUUID().toString());
        edge.setDepartureHub(UUID.randomUUID().toString());
        edge.setDepartureLat(28.66866);
        edge.setDepartureLon(77.10194);
        edge.setDepartureTime(1694428200);
        edge.setArrivalHub(UUID.randomUUID().toString());
        edge.setArrivalLat(1.37236);
        edge.setArrivalLon(103.93286);
        edge.setDistance(new BigDecimal("4160.7920"));
        edge.setArrivalTime(1694435400);
        edge.setDuration(120);
        edge.setCost(new BigDecimal("200.50"));
        edge.setCapacity(100);
        edge.setShipmentProfiles(dummyShipmentProfileExtension());
        edge.setCapacityProfile(dummyCapacityProfile());
        edge.setMeasurementUnits(dummyMeasurementUnits());
        return edge;
    }
}
