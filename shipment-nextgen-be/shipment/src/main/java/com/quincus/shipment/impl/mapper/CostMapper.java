package com.quincus.shipment.impl.mapper;


import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.CostFacility;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.dto.CostSearchResponse;
import com.quincus.shipment.impl.repository.entity.CostEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface CostMapper {

    Cost toDomain(CostEntity costEntity);

    CostEntity toEntity(Cost cost);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "issuedTimezone", ignore = true)
    @Mapping(target = "createdTimezone", ignore = true)
    CostEntity update(Cost domain, @MappingTarget CostEntity entity);


    @Mapping(source = "location.countryId", target = "countryId")
    @Mapping(source = "location.countryName", target = "countryName")
    @Mapping(source = "location.stateId", target = "stateId")
    @Mapping(source = "location.stateName", target = "stateName")
    @Mapping(source = "location.cityId", target = "cityId")
    @Mapping(source = "location.cityName", target = "cityName")
    @Mapping(source = "name", target = "facilityName")
    @Mapping(source = "externalId", target = "facilityId")
    CostFacility toCostFacility(Facility facility);

    @Mapping(source = "costType.name", target = "costType")
    @Mapping(source = "currency.symbol", target = "currencySymbol")
    @Mapping(source = "currency.code", target = "currencyCode")
    @Mapping(source = "driverName", target = "incurredBy")
    @Mapping(source = "issuedDate", target = "incurredByDate")
    @Mapping(source = "issuedTimezone", target = "incurredByTimezone")
    @Mapping(source = "partnerName", target = "vendor")
    CostSearchResponse toCostResponse(CostEntity cost);

}
