package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface VehicleMapper {
    @Mapping(source = "vehicleId", target = "id")
    @Mapping(source = "vehicleType", target = "type")
    @Mapping(source = "vehicleName", target = "name")
    @Mapping(source = "vehicleNumber", target = "number")
    Vehicle milestoneToVehicle(Milestone milestone);

}
