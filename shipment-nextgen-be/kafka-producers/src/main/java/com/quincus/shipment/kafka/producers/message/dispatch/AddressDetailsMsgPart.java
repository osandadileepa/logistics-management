package com.quincus.shipment.kafka.producers.message.dispatch;

import lombok.Data;

@Data
public class AddressDetailsMsgPart {
    private String id;
    private String city;
    private String state;
    private String country;
    private String address;
    private String cityId;
    private String stateId;
    private String countryId;
    private String latitude;
    private String longitude;
    private String postalCode;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private Boolean manualCoordinates;
    private String company;
    private String department;
}
