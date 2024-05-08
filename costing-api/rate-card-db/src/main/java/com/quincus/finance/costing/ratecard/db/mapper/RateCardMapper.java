package com.quincus.finance.costing.ratecard.db.mapper;

import com.quincus.finance.costing.ratecard.api.model.RateCard;
import com.quincus.finance.costing.ratecard.db.model.RateCardEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", imports = BigDecimal.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RateCardMapper {

    @Mapping(source = "id", target = "id")
    RateCard mapEntityToDomain(RateCardEntity rateCardEntity);

    RateCardEntity mapDomainToEntity(RateCard rateCard);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    RateCardEntity update(RateCard domain, @MappingTarget RateCardEntity entity);

}
