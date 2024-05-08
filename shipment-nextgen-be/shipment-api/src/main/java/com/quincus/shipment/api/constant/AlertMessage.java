package com.quincus.shipment.api.constant;

import lombok.Getter;

import java.util.List;

import static com.quincus.shipment.api.constant.AlertLevel.CRITICAL;
import static com.quincus.shipment.api.constant.AlertLevel.STANDARD;
import static com.quincus.shipment.api.constant.ConstraintType.HARD_CONSTRAINT;
import static com.quincus.shipment.api.constant.ConstraintType.SOFT_CONSTRAINT;

public enum AlertMessage {
    MISSING_MANDATORY_FIELDS("Blank mandatory field", "Mandatory field(s) is blank.", CRITICAL, HARD_CONSTRAINT),
    TIME_OVERLAP_ACROSS_SEGMENTS("Time overlaps across segments", "There is an overlap in time across segments.", STANDARD, SOFT_CONSTRAINT),
    MAWB_NO_CHECKSUM_VALIDATION("AWB has invalid checksum", "Airwaybill does not have a checksum validation.", STANDARD, SOFT_CONSTRAINT),
    FLIGHT_NOT_FOUND_FLIGHTSTATS("Flight untrackable", "The flight cannot be tracked as it is not found on Flightstats.", STANDARD, SOFT_CONSTRAINT),
    TIME_OVERLAP_WITHIN_SEGMENT("Time overlaps within segment", "The %s time must be after the %s time.", STANDARD, SOFT_CONSTRAINT),
    ORIGIN_ADDRESS_CHANGE("Origin address updated", "The origin address has been updated.", STANDARD, SOFT_CONSTRAINT),
    DESTINATION_ADDRESS_CHANGE("Destination address updated", "The destination address has been updated.", STANDARD, SOFT_CONSTRAINT),
    SERVICE_TYPE_CHANGE("Service type updated", "The service type has been updated.", STANDARD, SOFT_CONSTRAINT),
    FLIGHT_DEPARTURE_DELAY("Flight departure time updated", "The scheduled flight departure time has been updated.", STANDARD, SOFT_CONSTRAINT),
    FLIGHT_ARRIVAL_DELAY("Flight arrival time updated", "The scheduled flight arrival time has been updated.", STANDARD, SOFT_CONSTRAINT),
    FLIGHT_CANCELLATION("Flight cancelled", "The flight has been cancelled. Please review the journey and make any necessary arrangements.", CRITICAL, HARD_CONSTRAINT),
    VENDOR_ASSIGNMENT_REJECTED("Assignment rejected", "Vendor rejected assignment", CRITICAL, HARD_CONSTRAINT),
    VENDOR_ASSIGNMENT_FAILED("Assignment: system error", "System error occurred for vendor assignment", CRITICAL, HARD_CONSTRAINT),
    FAILED_PICKUP_OR_DELIVERY("Pick up/delivery failed", "Attempt to pick up/deliver shipment has failed.", CRITICAL, HARD_CONSTRAINT),
    RISK_OF_DELAY("Potential delay", "There may be a potential delay in the shipment.", STANDARD, SOFT_CONSTRAINT),
    SHIPMENT_DEPARTURE_DELAYED("Shipment departure delayed", "The shipment departure is delayed", STANDARD, SOFT_CONSTRAINT),
    SHIPMENT_ARRIVAL_DELAYED("Shipment arrival delayed", "The shipment arrival is delayed. Please review the journey.", STANDARD, SOFT_CONSTRAINT),
    SHIPMENT_LEAD_TIME_EXCEED_SLA("Exceed SLA", "The shipment cannot be fulfilled within the timeframe specified in Service Level Agreement (SLA)", CRITICAL, HARD_CONSTRAINT),
    OUT_OF_FACILITY_OPERATING_HOURS("Out of facility operating hours", "The scheduled pick up/drop off time is not within facility opening hours.", STANDARD, SOFT_CONSTRAINT),
    JOURNEY_NOT_MATCH_PARENT_SHIPMENT("", "Journey does not match parent shipment’s journey", null, SOFT_CONSTRAINT),
    JOURNEY_SEGMENT_LOCATIONS_MISMATCH("Segment Location Mismatch", "There is a mismatch of pickup and drop-off location in segments.", STANDARD, SOFT_CONSTRAINT),
    UNSUITABLE_COMMODITY("", "Shipment contains commodities unsuitable for parent shipment’s packaging type", null, SOFT_CONSTRAINT);

    private final String message;
    @Getter
    private final String fullMessage;
    @Getter
    private final AlertLevel level;
    @Getter
    private final ConstraintType constraintType;

    AlertMessage(String message, String fullMessage, AlertLevel level, ConstraintType constraintType) {
        this.message = message;
        this.fullMessage = fullMessage;
        this.level = level;
        this.constraintType = constraintType;
    }

    @Override
    public String toString() {
        return message;
    }

    public static List<String> getJourneyPageTriggeredAlerts() {
        return List.of(MISSING_MANDATORY_FIELDS.toString(),
                TIME_OVERLAP_ACROSS_SEGMENTS.toString(),
                MAWB_NO_CHECKSUM_VALIDATION.toString(),
                FLIGHT_NOT_FOUND_FLIGHTSTATS.toString(),
                TIME_OVERLAP_WITHIN_SEGMENT.toString(),
                FLIGHT_CANCELLATION.toString(),
                OUT_OF_FACILITY_OPERATING_HOURS.toString()
        );
    }
}
