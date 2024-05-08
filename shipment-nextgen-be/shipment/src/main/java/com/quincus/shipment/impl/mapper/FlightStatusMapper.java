package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.FlightStatus;
import com.quincus.shipment.impl.repository.entity.FlightStatusEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface FlightStatusMapper {
    @Mapping(target = "departure",
            expression = "java(flightStatus.getDeparture() != null ? FlightDetailsMapper.INSTANCE.mapDomainToEntity(flightStatus.getDeparture()) : null)")
    @Mapping(target = "arrival",
            expression = "java(flightStatus.getArrival() != null ? FlightDetailsMapper.INSTANCE.mapDomainToEntity(flightStatus.getArrival()) : null)")
    FlightStatusEntity mapDomainToEntity(FlightStatus flightStatus);

    @Mapping(target = "departure",
            expression = "java(FlightDetailsMapper.INSTANCE.mapEntityToDomain(flightStatusEntity.getDeparture()))")
    @Mapping(target = "arrival",
            expression = "java(FlightDetailsMapper.INSTANCE.mapEntityToDomain(flightStatusEntity.getArrival()))")
    FlightStatus mapEntityToDomain(FlightStatusEntity flightStatusEntity);

    @Mapping(source = "carrier", target = "airlineCode")
    FlightStatus mapFlightToFlightStatus(Flight flight);
}
