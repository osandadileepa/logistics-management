package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.impl.repository.entity.FlightEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface FlightMapper {
    FlightEntity mapDomainToEntity(Flight flight);

    Flight mapEntityToDomain(FlightEntity flightEntity);
}
