package com.quincus.networkmanagement.impl.mapper;

import com.quincus.networkmanagement.api.constant.TransportType;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.VehicleType;
import com.quincus.networkmanagement.impl.repository.entity.ConnectionEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ConnectionMapper {

    String AIR_CONNECTION_VEHICLE_TYPE_NAME = "aircraft";

    @BeforeMapping
    default void beforeMapping(@MappingTarget ConnectionEntity target, Connection source) {
        // ensure vehicle type is set to aircraft when transportType is AIR
        if (source.getTransportType() == TransportType.AIR) {
            VehicleType airVehicleType = new VehicleType();
            airVehicleType.setName(AIR_CONNECTION_VEHICLE_TYPE_NAME);
            source.setVehicleType(airVehicleType);
        }
    }

    Connection toDomain(ConnectionEntity connectionEntity);

    ConnectionEntity toEntity(Connection connection);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    ConnectionEntity update(Connection domain, @MappingTarget ConnectionEntity entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "vendor.name", source = "vendor.name")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "transportType", source = "transportType")
    @Mapping(target = "connectionCode", source = "connectionCode")
    @Mapping(target = "departureNode.id", source = "departureNode.id")
    @Mapping(target = "departureNode.nodeCode", source = "departureNode.nodeCode")
    @Mapping(target = "arrivalNode.id", source = "arrivalNode.id")
    @Mapping(target = "arrivalNode.nodeCode", source = "arrivalNode.nodeCode")
    Connection toSearchResult(ConnectionEntity nodeEntity);
}
