package com.quincus.shipment.impl.mapper;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.filter.SearchFilter;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.impl.repository.criteria.ShipmentCriteria;
import com.quincus.shipment.impl.repository.specification.predicate.ShipmentLocationCoveragePredicate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ShipmentCriteriaMapper {

    ShipmentCriteria mapFilterToCriteria(
            SearchFilter filter,
            ObjectMapper objectMapper,
            List<ShipmentLocationCoveragePredicate> shipmentLocationCoveragePredicates
    );

    @AfterMapping
    default void addShipmentFilterInfo(@MappingTarget ShipmentCriteria shipmentCriteria, SearchFilter filter) {
        if (filter instanceof ShipmentFilter shipmentFilter) {
            shipmentCriteria.setPage(shipmentFilter.getPageNumber() + 1);
            shipmentCriteria.setPerPage(shipmentFilter.getSize());
        }
    }

}
