package com.quincus.shipment.impl.helper;

import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.impl.mapper.InstructionMapper;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import lombok.experimental.UtilityClass;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.quincus.shipment.api.constant.InstructionApplyToType.DELIVERY;
import static com.quincus.shipment.api.constant.InstructionApplyToType.JOURNEY;
import static com.quincus.shipment.api.constant.InstructionApplyToType.PICKUP;

@UtilityClass
public class InstructionUtil {
    private static final InstructionMapper INSTRUCTION_MAPPER = Mappers.getMapper(InstructionMapper.class);

    public void enrichOrderInstructionsToSegments(List<Instruction> instructions,
                                                  List<PackageJourneySegment> packageJourneySegments) {
        if (CollectionUtils.isEmpty(packageJourneySegments)) {
            return;
        }

        if (CollectionUtils.isEmpty(instructions)) {
            clearOrderInstructions(packageJourneySegments);
            return;
        }

        List<PackageJourneySegment> nonDeletedSegments = packageJourneySegments.stream()
                .filter(Predicate.not(PackageJourneySegment::isDeleted))
                .sorted(Comparator.comparing(PackageJourneySegment::getSequence))
                .collect(Collectors.toCollection(ArrayList::new));

        clearOrderInstructions(nonDeletedSegments);

        PackageJourneySegment firstSegment = nonDeletedSegments.get(0);
        int pjsSize = nonDeletedSegments.size();

        if (pjsSize == 1) {
            addNewInstructions(firstSegment, instructions);
        } else {
            PackageJourneySegment lastSegment = nonDeletedSegments.get(pjsSize - 1);

            List<Instruction> firstSegmentInstructions = new ArrayList<>();
            List<Instruction> lastSegmentInstructions = new ArrayList<>();

            instructions.forEach(instruction -> {
                if (instruction.getApplyTo() == PICKUP || instruction.getApplyTo() == JOURNEY) {
                    firstSegmentInstructions.add(instruction);
                }
                if (instruction.getApplyTo() == InstructionApplyToType.DELIVERY || instruction.getApplyTo() == JOURNEY) {
                    lastSegmentInstructions.add(instruction);
                }
            });

            addNewInstructions(firstSegment, firstSegmentInstructions);
            addNewInstructions(lastSegment, lastSegmentInstructions);
        }
    }

    public void updateInstructionList(List<InstructionEntity> existingInstructions, List<Instruction> newOrUpdatedInstructions) {
        Map<String, InstructionEntity> existingInstructionsMap = existingInstructions != null ?
                existingInstructions.stream()
                        .collect(Collectors.toMap(InstructionEntity::getExternalId, Function.identity()))
                : Collections.emptyMap();

        List<Instruction> refInstructions = newOrUpdatedInstructions != null ? newOrUpdatedInstructions
                : Collections.emptyList();
        List<Instruction> newInstructions = new ArrayList<>();
        List<String> updatedInstructionIds = new ArrayList<>();
        for (Instruction instruction : refInstructions) {
            String instructionId = instruction.getExternalId();
            InstructionEntity instructionEntity = existingInstructionsMap.get(instructionId);
            if (instructionEntity == null) {
                newInstructions.add(instruction);
                continue;
            }
            INSTRUCTION_MAPPER.update(instructionEntity, instruction);
            updatedInstructionIds.add(instructionId);
        }
        if (existingInstructions != null) {
            existingInstructions.removeIf(instruction -> !updatedInstructionIds.contains(instruction.getExternalId()));
        }

        if (!newInstructions.isEmpty()) {
            if (existingInstructions == null) {
                existingInstructions = new ArrayList<>();
            }
            existingInstructions.addAll(newInstructions.stream().map(INSTRUCTION_MAPPER::toEntity).toList());
        }
    }

    public void enrichNewInstructions(List<InstructionEntity> newInstructions, String organizationId) {
        if (CollectionUtils.isEmpty(newInstructions)) {
            return;
        }
        for (InstructionEntity instruction : newInstructions) {
            if (instruction.getOrganizationId() != null) {
                continue;
            }
            instruction.setOrganizationId(organizationId);
        }
    }

    private void clearOrderInstructions(List<PackageJourneySegment> packageJourneySegments) {
        packageJourneySegments.stream()
                .filter(Predicate.not(segment -> CollectionUtils.isEmpty(segment.getInstructions())))
                .map(PackageJourneySegment::getInstructions)
                .forEach(instructionList -> instructionList
                        .removeIf(InstructionUtil::isOrderInstruction));
    }

    private boolean isOrderInstruction(Instruction instruction) {
        List<InstructionApplyToType> orderTypes = List.of(PICKUP, DELIVERY, JOURNEY);
        return orderTypes.contains(instruction.getApplyTo());
    }

    private void addNewInstructions(PackageJourneySegment segment, List<Instruction> newInstructions) {
        if (segment.getInstructions() == null) {
            segment.setInstructions(new ArrayList<>());
        }
        segment.getInstructions().addAll(newInstructions);
    }
}
