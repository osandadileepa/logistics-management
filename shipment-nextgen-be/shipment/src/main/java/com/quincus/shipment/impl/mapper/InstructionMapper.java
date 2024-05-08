package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface InstructionMapper {
    InstructionEntity toEntity(Instruction instruction);

    Instruction toDomain(InstructionEntity instructionEntity);

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget InstructionEntity entity, Instruction updateEntity);
}
