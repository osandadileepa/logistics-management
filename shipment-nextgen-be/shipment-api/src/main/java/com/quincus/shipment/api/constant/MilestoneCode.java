package com.quincus.shipment.api.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.quincus.shipment.api.exception.InvalidEnumValueException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.quincus.shipment.api.constant.MilestoneCategory.ACTIVE;
import static com.quincus.shipment.api.constant.MilestoneCategory.COMPLETED;

/**
 * Milestone Codes mapping
 * see <a href="https://quincus.atlassian.net/wiki/spaces/PROD/pages/1040711702/Milestones+-+Modules+Relation+Trigger">...</a>
 */
public enum MilestoneCode {
    OM_DRAFT("1001", ACTIVE),
    OM_UNVERIFIED("1002", ACTIVE),
    OM_REJECTED("1003", ACTIVE),
    OM_PENDING_RETURN("1004", ACTIVE),
    OM_RETURNED("1005", COMPLETED),
    OM_PENDING_PICKUP("1006", ACTIVE),
    OM_ARRIVED_AT_AGENT_OFFICE("1007", ACTIVE),
    OM_PENDING_DELIVERY("1008", ACTIVE),
    OM_BOOKED("1100", ACTIVE),
    OM_EDITED("1101", ACTIVE),
    SHP_PICKUP_IN_PROGRESS("1102", ACTIVE),
    SHP_ARRIVED_AT_HUB("1103", ACTIVE),
    SHP_SORTED_IN_HUB("1104", ACTIVE),
    SHP_CONSOLIDATED("1105", ACTIVE),
    SHP_DECONSOLIDATED("1106", ACTIVE),
    SHP_ARRIVED_IN_PORT("1112", ACTIVE),
    SHP_EXPORT_CUSTOMS_CLEARANCE("1113", ACTIVE),
    DSP_DEPARTED_FROM_HUB("1114", ACTIVE),
    SHP_LINEHAUL_COMPLETED("1115", ACTIVE),
    SHP_IMPORT_CUSTOMS_CLEARANCE("1116", ACTIVE),
    DSP_DELIVERY_SUCCESSFUL("1117", COMPLETED),
    SHP_RETURNED("1119", COMPLETED),
    SHP_PENDING_DELIVERY("1122", ACTIVE),
    DSP_PICKUP_SCHEDULED("1400", ACTIVE),
    DSP_ON_ROUTE_TO_PICKUP("1401", ACTIVE),
    DSP_PICKUP_COMPLETED("1402", ACTIVE),
    DSP_DELIVERY_ON_ROUTE("1403", ACTIVE),
    DSP_ARRIVED_AT_PICKUP_LOCATION("1404", ACTIVE),
    DSP_PICKUP_SUCCESSFUL("1405", ACTIVE),
    DSP_ARRIVED_AT_DELIVERY_LOCATION("1406", ACTIVE),
    DSP_PENDING_RESCHEDULE("1407", ACTIVE),
    DSP_VENDOR_ASSIGNED_TO_PICKUP("1408", ACTIVE),
    DSP_PICKED_UP_FROM_SHIPPER("1412", ACTIVE),
    OM_ORDER_CANCELED("1500", COMPLETED),
    OM_PICKUP_CANCELED("1501", ACTIVE),
    DSP_PICKUP_FAILED("1502", ACTIVE),
    SHP_LOADING_REJECTED("1503", ACTIVE),
    SHP_ON_HOLD("1504", ACTIVE),
    SHP_MISROUTED("1505", ACTIVE),
    SHP_LOST("1506", COMPLETED),
    SHP_DAMAGED("1507", ACTIVE),
    SHP_PENDING_RETURN("1508", ACTIVE),
    OM_DELIVERY_CANCELED("1509", ACTIVE),
    DSP_DELIVERY_FAILED("1510", ACTIVE),
    OM_SCRAPPED("1515", COMPLETED),
    SHP_DIMS_WEIGHT_UPDATED("1520", ACTIVE),
    DSP_DISPATCH_SCHEDULED("1601", ACTIVE),
    DSP_ASSIGNMENT_UPDATED("1602", ACTIVE),
    DSP_ASSIGNMENT_CANCELED("1603", ACTIVE),
    DSP_DRIVER_ARRIVED("1606", ACTIVE),
    DSP_DRIVER_ARRIVED_FOR_PICKUP("1607", ACTIVE),
    DSP_DRIVER_ARRIVED_FOR_DELIVERY("1608", ACTIVE),
    SHP_FLIGHT_DEPARTED("2114", ACTIVE),
    SHP_FLIGHT_ARRIVED("2115", ACTIVE),
    SHP_AIR_DELIVERED("2117", COMPLETED),
    DSP_AIR_ARRIVED_AT_PICKUP_LOCATION("2404", ACTIVE),
    DSP_AIR_ARRIVED_AT_DELIVERY_LOCATION("2406", ACTIVE),
    DSP_AIR_VENDOR_ASSIGNED_TO_PICKUP("2408", ACTIVE),
    DSP_ARRIVED_AT_ORIGIN_AIRPORT("2409", ACTIVE),
    DSP_TENDERED_SHIPMENT_TO_AIRLINE("2410", ACTIVE),
    DSP_ARRIVED_AT_DEST_AIRPORT("2411", ACTIVE),
    DSP_AIR_PICKED_UP_FROM_SHIPPER("2412", ACTIVE),
    DSP_PICKED_UP_FROM_DEST_AIRPORT("2413", ACTIVE),
    SHP_ARRIVED_IN_SCAN_IN_HUB("0140", ACTIVE),
    SHP_SEGMENT_COMPLETED_ACTIVE_AND_DELIVERED("11220222", ACTIVE),
    SHP_SEGMENT_CANCELLED("013023", ACTIVE),
    SHP_SCANNED_OUT_OF_HUB("1118", ACTIVE),
    SHP_LINE_HAUL_SCHEDULED("1106", ACTIVE),
    DSP_LINE_HAUL_CANCELLED("2413", ACTIVE),
    SHP_ASSIGNMENT_SCHEDULED("1111", ACTIVE),
    SHP_ASSIGNMENT_CANCELLED("1110", ACTIVE);

    private static final List<String> LINE_HAUL_SCHEDULED_MILESTONE_CODES = List.of("1109", "1110", "1111", "1107"); //1106
    private static final List<String> LINE_HAUL_CANCELLED_MILESTONE_CODES = List.of("1511", "1512", "1513", "1514", "1516"); //2413
    //These milestone codes should be excluded in the main milestone code map since they are sharing same milestone code with 1106 and 2413
    private static final List<MilestoneCode> EXCLUDED_MILESTONE_CODES = List.of(SHP_LINE_HAUL_SCHEDULED, DSP_LINE_HAUL_CANCELLED);
    private static final Map<String, MilestoneCode> MILESTONE_CODE_MAP = new HashMap<>();
    public static final List<MilestoneCode> OM_TRIGGERED_MILESTONES_CODES = List.of(OM_BOOKED, OM_ORDER_CANCELED);

    static {
        for (MilestoneCode code : MilestoneCode.values()) {
            if (!EXCLUDED_MILESTONE_CODES.contains(code)) {
                MILESTONE_CODE_MAP.put(code.toString(), code);
            }
        }
    }

    private final String code;
    @Getter
    private final MilestoneCategory category;

    MilestoneCode(String code, MilestoneCategory category) {
        this.code = code;
        this.category = category;
    }

    @JsonCreator
    public static MilestoneCode fromValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        MilestoneCode milestoneCode = MILESTONE_CODE_MAP.get(value);
        if (milestoneCode != null) {
            return milestoneCode;
        }
        if (LINE_HAUL_SCHEDULED_MILESTONE_CODES.contains(value)) {
            return MilestoneCode.SHP_LINE_HAUL_SCHEDULED;
        }
        if (LINE_HAUL_CANCELLED_MILESTONE_CODES.contains(value)) {
            return MilestoneCode.DSP_LINE_HAUL_CANCELLED;
        }
        throw new InvalidEnumValueException(value, MilestoneCode.class);
    }

    @Override
    public String toString() {
        return code;
    }
}
