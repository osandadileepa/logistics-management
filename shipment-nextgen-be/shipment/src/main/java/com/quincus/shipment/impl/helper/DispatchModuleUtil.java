package com.quincus.shipment.impl.helper;

import com.quincus.shipment.api.constant.MilestoneCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_FAILED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_ON_ROUTE;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_FAILED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.OM_DELIVERY_CANCELED;
import static com.quincus.shipment.api.constant.MilestoneCode.OM_ORDER_CANCELED;
import static com.quincus.shipment.api.constant.MilestoneCode.OM_PICKUP_CANCELED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_FLIGHT_ARRIVED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_FLIGHT_DEPARTED;


@NoArgsConstructor(access = AccessLevel.NONE)
public final class DispatchModuleUtil {

    public static final int RECENT_MILESTONE_EVENTS_COUNT = 3;
    public static final List<MilestoneCode> FAILED_STATUS_CODES_FROM_DISPATCH = List.of(
            DSP_PICKUP_FAILED,
            DSP_DELIVERY_FAILED
    );

    public static final List<MilestoneCode> IN_PROGRESS_STATUS_CODES_FROM_DISPATCH = List.of(
            DSP_DELIVERY_ON_ROUTE,
            SHP_FLIGHT_DEPARTED,
            DSP_PICKUP_SUCCESSFUL
    );

    public static final List<MilestoneCode> COMPLETED_STATUS_CODES_FROM_DISPATCH = List.of(
            DSP_DELIVERY_SUCCESSFUL,
            SHP_FLIGHT_ARRIVED
    );

    public static final List<MilestoneCode> CANCELLED_STATUS_CODES_FROM_DISPATCH = List.of(
            OM_PICKUP_CANCELED,
            OM_DELIVERY_CANCELED,
            OM_ORDER_CANCELED
    );
}
