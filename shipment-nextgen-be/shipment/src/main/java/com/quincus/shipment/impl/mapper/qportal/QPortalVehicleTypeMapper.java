package com.quincus.shipment.impl.mapper.qportal;

import com.quincus.qportal.model.QPortalVehicleType;
import com.quincus.shipment.api.domain.VehicleType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface QPortalVehicleTypeMapper {
    VehicleType toVehicleType(QPortalVehicleType qPortalVehicleType);
}
