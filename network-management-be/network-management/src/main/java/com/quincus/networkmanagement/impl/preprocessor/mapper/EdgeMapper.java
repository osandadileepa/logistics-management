package com.quincus.networkmanagement.impl.preprocessor.mapper;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.impl.preprocessor.model.Edge;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface EdgeMapper {

    @Mapping(target = "flightNumber", source = "connectionCode")
    @Mapping(target = "departureHub", source = "departureNode.nodeCode")
    @Mapping(target = "departureLat", source = "departureNode.facility.lat")
    @Mapping(target = "departureLon", source = "departureNode.facility.lon")
    @Mapping(target = "arrivalHub", source = "arrivalNode.nodeCode")
    @Mapping(target = "arrivalLat", source = "arrivalNode.facility.lat")
    @Mapping(target = "arrivalLon", source = "arrivalNode.facility.lon")
    @Mapping(target = "vehicleType", source = "vehicleType.name")
    @Mapping(target = "distance", source = "distance")
    @Mapping(target = "duration", source = "duration")
    @Mapping(target = "cost", source = "cost")
    @Mapping(target = "shipmentProfiles", source = "shipmentProfile")
    @Mapping(target = "capacityProfile", source = "capacityProfile")
    @Mapping(target = "measurementUnits", source = "measurementUnits")
    Edge toEdge(Connection connection);

    @AfterMapping
    default void setTemporaryValues(@MappingTarget Edge edge) {
        // todo get actual values
        edge.setCapacity(0);
        edge.setCo2Emissions(BigDecimal.ZERO);
    }

}
