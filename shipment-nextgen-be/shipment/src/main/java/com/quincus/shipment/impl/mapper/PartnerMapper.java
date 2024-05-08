package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.impl.repository.entity.PartnerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PartnerMapper {

    PartnerMapper INSTANCE = Mappers.getMapper(PartnerMapper.class);

    @Mapping(source = "id", target = "externalId")
    PartnerEntity mapDomainToEntity(Partner partner);

    @Mapping(source = "externalId", target = "id")
    Partner mapEntityToDomain(PartnerEntity partnerEntity);
}
