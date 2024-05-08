package com.quincus.shipment.impl.helper;

import com.quincus.ext.DateTimeUtil;
import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.constant.AlertMessage;
import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.repository.constant.SegmentTupleAlias;
import com.quincus.shipment.impl.repository.constant.ShipmentTupleAlias;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.Tuple;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@NoArgsConstructor(access = AccessLevel.NONE)
public final class ShipmentUtil {
    public static long getTotalJourneyLeadTime(ShipmentJourney journeyDomain) {
        List<PackageJourneySegment> segments = journeyDomain.getPackageJourneySegments();
        PackageJourneySegment firstSegment = segments.get(0);
        PackageJourneySegment lastSegment = segments.get(segments.size() - 1);

        String startTimeStr;
        if (firstSegment.getTransportType() == TransportType.AIR) {
            startTimeStr = firstSegment.getLockOutTime();
        } else {
            startTimeStr = firstSegment.getPickUpTime();
        }

        String endTimeStr;
        if (lastSegment.getTransportType() == TransportType.AIR) {
            endTimeStr = lastSegment.getRecoveryTime();
        } else {
            endTimeStr = lastSegment.getDropOffTime();
        }

        ZonedDateTime startZonedDateTime = DateTimeUtil.parseZonedDateTime(startTimeStr);
        ZonedDateTime endZonedDateTime = DateTimeUtil.parseZonedDateTime(endTimeStr);

        return SECONDS.between(startZonedDateTime, endZonedDateTime);
    }

    public static long getSla(Shipment shipmentDomain) {
        Order orderDomain = shipmentDomain.getOrder();
        LocalDateTime startDateTime = DateTimeUtil.convertStringToLocalDateTime(orderDomain.getPickupStartTime());
        LocalDateTime endDateTime = DateTimeUtil.convertStringToLocalDateTime(orderDomain.getDeliveryCommitTime());

        ZoneId startZoneId = DateTimeUtil.parseZoneId(orderDomain.getPickupTimezone());
        ZonedDateTime startZonedDateTime = ZonedDateTime.of(startDateTime, startZoneId);

        ZoneId endZoneId = DateTimeUtil.parseZoneId(orderDomain.getDeliveryTimezone());
        ZonedDateTime endZonedDateTime = ZonedDateTime.of(endDateTime, endZoneId);

        return SECONDS.between(startZonedDateTime, endZonedDateTime);
    }

    public static boolean isShipmentDelayed(Shipment shipmentDomain) {
        long sla = getSla(shipmentDomain);
        long leadTime = getTotalJourneyLeadTime(shipmentDomain.getShipmentJourney());

        if (leadTime > sla) {
            shipmentDomain.setEtaStatus(EtaStatus.DELAYED);
            addDelayAlert(shipmentDomain.getShipmentJourney(), AlertType.ERROR);
            return true;
        }

        return false;
    }

    public static Shipment updateEtaStatusFromSegmentStatuses(Shipment shipmentDomain) {
        ShipmentJourney journeyDomain = shipmentDomain.getShipmentJourney();
        List<PackageJourneySegment> segmentDomains = journeyDomain.getPackageJourneySegments();
        boolean isAllSegmentPlanned = true;
        for (PackageJourneySegment segmentDomain : segmentDomains) {
            if (SegmentStatus.IN_PROGRESS == segmentDomain.getStatus()) {
                if (isShipmentDelayed(shipmentDomain)) {
                    return shipmentDomain;
                }
                shipmentDomain.setEtaStatus(EtaStatus.ON_TIME);
                return shipmentDomain;
            } else if (SegmentStatus.PLANNED != segmentDomain.getStatus()) {
                isAllSegmentPlanned = false;
            }
        }
        if (isAllSegmentPlanned) {
            clearEtaStatus(shipmentDomain);
            return shipmentDomain;
        }
        isShipmentDelayed(shipmentDomain);
        return shipmentDomain;
    }

    public static void clearEtaStatus(Shipment shipmentDomain) {
        shipmentDomain.setEtaStatus(null);
    }

    public static void convertOrderTimezonesToUtc(Order orderDomain) {
        String pickupTimezoneUtc = DateTimeUtil.convertTimezoneToUtc(orderDomain.getPickupTimezone(), DateTimeUtil.convertStringToLocalDateTime(orderDomain.getPickupStartTime()));
        String deliveryTimezoneUtc = DateTimeUtil.convertTimezoneToUtc(orderDomain.getDeliveryTimezone(), DateTimeUtil.convertStringToLocalDateTime(orderDomain.getDeliveryStartTime()));

        orderDomain.setPickupTimezone(pickupTimezoneUtc);
        orderDomain.setDeliveryTimezone(deliveryTimezoneUtc);
    }

    public static Optional<Shipment> filterShipmentFromSegmentId(List<Shipment> shipments, String segmentId) {
        return shipments.stream()
                .filter(shipment -> isShipmentContainSegmentId(shipment, segmentId))
                .findFirst();
    }

    public static Shipment convertObjectArrayToShipmentLimited(Tuple tuple) {
        if (tuple == null) {
            return null;
        }

        Shipment shipment = new Shipment();
        shipment.setId(tuple.get(ShipmentTupleAlias.SHIPMENT_ID, String.class));
        shipment.setShipmentTrackingId(tuple.get(ShipmentTupleAlias.SHIPMENT_TRACKING_ID, String.class));

        Organization organization = new Organization();
        organization.setId(tuple.get(ShipmentTupleAlias.ORGANIZATION_ID, String.class));
        shipment.setOrganization(organization);

        Order order = new Order();
        order.setId(tuple.get(ShipmentTupleAlias.ORDER_ID, String.class));
        shipment.setOrder(order);

        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId(tuple.get(ShipmentTupleAlias.JOURNEY_ID, String.class));
        journey.setPackageJourneySegments(new ArrayList<>());
        shipment.setShipmentJourney(journey);

        return shipment;
    }

    public static Shipment tupleToShipmentWithMultipleSegmentsLimited(List<Tuple> tupleList) {
        if (CollectionUtils.isEmpty(tupleList)) {
            return null;
        }

        Tuple refTuple = tupleList.get(0);

        Shipment shipment = new Shipment();
        shipment.setId(refTuple.get(BaseEntity_.ID, String.class));

        Organization organization = new Organization();
        organization.setId(refTuple.get(ShipmentTupleAlias.ORGANIZATION_ID, String.class));
        shipment.setOrganization(organization);

        ShipmentJourney journey = new ShipmentJourney();
        journey.setJourneyId(refTuple.get(ShipmentTupleAlias.JOURNEY_ID, String.class));

        List<PackageJourneySegment> segments = tupleList.stream()
                .map(ShipmentUtil::convertObjectArrayToSegmentLimited)
                .collect(Collectors.toCollection(ArrayList::new));
        journey.setPackageJourneySegments(segments);
        shipment.setShipmentJourney(journey);

        return shipment;
    }

    public static PackageJourneySegment convertObjectArrayToSegmentLimited(Tuple tuple) {
        PackageJourneySegment segment = new PackageJourneySegment();

        segment.setSegmentId(tuple.get(SegmentTupleAlias.SEGMENT_ID, String.class));
        segment.setRefId(tuple.get(SegmentTupleAlias.REF_ID, String.class));
        segment.setSequence(tuple.get(SegmentTupleAlias.SEQUENCE, String.class));

        return segment;
    }

    public static void addSegmentsToShipments(List<Shipment> shipments, List<PackageJourneySegment> segments) {
        Map<String, List<Shipment>> shipmentMap = shipments.stream()
                .filter(s -> Objects.nonNull(s.getShipmentJourney())
                        && Objects.nonNull(s.getShipmentJourney().getJourneyId()))
                .collect(Collectors.groupingBy(s -> s.getShipmentJourney().getJourneyId()));

        Map<String, List<PackageJourneySegment>> segmentMap = segments.stream()
                .collect(Collectors.groupingBy(PackageJourneySegment::getJourneyId));

        shipmentMap.forEach((journeyId, shipmentList) -> {
            List<PackageJourneySegment> pjsList = segmentMap.get(journeyId);
            if (pjsList != null) {
                ShipmentJourney shipmentJourney = shipmentList.get(0).getShipmentJourney();
                shipmentJourney.setPackageJourneySegments(pjsList);
                shipmentList.forEach(shipment -> shipment.setShipmentJourney(shipmentJourney));
            }
        });
    }

    public static PackageJourneySegment getActiveSegment(Shipment shipment) {
        return shipment.getShipmentJourney()
                .getPackageJourneySegments()
                .stream()
                .filter(Predicate.not(PackageJourneySegment::isDeleted))
                .filter(e -> e.getStatus() != null)
                .filter(e -> (SegmentStatus.COMPLETED != e.getStatus()) && (SegmentStatus.CANCELLED != e.getStatus()))
                .findFirst().orElse(null);
    }

    public static PackageJourneySegmentEntity getActiveSegmentEntity(ShipmentEntity shipmentEntity) {
        return shipmentEntity.getShipmentJourney()
                .getPackageJourneySegments()
                .stream().sorted(Comparator.comparing(PackageJourneySegmentEntity::getSequence))
                .filter(Predicate.not(PackageJourneySegmentEntity::isDeleted))
                .filter(e -> e.getStatus() != null)
                .filter(e -> (SegmentStatus.COMPLETED != e.getStatus()) && (SegmentStatus.CANCELLED != e.getStatus()))
                .findFirst().orElse(null);
    }

    public static Optional<String> getPickupInstruction(Shipment shipment) {
        return getSpecifiedInstruction(InstructionApplyToType.PICKUP, shipment);
    }

    public static Optional<String> getDeliveryInstruction(Shipment shipment) {
        return getSpecifiedInstruction(InstructionApplyToType.DELIVERY, shipment);
    }

    private static boolean isShipmentContainSegmentId(Shipment shipment, String segmentId) {
        List<PackageJourneySegment> segments = shipment.getShipmentJourney().getPackageJourneySegments();
        long matchedSegmentsCount = segments.stream()
                .filter(s -> StringUtils.equals(s.getSegmentId(), segmentId))
                .count();
        return matchedSegmentsCount > 0;
    }

    private static void addDelayAlert(ShipmentJourney journeyDomain, AlertType alertType) {
        List<Alert> alerts = journeyDomain.getAlerts();
        if (CollectionUtils.isEmpty(alerts)) {
            alerts = new ArrayList<>();
            journeyDomain.setAlerts(alerts);
        }
        alerts.add(new Alert(AlertMessage.SHIPMENT_LEAD_TIME_EXCEED_SLA, alertType));
    }

    private static Optional<String> getSpecifiedInstruction(InstructionApplyToType type, Shipment shipment) {
        if (CollectionUtils.isEmpty(shipment.getInstructions())) {
            return Optional.empty();
        }

        return getSpecifiedInstruction(type, shipment.getInstructions()).map(Instruction::getValue);
    }

    private static Optional<Instruction> getSpecifiedInstruction(InstructionApplyToType type,
                                                                 List<Instruction> instructions) {
        return instructions.stream().filter(instruction -> instruction.getApplyTo().equals(type)).findFirst();
    }

    public static UnitOfMeasure convertCalculatedMileageUomStringToUom(String unitOfMeasure) {
        if (unitOfMeasure == null) {
            return null;
        }
        return switch (unitOfMeasure) {
            case Root.DISTANCE_UOM_METRIC -> UnitOfMeasure.KM;
            case Root.DISTANCE_UOM_IMPERIAL -> UnitOfMeasure.MILE;
            default -> null;
        };
    }
}
