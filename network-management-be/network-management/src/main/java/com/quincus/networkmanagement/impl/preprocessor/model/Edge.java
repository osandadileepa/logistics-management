package com.quincus.networkmanagement.impl.preprocessor.model;

import com.quincus.networkmanagement.api.domain.CapacityProfile;
import com.quincus.networkmanagement.api.domain.MeasurementUnits;
import com.quincus.networkmanagement.api.domain.ShipmentProfileExtension;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Edge {
    private String flightNumber;
    private String departureHub;
    private double departureLat;
    private double departureLon;
    private long departureTime;
    private String arrivalHub;
    private double arrivalLat;
    private double arrivalLon;
    private long arrivalTime;
    private String vehicleType;
    private BigDecimal distance;
    private int duration;
    private BigDecimal cost;
    private int capacity;
    private ShipmentProfileExtension shipmentProfiles;
    private CapacityProfile capacityProfile;
    private MeasurementUnits measurementUnits;
    private BigDecimal co2Emissions;
}
