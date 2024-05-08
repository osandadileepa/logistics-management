package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity_;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.Tuple;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneSegmentTupleMapperTest {
    private final NetworkLaneSegmentTupleMapper networkLaneSegmentTupleMapper = new NetworkLaneSegmentTupleMapper();

    @Test
    void giveTuple_whenValuePresent_thenMapToNetworkLaneSegment() {
        Tuple tuple = mock(Tuple.class);

        when(tuple.get(BaseEntity_.ID, String.class)).thenReturn("id1");
        when(tuple.get(NetworkLaneSegmentEntity_.NETWORK_LANE_ID, String.class)).thenReturn("networkLaneId");
        when(tuple.get(NetworkLaneSegmentEntity_.TRANSPORT_TYPE, TransportType.class)).thenReturn(TransportType.GROUND);
        when(tuple.get(NetworkLaneSegmentEntity_.SEQUENCE, String.class)).thenReturn("1");
        when(tuple.get(NetworkLaneSegmentEntity_.AIRLINE, String.class)).thenReturn("Cebu Pacific");
        when(tuple.get(NetworkLaneSegmentEntity_.AIRLINE_CODE, String.class)).thenReturn("CEB");
        when(tuple.get(NetworkLaneSegmentEntity_.FLIGHT_NUMBER, String.class)).thenReturn("5J-451");
        when(tuple.get(NetworkLaneSegmentEntity_.VEHICLE_INFO, String.class)).thenReturn("VT-123");
        when(tuple.get(NetworkLaneSegmentEntity_.MASTER_WAYBILL, String.class)).thenReturn("123-123-123-123");
        when(tuple.get(NetworkLaneSegmentEntity_.PICKUP_INSTRUCTION, String.class)).thenReturn("Pickup Instructions");
        when(tuple.get(NetworkLaneSegmentEntity_.DELIVERY_INSTRUCTION, String.class)).thenReturn("Delivery Instructions");
        when(tuple.get(NetworkLaneSegmentEntity_.DURATION, BigDecimal.class)).thenReturn(BigDecimal.valueOf(20));
        when(tuple.get(NetworkLaneSegmentEntity_.DURATION_UNIT, UnitOfMeasure.class)).thenReturn(UnitOfMeasure.MINUTE);
        when(tuple.get(NetworkLaneSegmentEntity_.PICK_UP_TIME, String.class)).thenReturn("2022-12-16T16:27:02+07:00");
        when(tuple.get(NetworkLaneSegmentEntity_.PICK_UP_TIMEZONE, String.class)).thenReturn("UTC+08:00");
        when(tuple.get(NetworkLaneSegmentEntity_.DROP_OFF_TIME, String.class)).thenReturn("2022-12-17T16:27:02+07:00");
        when(tuple.get(NetworkLaneSegmentEntity_.DROP_OFF_TIMEZONE, String.class)).thenReturn("UTC+09:00");
        when(tuple.get(NetworkLaneSegmentEntity_.LOCK_OUT_TIME, String.class)).thenReturn("2022-12-18T16:27:02+07:00");
        when(tuple.get(NetworkLaneSegmentEntity_.LOCK_OUT_TIMEZONE, String.class)).thenReturn("UTC+08:00");
        when(tuple.get(NetworkLaneSegmentEntity_.DEPARTURE_TIME, String.class)).thenReturn("2022-12-19T16:27:02+07:00");
        when(tuple.get(NetworkLaneSegmentEntity_.DEPARTURE_TIMEZONE, String.class)).thenReturn("UTC+08:00");
        when(tuple.get(NetworkLaneSegmentEntity_.ARRIVAL_TIME, String.class)).thenReturn("2022-12-20T16:27:02+07:00");
        when(tuple.get(NetworkLaneSegmentEntity_.ARRIVAL_TIMEZONE, String.class)).thenReturn("UTC+09:00");
        when(tuple.get(NetworkLaneSegmentEntity_.RECOVERY_TIME, String.class)).thenReturn("2022-12-21T16:27:02+07:00");
        when(tuple.get(NetworkLaneSegmentEntity_.RECOVERY_TIMEZONE, String.class)).thenReturn("UTC+09:00");
        when(tuple.get(NetworkLaneSegmentEntity_.CALCULATED_MILEAGE, BigDecimal.class)).thenReturn(BigDecimal.valueOf(100));
        when(tuple.get(NetworkLaneSegmentEntity_.CALCULATED_MILEAGE_UNIT, UnitOfMeasure.class)).thenReturn(UnitOfMeasure.MILE);
        when(tuple.get(NetworkLaneSegmentEntity_.ORGANIZATION_ID, String.class)).thenReturn("orgId");

        NetworkLaneSegmentEntity entity = networkLaneSegmentTupleMapper.toEntity(tuple);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNotBlank().isEqualTo("id1");
        assertThat(entity.getOrganizationId()).isEqualTo("orgId");
        assertThat(entity.getNetworkLaneId()).isEqualTo("networkLaneId");
        assertThat(entity.getAirline()).isEqualTo("Cebu Pacific");
        assertThat(entity.getSequence()).isEqualTo("1");
        assertThat(entity.getAirlineCode()).isEqualTo("CEB");
        assertThat(entity.getTransportType()).isEqualTo(TransportType.GROUND);
        assertThat(entity.getCalculatedMileage()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(entity.getCalculatedMileageUnit()).isEqualTo(UnitOfMeasure.MILE);
        assertThat(entity.getDuration()).isEqualTo(BigDecimal.valueOf(20));
        assertThat(entity.getDurationUnit()).isEqualTo(UnitOfMeasure.MINUTE);
        assertThat(entity.getMasterWaybill()).isEqualTo("123-123-123-123");
        assertThat(entity.getPickupInstruction()).isEqualTo("Pickup Instructions");
        assertThat(entity.getDeliveryInstruction()).isEqualTo("Delivery Instructions");
        assertTimeAndTimezoneField(entity);
    }

    private void assertTimeAndTimezoneField(NetworkLaneSegmentEntity entity) {
        assertThat(entity.getPickUpTime()).isEqualTo("2022-12-16T16:27:02+07:00");
        assertThat(entity.getPickUpTimezone()).isEqualTo("UTC+08:00");
        assertThat(entity.getDropOffTime()).isEqualTo("2022-12-17T16:27:02+07:00");
        assertThat(entity.getDropOffTimezone()).isEqualTo("UTC+09:00");
        assertThat(entity.getLockOutTime()).isEqualTo("2022-12-18T16:27:02+07:00");
        assertThat(entity.getLockOutTimezone()).isEqualTo("UTC+08:00");
        assertThat(entity.getDepartureTime()).isEqualTo("2022-12-19T16:27:02+07:00");
        assertThat(entity.getDepartureTimezone()).isEqualTo("UTC+08:00");
        assertThat(entity.getArrivalTime()).isEqualTo("2022-12-20T16:27:02+07:00");
        assertThat(entity.getArrivalTimezone()).isEqualTo("UTC+09:00");
        assertThat(entity.getRecoveryTime()).isEqualTo("2022-12-21T16:27:02+07:00");
        assertThat(entity.getRecoveryTimezone()).isEqualTo("UTC+09:00");
    }
}
