package com.quincus.shipment.impl.mapper;

import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.helper.EnumUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.NONE)
public class OMInstructionMapper {

    public static List<Instruction> mapOmInstructions(List<com.quincus.order.api.domain.Instruction> omInstructions, String organisationId, String orderId) {
        return Optional.ofNullable(omInstructions)
                .orElse(Collections.emptyList())
                .stream()
                .map(i -> mapInstruction(i, organisationId, orderId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Instruction mapInstruction(com.quincus.order.api.domain.Instruction omInstruction, String organisationId, String orderId) {
        Instruction instruction = new Instruction();
        instruction.setOrganizationId(organisationId);
        instruction.setOrderId(orderId);
        instruction.setExternalId(omInstruction.getId());
        instruction.setLabel(omInstruction.getLabel());
        instruction.setSource(omInstruction.getSource());
        instruction.setValue(omInstruction.getValue());
        instruction.setApplyTo(EnumUtil.toEnum(InstructionApplyToType.class, omInstruction.getApplyTo()));
        instruction.setCreatedAt(omInstruction.getCreatedAt());
        instruction.setUpdatedAt(omInstruction.getUpdatedAt());
        return instruction;
    }
}
