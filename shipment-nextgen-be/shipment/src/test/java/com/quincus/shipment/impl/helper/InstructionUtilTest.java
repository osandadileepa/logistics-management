package com.quincus.shipment.impl.helper;

import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.quincus.shipment.api.constant.InstructionApplyToType.DELIVERY;
import static com.quincus.shipment.api.constant.InstructionApplyToType.JOURNEY;
import static com.quincus.shipment.api.constant.InstructionApplyToType.PICKUP;
import static com.quincus.shipment.api.constant.InstructionApplyToType.SEGMENT;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InstructionUtilTest {

    private static Stream<Arguments> provideOrderInstructions() {
        Instruction pickupInstruction1 = new Instruction();
        pickupInstruction1.setId("pickup-abc-1");
        pickupInstruction1.setExternalId("ext-pickup-abc-1");
        pickupInstruction1.setSource(Instruction.SOURCE_ORDER);
        pickupInstruction1.setApplyTo(PICKUP);
        pickupInstruction1.setLabel("Pickup Instruction X1");
        pickupInstruction1.setValue("Careful on pickup 1");

        Instruction pickupInstruction2 = new Instruction();
        pickupInstruction2.setId("pickup-abc-2");
        pickupInstruction2.setExternalId("ext-pickup-abc-2");
        pickupInstruction2.setSource(Instruction.SOURCE_ORDER);
        pickupInstruction2.setApplyTo(PICKUP);
        pickupInstruction2.setLabel("Pickup Instruction X2");
        pickupInstruction2.setValue("Careful on pickup 2");
        List<Instruction> pickupInstructions = List.of(pickupInstruction1, pickupInstruction2);

        Instruction deliveryInstruction1 = new Instruction();
        deliveryInstruction1.setId("delivery-abc-1");
        deliveryInstruction1.setExternalId("ext-delivery-abc-1");
        deliveryInstruction1.setSource(Instruction.SOURCE_ORDER);
        deliveryInstruction1.setApplyTo(DELIVERY);
        deliveryInstruction1.setLabel("Delivery Instruction X1");
        deliveryInstruction1.setValue("Careful on delivery 1");

        Instruction deliveryInstruction2 = new Instruction();
        deliveryInstruction2.setId("delivery-abc-2");
        deliveryInstruction2.setExternalId("ext-delivery-abc-2");
        deliveryInstruction2.setSource(Instruction.SOURCE_ORDER);
        deliveryInstruction2.setApplyTo(DELIVERY);
        deliveryInstruction2.setLabel("Delivery Instruction X2");
        deliveryInstruction2.setValue("Careful on delivery 2");
        List<Instruction> deliveryInstructions = List.of(deliveryInstruction1, deliveryInstruction2);

        Instruction journeyInstruction1 = new Instruction();
        journeyInstruction1.setId("journey-abc-1");
        journeyInstruction1.setExternalId("ext-journey-abc-1");
        journeyInstruction1.setSource(Instruction.SOURCE_ORDER);
        journeyInstruction1.setApplyTo(JOURNEY);
        journeyInstruction1.setLabel("Pickup and Delivery Instruction X1");
        journeyInstruction1.setValue("Careful 1");

        Instruction journeyInstruction2 = new Instruction();
        journeyInstruction2.setId("journey-abc-2");
        journeyInstruction2.setExternalId("ext-journey-abc-2");
        journeyInstruction2.setSource(Instruction.SOURCE_ORDER);
        journeyInstruction2.setApplyTo(JOURNEY);
        journeyInstruction2.setLabel("Pickup and Delivery Instruction X2");
        journeyInstruction2.setValue("Careful 2");
        List<Instruction> journeyInstructions = List.of(journeyInstruction1, journeyInstruction2);

        return Stream.of(
                Arguments.of(Named.of("Pickup Instructions", pickupInstructions)),
                Arguments.of(Named.of("Delivery Instructions", deliveryInstructions)),
                Arguments.of(Named.of("Journey Instructions", journeyInstructions))
        );
    }

    private static Stream<Arguments> provideMultipleSegmentsWithExistingInstructions() {
        List<Instruction> orderOnlyInstructions = createBaseOrderInstructions();
        List<Instruction> segmentOnlyInstructions = createSegmentInstructions();
        List<Instruction> mixedInstructions = new ArrayList<>();
        mixedInstructions.addAll(createBaseOrderInstructions());
        mixedInstructions.addAll(createSegmentInstructions());

        PackageJourneySegment segmentFirstWithOnlyOrderInstructions = setupDummySegmentWithInstructions(0, orderOnlyInstructions);
        PackageJourneySegment segmentFirstWithOnlySegmentInstructions = setupDummySegmentWithInstructions(0, segmentOnlyInstructions);
        PackageJourneySegment segmentFirstWithOrderAndSegmentInstructions = setupDummySegmentWithInstructions(0, mixedInstructions);
        PackageJourneySegment segmentMidWithOnlyOrderInstructions = setupDummySegmentWithInstructions(1, orderOnlyInstructions);
        PackageJourneySegment segmentMidWithOnlySegmentInstructions = setupDummySegmentWithInstructions(1, segmentOnlyInstructions);
        PackageJourneySegment segmentMidWithOrderAndSegmentInstructions = setupDummySegmentWithInstructions(1, mixedInstructions);
        PackageJourneySegment segmentLastWithOnlyOrderInstructions = setupDummySegmentWithInstructions(2, orderOnlyInstructions);
        PackageJourneySegment segmentLastWithOnlySegmentInstructions = setupDummySegmentWithInstructions(2, segmentOnlyInstructions);
        PackageJourneySegment segmentLastWithOrderAndSegmentInstructions = setupDummySegmentWithInstructions(2, mixedInstructions);

        List<PackageJourneySegment> multipleSegmentsOrderInstructions = List.of(segmentFirstWithOnlyOrderInstructions,
                segmentMidWithOnlyOrderInstructions,
                segmentLastWithOnlyOrderInstructions);
        List<PackageJourneySegment> multipleSegmentsSegmentInstructions = List.of(segmentFirstWithOnlySegmentInstructions,
                segmentMidWithOnlySegmentInstructions,
                segmentLastWithOnlySegmentInstructions);
        List<PackageJourneySegment> multipleSegmentsMixedInstructions = List.of(segmentFirstWithOnlyOrderInstructions,
                segmentMidWithOnlySegmentInstructions,
                segmentLastWithOrderAndSegmentInstructions);
        List<PackageJourneySegment> multipleSegmentsMixed2Instructions = List.of(segmentFirstWithOrderAndSegmentInstructions,
                segmentMidWithOrderAndSegmentInstructions,
                segmentLastWithOrderAndSegmentInstructions);

        return Stream.of(
                Arguments.of(Named.of("Multiple Segments - Order Instructions", multipleSegmentsOrderInstructions)),
                Arguments.of(Named.of("Multiple Segments - Segment Instructions", multipleSegmentsSegmentInstructions)),
                Arguments.of(Named.of("Multiple Segments - Mixed Instructions", multipleSegmentsMixedInstructions)),
                Arguments.of(Named.of("Multiple Segments - Mixed Instructions 2", multipleSegmentsMixed2Instructions))
        );
    }

    private static PackageJourneySegment setupDummySegmentWithInstructions(int ref, List<Instruction> instructions) {
        String refSequence = Integer.toString(ref);
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(UUID.randomUUID().toString());
        segment.setRefId(refSequence);
        segment.setSequence(refSequence);
        segment.setInstructions(new ArrayList<>(instructions));
        return segment;
    }

    private static List<Instruction> createBaseOrderInstructions() {
        Instruction pickupInstruction = new Instruction();
        pickupInstruction.setId("order-instruction-1");
        pickupInstruction.setLabel("Pickup Instruction");
        pickupInstruction.setSource(Instruction.SOURCE_ORDER);
        pickupInstruction.setApplyTo(PICKUP);
        pickupInstruction.setValue("Handle with Care");

        Instruction deliveryInstruction = new Instruction();
        deliveryInstruction.setId("order-instruction-2");
        deliveryInstruction.setLabel("Delivery Instruction");
        deliveryInstruction.setSource(Instruction.SOURCE_ORDER);
        deliveryInstruction.setApplyTo(DELIVERY);
        deliveryInstruction.setValue("Wear PPE");

        return List.of(pickupInstruction, deliveryInstruction);
    }

    private static List<Instruction> createSegmentInstructions() {
        Instruction segmentInstruction1 = new Instruction();
        segmentInstruction1.setId("segment-instruction-1");
        segmentInstruction1.setLabel("Segment 1 Instruction");
        segmentInstruction1.setSource("segment");
        segmentInstruction1.setApplyTo(SEGMENT);
        segmentInstruction1.setValue("Fragile - Handle with Care");

        Instruction segmentInstruction2 = new Instruction();
        segmentInstruction2.setId("segment-instruction-1");
        segmentInstruction2.setLabel("Segment 2 Instruction");
        segmentInstruction2.setSource("segment");
        segmentInstruction2.setApplyTo(SEGMENT);
        segmentInstruction2.setValue("Live Animal - Do not cover holes");

        return List.of(segmentInstruction1, segmentInstruction2);
    }

    private static List<Instruction> getAllInstructionsFromSegment(List<PackageJourneySegment> packageJourneySegments) {
        return packageJourneySegments.stream().flatMap(segment -> segment.getInstructions().stream())
                .collect(Collectors.toList());
    }

    @ParameterizedTest
    @MethodSource("provideMultipleSegmentsWithExistingInstructions")
    void enrichOrderInstructionsToSegments_noInstructionsExistingSegments_shouldRemoveOrderInstructions(List<PackageJourneySegment> packageJourneySegments) {
        InstructionUtil.enrichOrderInstructionsToSegments(Collections.emptyList(), packageJourneySegments);
        assertThat(packageJourneySegments).isNotNull();
        assertThat(getAllInstructionsFromSegment(packageJourneySegments)).satisfies(hasNoOrderInstruction());
    }

    @ParameterizedTest
    @MethodSource("provideOrderInstructions")
    void enrichOrderInstructionsToSegments_newInstructionSingleSegment_shouldReplaceOrderInstructions(List<Instruction> newInstructions) {
        List<Instruction> existingInstructions = new ArrayList<>(createBaseOrderInstructions());
        existingInstructions.addAll(createSegmentInstructions());

        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId("single-segment-x-1");
        segment.setRefId("0");
        segment.setSequence("0");
        segment.setInstructions(existingInstructions);
        List<PackageJourneySegment> packageJourneySegments = List.of(segment);

        InstructionUtil.enrichOrderInstructionsToSegments(newInstructions, packageJourneySegments);
        assertThat(packageJourneySegments).isNotNull();

        PackageJourneySegment updatedSegment = packageJourneySegments.get(0);

        assertThat(updatedSegment.getInstructions()).containsAll(newInstructions);

        List<Instruction> segmentOrderInstructions = updatedSegment.getInstructions().stream()
                .filter(i -> Instruction.SOURCE_ORDER.equals(i.getSource())).toList();
        assertThat(segmentOrderInstructions).hasSize(newInstructions.size());

        List<Instruction> segmentSegmentInstructions = updatedSegment.getInstructions().stream()
                .filter(i -> Instruction.SOURCE_SEGMENT.equals(i.getSource())).toList();
        assertThat(segmentSegmentInstructions).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideMultipleSegmentsWithExistingInstructions")
    void enrichOrderInstructionsToSegments_newPickupInstructionMultipleSegments_shouldReplaceOrderInstructions(List<PackageJourneySegment> packageJourneySegments) {
        Instruction pickupInstruction1 = new Instruction();
        pickupInstruction1.setId("pickup-abc-1");
        pickupInstruction1.setSource(Instruction.SOURCE_ORDER);
        pickupInstruction1.setApplyTo(PICKUP);
        pickupInstruction1.setLabel("Pickup Instruction X1");
        pickupInstruction1.setValue("Careful on pickup 1");

        Instruction pickupInstruction2 = new Instruction();
        pickupInstruction2.setId("pickup-abc-2");
        pickupInstruction2.setSource(Instruction.SOURCE_ORDER);
        pickupInstruction2.setApplyTo(PICKUP);
        pickupInstruction2.setLabel("Pickup Instruction X2");
        pickupInstruction2.setValue("Careful on pickup 2");

        List<Instruction> pickupInstructions = List.of(pickupInstruction1, pickupInstruction2);

        InstructionUtil.enrichOrderInstructionsToSegments(pickupInstructions, packageJourneySegments);

        assertThat(packageJourneySegments).isNotNull();

        PackageJourneySegment firstSegment = packageJourneySegments.get(0);
        assertThat(firstSegment.getInstructions()).contains(pickupInstruction1, pickupInstruction2);

        List<Instruction> firstSegmentOrderInstructions = firstSegment.getInstructions().stream()
                .filter(i -> Instruction.SOURCE_ORDER.equals(i.getSource())).toList();
        assertThat(firstSegmentOrderInstructions).hasSize(pickupInstructions.size());

        List<PackageJourneySegment> remainingSegments = packageJourneySegments.subList(1, packageJourneySegments.size());
        assertThat(getAllInstructionsFromSegment(remainingSegments)).satisfies(hasNoOrderInstruction());
    }

    @ParameterizedTest
    @MethodSource("provideMultipleSegmentsWithExistingInstructions")
    void enrichOrderInstructionsToSegments_newDeliveryInstructionMultipleSegments_shouldReplaceOrderInstructions(List<PackageJourneySegment> packageJourneySegments) {
        Instruction deliveryInstruction1 = new Instruction();
        deliveryInstruction1.setId("delivery-abc-1");
        deliveryInstruction1.setSource(Instruction.SOURCE_ORDER);
        deliveryInstruction1.setApplyTo(DELIVERY);
        deliveryInstruction1.setLabel("Delivery Instruction X1");
        deliveryInstruction1.setValue("Careful on delivery 1");

        Instruction deliveryInstruction2 = new Instruction();
        deliveryInstruction2.setId("delivery-abc-2");
        deliveryInstruction2.setSource(Instruction.SOURCE_ORDER);
        deliveryInstruction2.setApplyTo(DELIVERY);
        deliveryInstruction2.setLabel("Delivery Instruction X2");
        deliveryInstruction2.setValue("Careful on delivery 2");

        List<Instruction> deliveryInstructions = List.of(deliveryInstruction1, deliveryInstruction2);

        InstructionUtil.enrichOrderInstructionsToSegments(deliveryInstructions, packageJourneySegments);

        assertThat(packageJourneySegments).isNotNull();

        int segmentSize = packageJourneySegments.size();

        PackageJourneySegment lastSegment = packageJourneySegments.get(segmentSize - 1);
        assertThat(lastSegment.getInstructions()).contains(deliveryInstruction1, deliveryInstruction2);

        List<Instruction> lastSegmentOrderInstructions = lastSegment.getInstructions().stream()
                .filter(i -> Instruction.SOURCE_ORDER.equals(i.getSource())).toList();
        assertThat(lastSegmentOrderInstructions).hasSize(deliveryInstructions.size());

        List<PackageJourneySegment> remainingSegments = packageJourneySegments.subList(0, segmentSize - 1);
        assertThat(getAllInstructionsFromSegment(remainingSegments)).satisfies(hasNoOrderInstruction());
    }

    @ParameterizedTest
    @MethodSource("provideMultipleSegmentsWithExistingInstructions")
    void enrichOrderInstructionsToSegments_newJourneyInstructionMultipleSegments_shouldReplaceOrderInstructions(List<PackageJourneySegment> packageJourneySegments) {
        Instruction journeyInstruction1 = new Instruction();
        journeyInstruction1.setId("journey-abc-1");
        journeyInstruction1.setSource(Instruction.SOURCE_ORDER);
        journeyInstruction1.setApplyTo(JOURNEY);
        journeyInstruction1.setLabel("Pickup and Delivery Instruction X1");
        journeyInstruction1.setValue("Careful 1");

        Instruction journeyInstruction2 = new Instruction();
        journeyInstruction2.setId("journey-abc-2");
        journeyInstruction2.setSource(Instruction.SOURCE_ORDER);
        journeyInstruction2.setApplyTo(JOURNEY);
        journeyInstruction2.setLabel("Pickup and Delivery Instruction X2");
        journeyInstruction2.setValue("Careful 2");

        List<Instruction> journeyInstructions = List.of(journeyInstruction1, journeyInstruction2);

        InstructionUtil.enrichOrderInstructionsToSegments(journeyInstructions, packageJourneySegments);

        assertThat(packageJourneySegments).isNotNull();

        int segmentSize = packageJourneySegments.size();

        PackageJourneySegment firstSegment = packageJourneySegments.get(0);
        assertThat(firstSegment.getInstructions()).contains(journeyInstruction1, journeyInstruction2);

        PackageJourneySegment lastSegment = packageJourneySegments.get(segmentSize - 1);
        assertThat(lastSegment.getInstructions()).contains(journeyInstruction1, journeyInstruction2);

        List<Instruction> firstSegmentOrderInstructions = firstSegment.getInstructions().stream()
                .filter(i -> Instruction.SOURCE_ORDER.equals(i.getSource())).toList();
        assertThat(firstSegmentOrderInstructions).hasSize(journeyInstructions.size());

        List<Instruction> lastSegmentOrderInstructions = lastSegment.getInstructions().stream()
                .filter(i -> Instruction.SOURCE_ORDER.equals(i.getSource())).toList();
        assertThat(lastSegmentOrderInstructions).hasSize(journeyInstructions.size());

        List<PackageJourneySegment> remainingSegments = packageJourneySegments.subList(1, packageJourneySegments.size() - 1);
        assertThat(getAllInstructionsFromSegment(remainingSegments)).satisfies(hasNoOrderInstruction());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void enrichOrderInstructionsToSegments_noInstructionsAndSegments_shouldDoNothing(List<PackageJourneySegment> packageJourneySegments) {
        InstructionUtil.enrichOrderInstructionsToSegments(Collections.emptyList(), packageJourneySegments);
        assertThat(packageJourneySegments).isNullOrEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void enrichOrderInstructionsToSegments_noSegments_shouldDoNothing(List<PackageJourneySegment> packageJourneySegments) {
        List<Instruction> orderInstructions = createBaseOrderInstructions();

        InstructionUtil.enrichOrderInstructionsToSegments(orderInstructions, packageJourneySegments);
        assertThat(packageJourneySegments).isNullOrEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideOrderInstructions")
    void updateInstructionList_emptyEntityNewInstructions_shouldAddNewInstructions(List<Instruction> newInstructions) {
        List<InstructionEntity> existingInstructions = new ArrayList<>();

        InstructionUtil.updateInstructionList(existingInstructions, newInstructions);
        assertThat(existingInstructions).hasSize(newInstructions.size());
        assertThat(existingInstructions.get(0).getId()).isEqualTo(newInstructions.get(0).getId());
        assertThat(existingInstructions.get(0).getExternalId()).isEqualTo(newInstructions.get(0).getExternalId());
        assertThat(existingInstructions.get(0).getSource()).isEqualTo(newInstructions.get(0).getSource());
        assertThat(existingInstructions.get(0).getApplyTo()).isEqualTo(newInstructions.get(0).getApplyTo());
        assertThat(existingInstructions.get(0).getLabel()).isEqualTo(newInstructions.get(0).getLabel());
        assertThat(existingInstructions.get(0).getValue()).isEqualTo(newInstructions.get(0).getValue());
    }

    @ParameterizedTest
    @MethodSource("provideOrderInstructions")
    void updateInstructionList_existingEntityNewInstructions_shouldAddNewInstructions(List<Instruction> newInstructions) {
        List<InstructionEntity> existingInstructions = new ArrayList<>();
        InstructionEntity existingInstruction = new InstructionEntity();
        existingInstruction.setId("throwaway-abc-1");
        existingInstruction.setExternalId(newInstructions.get(0).getExternalId());
        existingInstruction.setSource(Instruction.SOURCE_SEGMENT);
        existingInstruction.setApplyTo(SEGMENT);
        existingInstruction.setLabel("throwaway label");
        existingInstruction.setValue("throwaway value");
        existingInstructions.add(existingInstruction);

        InstructionUtil.updateInstructionList(existingInstructions, newInstructions);
        assertThat(existingInstructions).hasSize(newInstructions.size());
        //ID is unchanged
        assertThat(existingInstructions.get(0).getId()).isEqualTo(existingInstructions.get(0).getId());

        assertThat(existingInstructions.get(0).getExternalId()).isEqualTo(newInstructions.get(0).getExternalId());
        assertThat(existingInstructions.get(0).getSource()).isEqualTo(newInstructions.get(0).getSource());
        assertThat(existingInstructions.get(0).getApplyTo()).isEqualTo(newInstructions.get(0).getApplyTo());
        assertThat(existingInstructions.get(0).getLabel()).isEqualTo(newInstructions.get(0).getLabel());
        assertThat(existingInstructions.get(0).getValue()).isEqualTo(newInstructions.get(0).getValue());
    }

    @Test
    void updateInstructionList_existingEntityNoInstructions_shouldReturnEmpty() {
        List<Instruction> newOrUpdatedInstructions = Collections.emptyList();
        List<InstructionEntity> existingInstructions = new ArrayList<>();
        InstructionEntity existingInstruction = new InstructionEntity();
        existingInstruction.setId("segment-abc-1");
        existingInstruction.setExternalId("xyz-segment-abc-1");
        existingInstruction.setSource(Instruction.SOURCE_SEGMENT);
        existingInstruction.setApplyTo(SEGMENT);
        existingInstruction.setLabel("Segment Instruction Z1");
        existingInstruction.setValue("Extra Careful in this area 1");
        existingInstructions.add(existingInstruction);

        InstructionUtil.updateInstructionList(existingInstructions, newOrUpdatedInstructions);
        assertThat(existingInstructions).isEmpty();
    }

    @Test
    void updateInstructionList_emptyEntityNoInstructions_shouldReturnEmpty() {
        List<Instruction> newOrUpdatedInstructions = Collections.emptyList();
        List<InstructionEntity> existingInstructions = Collections.emptyList();

        InstructionUtil.updateInstructionList(existingInstructions, newOrUpdatedInstructions);
        assertThat(existingInstructions).isEmpty();
    }

    private Condition<List<? extends Instruction>> hasNoOrderInstruction() {
        return new Condition<>(Predicate.not(instructionContainsSource(Instruction.SOURCE_ORDER)),
                "does not contain order instruction");
    }

    private Predicate<List<? extends Instruction>> instructionContainsSource(String source) {
        return instructions -> instructions.stream()
                .map(Instruction::getSource)
                .toList()
                .contains(source);
    }
}
