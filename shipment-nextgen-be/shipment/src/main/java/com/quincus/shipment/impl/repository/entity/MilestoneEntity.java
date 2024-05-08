package com.quincus.shipment.impl.repository.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.MilestoneSource;
import com.quincus.shipment.api.domain.Coordinate;
import com.quincus.shipment.api.domain.MilestoneAdditionalInfo;
import com.quincus.shipment.impl.repository.entity.component.MultiTenantEntity;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "milestone")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class MilestoneEntity extends MultiTenantEntity implements Comparable<MilestoneEntity> {

    @Column(name = "code", length = 100)
    @Enumerated(EnumType.STRING)
    private MilestoneCode milestoneCode;

    @Column(name = "milestone_ref_id", length = 48)
    private String milestoneRefId;

    @Column(name = "milestone_name", length = 48)
    private String milestoneName;

    @Column(name = "milestone_time", length = 50)
    private String milestoneTime;

    @Column(name = "milestone_timezone")
    private String milestoneTimezone;

    @Column(name = "service_type", length = 48)
    private String serviceType;

    @Column(name = "job_type", length = 48)
    private String jobType;

    @Column(name = "from_location_id", length = 48)
    private String fromLocationId;

    @Column(name = "from_country_id", length = 48)
    private String fromCountryId;

    @Column(name = "from_state_id", length = 48)
    private String fromStateId;

    @Column(name = "from_city_id", length = 48)
    private String fromCityId;

    @Column(name = "from_ward_id", length = 48)
    private String fromWardId;

    @Column(name = "from_district_id", length = 48)
    private String fromDistrictId;

    @Column(name = "from_coordinates")
    @Type(type = "json")
    private Coordinate fromCoordinates;

    @Column(name = "to_location_id", length = 48)
    private String toLocationId;

    @Column(name = "to_country_id", length = 48)
    private String toCountryId;

    @Column(name = "to_state_id", length = 48)
    private String toStateId;

    @Column(name = "to_city_id", length = 48)
    private String toCityId;

    @Column(name = "to_ward_id", length = 48)
    private String toWardId;

    @Column(name = "to_district_id", length = 48)
    private String toDistrictId;

    @Column(name = "to_coordinates")
    @Type(type = "json")
    private Coordinate toCoordinates;

    @Column(name = "user_id", length = 48)
    private String userId;

    @Column(name = "partner_id", length = 48)
    private String partnerId;

    @Column(name = "user_name")
    private String userName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private ShipmentEntity shipment;

    @Column(name = "shipment_id", insertable = false, updatable = false)
    private String shipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", referencedColumnName = "id")
    private PackageJourneySegmentEntity segment;

    @Column(name = "segment_id", insertable = false, updatable = false)
    private String segmentId;

    @Column(name = "hub_id", length = 48)
    private String hubId;

    @Column(name = "driver_id", length = 48)
    private String driverId;

    @Column(name = "driver_name", length = 64)
    private String driverName;

    @Column(name = "driver_phone_code", length = 10)
    private String driverPhoneCode;

    @Column(name = "driver_phone_number", length = 24)
    private String driverPhoneNumber;

    @Column(name = "driver_email", length = 128)
    private String driverEmail;

    @Column(name = "vehicle_id", length = 48)
    private String vehicleId;

    @Column(name = "vehicle_type", length = 64)
    private String vehicleType;

    @Column(name = "vehicle_name", length = 64)
    private String vehicleName;

    @Column(name = "vehicle_number", length = 24)
    private String vehicleNumber;

    @Column(name = "sender_name", length = 128)
    private String senderName;

    @Column(name = "sender_company", length = 128)
    private String senderCompany;

    @Column(name = "sender_department", length = 128)
    private String senderDepartment;

    @Column(name = "receiver_name", length = 128)
    private String receiverName;

    @Column(name = "receiver_company", length = 128)
    private String receiverCompany;

    @Column(name = "receiver_department", length = 128)
    private String receiverDepartment;

    @Column(name = "eta", length = 50)
    private String eta;

    @Column(name = "eta_timezone")
    private String etaTimezone;

    @Column(name = "failed_reason")
    private String failedReason;

    @Column(name = "failed_reason_code", length = 32)
    private String failedReasonCode;

    @Column(name = "milestone_coordinates")
    @Type(type = "json")
    private Coordinate milestoneCoordinates;

    @Column(name = "additional_info")
    @Type(type = "json")
    private MilestoneAdditionalInfo additionalInfo;

    @Column(name = "data")
    @Type(type = "json")
    private JsonNode data;

    @Column(name = "order_number", length = 64)
    private String orderNumber;

    @Column(name = "vendor_id", length = 48)
    private String vendorId;

    @Column(name = "waybill_number", length = 50)
    private String waybillNumber;

    @Column(name = "department_floor_suite_comments", length = 2000)
    private String departmentFloorSuiteComments;

    @Column(name = "branch_name", length = 120)
    private String branchName;

    @Column(name = "proof_of_delivery_time")
    private String proofOfDeliveryTime;

    @Column(name = "proof_of_delivery_timezone")
    private String proofOfDeliveryTimezone;

    @Column(name = "external_segment_id", length = 50)
    private String externalSegmentId;

    @Column(name = "external_order_id", length = 64)
    private String externalOrderId;

    @Column(name = "source")
    @Enumerated(EnumType.STRING)
    private MilestoneSource source;

    @Override
    public int compareTo(MilestoneEntity m) {
        if (m == null || m.getMilestoneTime() == null) return 0;
        return getMilestoneTime().compareTo(m.getMilestoneTime());
    }
}
