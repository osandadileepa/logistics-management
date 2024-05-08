package com.quincus.order.api.domain;

import com.quincus.shipment.api.constant.SegmentState;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SegmentsPayload {

    private String cost;
    private String refId;
    private String sequence;
    private String airline;
    private String airlineCode;
    private String partnerId;
    private String currencyId;
    private String instruction;
    private String vehicleInfo;
    private String arrivalTime;
    private String pickUpTime;
    private String pickUpCommitTime;
    private String dropOffTime;
    private String dropOffCommitTime;
    private String flightNumber;
    private String lockOutTime;
    private String recoveryTime;
    private String departureTime;
    private String masterWaybill;
    private String transportCategory;
    private String pickUpFacilityId;
    private String dropOffFacilityId;

    //Note: this is only a placeholder. OM does not have this yet. Actual field name may change.
    private String handleFacilityId;

    private BigDecimal calculatedMileage;
    private BigDecimal duration;

    private SegmentState state;
}
