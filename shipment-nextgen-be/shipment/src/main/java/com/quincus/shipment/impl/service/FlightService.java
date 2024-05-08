package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.impl.mapper.FlightMapper;
import com.quincus.shipment.impl.mapper.FlightStatusMapper;
import com.quincus.shipment.impl.repository.FlightRepository;
import com.quincus.shipment.impl.repository.entity.FlightEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class FlightService {
    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final FlightStatusMapper flightStatusMapper;

    @Transactional
    public FlightEntity save(Flight flight) {
        FlightEntity flightEntity = flightMapper.mapDomainToEntity(flight);
        return flightRepository.saveAndFlush(flightEntity);
    }

    @Transactional
    public FlightEntity createOrUpdate(Flight flight) {
        if (flight.getFlightId() == null) return null;
        FlightEntity flightEntity = flightRepository.findByFlightId(flight.getFlightId());
        if (flightEntity == null) {
            flightEntity = save(flight);
        }
        flightEntity.addFlightStatus(flightStatusMapper.mapDomainToEntity(flight.getFlightStatus()));
        flightRepository.save(flightEntity);
        return flightEntity;
    }

    public FlightEntity findByFlightDetails(String carrier, String flightNumber, String departureDate, String origin, String destination) {
        return flightRepository.findByCarrierAndFlightNumberAndDepartureDateAndOriginAndDestination(
                carrier, flightNumber, departureDate, origin, destination);
    }

}
