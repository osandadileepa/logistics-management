package com.quincus.shipment.api.dto.csv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class PackageJourneyAirSegmentCsv {
    public static final String CSV_FIELD_SHIPMENT_ID = "Shipment ID";
    public static final String CSV_FIELD_MEASUREMENT_UNIT = "Unit (Do not edit)";
    public static final String CSV_FIELD_VOLUME_WEIGHT = "Volume Weight (Do not edit)";
    public static final String CSV_FIELD_GROSS_WEIGHT = "Gross Weight (Do not edit)";
    public static final String CSV_FIELD_AIRLINE_CODE = "Airline Code";
    public static final String CSV_FIELD_FLIGHT_NUMBER = "Flight Number";
    public static final String CSV_FIELD_DEPARTURE_DATE = "Departure DateTime";
    public static final String CSV_FIELD_DEPARTURE_DATE_TIMEZONE = "Departure Timezone";
    public static final String CSV_FIELD_ORIGIN_FACILITY = "Origin Facility";
    public static final String CSV_FIELD_ORIGIN_COUNTRY = "Origin Country";
    public static final String CSV_FIELD_ORIGIN_STATE = "Origin State/Province";
    public static final String CSV_FIELD_ORIGIN_CITY = "Origin City";
    public static final String CSV_FIELD_INSTRUCTION_CONTENT = "Segment Note/Instruction Content";
    public static final String CSV_FIELD_AIR_WAY_BILL = "Airway Bill";
    public static final String CSV_FIELD_VENDOR = "Vendor";
    public static final String CSV_FIELD_LOCK_OUT_DATE = "Lockout DateTime";
    public static final String CSV_FIELD_LOCK_OUT_DATE_TIMEZONE = "Lockout Timezone";
    public static final String CSV_FIELD_ARRIVAL_DATE = "Arrival Datetime";
    public static final String CSV_FIELD_ARRIVAL_DATE_TIMEZONE = "Arrival Timezone";
    public static final String CSV_FIELD_RECOVERY_DATE = "Recovery DateTime";
    public static final String CSV_FIELD_RECOVERY_DATE_TIMEZONE = "Recovery Timezone";
    public static final String ERROR_MSG_DELIMITER = " | ";
    public static final String FAILED_REASON_IDENTIFIER = "Validation Error:";
    @JsonIgnore
    private List<String> errorMessages;
    @JsonIgnore
    private String organizationId;
    @JsonIgnore
    private String vendorId;
    private String shipmentId;
    private String measurementUnit;
    private String volumeWeight;
    private String grossWeight;
    private String airlineCode;
    private String flightNumber;
    private String departureDatetime;
    private String departureTimezone;
    private String originFacility;
    private String originCountry;
    private String originState;
    private String originCity;
    private String instructionContent;
    private String airWayBill;
    private String vendor;
    private String lockoutDatetime;
    private String lockoutTimezone;
    private String arrivalDatetime;
    private String arrivalTimezone;
    private String recoveryDatetime;
    private String recoveryTimezone;
    private String failedReason;

    public void addErrorMessage(String errorMessage) {
        if (CollectionUtils.isEmpty(errorMessages)) {
            errorMessages = new ArrayList<>();
        }
        errorMessages.add(errorMessage);
    }

    public void buildFailedReason() {
        if (StringUtils.isBlank(failedReason) && !CollectionUtils.isEmpty(errorMessages)) {
            failedReason = FAILED_REASON_IDENTIFIER + String.join(ERROR_MSG_DELIMITER, errorMessages);
        }
    }
}