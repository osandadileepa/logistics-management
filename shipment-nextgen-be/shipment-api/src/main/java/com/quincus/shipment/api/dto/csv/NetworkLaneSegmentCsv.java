package com.quincus.shipment.api.dto.csv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude
public class NetworkLaneSegmentCsv {

    private String sequenceNumber;
    private String transportCategory;
    private String partnerName;
    private String vehicleInfo;
    private String flightNumber;
    private String airline;
    private String airlineCode;
    private String masterWaybill;
    private String pickupFacilityName;
    private String dropOffFacilityName;
    private String pickupInstruction;
    private String dropOffInstruction;
    private String duration;
    private String durationUnit;
    private String pickUpTime;
    private String dropOffTime;
    private String lockOutTime;
    private String departureTime;
    private String arrivalTime;
    private String recoveryTime;
    private String calculatedMileage;
    private String calculatedMileageUnit;
    @JsonIgnore
    private String organizationId;
    @JsonIgnore
    private boolean ignoreRecord;
}
