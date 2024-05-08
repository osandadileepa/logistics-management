package com.quincus.shipment.api;

public interface FlightStatsApi {
    void receiveFlightStatsMessage(String message, String uuid);
}
