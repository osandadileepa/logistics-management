package com.quincus.networkmanagement.impl.mapper;

import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.repository.entity.NodeEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface NodeMapper {
    Node toDomain(NodeEntity nodeEntity);

    NodeEntity toEntity(Node node);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    NodeEntity update(Node domain, @MappingTarget NodeEntity entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "nodeCode", source = "nodeCode")
    @Mapping(target = "nodeType", source = "nodeType")
    @Mapping(target = "facility", source = "facility")
    @Mapping(target = "facility.lat", source = "facility.lat", ignore = true)
    @Mapping(target = "facility.lon", source = "facility.lon", ignore = true)
    @Mapping(target = "vendor.name", source = "vendor.name")
    @Mapping(target = "tags", source = "tags")
    Node toSearchResult(NodeEntity nodeEntity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nodeCode", source = "nodeCode")
    @Mapping(target = "facility", source = "facility")
    Node toConnectionNode(NodeEntity nodeEntity);
}
