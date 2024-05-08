package com.quincus.shipment.impl.mapper.qportal;

import com.quincus.qportal.model.QPortalUser;
import com.quincus.shipment.api.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface QPortalUserMapper {
    @Mapping(target = "name", expression = "java(qPortalUser.getFirstName() + \" \" + qPortalUser.getLastName())")
    @Mapping(target = "fullPhoneNumber", expression = "java(qPortalUser.getFullPhoneNumber())")
    User toUser(QPortalUser qPortalUser);
}
