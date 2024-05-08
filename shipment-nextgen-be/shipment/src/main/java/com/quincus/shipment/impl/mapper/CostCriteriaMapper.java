package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.filter.CostFilter;
import com.quincus.shipment.impl.repository.criteria.CostCriteria;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface CostCriteriaMapper {

    CostCriteria mapFilterToCriteria(CostFilter filter, String organizationId);

    @AfterMapping
    default void addPaginationFilter(@MappingTarget CostCriteria costCriteria, CostFilter filter) {
        costCriteria.setPage(filter.getPageNumber() + 1);
        costCriteria.setPerPage(filter.getSize());
        costCriteria.setSortDir(filter.getSortDir());
        costCriteria.setSortBy(filter.getSortBy());
    }
}
