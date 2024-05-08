package com.quincus.shipment.impl.mapper;


import com.quincus.shipment.api.filter.NetworkLaneFilter;
import com.quincus.shipment.impl.repository.criteria.NetworkLaneCriteria;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface NetworkLaneCriteriaMapper {

    NetworkLaneCriteria mapFilterToCriteria(NetworkLaneFilter filter, String organizationId);

    @AfterMapping
    default void addNetworkLaneFilterInfo(@MappingTarget NetworkLaneCriteria shipmentCriteria, NetworkLaneFilter filter) {
        shipmentCriteria.setPage(filter.getPageNumber() + 1);
        shipmentCriteria.setPerPage(filter.getSize());
    }

}
