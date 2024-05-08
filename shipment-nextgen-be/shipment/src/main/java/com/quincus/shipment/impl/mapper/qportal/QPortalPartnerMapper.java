package com.quincus.shipment.impl.mapper.qportal;

import com.quincus.qportal.model.QPortalPartner;
import com.quincus.shipment.api.domain.Partner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface QPortalPartnerMapper {
    @Mapping(source = "locationId", target = "address.id")
    @Mapping(source = "addressLine", target = "address.line1")
    @Mapping(source = "location.countryId", target = "address.countryId")
    @Mapping(source = "location.stateId", target = "address.stateId")
    @Mapping(source = "location.cityId", target = "address.cityId")
    @Mapping(source = "location.countryName", target = "address.countryName")
    @Mapping(source = "location.stateName", target = "address.stateName")
    @Mapping(source = "location.cityName", target = "address.cityName")
    @Mapping(source = "location.countryName", target = "address.country")
    @Mapping(source = "location.stateName", target = "address.state")
    @Mapping(source = "location.cityName", target = "address.city")
    @Mapping(source = "postalCode", target = "address.postalCode")
    @Mapping(source = "partnerCode", target = "code")
    @Mapping(source = "organisationId", target = "organizationId")
    Partner toPartner(QPortalPartner qPortalPartner);
}
