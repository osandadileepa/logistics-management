package com.quincus.shipment.impl.test_utils;

import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.repository.constant.SegmentTupleAlias;
import com.quincus.shipment.impl.repository.constant.ShipmentTupleAlias;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity_;
import com.quincus.shipment.impl.repository.entity.OrderEntity_;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class TupleDataFactory {

    public static Tuple ofShipmentFromFlightDelay(String shipmentId, String shipmentTrackingId, String organizationId,
                                                  String orderId, String journeyId) {
        ListMapTuple tuple = new ListMapTuple(5);
        tuple.addField(ShipmentTupleAlias.SHIPMENT_ID, shipmentId);
        tuple.addField(ShipmentTupleAlias.SHIPMENT_TRACKING_ID, shipmentTrackingId);
        tuple.addField(ShipmentTupleAlias.ORGANIZATION_ID, organizationId);
        tuple.addField(ShipmentTupleAlias.ORDER_ID, orderId);
        tuple.addField(ShipmentTupleAlias.JOURNEY_ID, journeyId);
        return tuple;
    }

    public static Tuple ofSegmentsFromShipments(String segmentId, String journeyId, String refId, String sequence,
                                                String status, String transportType, String lockoutTime,
                                                String lockoutTimeTimezone) {
        ListMapTuple tuple = new ListMapTuple(8);
        tuple.addField(SegmentTupleAlias.SEGMENT_ID, segmentId);
        tuple.addField(SegmentTupleAlias.JOURNEY_ID, journeyId);
        tuple.addField(SegmentTupleAlias.REF_ID, refId);
        tuple.addField(SegmentTupleAlias.SEQUENCE, sequence);
        tuple.addField(SegmentTupleAlias.STATUS, status);
        tuple.addField(SegmentTupleAlias.TRANSPORT_TYPE, transportType);
        tuple.addField(SegmentTupleAlias.LOCKOUT_TIME, lockoutTime);
        tuple.addField(SegmentTupleAlias.LOCKOUT_TIMEZONE, lockoutTimeTimezone);
        return tuple;
    }

    public static CustomTuple ofPackageJourneyAirSegment(String id, String airline, String airlineCode, String flightNumber) {
        CustomTuple tuple = new CustomTuple();
        tuple.put(PackageJourneySegmentEntity_.ID, id);
        tuple.put(PackageJourneySegmentEntity_.AIRLINE, airline);
        tuple.put(PackageJourneySegmentEntity_.AIRLINE_CODE, airlineCode);
        tuple.put(PackageJourneySegmentEntity_.FLIGHT_NUMBER, flightNumber);
        return tuple;
    }

    public static List<Tuple> ofShipmentWithSegments(Shipment refShipment, int segmentSize) {
        List<Tuple> tupleList = new ArrayList<>(segmentSize);
        int ctr = 0;
        do {
            ListMapTuple tuple = new ListMapTuple(countNonNullFields(refShipment, Shipment.class) + 10);

            if (refShipment.getId() != null) {
                tuple.addField(ShipmentTupleAlias.SHIPMENT_ID, refShipment.getId());
                tuple.addField(BaseEntity_.ID, refShipment.getId());
            }
            Optional.ofNullable(refShipment.getId())
                    .ifPresent(id -> tuple.addField(ShipmentTupleAlias.SHIPMENT_ID, id));
            Optional.ofNullable(refShipment.getShipmentTrackingId())
                    .ifPresent(id -> tuple.addField(ShipmentTupleAlias.SHIPMENT_TRACKING_ID, id));
            Optional.ofNullable(refShipment.getOrganization()).map(Organization::getId)
                    .ifPresent(id -> tuple.addField(ShipmentTupleAlias.ORGANIZATION_ID, id));
            Optional.ofNullable(refShipment.getOrder()).map(Order::getId)
                    .ifPresent(id -> tuple.addField(ShipmentTupleAlias.ORDER_ID, id));
            ShipmentJourney journey = refShipment.getShipmentJourney();
            if (journey != null) {
                tuple.addField(ShipmentTupleAlias.JOURNEY_ID, journey.getJourneyId());

                if (!CollectionUtils.isEmpty(journey.getPackageJourneySegments())) {
                    PackageJourneySegment segment = journey.getPackageJourneySegments().get(ctr);

                    Optional.ofNullable(segment.getSegmentId())
                            .ifPresent(id -> tuple.addField(SegmentTupleAlias.SEGMENT_ID, id));
                    Optional.ofNullable(segment.getJourneyId())
                            .ifPresent(id -> tuple.addField(SegmentTupleAlias.JOURNEY_ID, id));
                    Optional.ofNullable(segment.getRefId())
                            .ifPresent(id -> tuple.addField(SegmentTupleAlias.REF_ID, id));
                    Optional.ofNullable(segment.getSequence())
                            .ifPresent(sequence -> tuple.addField(SegmentTupleAlias.SEQUENCE, sequence));
                    Optional.ofNullable(segment.getStatus()).map(Enum::name)
                            .ifPresent(status -> tuple.addField(SegmentTupleAlias.STATUS, status));
                    Optional.ofNullable(segment.getTransportType()).map(Enum::name)
                            .ifPresent(type -> tuple.addField(SegmentTupleAlias.TRANSPORT_TYPE, type));
                }
            }
            tupleList.add(tuple);
        } while (++ctr < segmentSize);

        return tupleList;
    }

    public static CustomTuple ofMilestone(MilestoneCode code, String milestoneTime) {
        CustomTuple tuple = new CustomTuple();
        tuple.put(MilestoneEntity_.MILESTONE_CODE, code);
        tuple.put(MilestoneEntity_.MILESTONE_TIME, milestoneTime);
        return tuple;
    }

    public static Tuple ofOrderWithIdAndStatus(String id, String status) {
        ListMapTuple tuple = new ListMapTuple(2);
        tuple.addField(OrderEntity_.ID, id);
        tuple.addField(OrderEntity_.STATUS, status);
        return tuple;
    }

    private static <T> int countNonNullFields(T object, Class<T> clazz) {
        int count = 0;

        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.get(object) != null) {
                    count++;
                }
            }
        } catch (IllegalAccessException e) {
        }
        return count;
    }

    private static class ListMapTuple implements Tuple {
        List<Map<String, ?>> store;

        ListMapTuple(int size) {
            store = new ArrayList<>(size);
        }

        <T> void addField(String fieldName, T obj) {
            Map<String, T> item = Collections.singletonMap(fieldName, obj);
            store.add(item);
        }

        @Override
        public <X> X get(TupleElement<X> tupleElement) {
            String alias = tupleElement.getAlias();
            return (X) get(alias);
        }

        @Override
        public <X> X get(String s, Class<X> aClass) {
            return (X) get(s);
        }

        @Override
        public Object get(String s) {
            for (Map<String, ?> entry : store) {
                if (!entry.containsKey(s)) {
                    continue;
                }
                return entry.get(s);
            }
            return null;
        }

        @Override
        public <X> X get(int i, Class<X> aClass) {
            return (X) get(i);
        }

        @Override
        public Object get(int i) {
            if (i >= store.size()) {
                return null;
            }
            Map<String, ?> entry = store.get(i);
            String[] keySets = entry.keySet().toArray(new String[0]);
            return entry.get(keySets[0]);
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public List<TupleElement<?>> getElements() {
            return null;
        }
    }

}
