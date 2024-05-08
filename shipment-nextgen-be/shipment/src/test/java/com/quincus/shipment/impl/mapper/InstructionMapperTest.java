package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InstructionMapperTest {
    @Spy
    private InstructionMapper mapper = Mappers.getMapper(InstructionMapper.class);

    @Test
    void toEntity_instructionDomain_shouldReturnInstructionEntity() {
        Instruction domain = new Instruction();
        domain.setExternalId("2678bdeb-73a5-4679-8d11-b169867cd111");
        domain.setLabel("label");
        domain.setSource(Instruction.SOURCE_ORDER);
        domain.setApplyTo(InstructionApplyToType.JOURNEY);
        domain.setCreatedAt("2023-04-20T11:10:43.614Z");
        domain.setUpdatedAt("2023-04-21T01:10:43.614Z");

        final InstructionEntity entity = mapper.toEntity(domain);

        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version", "packageJourneySegment", "organizationId")
                .isEqualTo(domain);
    }

    @Test
    void toDomain_instructionEntity_shouldReturnInstructionDomain() {
        InstructionEntity entity = new InstructionEntity();
        entity.setExternalId("2678bdeb-73a5-4679-8d11-b169867cd111");
        entity.setLabel("label");
        entity.setSource(Instruction.SOURCE_ORDER);
        entity.setApplyTo(InstructionApplyToType.JOURNEY);
        entity.setCreatedAt("2023-04-20T11:10:43.614Z");
        entity.setUpdatedAt("2023-04-21T01:10:43.614Z");

        final Instruction domain = mapper.toDomain(entity);

        assertThat(domain)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version")
                .isEqualTo(entity);
    }
}
