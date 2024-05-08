package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.FlightDetails;
import com.quincus.shipment.impl.repository.entity.component.FlightDetailsEntity;
import com.quincus.shipment.kafka.producers.mapper.MapperUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring", imports = {OffsetDateTime.class, MapperUtil.class}, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface FlightDetailsMapper {

    FlightDetailsMapper INSTANCE = Mappers.getMapper(FlightDetailsMapper.class);

    @Mapping(target = "timezone", expression = "java(MapperUtil.getTimezoneFromScheduledTime(flightDetails.getScheduledTime()))")
    FlightDetailsEntity mapDomainToEntity(FlightDetails flightDetails);

    @Mapping(target = "timezone", expression = "java(MapperUtil.getTimezoneFromScheduledTime(flightDetailsEntity.getScheduledTime()))")
    FlightDetails mapEntityToDomain(FlightDetailsEntity flightDetailsEntity);
}
