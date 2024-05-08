package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.FlightStatus;
import com.quincus.shipment.impl.mapper.FlightStatusMapper;
import com.quincus.shipment.impl.repository.FlightStatusRepository;
import com.quincus.shipment.impl.repository.entity.FlightStatusEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class FlightStatusService {
    private final FlightStatusRepository flightStatusRepository;
    private final FlightStatusMapper flightStatusMapper;

    @Transactional
    public FlightStatusEntity save(FlightStatus flightStatus) {
        FlightStatusEntity flightStatusEntity = flightStatusMapper.mapDomainToEntity(flightStatus);
        return flightStatusRepository.save(flightStatusEntity);
    }
}
