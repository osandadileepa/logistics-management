package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Archived;
import com.quincus.shipment.impl.repository.entity.ArchivedEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ArchivedMapper {
    
    Archived toDomain(ArchivedEntity archivedEntity);

    ArchivedEntity toEntity(Archived archive);
}
