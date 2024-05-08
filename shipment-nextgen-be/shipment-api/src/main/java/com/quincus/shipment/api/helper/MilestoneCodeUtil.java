package com.quincus.shipment.api.helper;

import com.quincus.shipment.api.constant.MilestoneCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.quincus.shipment.api.constant.MilestoneCode.DSP_ASSIGNMENT_CANCELED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_ASSIGNMENT_UPDATED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_FAILED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DISPATCH_SCHEDULED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DRIVER_ARRIVED_FOR_DELIVERY;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DRIVER_ARRIVED_FOR_PICKUP;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_FAILED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_SUCCESSFUL;

@NoArgsConstructor(access = AccessLevel.NONE)
public class MilestoneCodeUtil {

    public static final List<MilestoneCode> MILESTONE_STATUS_CODE_FOR_UPDATE_DRIVER_VEHICLE_INFO = List.of(
            DSP_DISPATCH_SCHEDULED, DSP_ASSIGNMENT_UPDATED, DSP_ASSIGNMENT_CANCELED);
    private static final List<MilestoneCode> DSP_PICK_UP_AND_DROP_OFF_MILESTONE_CODE
            = List.of(DSP_PICKUP_SUCCESSFUL, DSP_PICKUP_FAILED
            , DSP_DELIVERY_SUCCESSFUL, DSP_DELIVERY_FAILED);

    public static boolean isDispatchSuccessful(MilestoneCode code) {
        return DSP_PICKUP_SUCCESSFUL == code || DSP_DELIVERY_SUCCESSFUL == code;
    }

    public static boolean isCodePickupOrDeliveryRelated(MilestoneCode code) {
        return DSP_PICK_UP_AND_DROP_OFF_MILESTONE_CODE.contains(code);
    }

    public static boolean isCodeUpdatingDriver(MilestoneCode code) {
        return MILESTONE_STATUS_CODE_FOR_UPDATE_DRIVER_VEHICLE_INFO.contains(code);
    }

    public static boolean isCodeDriverArrived(MilestoneCode code) {
        return DSP_DRIVER_ARRIVED_FOR_PICKUP == code || DSP_DRIVER_ARRIVED_FOR_DELIVERY == code;
    }

    public static boolean isSegmentRelated(MilestoneCode code) {
        return (code != null) && MilestoneCode.OM_BOOKED != code;
    }
}
