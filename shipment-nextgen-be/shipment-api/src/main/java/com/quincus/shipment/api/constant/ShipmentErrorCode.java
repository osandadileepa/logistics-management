package com.quincus.shipment.api.constant;

public enum ShipmentErrorCode {
    VALIDATION_ERROR,
    INVALID_FORMAT,
    JSON_PARSE_ERROR,
    JSON_MAPPING_ERROR,
    SHIPMENT_NOT_FOUND,
    SHIPMENT_INVALID_STATUS,
    ALERT_NOT_FOUND,
    SEGMENT_ERROR,
    SHIPMENT_JOURNEY_NOT_FOUND,
    SHIPMENT_JOURNEY_MISMATCH,
    SHIPMENT_JOURNEY_ERROR,
    INVALID_MILESTONE,
    MILESTONE_NOT_FOUND,
    PARTNER_NOT_FOUND,
    SEGMENT_NOT_FOUND,
    PACKAGE_DIMENSION_ERROR,
    INVALID_FACILITY,
    FLIGHT_STATS_MESSAGE_ERROR,
    FLIGHT_SUBSCRIPTION_ERROR,
    INVALID_COST_ERROR,
    COST_NOT_FOUND,
    INVALID_PROOF_OF_COST_ERROR,
    QPORTAL_UPSERT_ERROR,
    INVALID_ENUM_ERROR,
    DUPLICATE_LOCATION_HIERARCHY_ERROR,
    SEGMENT_LOCATION_UPSERT_NOT_ALLOWED,
    MILESTONE_INFO_UPDATE_NOT_ALLOWED,
    SHIPMENT_CANCEL_NOT_ALLOWED,
    ALERT_DISMISS_NOT_ALLOWED,
    RESOURCE_ACCESS_FORBIDDEN,
    JOB_NOT_FOUND,
    FAILED_UPDATE_ORDER_ADDITIONAL_CHARGES,
    ACCESS_LOCATION_NOT_ALLOWED,
    INTERNAL_PERSISTENCE_ERROR,
    INVALID_NETWORK_LANE_ERROR,
}
