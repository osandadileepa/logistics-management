package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocationHierarchyEntityAddressMapper {

    @Mapping(source = "id", target = "locationHierarchyId")
    @Mapping(source = "country.id", target = "country")
    @Mapping(source = "state.id", target = "state")
    @Mapping(source = "city.id", target = "city")
    @Mapping(source = "country.externalId", target = "countryId")
    @Mapping(source = "state.externalId", target = "stateId")
    @Mapping(source = "city.externalId", target = "cityId")
    @Mapping(source = "countryCode", target = "countryName")
    @Mapping(source = "stateCode", target = "stateName")
    @Mapping(source = "cityCode", target = "cityName")
    Address mapLocationHierarchyToAddress(LocationHierarchyEntity lh);
}
