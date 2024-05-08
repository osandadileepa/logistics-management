package com.quincus.shipment.kafka.producers.message.qship;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.MeasuredValue;
import com.quincus.shipment.api.domain.OrderReference;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class QshipSegmentMessage {
    public static final String ZONED_DATE_TIME_FMT = "yyyy-MM-dd'T'HH:mm:ssxxx";
    private String id;
    private String refId;
    private String journeyId;
    private String orderId;
    private String organisationId;
    private String type;
    private String status;
    private String sequenceNo;
    private String transportCategory;
    private String partnerId;
    private String vehicleInfo;
    private String flightNumber;
    private String flightId;
    private String airline;
    private String airlineCode;
    private String masterWaybill;
    private String pickUpFacilityId;
    private String dropOffFacilityId;
    private List<Instruction> instructions;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime pickUpTime;
    private String pickUpTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime pickUpActualTime;
    private String pickUpActualTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime lockoutTime;
    private String lockoutTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime departureTime;
    private String departureTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime arrivalTime;
    private String arrivalTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime dropOffTime;
    private String dropOffTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime dropOffActualTime;
    private String dropOffActualTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime recoveryTime;
    private String recoveryTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime pickUpOnSiteTime;
    private String pickUpOnSiteTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime dropOffOnSiteTime;
    private String dropOffOnSiteTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime arrivalActualTime;
    private String arrivalActualTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime departureActualTime;
    private String departureActualTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime updatedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_FMT)
    private ZonedDateTime deletedAt;
    private String lat;
    private String lon;
    private MeasuredValue calculatedMileage;
    private MeasuredValue duration;
    private List<PackageMsgPart> packages;
    private String internalOrderId;
    private String externalOrderId;
    private String customerOrderId;
    private String driverId;
    private String driverName;
    private String driverPhoneCode;
    private String driverPhoneNumber;
    private String vehicleId;
    private String vehicleType;
    private String vehicleName;
    private String vehicleNumber;
    private List<OrderReference> orderReferences;
    private String internalBookingReference;
    private String externalBookingReference;
    private String assignmentStatus;
    private String rejectionReason;
}
