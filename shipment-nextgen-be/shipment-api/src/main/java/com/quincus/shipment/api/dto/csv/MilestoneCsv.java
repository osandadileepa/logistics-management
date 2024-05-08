package com.quincus.shipment.api.dto.csv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@JsonInclude
public class MilestoneCsv {
    public static final String HEADER_SHIPMENT_TRACKING_ID = "Shipment ID";
    public static final String HEADER_MILESTONE_NAME = "Name";
    public static final String HEADER_MILESTONE_CODE = "Code";
    public static final String HEADER_MILESTONE_DATE_TIME = "DateTime";
    public static final String HEADER_FROM_COUNTRY = "From Country";
    public static final String HEADER_FROM_STATE = "From State/Province";
    public static final String HEADER_FROM_CITY = "From City";
    public static final String HEADER_FROM_WARD = "From Ward";
    public static final String HEADER_FROM_DISTRICT = "From District";
    public static final String HEADER_FROM_LOCATION = "From Location";
    public static final String HEADER_FROM_LATITUDE = "From Latitude";
    public static final String HEADER_FROM_LONGITUDE = "From Longitude";
    public static final String HEADER_TO_COUNTRY = "To Country";
    public static final String HEADER_TO_STATE = "To State/Province";
    public static final String HEADER_TO_CITY = "To City";
    public static final String HEADER_TO_WARD = "To Ward";
    public static final String HEADER_TO_DISTRICT = "To District";
    public static final String HEADER_TO_LOCATION = "To Location";
    public static final String HEADER_TO_LATITUDE = "To Latitude";
    public static final String HEADER_TO_LONGITUDE = "To Longitude";
    public static final String HEADER_LATITUDE = "Current Latitude";
    public static final String HEADER_LONGITUDE = "Current Longitude";
    public static final String HEADER_HUB_ID = "Hub";
    public static final String HEADER_DRIVER_NAME = "Driver Name";
    public static final String HEADER_DRIVER_PHONE_CODE = "Driver Phone Code";
    public static final String HEADER_DRIVER_PHONE_NUM = "Driver Phone Number";
    public static final String HEADER_DRIVER_EMAIL = "Driver Email";
    public static final String HEADER_VEHICLE_TYPE = "Vehicle Type";
    public static final String HEADER_VEHICLE_NAME = "Vehicle Name";
    public static final String HEADER_VEHICLE_NUM = "Vehicle Number";
    public static final String HEADER_SENDER_NAME = "Sender Name";
    public static final String HEADER_SENDER_COMPANY = "Sender Company";
    public static final String HEADER_SENDER_DEPARTMENT = "Sender Department";
    public static final String HEADER_RECEIVER_NAME = "Receiver Name";
    public static final String HEADER_RECEIVER_COMPANY = "Receiver Company";
    public static final String HEADER_RECEIVER_DEPARTMENT = "Receiver Department";
    public static final String HEADER_ETA = "ETA";
    public static final String HEADER_NOTES = "Notes";

    private static final String[] CSV_HEADERS = {
            HEADER_SHIPMENT_TRACKING_ID,
            HEADER_MILESTONE_NAME,
            HEADER_MILESTONE_CODE,
            HEADER_MILESTONE_DATE_TIME,
            HEADER_FROM_COUNTRY,
            HEADER_FROM_STATE,
            HEADER_FROM_CITY,
            HEADER_FROM_WARD,
            HEADER_FROM_DISTRICT,
            HEADER_FROM_LOCATION,
            HEADER_FROM_LATITUDE,
            HEADER_FROM_LONGITUDE,
            HEADER_TO_COUNTRY,
            HEADER_TO_STATE,
            HEADER_TO_CITY,
            HEADER_TO_WARD,
            HEADER_TO_DISTRICT,
            HEADER_TO_LOCATION,
            HEADER_TO_LATITUDE,
            HEADER_TO_LONGITUDE,
            HEADER_LATITUDE,
            HEADER_LONGITUDE,
            HEADER_HUB_ID,
            HEADER_DRIVER_NAME,
            HEADER_DRIVER_PHONE_CODE,
            HEADER_DRIVER_PHONE_NUM,
            HEADER_DRIVER_EMAIL,
            HEADER_VEHICLE_TYPE,
            HEADER_VEHICLE_NAME,
            HEADER_VEHICLE_NUM,
            HEADER_SENDER_NAME,
            HEADER_SENDER_COMPANY,
            HEADER_SENDER_DEPARTMENT,
            HEADER_RECEIVER_NAME,
            HEADER_RECEIVER_COMPANY,
            HEADER_RECEIVER_DEPARTMENT,
            HEADER_ETA,
            HEADER_NOTES
    };
    @NotBlank
    private String shipmentTrackingId;
    private String milestoneName;
    @NotBlank
    private String milestoneCode;
    @NotBlank
    private String milestoneTime;
    private String fromCountry;
    private String fromState;
    private String fromCity;
    private String fromWard;
    private String fromDistrict;
    private String fromLatitude;
    private String fromLongitude;
    private String fromFacility;
    private String toCountry;
    private String toState;
    private String toCity;
    private String toWard;
    private String toDistrict;
    private String toLatitude;
    private String toLongitude;
    private String toFacility;
    private String latitude;
    private String longitude;
    private String hub;
    private String driverName;
    private String driverPhoneCode;
    private String driverPhoneNumber;
    private String driverEmail;
    private String vehicleType;
    private String vehicleName;
    private String vehicleNumber;
    private String senderName;
    private String senderCompany;
    private String senderDepartment;
    private String receiverName;
    private String receiverCompany;
    private String receiverDepartment;
    private String eta;
    private String notes;
    private String failedReason;
    @JsonIgnore
    private String organizationId;
    @JsonIgnore
    private String partnerId;
    @JsonIgnore
    private String driverId;
    @JsonIgnore
    private String vehicleId;
    @JsonIgnore
    private String userId;
    @JsonIgnore
    private long recordNumber;
    @JsonIgnore
    private int size;

    public static String[] getCsvHeaders() {
        return CSV_HEADERS;
    }
}
