package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.impl.repository.entity.AlertEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AlertMapper {
    @Mapping(source = "id", target = "id")
    Alert toDomain(AlertEntity alertEntity);

    AlertEntity toEntity(Alert alert);
}
