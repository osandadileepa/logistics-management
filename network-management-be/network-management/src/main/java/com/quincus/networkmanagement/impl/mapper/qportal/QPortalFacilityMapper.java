package com.quincus.networkmanagement.impl.mapper.qportal;

import com.quincus.networkmanagement.api.domain.Facility;
import com.quincus.qportal.model.QPortalFacility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface QPortalFacilityMapper {
    @Mapping(target = "code", source = "locationCode")
    @Mapping(target = "timezone", source = "timezoneTimeInGmt")
    Facility toFacility(QPortalFacility qPortalFacility);
}
