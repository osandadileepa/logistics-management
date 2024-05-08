package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "network_lane_segment")
public class NetworkLaneSegmentEntity extends MultiTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "network_lane_id", referencedColumnName = "id")
    private NetworkLaneEntity networkLane;

    @Column(name = "network_lane_id", insertable = false, updatable = false)
    private String networkLaneId;

    @Column(name = "sequence", length = 45)
    private String sequence;

    @Column(name = "transport_type", length = 45)
    @Enumerated(EnumType.STRING)
    private TransportType transportType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "partner_id", referencedColumnName = "id")
    @NotFound(action = NotFoundAction.IGNORE)
    private PartnerEntity partner;

    @Column(name = "vehicle_info", length = 50)
    private String vehicleInfo;

    @Column(name = "flight_number", length = 50)
    private String flightNumber;

    @Column(name = "airline", length = 65)
    private String airline;

    @Column(name = "airline_code", length = 50)
    private String airlineCode;

    @Column(name = "master_waybill", length = 50)
    private String masterWaybill;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "start_location_hierarchy", referencedColumnName = "id")
    private LocationHierarchyEntity startLocationHierarchy;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "end_location_hierarchy", referencedColumnName = "id")
    private LocationHierarchyEntity endLocationHierarchy;

    @Column(name = "pick_up_instruction", length = 4000)
    private String pickupInstruction;

    @Column(name = "delivery_instruction", length = 4000)
    private String deliveryInstruction;

    @Column(name = "duration", precision = 15, scale = 4)
    private BigDecimal duration;

    @Column(name = "duration_unit", length = 60)
    @Enumerated(EnumType.STRING)
    private UnitOfMeasure durationUnit;

    @Column(name = "pick_up_time", length = 50)
    private String pickUpTime;
    @Column(name = "pick_up_timezone")
    private String pickUpTimezone;

    @Column(name = "drop_off_time", length = 50)
    private String dropOffTime;
    @Column(name = "drop_off_timezone")
    private String dropOffTimezone;

    @Column(name = "lock_out_time", length = 50)
    private String lockOutTime;
    @Column(name = "lock_out_timezone")
    private String lockOutTimezone;

    @Column(name = "departure_time", length = 50)
    private String departureTime;
    @Column(name = "departure_timezone")
    private String departureTimezone;

    @Column(name = "arrival_time", length = 50)
    private String arrivalTime;
    @Column(name = "arrival_timezone")
    private String arrivalTimezone;

    @Column(name = "recovery_time", length = 50)
    private String recoveryTime;
    @Column(name = "recovery_timezone")
    private String recoveryTimezone;

    @Column(name = "calculated_mileage", precision = 15, scale = 4)
    private BigDecimal calculatedMileage;

    @Column(name = "calculated_mileage_unit", length = 60)
    @Enumerated(EnumType.STRING)
    private UnitOfMeasure calculatedMileageUnit;

    @Column(name = "type", length = 16)
    @Enumerated(EnumType.STRING)
    private SegmentType type;

}
