package com.quincus.shipment.impl.repository.entity;

import com.quincus.shipment.api.constant.BookingStatus;
import com.quincus.shipment.api.constant.FlightSubscriptionStatus;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.Vehicle;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "package_journey_segment")
@FilterDef(name = "deletedFilter", defaultCondition = "isDeleted = false")
@Filter(name = "deletedFilter")
@Where(clause = "deleted=false")
public class PackageJourneySegmentEntity extends MultiTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ShipmentJourneyEntity shipmentJourney;

    @Column(name = "type", length = 16)
    @Enumerated(EnumType.STRING)
    private SegmentType type;
    @Column(name = "ops_type", length = 45)
    private String opsType;
    @Column(name = "status", length = 45)
    @Enumerated(EnumType.STRING)
    private SegmentStatus status;
    @Column(name = "transport_type", length = 45)
    @Enumerated(EnumType.STRING)
    private TransportType transportType;
    @Column(name = "serviced_by", length = 45)
    private String servicedBy;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "partner_id", referencedColumnName = "id")
    @NotFound(action = NotFoundAction.IGNORE)
    private PartnerEntity partner;
    @Column(name = "hub_id", length = 48)
    private String hubId;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "start_location_hierarchy", referencedColumnName = "id")
    private LocationHierarchyEntity startLocationHierarchy;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "end_location_hierarchy", referencedColumnName = "id")
    private LocationHierarchyEntity endLocationHierarchy;
    @Column(name = "cost", length = 45)
    private String cost;
    @Column(name = "ref_id", length = 50)
    private String refId;
    @Column(name = "sequence", length = 45)
    private String sequence;
    @Column(name = "airline", length = 65)
    private String airline;
    @Column(name = "airline_code", length = 50)
    private String airlineCode;
    @Column(name = "currency_id", length = 50)
    private String currencyId;
    @Column(name = "instruction", length = 4000)
    private String instruction;
    @Column(name = "vehicle_info", length = 50)
    private String vehicleInfo;
    @Column(name = "flight_number", length = 50)
    private String flightNumber;
    @Column(name = "arrival_time", length = 50)
    private String arrivalTime;
    @Column(name = "arrival_timezone")
    private String arrivalTimezone;
    @Column(name = "pick_up_time", length = 50)
    private String pickUpTime;
    @Column(name = "pick_up_timezone")
    private String pickUpTimezone;
    @Column(name = "pick_up_commit_time", length = 50)
    private String pickUpCommitTime;
    @Column(name = "pick_up_commit_timezone")
    private String pickUpCommitTimezone;
    @Column(name = "pick_up_actual_time", length = 50)
    private String pickUpActualTime;
    @Column(name = "pick_up_actual_timezone")
    private String pickUpActualTimezone;
    @Column(name = "pick_up_on_site_time", length = 50)
    private String pickUpOnSiteTime;
    @Column(name = "pick_up_on_site_timezone")
    private String pickUpOnSiteTimezone;
    @Column(name = "drop_off_time", length = 50)
    private String dropOffTime;
    @Column(name = "drop_off_timezone")
    private String dropOffTimezone;
    @Column(name = "drop_off_commit_time", length = 50)
    private String dropOffCommitTime;
    @Column(name = "drop_off_commit_timezone")
    private String dropOffCommitTimezone;
    @Column(name = "drop_off_actual_time", length = 50)
    private String dropOffActualTime;
    @Column(name = "drop_off_actual_timezone")
    private String dropOffActualTimezone;
    @Column(name = "drop_off_on_site_time", length = 50)
    private String dropOffOnSiteTime;
    @Column(name = "drop_off_on_site_timezone")
    private String dropOffOnSiteTimezone;
    @Column(name = "lock_out_time", length = 50)
    private String lockOutTime;
    @Column(name = "lock_out_timezone")
    private String lockOutTimezone;
    @Column(name = "recovery_time", length = 50)
    private String recoveryTime;
    @Column(name = "recovery_timezone")
    private String recoveryTimezone;
    @Column(name = "departure_time", length = 50)
    private String departureTime;
    @Column(name = "departure_timezone")
    private String departureTimezone;
    @Column(name = "master_waybill", length = 50)
    private String masterWaybill;
    @Column(name = "calculated_mileage", precision = 15, scale = 4)
    private BigDecimal calculatedMileage;
    @Column(name = "calculated_mileage_unit", length = 60)
    @Enumerated(EnumType.STRING)
    private UnitOfMeasure calculatedMileageUnit;
    @Column(name = "duration", precision = 15, scale = 4)
    private BigDecimal duration;
    @Column(name = "duration_unit", length = 60)
    @Enumerated(EnumType.STRING)
    private UnitOfMeasure durationUnit;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "package_journey_segment_id")
    private List<AlertEntity> alerts;
    @Column(name = "deleted")
    private boolean deleted;
    @Column(name = "shipmentjourney_id", insertable = false, updatable = false)
    private String shipmentJourneyId;
    @Column(name = "flight_origin")
    private String flightOrigin;
    @Column(name = "flight_destination")
    private String flightDestination;
    @Column(name = "flight_subscription_status", length = 50)
    @Enumerated(EnumType.STRING)
    private FlightSubscriptionStatus flightSubscriptionStatus;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "flight_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private FlightEntity flight;
    @Column(name = "vehicle")
    @Type(type = "json")
    private Vehicle vehicle;
    @Column(name = "driver")
    @Type(type = "json")
    private Driver driver;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "package_journey_segment_id")
    private List<InstructionEntity> instructions;
    //Vendor Booking related fields
    @Column(name = "internal_booking_reference", length = 64)
    private String internalBookingReference;
    @Column(name = "external_booking_reference", length = 64)
    private String externalBookingReference;
    @Column(name = "booking_status", length = 64)
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
    @Column(name = "assignment_status", length = 64)
    private String assignmentStatus;
    @Column(name = "rejection_reason")
    private String rejectionReason;
}