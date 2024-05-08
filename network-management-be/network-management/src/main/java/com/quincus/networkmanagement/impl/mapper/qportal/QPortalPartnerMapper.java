package com.quincus.networkmanagement.impl.mapper.qportal;

import com.quincus.networkmanagement.api.domain.Partner;
import com.quincus.qportal.model.QPortalPartner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface QPortalPartnerMapper {
    @Mapping(source = "partnerCode", target = "code")
    Partner toPartner(QPortalPartner qPortalPartner);
}
