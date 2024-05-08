package com.quincus.shipment.api.helper;

import com.quincus.shipment.api.constant.MilestoneCode;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MilestoneCodeUtilTest {

    private static Stream<Arguments> provideDispatchSuccessfulMilestoneCode() {
        return Stream.of(
                Arguments.of(MilestoneCode.DSP_PICKUP_SUCCESSFUL),
                Arguments.of(MilestoneCode.DSP_DELIVERY_SUCCESSFUL)
        );
    }

    private static Stream<Arguments> providePickupOrDeliveryOutcomeMilestoneCode() {
        return Stream.of(
                Arguments.of(MilestoneCode.DSP_PICKUP_SUCCESSFUL),
                Arguments.of(MilestoneCode.DSP_DELIVERY_SUCCESSFUL),
                Arguments.of(MilestoneCode.DSP_PICKUP_FAILED),
                Arguments.of(MilestoneCode.DSP_DELIVERY_FAILED)
        );
    }

    private static Stream<Arguments> provideDriverUpdatesMilestoneCode() {
        return Stream.of(
                Arguments.of(MilestoneCode.DSP_DISPATCH_SCHEDULED),
                Arguments.of(MilestoneCode.DSP_ASSIGNMENT_UPDATED),
                Arguments.of(MilestoneCode.DSP_ASSIGNMENT_CANCELED)
        );
    }

    private static Stream<Arguments> provideDriverArrivedMilestoneCode() {
        return Stream.of(
                Arguments.of(MilestoneCode.DSP_DRIVER_ARRIVED_FOR_PICKUP),
                Arguments.of(MilestoneCode.DSP_DRIVER_ARRIVED_FOR_DELIVERY)
        );
    }

    private static Stream<Arguments> provideNonSegmentRelatedMilestoneCode() {
        return Stream.of(
                Arguments.of(MilestoneCode.OM_BOOKED)
        );
    }

    @ParameterizedTest
    @MethodSource("provideDispatchSuccessfulMilestoneCode")
    void isDispatchSuccessful_pickUpOrDeliverySuccessfulMilestone_shouldReturnTrue(MilestoneCode milestoneCode) {
        assertThat(MilestoneCodeUtil.isDispatchSuccessful(milestoneCode)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("providePickupOrDeliveryOutcomeMilestoneCode")
    void isCodePickupOrDeliveryRelated_pickUpOrDeliveryMilestone_shouldReturnTrue(MilestoneCode milestoneCode) {
        assertThat(MilestoneCodeUtil.isCodePickupOrDeliveryRelated(milestoneCode)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideDriverUpdatesMilestoneCode")
    void isCodeUpdatingDriver_driverUpdateMilestone_shouldReturnTrue(MilestoneCode milestoneCode) {
        assertThat(MilestoneCodeUtil.isCodeUpdatingDriver(milestoneCode)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideDriverArrivedMilestoneCode")
    void isCodeDriverArrived_driverArrivedMilestone_shouldReturnTrue(MilestoneCode milestoneCode) {
        assertThat(MilestoneCodeUtil.isCodeDriverArrived(milestoneCode)).isTrue();
    }

    @ParameterizedTest
    @MethodSource({"provideDriverUpdatesMilestoneCode", "provideDriverArrivedMilestoneCode"})
    void isDispatchSuccessful_otherMilestones_shouldReturnFalse(MilestoneCode milestoneCode) {
        assertThat(MilestoneCodeUtil.isDispatchSuccessful(milestoneCode)).isFalse();
    }

    @ParameterizedTest
    @MethodSource({"provideDriverUpdatesMilestoneCode", "provideDriverArrivedMilestoneCode"})
    void isCodePickupOrDeliveryRelated_otherMilestones_shouldReturnFalse(MilestoneCode milestoneCode) {
        assertThat(MilestoneCodeUtil.isCodePickupOrDeliveryRelated(milestoneCode)).isFalse();
    }

    @ParameterizedTest
    @MethodSource({"providePickupOrDeliveryOutcomeMilestoneCode", "provideDriverArrivedMilestoneCode"})
    void isCodeUpdatingDriver_otherMilestones_shouldReturnFalse(MilestoneCode milestoneCode) {
        assertThat(MilestoneCodeUtil.isCodeUpdatingDriver(milestoneCode)).isFalse();
    }

    @ParameterizedTest
    @MethodSource({"providePickupOrDeliveryOutcomeMilestoneCode", "provideDriverUpdatesMilestoneCode"})
    void isCodeDriverArrived_otherMilestones_shouldReturnFalse(MilestoneCode milestoneCode) {
        assertThat(MilestoneCodeUtil.isCodeDriverArrived(milestoneCode)).isFalse();
    }

    @ParameterizedTest
    @MethodSource({"providePickupOrDeliveryOutcomeMilestoneCode",
            "provideDriverArrivedMilestoneCode",
            "provideDriverUpdatesMilestoneCode"})
    void isSegmentRelated_segmentRelatedMilestoneCode_shouldReturnTrue(MilestoneCode milestoneCode) {
        assertThat(MilestoneCodeUtil.isSegmentRelated(milestoneCode)).isTrue();
    }

    @ParameterizedTest
    @MethodSource({"provideNonSegmentRelatedMilestoneCode"})
    @NullSource
    void isSegmentRelated_segmentNonRelatedMilestoneCode_shouldReturnFalse(MilestoneCode milestoneCode) {
        assertThat(MilestoneCodeUtil.isSegmentRelated(milestoneCode)).isFalse();
    }
}
