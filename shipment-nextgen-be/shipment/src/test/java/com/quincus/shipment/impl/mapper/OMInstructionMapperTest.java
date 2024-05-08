package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.api.domain.Instruction;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OMInstructionMapperTest {

    @Test
    void omInstruction_whenMapOmInstruction_shouldMapToShipmentInstructionsProperly() {
        String orgId = "testOrgId";
        String orderId = "orderId1";
        List<com.quincus.order.api.domain.Instruction> omInstructions = createInstuctions();
        List<Instruction> instructionList = OMInstructionMapper.mapOmInstructions(omInstructions, orgId, orderId);

        assertThat(instructionList).isNotEmpty().hasSize(3);
        Instruction firstInstruction = instructionList.get(0);
        assertThat(firstInstruction.getOrganizationId()).isEqualTo(orgId);
        assertThat(firstInstruction.getOrderId()).isEqualTo(orderId);
        assertThat(firstInstruction.getExternalId()).isEqualTo("2678bdeb-73a5-4679-8d11-b169867cd901");
        assertThat(firstInstruction.getLabel()).isEqualTo("pickup label");
        assertThat(firstInstruction.getSource()).isEqualTo(Instruction.SOURCE_ORDER);
        assertThat(firstInstruction.getApplyTo()).isEqualTo(InstructionApplyToType.PICKUP);
        assertThat(firstInstruction.getCreatedAt()).isEqualTo("2023-04-19T09:10:43.614Z");
        assertThat(firstInstruction.getUpdatedAt()).isEqualTo("2023-04-19T10:10:43.614Z");
        assertThat(instructionList.get(1).getExternalId()).isEqualTo("2678bdeb-73a5-4679-8d11-b169867cd111");
        assertThat(instructionList.get(2).getExternalId()).isEqualTo("2678bdeb-73a5-4679-8d11-b169867cd111");
    }

    @Test
    void nullOmInstruction_whenMapOmInstruction_shouldReturnEmptyCollection() {
        String orgId = "testOrgId";
        String orderId = "orderId1";
        List<Instruction> instructionList = OMInstructionMapper.mapOmInstructions(null, orgId, orderId);

        assertThat(instructionList).isEmpty();
    }

    List<com.quincus.order.api.domain.Instruction> createInstuctions() {

        List<com.quincus.order.api.domain.Instruction> instructions = new ArrayList<>();
        com.quincus.order.api.domain.Instruction pickUpInstruction = new com.quincus.order.api.domain.Instruction();
        pickUpInstruction.setId("2678bdeb-73a5-4679-8d11-b169867cd901");
        pickUpInstruction.setLabel("pickup label");
        pickUpInstruction.setSource("order");
        pickUpInstruction.setApplyTo("pickup");
        pickUpInstruction.setCreatedAt("2023-04-19T09:10:43.614Z");
        pickUpInstruction.setUpdatedAt("2023-04-19T10:10:43.614Z");
        instructions.add(pickUpInstruction);

        com.quincus.order.api.domain.Instruction deliveryInstruction = new com.quincus.order.api.domain.Instruction();
        deliveryInstruction.setId("2678bdeb-73a5-4679-8d11-b169867cd111");
        deliveryInstruction.setLabel("delivery label");
        deliveryInstruction.setSource("order");
        deliveryInstruction.setApplyTo("delivery");
        deliveryInstruction.setCreatedAt("2023-04-20T11:10:43.614Z");
        deliveryInstruction.setUpdatedAt("2023-04-21T01:10:43.614Z");
        instructions.add(deliveryInstruction);

        com.quincus.order.api.domain.Instruction journeyInstruction = new com.quincus.order.api.domain.Instruction();
        journeyInstruction.setId("2678bdeb-73a5-4679-8d11-b169867cd111");
        journeyInstruction.setLabel("journey label");
        journeyInstruction.setSource("order");
        journeyInstruction.setApplyTo("journey");
        journeyInstruction.setCreatedAt("2023-04-20T11:10:43.614Z");
        journeyInstruction.setUpdatedAt("2023-04-21T01:10:43.614Z");
        instructions.add(journeyInstruction);

        return instructions;
    }
}
