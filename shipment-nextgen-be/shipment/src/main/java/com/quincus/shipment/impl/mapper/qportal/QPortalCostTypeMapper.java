package com.quincus.shipment.impl.mapper.qportal;

import com.quincus.qportal.model.QPortalCostType;
import com.quincus.shipment.api.constant.CostCategory;
import com.quincus.shipment.api.domain.CostType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface QPortalCostTypeMapper {

    @Mapping(target = "category", source = "category", qualifiedByName = "toCostCategory")
    CostType toCostType(QPortalCostType qPortalCostType);

    @Named("toCostCategory")
    default CostCategory toCostCategory(String category){
        return CostCategory.valueOf(category.toUpperCase());
    }
}
