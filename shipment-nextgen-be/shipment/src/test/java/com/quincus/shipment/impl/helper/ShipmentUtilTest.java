package com.quincus.shipment.impl.helper;

import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.test_utils.TupleDataFactory;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.Tuple;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ShipmentUtilTest {

    private static void addGroundSegment(List<PackageJourneySegment> segmentList, String pickupTime, String dropOffTime, String refId) {
        PackageJourneySegment newSegment = new PackageJourneySegment();
        newSegment.setRefId(refId);
        newSegment.setSequence(refId);
        newSegment.setTransportType(TransportType.GROUND);
        newSegment.setPickUpTime(pickupTime);
        newSegment.setDropOffTime(dropOffTime);
        segmentList.add(newSegment);
    }

    private static void addAirSegment(List<PackageJourneySegment> segmentList, String lockOutTime,
                                      String departureTime, String arrivalTime, String recoveryTime, String refId) {
        PackageJourneySegment newSegment = new PackageJourneySegment();
        newSegment.setRefId(refId);
        newSegment.setSequence(refId);
        newSegment.setTransportType(TransportType.AIR);
        newSegment.setLockOutTime(lockOutTime);
        newSegment.setDepartureTime(departureTime);
        newSegment.setArrivalTime(arrivalTime);
        newSegment.setRecoveryTime(recoveryTime);
        segmentList.add(newSegment);
    }

    private static Stream<Arguments> provideSegmentLists() {
        List<PackageJourneySegment> singleGround = new ArrayList<>();
        addGroundSegment(singleGround, "2023-01-25 09:46:06 +0800", "2023-01-25 10:46:06 +0800", "1");
        long expectedLeadTimeSingleGround = 3600;
        List<PackageJourneySegment> singleAir = new ArrayList<>();
        addAirSegment(singleAir, "2023-01-25 09:46:06 +0800", "2023-01-25 09:52:09 +0800", "2023-01-25 10:44:09 +0800", "2023-01-25 10:46:06 +0800", "1");
        long expectedLeadTimeSingleAir = 3600;
        List<PackageJourneySegment> groundToGround = new ArrayList<>();
        addGroundSegment(groundToGround, "2023-01-25 09:46:06 +0800", "2023-01-25 12:46:06 +0800", "1");
        addGroundSegment(groundToGround, "2023-01-25 17:46:06 +0800", "2023-01-26 09:46:06 +0800", "2");
        long expectedLeadTimeGroundToGround = 86400;
        List<PackageJourneySegment> groundToAir = new ArrayList<>();
        addGroundSegment(groundToAir, "2023-01-25 09:46:06 +0800", "2023-01-25 12:46:06 +0800", "1");
        addAirSegment(groundToAir, "2023-01-25 14:46:06 +0800", "2023-01-25 15:46:06 +0800", "2023-01-26 08:46:06 +0800", "2023-01-26 09:46:06 +0800", "2");
        long expectedLeadTimeGroundToAir = 86400;
        List<PackageJourneySegment> airToGround = new ArrayList<>();
        addAirSegment(airToGround, "2023-01-25 09:46:06 +0800", "2023-01-25 15:46:06 +0800", "2023-01-25 23:46:06 +0800", "2023-01-26 02:46:06 +0800", "1");
        addGroundSegment(airToGround, "2023-01-27 00:00:00 +0800", "2023-01-27 09:46:06 +0800", "2");
        long expectedLeadTimeAirToGround = 172800;
        List<PackageJourneySegment> airToAir = new ArrayList<>();
        addAirSegment(airToAir, "2023-01-25 09:46:06 +0800", "2023-01-25 12:46:06 +0800", "2023-01-26 12:46:06 +0800", "2023-01-26 22:46:06 +0800", "1");
        addAirSegment(airToAir, "2023-01-27 00:00:00 +0800", "2023-01-27 02:00:00 +0800", "2023-01-27 08:00:00 +0800", "2023-01-27 09:46:06 +0800", "2");
        long expectedLeadTimeAirToAir = 172800;
        List<PackageJourneySegment> groundAirGround = new ArrayList<>();
        addGroundSegment(groundAirGround, "2023-01-25 09:46:06 +0800", "2023-01-25 17:46:06 +0800", "1");
        addAirSegment(groundAirGround, "2023-01-26 09:46:06 +0800", "2023-01-26 17:46:06 +0800", "2023-01-27 09:46:06 +0800", "2023-01-27 17:46:06 +0800", "2");
        addGroundSegment(groundAirGround, "2023-01-28 00:46:06 +0800", "2023-01-28 09:46:06 +0800", "3");
        long expectedLeadTimeGroundAirGround = 259200;
        List<PackageJourneySegment> airGroundAir = new ArrayList<>();
        addAirSegment(airGroundAir, "2023-01-25 09:46:06 +0800", "2023-01-25 11:46:06 +0800", "2023-01-26 09:46:06 +0800", "2023-01-26 11:46:06 +0800", "1");
        addGroundSegment(airGroundAir, "2023-01-26 15:46:06 +0800", "2023-01-27 00:46:06 +0800", "2");
        addAirSegment(airGroundAir, "2023-01-27 02:46:06 +0800", "2023-01-27 04:46:06 +0800", "2023-01-28 00:00:00 +0800", "2023-01-28 09:46:06 +0800", "3");
        long expectedLeadTimeAirGroundAir = 259200;
        List<PackageJourneySegment> withTimezoneDiff = new ArrayList<>();
        addGroundSegment(withTimezoneDiff, "2023-01-25 09:46:06 +0800", "", "1");
        addAirSegment(withTimezoneDiff, "", "", "", "", "2");
        addGroundSegment(withTimezoneDiff, "", "2023-01-28 09:46:06 +0600", "3");
        long expectedLeadTimeWithTimezoneDiff = 266400;
        return Stream.of(
                Arguments.of(Named.of("Single Segment (Ground)", singleGround), expectedLeadTimeSingleGround),
                Arguments.of(Named.of("Single Segment (Air)", singleAir), expectedLeadTimeSingleAir),
                Arguments.of(Named.of("Two Segments (Ground -> Ground)", groundToGround), expectedLeadTimeGroundToGround),
                Arguments.of(Named.of("Two Segments (Ground -> Air)", groundToAir), expectedLeadTimeGroundToAir),
                Arguments.of(Named.of("Two Segments (Air -> Ground)", airToGround), expectedLeadTimeAirToGround),
                Arguments.of(Named.of("Two Segments (Air -> Air)", airToAir), expectedLeadTimeAirToAir),
                Arguments.of(Named.of("Three Segments (Ground -> Air -> Ground)", groundAirGround), expectedLeadTimeGroundAirGround),
                Arguments.of(Named.of("Three Segments (Air -> Ground -> Air)", airGroundAir), expectedLeadTimeAirGroundAir),
                Arguments.of(Named.of("Segments with Timezone Diff", withTimezoneDiff), expectedLeadTimeWithTimezoneDiff)
        );
    }

    private static Stream<Arguments> provideShipmentsWithActiveSegments() {
        Shipment shp1 = new Shipment();
        shp1.setShipmentJourney(new ShipmentJourney());
        PackageJourneySegment seg1 = new PackageJourneySegment();
        seg1.setSegmentId("uuid-seg1");
        seg1.setRefId("1");
        seg1.setSequence("1");
        seg1.setStatus(SegmentStatus.PLANNED);
        shp1.getShipmentJourney().addPackageJourneySegment(seg1);

        Shipment shp2 = new Shipment();
        shp2.setShipmentJourney(new ShipmentJourney());
        PackageJourneySegment seg2a = new PackageJourneySegment();
        seg2a.setSegmentId("uuid-seg2a");
        seg2a.setRefId("1");
        seg2a.setSequence("1");
        seg2a.setStatus(SegmentStatus.COMPLETED);
        PackageJourneySegment seg2b = new PackageJourneySegment();
        seg2b.setSegmentId("uuid-seg2b");
        seg2b.setRefId("2");
        seg2b.setSequence("2");
        seg2b.setStatus(SegmentStatus.PLANNED);
        shp2.getShipmentJourney().addPackageJourneySegment(seg2a);
        shp2.getShipmentJourney().addPackageJourneySegment(seg2b);

        Shipment shp3 = new Shipment();
        shp3.setShipmentJourney(new ShipmentJourney());
        PackageJourneySegment seg3a = new PackageJourneySegment();
        seg3a.setSegmentId("uuid-seg3a");
        seg3a.setRefId("1");
        seg3a.setSequence("1");
        seg3a.setStatus(SegmentStatus.CANCELLED);
        PackageJourneySegment seg3b = new PackageJourneySegment();
        seg3b.setSegmentId("uuid-seg3b");
        seg3b.setRefId("2");
        seg3b.setSequence("2");
        seg3b.setStatus(SegmentStatus.IN_PROGRESS);
        shp3.getShipmentJourney().addPackageJourneySegment(seg3a);
        shp3.getShipmentJourney().addPackageJourneySegment(seg3b);

        Shipment shp4 = new Shipment();
        shp4.setShipmentJourney(new ShipmentJourney());
        PackageJourneySegment seg4a = new PackageJourneySegment();
        seg4a.setSegmentId("uuid-seg4a");
        seg4a.setRefId("1");
        seg4a.setSequence("1");
        PackageJourneySegment seg4b = new PackageJourneySegment();
        seg4b.setSegmentId("uuid-seg4b");
        seg4b.setRefId("2");
        seg4b.setSequence("2");
        seg4b.setStatus(SegmentStatus.FAILED);
        shp4.getShipmentJourney().addPackageJourneySegment(seg4a);
        shp4.getShipmentJourney().addPackageJourneySegment(seg4b);

        Shipment shp5 = new Shipment();
        shp5.setShipmentJourney(new ShipmentJourney());
        PackageJourneySegment seg5 = new PackageJourneySegment();
        seg5.setSegmentId("uuid-seg5");
        seg5.setRefId("1");
        seg5.setSequence("1");
        seg5.setStatus(SegmentStatus.COMPLETED);
        shp5.getShipmentJourney().addPackageJourneySegment(seg5);

        Shipment shp6 = new Shipment();
        shp6.setShipmentJourney(new ShipmentJourney());
        PackageJourneySegment seg6a = new PackageJourneySegment();
        seg6a.setSegmentId("uuid-seg6a");
        seg6a.setRefId("1");
        seg6a.setSequence("1");
        seg6a.setStatus(SegmentStatus.PLANNED);
        seg6a.setDeleted(true);
        PackageJourneySegment seg6b = new PackageJourneySegment();
        seg6b.setSegmentId("uuid-seg6b");
        seg6b.setRefId("2");
        seg6b.setSequence("2");
        seg6b.setStatus(SegmentStatus.PLANNED);
        seg6b.setDeleted(false);
        shp6.getShipmentJourney().addPackageJourneySegment(seg6a);
        shp6.getShipmentJourney().addPackageJourneySegment(seg6b);

        return Stream.of(
                Arguments.of(shp1, seg1),
                Arguments.of(shp2, seg2b),
                Arguments.of(shp3, seg3b),
                Arguments.of(shp4, seg4b),
                Arguments.of(shp5, null),
                Arguments.of(shp6, seg6b)
        );
    }

    private static Stream<Arguments> provideShipmentEntitiesWithActiveSegments() {
        ShipmentEntity shp1 = new ShipmentEntity();
        shp1.setShipmentJourney(new ShipmentJourneyEntity());
        PackageJourneySegmentEntity seg1 = new PackageJourneySegmentEntity();
        seg1.setId("uuid-seg1");
        seg1.setRefId("1");
        seg1.setSequence("1");
        seg1.setStatus(SegmentStatus.PLANNED);
        shp1.getShipmentJourney().addPackageJourneySegment(seg1);

        ShipmentEntity shp2 = new ShipmentEntity();
        shp2.setShipmentJourney(new ShipmentJourneyEntity());
        PackageJourneySegmentEntity seg2a = new PackageJourneySegmentEntity();
        seg2a.setId("uuid-seg2a");
        seg2a.setRefId("1");
        seg2a.setSequence("1");
        seg2a.setStatus(SegmentStatus.COMPLETED);
        PackageJourneySegmentEntity seg2b = new PackageJourneySegmentEntity();
        seg2b.setId("uuid-seg2b");
        seg2b.setRefId("2");
        seg2b.setSequence("2");
        seg2b.setStatus(SegmentStatus.PLANNED);
        shp2.getShipmentJourney().addPackageJourneySegment(seg2a);
        shp2.getShipmentJourney().addPackageJourneySegment(seg2b);

        ShipmentEntity shp3 = new ShipmentEntity();
        shp3.setShipmentJourney(new ShipmentJourneyEntity());
        PackageJourneySegmentEntity seg3a = new PackageJourneySegmentEntity();
        seg3a.setId("uuid-seg3a");
        seg3a.setRefId("1");
        seg3a.setSequence("1");
        seg3a.setStatus(SegmentStatus.CANCELLED);
        PackageJourneySegmentEntity seg3b = new PackageJourneySegmentEntity();
        seg3b.setId("uuid-seg3b");
        seg3b.setRefId("2");
        seg3b.setSequence("2");
        seg3b.setStatus(SegmentStatus.IN_PROGRESS);
        shp3.getShipmentJourney().addPackageJourneySegment(seg3a);
        shp3.getShipmentJourney().addPackageJourneySegment(seg3b);

        ShipmentEntity shp4 = new ShipmentEntity();
        shp4.setShipmentJourney(new ShipmentJourneyEntity());
        PackageJourneySegmentEntity seg4a = new PackageJourneySegmentEntity();
        seg4a.setId("uuid-seg4a");
        seg4a.setRefId("1");
        seg4a.setSequence("1");
        PackageJourneySegmentEntity seg4b = new PackageJourneySegmentEntity();
        seg4b.setId("uuid-seg4b");
        seg4b.setRefId("2");
        seg4b.setSequence("2");
        seg4b.setStatus(SegmentStatus.FAILED);
        shp4.getShipmentJourney().addPackageJourneySegment(seg4a);
        shp4.getShipmentJourney().addPackageJourneySegment(seg4b);

        ShipmentEntity shp5 = new ShipmentEntity();
        shp5.setShipmentJourney(new ShipmentJourneyEntity());
        PackageJourneySegmentEntity seg5 = new PackageJourneySegmentEntity();
        seg5.setId("uuid-seg5");
        seg5.setRefId("1");
        seg5.setSequence("1");
        seg5.setStatus(SegmentStatus.COMPLETED);
        shp5.getShipmentJourney().addPackageJourneySegment(seg5);

        ShipmentEntity shp6 = new ShipmentEntity();
        shp6.setShipmentJourney(new ShipmentJourneyEntity());
        PackageJourneySegmentEntity seg6a = new PackageJourneySegmentEntity();
        seg6a.setId("uuid-seg6a");
        seg6a.setRefId("1");
        seg6a.setSequence("1");
        seg6a.setStatus(SegmentStatus.PLANNED);
        PackageJourneySegmentEntity seg6b = new PackageJourneySegmentEntity();
        seg6b.setId("uuid-seg6b");
        seg6b.setRefId("2");
        seg6b.setSequence("2");
        seg6b.setStatus(SegmentStatus.PLANNED);
        shp6.getShipmentJourney().addPackageJourneySegment(seg6a);
        shp6.getShipmentJourney().addPackageJourneySegment(seg6b);
        shp6.getShipmentJourney().getPackageJourneySegments().get(0).setDeleted(true);

        return Stream.of(
                Arguments.of(shp1, seg1),
                Arguments.of(shp2, seg2b),
                Arguments.of(shp3, seg3b),
                Arguments.of(shp4, seg4b),
                Arguments.of(shp5, null),
                Arguments.of(shp6, seg6b)
        );
    }

    private static String formatSegmentDateTime(OffsetDateTime dateTime) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
        return dateTime.format(dtf);
    }

    @ParameterizedTest
    @MethodSource("provideSegmentLists")
    void getTotalJourneyLeadTime_variousSegments_shouldReturnPositiveLeadTime(List<PackageJourneySegment> segmentList,
                                                                              long expectedLeadTime) {
        ShipmentJourney journeyDomain = new ShipmentJourney();
        journeyDomain.setPackageJourneySegments(segmentList);

        final long leadTime = ShipmentUtil.getTotalJourneyLeadTime(journeyDomain);
        assertThat(leadTime)
                .isPositive()
                .isEqualTo(expectedLeadTime);
    }

    @Test
    void getSla_hasPickupAndDeliveryTime_shouldReturnPositiveSla() {
        Shipment shipmentDomain = new Shipment();
        Order orderDomain = new Order();
        orderDomain.setPickupStartTime(LocalDateTime.now().minusHours(20).toString());
        orderDomain.setPickupCommitTime(LocalDateTime.now().minusHours(10).toString());
        orderDomain.setPickupTimezone("GMT+08:00");
        orderDomain.setDeliveryStartTime(LocalDateTime.now().toString());
        orderDomain.setDeliveryCommitTime(LocalDateTime.now().plusHours(30).toString());
        orderDomain.setDeliveryTimezone("GMT+10:00");
        shipmentDomain.setOrder(orderDomain);

        final long sla = ShipmentUtil.getSla(shipmentDomain);
        assertThat(sla)
                .isEqualTo(48 * 60 * 60);
    }

    @Test
    void isShipmentDelayed_newShipmentAndTimesSync_shouldReturnFalse() {
        Shipment shipmentDomain = new Shipment();
        Order orderDomain = new Order();
        orderDomain.setPickupStartTime(LocalDateTime.of(2023, 2, 25, 9, 46, 6, 0).toString());
        orderDomain.setPickupCommitTime(LocalDateTime.of(2023, 2, 25, 12, 46, 6, 0).toString());
        orderDomain.setPickupTimezone("GMT+08:00");
        orderDomain.setDeliveryStartTime(LocalDateTime.of(2023, 2, 27, 9, 46, 6, 0).toString());
        orderDomain.setDeliveryCommitTime(LocalDateTime.of(2023, 2, 27, 11, 46, 6, 0).toString());
        orderDomain.setDeliveryTimezone("GMT+10:00");
        shipmentDomain.setOrder(orderDomain);

        ShipmentJourney journeyDomain = new ShipmentJourney();
        PackageJourneySegment segmentDomain = new PackageJourneySegment();
        segmentDomain.setRefId("1");
        segmentDomain.setType(SegmentType.LAST_MILE);
        segmentDomain.setTransportType(TransportType.GROUND);
        segmentDomain.setStatus(SegmentStatus.PLANNED);
        segmentDomain.setPickUpTime("2023-02-25 09:46:06 +0800");
        segmentDomain.setDropOffTime("2023-02-27 11:46:06 +1000");
        journeyDomain.setPackageJourneySegments(List.of(segmentDomain));
        shipmentDomain.setShipmentJourney(journeyDomain);

        final boolean isDelayed = ShipmentUtil.isShipmentDelayed(shipmentDomain);
        assertThat(isDelayed)
                .isFalse();
        assertThat(journeyDomain.getAlerts())
                .isNullOrEmpty();
    }

    @Test
    void isShipmentDelayed_SegmentTimeExceed_shouldReturnTrue() {
        Shipment shipmentDomain = new Shipment();
        Order orderDomain = new Order();
        orderDomain.setPickupStartTime(LocalDateTime.of(2023, 2, 25, 9, 46, 6, 0).toString());
        orderDomain.setPickupCommitTime(LocalDateTime.of(2023, 2, 25, 12, 46, 6, 0).toString());
        orderDomain.setPickupTimezone("GMT+08:00");
        orderDomain.setDeliveryStartTime(LocalDateTime.of(2023, 2, 27, 9, 46, 6, 0).toString());
        orderDomain.setDeliveryCommitTime(LocalDateTime.of(2023, 2, 27, 11, 46, 6, 0).toString());
        orderDomain.setDeliveryTimezone("GMT+10:00");
        shipmentDomain.setOrder(orderDomain);

        ShipmentJourney journeyDomain = new ShipmentJourney();
        PackageJourneySegment segmentDomain = new PackageJourneySegment();
        segmentDomain.setRefId("1");
        segmentDomain.setType(SegmentType.LAST_MILE);
        segmentDomain.setTransportType(TransportType.GROUND);
        segmentDomain.setStatus(SegmentStatus.PLANNED);
        segmentDomain.setPickUpTime("2023-02-25 09:46:06 +0800");
        segmentDomain.setDropOffTime("2023-02-28 11:46:06 +1000");
        journeyDomain.setPackageJourneySegments(List.of(segmentDomain));
        shipmentDomain.setShipmentJourney(journeyDomain);

        final boolean isDelayed = ShipmentUtil.isShipmentDelayed(shipmentDomain);
        assertThat(isDelayed)
                .isTrue();
        assertThat(journeyDomain.getAlerts())
                .hasSize(1);
    }

    @Test
    void updateEtaStatusFromSegmentStatuses_AllSegmentsPlanned_shouldHaveEtaStatusNull() {
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setRefId("1");
        segment1.setSequence("1");
        segment1.setStatus(SegmentStatus.PLANNED);
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setRefId("2");
        segment2.setSequence("2");
        segment2.setStatus(SegmentStatus.PLANNED);
        PackageJourneySegment segment3 = new PackageJourneySegment();
        segment3.setRefId("3");
        segment3.setSequence("3");
        segment3.setStatus(SegmentStatus.PLANNED);
        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setShipmentJourney(new ShipmentJourney());
        shipmentDomain.getShipmentJourney().setPackageJourneySegments(List.of(segment1, segment2, segment3));
        shipmentDomain.setEtaStatus(EtaStatus.DELAYED);

        Shipment updatedShipment = ShipmentUtil.updateEtaStatusFromSegmentStatuses(shipmentDomain);

        assertThat(updatedShipment.getEtaStatus())
                .withFailMessage("ETA Status is not cleared.")
                .isNull();
    }

    @Test
    void updateEtaStatusFromSegmentStatuses_SegmentInProgressNoDelay_shouldHaveEtaStatusOnTime() {
        OffsetDateTime refTime = OffsetDateTime.now();
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setRefId("1");
        segment1.setSequence("1");
        segment1.setTransportType(TransportType.GROUND);
        segment1.setStatus(SegmentStatus.IN_PROGRESS);
        segment1.setPickUpTime(formatSegmentDateTime(refTime.minusHours(8)));
        segment1.setDropOffTime(formatSegmentDateTime(refTime.minusHours(6)));
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setRefId("2");
        segment2.setSequence("2");
        segment2.setTransportType(TransportType.GROUND);
        segment2.setStatus(SegmentStatus.PLANNED);
        segment2.setPickUpTime(formatSegmentDateTime(refTime.minusHours(4)));
        segment2.setDropOffTime(formatSegmentDateTime(refTime));
        PackageJourneySegment segment3 = new PackageJourneySegment();
        segment3.setRefId("3");
        segment3.setSequence("3");
        segment3.setTransportType(TransportType.GROUND);
        segment3.setStatus(SegmentStatus.PLANNED);
        segment3.setPickUpTime(formatSegmentDateTime(refTime.plusHours(4)));
        segment3.setDropOffTime(formatSegmentDateTime(refTime.plusHours(8)));
        Shipment shipmentDomain = new Shipment();
        Order orderDomain = new Order();
        orderDomain.setPickupStartTime(refTime.minusDays(3).toLocalDateTime().toString());
        orderDomain.setPickupCommitTime(refTime.minusDays(2).toLocalDateTime().toString());
        orderDomain.setPickupTimezone("GMT+08:00");
        orderDomain.setDeliveryStartTime(refTime.toLocalDateTime().toString());
        orderDomain.setDeliveryCommitTime(refTime.plusDays(1).toLocalDateTime().toString());
        orderDomain.setDeliveryTimezone("GMT+08:00");
        shipmentDomain.setOrder(orderDomain);
        shipmentDomain.setShipmentJourney(new ShipmentJourney());
        shipmentDomain.getShipmentJourney().setPackageJourneySegments(List.of(segment1, segment2, segment3));
        shipmentDomain.setEtaStatus(EtaStatus.DELAYED);

        Shipment updatedShipment = ShipmentUtil.updateEtaStatusFromSegmentStatuses(shipmentDomain);

        assertThat(updatedShipment.getEtaStatus())
                .withFailMessage("ETA Status is incorrectly set.")
                .isEqualTo(EtaStatus.ON_TIME);
    }

    @Test
    void updateEtaStatusFromSegmentStatuses_SegmentInProgressDelayed_shouldHaveEtaStatusDelayed() {
        OffsetDateTime refTime = OffsetDateTime.now();
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setRefId("1");
        segment1.setSequence("1");
        segment1.setTransportType(TransportType.GROUND);
        segment1.setStatus(SegmentStatus.IN_PROGRESS);
        segment1.setPickUpTime(formatSegmentDateTime(refTime.minusDays(3)));
        segment1.setDropOffTime(formatSegmentDateTime(refTime.minusDays(2)));
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setRefId("2");
        segment2.setSequence("2");
        segment2.setTransportType(TransportType.GROUND);
        segment2.setStatus(SegmentStatus.PLANNED);
        segment2.setPickUpTime(formatSegmentDateTime(refTime.minusHours(4)));
        segment2.setDropOffTime(formatSegmentDateTime(refTime));
        PackageJourneySegment segment3 = new PackageJourneySegment();
        segment3.setRefId("3");
        segment3.setSequence("3");
        segment3.setTransportType(TransportType.GROUND);
        segment3.setStatus(SegmentStatus.PLANNED);
        segment3.setPickUpTime(formatSegmentDateTime(refTime.plusDays(2)));
        segment3.setDropOffTime(formatSegmentDateTime(refTime.plusDays(3)));
        Shipment shipmentDomain = new Shipment();
        Order orderDomain = new Order();
        orderDomain.setPickupStartTime(refTime.minusDays(3).toLocalDateTime().toString());
        orderDomain.setPickupCommitTime(refTime.minusDays(2).toLocalDateTime().toString());
        orderDomain.setPickupTimezone("GMT+08:00");
        orderDomain.setDeliveryStartTime(refTime.toLocalDateTime().toString());
        orderDomain.setDeliveryCommitTime(refTime.plusDays(1).toLocalDateTime().toString());
        orderDomain.setDeliveryTimezone("GMT+08:00");
        shipmentDomain.setOrder(orderDomain);
        shipmentDomain.setShipmentJourney(new ShipmentJourney());
        shipmentDomain.getShipmentJourney().setPackageJourneySegments(List.of(segment1, segment2, segment3));
        shipmentDomain.setEtaStatus(EtaStatus.ON_TIME);

        Shipment updatedShipment = ShipmentUtil.updateEtaStatusFromSegmentStatuses(shipmentDomain);

        assertThat(updatedShipment.getEtaStatus())
                .withFailMessage("ETA Status is incorrectly set.")
                .isEqualTo(EtaStatus.DELAYED);
    }

    @Test
    void updateEtaStatusFromSegmentStatuses_SegmentsOtherStatus_shouldRetainEtaStatus() {
        OffsetDateTime refTime = OffsetDateTime.now();
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setRefId("1");
        segment1.setSequence("1");
        segment1.setTransportType(TransportType.GROUND);
        segment1.setStatus(SegmentStatus.COMPLETED);
        segment1.setPickUpTime(formatSegmentDateTime(refTime.minusHours(8)));
        segment1.setDropOffTime(formatSegmentDateTime(refTime.minusHours(6)));
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setRefId("2");
        segment2.setSequence("2");
        segment2.setTransportType(TransportType.GROUND);
        segment2.setStatus(SegmentStatus.COMPLETED);
        segment2.setPickUpTime(formatSegmentDateTime(refTime.minusHours(4)));
        segment2.setDropOffTime(formatSegmentDateTime(refTime));
        PackageJourneySegment segment3 = new PackageJourneySegment();
        segment3.setRefId("3");
        segment3.setSequence("3");
        segment3.setTransportType(TransportType.GROUND);
        segment3.setStatus(SegmentStatus.COMPLETED);
        segment3.setPickUpTime(formatSegmentDateTime(refTime.plusHours(4)));
        segment3.setDropOffTime(formatSegmentDateTime(refTime.plusHours(8)));
        Shipment shipmentDomain = new Shipment();
        Order orderDomain = new Order();
        orderDomain.setPickupStartTime(refTime.minusDays(3).toLocalDateTime().toString());
        orderDomain.setPickupCommitTime(refTime.minusDays(2).toLocalDateTime().toString());
        orderDomain.setPickupTimezone("GMT+08:00");
        orderDomain.setDeliveryStartTime(refTime.plusDays(2).toLocalDateTime().toString());
        orderDomain.setDeliveryCommitTime(refTime.plusDays(3).toLocalDateTime().toString());
        orderDomain.setDeliveryTimezone("GMT+08:00");
        shipmentDomain.setOrder(orderDomain);
        shipmentDomain.setShipmentJourney(new ShipmentJourney());
        shipmentDomain.getShipmentJourney().setPackageJourneySegments(List.of(segment1, segment2, segment3));
        EtaStatus currStatus = EtaStatus.ON_TIME;
        shipmentDomain.setEtaStatus(EtaStatus.ON_TIME);

        Shipment updatedShipment = ShipmentUtil.updateEtaStatusFromSegmentStatuses(shipmentDomain);

        assertThat(updatedShipment.getEtaStatus())
                .withFailMessage("ETA Status is changed.")
                .isEqualTo(currStatus);
    }

    @Test
    void convertOrderTimezonesToUtc_nonUtcTimezone_shouldConvertToUtc() {
        String nonUtcTimezone1 = "GMT+08:00";
        String nonUtcTimezone2 = "GMT-11:00";
        String expectedUtcTimezone1 = "UTC+08:00";
        String expectedUtcTimezone2 = "UTC-11:00";

        Order order = new Order();
        order.setPickupStartTime(LocalDateTime.now().toString());
        order.setPickupTimezone(nonUtcTimezone1);
        order.setDeliveryStartTime(LocalDateTime.now().toString());
        order.setDeliveryTimezone(nonUtcTimezone2);

        ShipmentUtil.convertOrderTimezonesToUtc(order);

        assertThat(order.getPickupTimezone())
                .withFailMessage("Pickup Timezone not converted to UTC")
                .isEqualTo(expectedUtcTimezone1);

        assertThat(order.getDeliveryTimezone())
                .withFailMessage("Delivery Timezone not converted to UTC")
                .isEqualTo(expectedUtcTimezone2);
    }

    @Test
    void filterShipmentFromSegment_shipmentContainsSegment_shouldReturnCorrespondingShipment() {
        List<Shipment> shipments = new ArrayList<>();
        String shipmentId = "shipmentX";
        String segmentId = "segmentX";
        Shipment shipment1 = new Shipment();
        shipment1.setId("shipment1");
        shipment1.setShipmentJourney(new ShipmentJourney());
        shipment1.getShipmentJourney().setPackageJourneySegments(List.of(new PackageJourneySegment()));
        shipment1.getShipmentJourney().getPackageJourneySegments().get(0).setSegmentId("testing");
        shipment1.getShipmentJourney().getPackageJourneySegments().get(0).setRefId("1");
        Shipment shipment2 = new Shipment();
        shipment2.setId(shipmentId);
        shipment2.setShipmentJourney(new ShipmentJourney());
        PackageJourneySegment segment1 = new PackageJourneySegment();
        segment1.setSegmentId("testing2");
        segment1.setRefId("1");
        segment1.setSequence("1");
        PackageJourneySegment segment2 = new PackageJourneySegment();
        segment2.setSegmentId(segmentId);
        segment2.setRefId("2");
        segment2.setSequence("2");
        shipment2.getShipmentJourney().setPackageJourneySegments(List.of(segment1, segment2));
        shipments.add(shipment1);
        shipments.add(shipment2);

        Optional<Shipment> optionalShipment = ShipmentUtil.filterShipmentFromSegmentId(shipments, segmentId);

        assertThat(optionalShipment).isNotEmpty();
        assertThat(optionalShipment.get().getId()).isEqualTo(shipmentId);
    }

    @Test
    void convertObjectArrayToShipmentLimited_shipmentTuple_shouldReturnShipment() {
        String shipmentId = "SHP1";
        String shipmentTrackingId = "SHP-1";
        String organizationId = "org1";
        String orderId = "order1";
        String journeyId = "journey1";

        Tuple tuple = TupleDataFactory.ofShipmentFromFlightDelay(shipmentId, shipmentTrackingId, organizationId, orderId,
                journeyId);

        Shipment result = ShipmentUtil.convertObjectArrayToShipmentLimited(tuple);
        assertThat(result).isNotNull();
        assertThat(result.getOrganization()).isNotNull();
        assertThat(result.getOrder()).isNotNull();
        assertThat(result.getShipmentJourney()).isNotNull();
        assertThat(result.getId()).isEqualTo(shipmentId);
        assertThat(result.getShipmentTrackingId()).isEqualTo(shipmentTrackingId);
        assertThat(result.getOrganization().getId()).isEqualTo(organizationId);
        assertThat(result.getOrder().getId()).isEqualTo(orderId);
        assertThat(result.getShipmentJourney().getJourneyId()).isEqualTo(journeyId);
    }

    @Test
    void convertObjectArrayToShipmentLimited_noResult_shouldReturnNull() {
        assertThat(ShipmentUtil.convertObjectArrayToShipmentLimited(null)).isNull();
    }

    @Test
    void tupleToShipmentWithMultipleSegmentsLimited_shipmentTuple_shouldReturnShipment() {
        Shipment refShipment = new Shipment();
        refShipment.setId("shipment-id1");
        refShipment.setOrganization(new Organization());
        refShipment.getOrganization().setId("org-id1");
        ShipmentJourney refJourney = new ShipmentJourney();
        refJourney.setJourneyId("journey-id1");
        PackageJourneySegment refSegment1 = new PackageJourneySegment();
        refSegment1.setSegmentId("segment-id1");
        refSegment1.setRefId("1");
        refSegment1.setSequence("0");
        PackageJourneySegment refSegment2 = new PackageJourneySegment();
        refSegment2.setSegmentId("segment-id2");
        refSegment2.setRefId("2");
        refSegment2.setSequence("1");
        refJourney.addPackageJourneySegment(refSegment1);
        refJourney.addPackageJourneySegment(refSegment2);
        refShipment.setShipmentJourney(refJourney);
        List<Tuple> tupleList = TupleDataFactory.ofShipmentWithSegments(refShipment, 2);

        Shipment shipment = ShipmentUtil.tupleToShipmentWithMultipleSegmentsLimited(tupleList);
        assertThat(shipment).isNotNull();
        ShipmentJourney journey = shipment.getShipmentJourney();
        assertThat(journey).isNotNull();
        Organization organization = shipment.getOrganization();
        assertThat(organization).isNotNull();
        assertThat(shipment.getId()).isEqualTo(refShipment.getId());
        assertThat(organization.getId()).isEqualTo(refShipment.getOrganization().getId());
        assertThat(journey.getJourneyId()).isEqualTo(refShipment.getShipmentJourney().getJourneyId());
        assertThat(journey.getPackageJourneySegments().get(0).getSegmentId())
                .isEqualTo(refShipment.getShipmentJourney().getPackageJourneySegments().get(0).getSegmentId());
        assertThat(journey.getPackageJourneySegments().get(0).getRefId())
                .isEqualTo(refShipment.getShipmentJourney().getPackageJourneySegments().get(0).getRefId());
        assertThat(journey.getPackageJourneySegments().get(0).getSequence())
                .isEqualTo(refShipment.getShipmentJourney().getPackageJourneySegments().get(0).getSequence());
    }

    @Test
    void tupleToShipmentWithMultipleSegmentsLimited_noResult_shouldReturnNull() {
        assertThat(ShipmentUtil.tupleToShipmentWithMultipleSegmentsLimited(null)).isNull();
    }

    @ParameterizedTest
    @MethodSource("provideShipmentsWithActiveSegments")
    void getActiveSegment_shipmentArgs_shouldReturnActiveSegment(Shipment shipment,
                                                                 PackageJourneySegment expectedSegment) {
        assertThat(ShipmentUtil.getActiveSegment(shipment)).isEqualTo(expectedSegment);
    }

    @ParameterizedTest
    @MethodSource("provideShipmentEntitiesWithActiveSegments")
    void getActiveSegmentEntity_shipmentArgs_shouldReturnActiveSegment(ShipmentEntity shipment,
                                                                       PackageJourneySegmentEntity expectedSegment) {
        assertThat(ShipmentUtil.getActiveSegmentEntity(shipment)).isEqualTo(expectedSegment);
    }

    @Test
    void getPickupInstruction_presentInList_shouldReturnInstructionValue() {
        Instruction instruction1 = new Instruction();
        instruction1.setSource(Instruction.SOURCE_ORDER);
        instruction1.setValue("Pickup - Fragile");
        instruction1.setApplyTo(InstructionApplyToType.PICKUP);

        Instruction instruction2 = new Instruction();
        instruction2.setSource(Instruction.SOURCE_ORDER);
        instruction2.setValue("Delivery - Fragile");
        instruction2.setApplyTo(InstructionApplyToType.DELIVERY);

        Instruction instruction3 = new Instruction();
        instruction3.setSource("shp");
        instruction3.setValue("Journey - Fragile");
        instruction3.setApplyTo(InstructionApplyToType.JOURNEY);

        Shipment shipment = new Shipment();
        shipment.setInstructions(List.of(instruction1, instruction2, instruction3));
        Optional<String> instructionOpt = ShipmentUtil.getPickupInstruction(shipment);
        assertThat(instructionOpt).isPresent().contains(instruction1.getValue());
    }

    @Test
    void getPickupInstruction_notPresentInList_shouldReturnEmpty() {
        Instruction instruction2 = new Instruction();
        instruction2.setSource(Instruction.SOURCE_ORDER);
        instruction2.setValue("Delivery - Fragile");
        instruction2.setApplyTo(InstructionApplyToType.DELIVERY);

        Instruction instruction3 = new Instruction();
        instruction3.setSource("shp");
        instruction3.setValue("Journey - Fragile");
        instruction3.setApplyTo(InstructionApplyToType.JOURNEY);

        Shipment shipment = new Shipment();
        shipment.setInstructions(List.of(instruction2, instruction3));
        Optional<String> instructionOpt = ShipmentUtil.getPickupInstruction(shipment);
        assertThat(instructionOpt).isEmpty();
    }

    @Test
    void getPickupInstruction_noInstructionList_shouldReturnEmpty() {
        assertThat(ShipmentUtil.getPickupInstruction(new Shipment())).isEmpty();
    }

    @Test
    void getDeliveryInstruction_presentInList_shouldReturnInstructionValue() {
        Instruction instruction1 = new Instruction();
        instruction1.setSource(Instruction.SOURCE_ORDER);
        instruction1.setValue("Pickup - Fragile");
        instruction1.setApplyTo(InstructionApplyToType.PICKUP);

        Instruction instruction2 = new Instruction();
        instruction2.setSource(Instruction.SOURCE_ORDER);
        instruction2.setValue("Delivery - Fragile");
        instruction2.setApplyTo(InstructionApplyToType.DELIVERY);

        Instruction instruction3 = new Instruction();
        instruction3.setSource("shp");
        instruction3.setValue("Journey - Fragile");
        instruction3.setApplyTo(InstructionApplyToType.JOURNEY);

        Shipment shipment = new Shipment();
        shipment.setInstructions(List.of(instruction1, instruction2, instruction3));
        Optional<String> instructionOpt = ShipmentUtil.getDeliveryInstruction(shipment);
        assertThat(instructionOpt).isPresent().contains(instruction2.getValue());
    }

    @Test
    void getDeliveryInstruction_notPresentInList_shouldReturnEmpty() {
        Instruction instruction1 = new Instruction();
        instruction1.setSource(Instruction.SOURCE_ORDER);
        instruction1.setValue("Pickup - Fragile");
        instruction1.setApplyTo(InstructionApplyToType.PICKUP);

        Instruction instruction3 = new Instruction();
        instruction3.setSource("shp");
        instruction3.setValue("Journey - Fragile");
        instruction3.setApplyTo(InstructionApplyToType.JOURNEY);

        Shipment shipment = new Shipment();
        shipment.setInstructions(List.of(instruction1, instruction3));
        Optional<String> instructionOpt = ShipmentUtil.getDeliveryInstruction(shipment);
        assertThat(instructionOpt).isEmpty();
    }

    @Test
    void getDeliveryInstruction_noInstructionList_shouldReturnEmpty() {
        assertThat(ShipmentUtil.getDeliveryInstruction(new Shipment())).isEmpty();
    }
}
