package com.quincus.shipment.impl.repository;

import com.quincus.shipment.impl.repository.entity.FlightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightRepository extends JpaRepository<FlightEntity, String> {
    FlightEntity findByFlightId(Long flightId);

    FlightEntity findByCarrierAndFlightNumberAndDepartureDateAndOriginAndDestination(String carrier, String flightNumber,
                                                                                     String departureDate, String origin,
                                                                                     String destination);
}
