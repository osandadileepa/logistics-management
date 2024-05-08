package com.quincus.networkmanagement.impl.mapper.qportal;

import com.quincus.networkmanagement.api.domain.VehicleType;
import com.quincus.qportal.model.QPortalVehicleType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface QPortalVehicleTypeMapper {

    @Mapping(
            target = "name",
            expression = "java(qPortalVehicleType.getName() == null ? qPortalVehicleType.getVehicleTypeName() : qPortalVehicleType.getName())"
    )
    VehicleType toVehicleType(QPortalVehicleType qPortalVehicleType);

}
