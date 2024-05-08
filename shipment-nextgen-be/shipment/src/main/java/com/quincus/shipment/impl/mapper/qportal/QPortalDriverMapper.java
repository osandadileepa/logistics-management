package com.quincus.shipment.impl.mapper.qportal;

import com.quincus.qportal.model.QPortalDriver;
import com.quincus.shipment.api.domain.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface QPortalDriverMapper {
    Driver toDriver(QPortalDriver qPortalDriver);
}
