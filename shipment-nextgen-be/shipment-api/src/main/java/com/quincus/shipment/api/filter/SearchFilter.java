package com.quincus.shipment.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.domain.Customer;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.validator.constraint.ValidStringArray;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SearchFilter {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Organization organization;
    private EtaStatus[] etaStatus;
    private JourneyStatus journeyStatus;
    private Customer[] customer;
    @ValidStringArray(maxLengthEach = 128, message = "Maximum of 128 characters each origin is allowed.", notNullEach = true)
    private String[] origin;
    @ValidStringArray(maxLengthEach = 128, message = "Maximum of 128 characters each destination is allowed.", notNullEach = true)
    private String[] destination;
    @ValidStringArray(maxLengthEach = 64, message = "Maximum of 64 characters each keys is allowed.", notNullEach = true)
    private String[] keys;
    @ValidStringArray(maxLengthEach = 64, message = "Maximum of 64 characters each excludeKeys is allowed.", notNullEach = true)
    private String[] excludeKeys;
    @ValidStringArray(maxLengthEach = 64, message = "Maximum of 64 characters each costKeys is allowed.", notNullEach = true)
    private String[] costKeys;
    private AirlineFilter[] airlineKeys;
    private ServiceType[] serviceType;
    private Order order;
    @ValidStringArray(maxLengthEach = 128, message = "Maximum of 128 characters each facilities is allowed.", notNullEach = true)
    private String[] facilities;
    private ShipmentLocationFilter facilityLocations;
    private ShipmentLocationFilter originLocations;
    private ShipmentLocationFilter destinationLocations;
    private Date bookingDateFrom;
    private Date bookingDateTo;
    @ValidStringArray(maxLengthEach = 256, message = "Maximum of 256 characters each alert is allowed.", notNullEach = true)
    private String[] alert;
}
