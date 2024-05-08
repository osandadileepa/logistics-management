package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.Milestone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface DriverMapper {
    @Mapping(source = "driverId", target = "id")
    @Mapping(source = "driverName", target = "name")
    @Mapping(source = "driverPhoneCode", target = "phoneCode")
    @Mapping(source = "driverPhoneNumber", target = "phoneNumber")
    Driver milestoneToDriver(Milestone milestone);

}
