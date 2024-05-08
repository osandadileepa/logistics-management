package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.NetworkLaneSegment;
import com.quincus.shipment.api.dto.csv.NetworkLaneSegmentCsv;
import com.quincus.shipment.impl.repository.entity.NetworkLaneSegmentEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class NetworkLaneSegmentMapper {

    @Autowired
    private LocationHierarchyEntityFacilityMapper lhToFacilityMapper;

    @Mapping(target = "deliveryInstruction", source = "dropOffInstruction")
    @Mapping(target = "startFacility.name", source = "pickupFacilityName")
    @Mapping(target = "endFacility.name", source = "dropOffFacilityName")
    @Mapping(target = "partner.name", source = "partnerName")
    @Mapping(target = "sequence", source = "sequenceNumber")
    @Mapping(target = "transportType", source = "transportCategory")
    public abstract NetworkLaneSegment mapCsvToDomain(NetworkLaneSegmentCsv csv);


    @Mapping(target = "partner", ignore = true)//ignoring mapping as partner has special setup
    public abstract NetworkLaneSegmentEntity mapDomainToEntity(NetworkLaneSegment domain);

    @Mapping(target = "partner.id", source = "partner.externalId")
    public abstract NetworkLaneSegment mapEntityToDomain(NetworkLaneSegmentEntity entity);

    public List<NetworkLaneSegment> mapEntitiesToDomain(List<NetworkLaneSegmentEntity> entities) {
        return entities.stream().map(this::mapEntityToDomain).toList();
    }

    @AfterMapping
    public void mapOriginAndDestination(@MappingTarget NetworkLaneSegment domain, NetworkLaneSegmentEntity entity) {
        domain.setStartFacility(lhToFacilityMapper.mapLocationHierarchyToFacility(entity.getStartLocationHierarchy()));
        domain.setEndFacility(lhToFacilityMapper.mapLocationHierarchyToFacility(entity.getEndLocationHierarchy()));
    }

}
