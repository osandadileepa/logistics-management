package com.quincus.networkmanagement.impl.mapper;

import com.quincus.networkmanagement.api.domain.TrainingLog;
import com.quincus.networkmanagement.impl.repository.entity.TrainingLogEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface TrainingLogMapper {

    TrainingLog toDomain(TrainingLogEntity entity);

    TrainingLogEntity toEntity(TrainingLog domain);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    TrainingLogEntity update(TrainingLog domain, @MappingTarget TrainingLogEntity entity);
}
