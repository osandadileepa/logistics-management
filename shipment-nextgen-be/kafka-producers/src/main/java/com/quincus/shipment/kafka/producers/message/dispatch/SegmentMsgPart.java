package com.quincus.shipment.kafka.producers.message.dispatch;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.quincus.shipment.api.deserializer.ZonedDateTimeDeserializer;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.MeasuredValue;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class SegmentMsgPart {
    private String id;
    private String refId;
    private String type;
    private String status;
    private String sequenceNo;
    private String transportCategory;
    private String partnerId;
    private String vehicleNumber;
    private String masterWaybillLabel;
    private String airNumber;
    private String airLine;
    private String airLineCode;
    private String hubId;
    private String fromFacilityId;
    private String toFacilityId;
    private List<Instruction> instructions;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime pickUpStartTime;
    private String pickUpStartTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime pickUpCommitTime;
    private String pickUpCommitTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime pickUpActualTime;
    private String pickUpActualTimezone;
    private String pickupTimeZone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime dropOffStartTime;
    private String dropOffStartTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime dropOffCommitTime;
    private String dropOffCommitTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime dropOffActualTime;
    private String dropOffActualTimezone;
    private String dropOffTimeZone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime lockoutTime;
    private String lockoutTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime departedTime;
    private String departedTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime arrivalTime;
    private String arrivalTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime recoverTime;
    private String recoverTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime dropOffOnSiteTime;
    private String dropOffOnSiteTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime pickUpOnSiteTime;
    private String pickUpOnSiteTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime arrivalActualTime;
    private String arrivalActualTimezone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = SegmentsDispatchMessage.ZONED_DATE_TIME_FMT)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime departureActualTime;
    private String departureActualTimezone;
    private MeasuredValue calculatedMileage;
    private MeasuredValue duration;
    private String internalBookingReference;
    private String externalBookingReference;
    private String assignmentStatus;
    private String rejectionReason;
    private String driverName;
    private String vehicleType;
}
