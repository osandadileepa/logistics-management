package com.quincus.finance.costing.weightcalculation.db.mapper;

import com.quincus.db.model.OrganizationEntity;
import com.quincus.finance.costing.common.web.model.RoundingLogic;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationRule;
import com.quincus.finance.costing.weightcalculation.db.model.WeightCalculationRuleEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", imports = BigDecimal.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WeightCalculationRuleMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(target = "organizationId", expression = "java(toOrganizationId(weightCalculationRuleEntity.getOrganization()))")
    @Mapping(target = "roundingLogic", expression = "java(toRoundingLogic(weightCalculationRuleEntity))")
    WeightCalculationRule mapEntityToDomain(WeightCalculationRuleEntity weightCalculationRuleEntity);

    @Mapping(target = "roundingPlace", source = "roundingLogic.roundTo")
    @Mapping(target = "roundingThreshold", source = "roundingLogic.threshold")
    WeightCalculationRuleEntity mapDomainToEntity(WeightCalculationRule weightCalculationRule);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    WeightCalculationRuleEntity update(WeightCalculationRule domain, @MappingTarget WeightCalculationRuleEntity entity);

    default String toOrganizationId(OrganizationEntity organizationEntity) {
        return organizationEntity.getId();
    }

    default RoundingLogic toRoundingLogic(WeightCalculationRuleEntity weightCalculationRuleEntity) {
        return new RoundingLogic(weightCalculationRuleEntity.getRoundingPlace(), weightCalculationRuleEntity.getRoundingThreshold());
    }
}
