package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity_;
import org.springframework.stereotype.Component;

import javax.persistence.Tuple;
import java.math.BigDecimal;

@Component
public class NetworkLaneSegmentTupleMapper {
    public NetworkLaneSegmentEntity toEntity(Tuple tuple) {
        NetworkLaneSegmentEntity entity = new NetworkLaneSegmentEntity();
        entity.setId(tuple.get(BaseEntity_.ID, String.class));
        entity.setNetworkLaneId(tuple.get(NetworkLaneSegmentEntity_.NETWORK_LANE_ID, String.class));
        entity.setTransportType(tuple.get(NetworkLaneSegmentEntity_.TRANSPORT_TYPE, TransportType.class));
        entity.setSequence(tuple.get(NetworkLaneSegmentEntity_.SEQUENCE, String.class));
        entity.setAirline(tuple.get(NetworkLaneSegmentEntity_.AIRLINE, String.class));
        entity.setAirlineCode(tuple.get(NetworkLaneSegmentEntity_.AIRLINE_CODE, String.class));
        entity.setFlightNumber(tuple.get(NetworkLaneSegmentEntity_.FLIGHT_NUMBER, String.class));
        entity.setVehicleInfo(tuple.get(NetworkLaneSegmentEntity_.VEHICLE_INFO, String.class));
        entity.setMasterWaybill(tuple.get(NetworkLaneSegmentEntity_.MASTER_WAYBILL, String.class));
        entity.setPickupInstruction(tuple.get(NetworkLaneSegmentEntity_.PICKUP_INSTRUCTION, String.class));
        entity.setDeliveryInstruction(tuple.get(NetworkLaneSegmentEntity_.DELIVERY_INSTRUCTION, String.class));
        entity.setDuration(tuple.get(NetworkLaneSegmentEntity_.DURATION, BigDecimal.class));
        entity.setDurationUnit(tuple.get(NetworkLaneSegmentEntity_.DURATION_UNIT, UnitOfMeasure.class));
        entity.setPickUpTime(tuple.get(NetworkLaneSegmentEntity_.PICK_UP_TIME, String.class));
        entity.setPickUpTimezone(tuple.get(NetworkLaneSegmentEntity_.PICK_UP_TIMEZONE, String.class));
        entity.setDropOffTime(tuple.get(NetworkLaneSegmentEntity_.DROP_OFF_TIME, String.class));
        entity.setDropOffTimezone(tuple.get(NetworkLaneSegmentEntity_.DROP_OFF_TIMEZONE, String.class));
        entity.setLockOutTime(tuple.get(NetworkLaneSegmentEntity_.LOCK_OUT_TIME, String.class));
        entity.setLockOutTimezone(tuple.get(NetworkLaneSegmentEntity_.LOCK_OUT_TIMEZONE, String.class));
        entity.setDepartureTime(tuple.get(NetworkLaneSegmentEntity_.DEPARTURE_TIME, String.class));
        entity.setDepartureTimezone(tuple.get(NetworkLaneSegmentEntity_.DEPARTURE_TIMEZONE, String.class));
        entity.setArrivalTime(tuple.get(NetworkLaneSegmentEntity_.ARRIVAL_TIME, String.class));
        entity.setArrivalTimezone(tuple.get(NetworkLaneSegmentEntity_.ARRIVAL_TIMEZONE, String.class));
        entity.setRecoveryTime(tuple.get(NetworkLaneSegmentEntity_.RECOVERY_TIME, String.class));
        entity.setRecoveryTimezone(tuple.get(NetworkLaneSegmentEntity_.RECOVERY_TIMEZONE, String.class));
        entity.setCalculatedMileage(tuple.get(NetworkLaneSegmentEntity_.CALCULATED_MILEAGE, BigDecimal.class));
        entity.setCalculatedMileageUnit(tuple.get(NetworkLaneSegmentEntity_.CALCULATED_MILEAGE_UNIT, UnitOfMeasure.class));
        entity.setOrganizationId(tuple.get(MultiTenantEntity_.ORGANIZATION_ID, String.class));
        return entity;
    }
}
