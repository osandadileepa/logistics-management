package com.quincus.qlogger.api;

public enum QLoggerCategory {
    /**
     * see https://quincus.atlassian.net/wiki/spaces/AD/pages/1123418162/qLogger+categories+definitions+Shipment
     * for qlogger categories
     */
    SHIPMENT_CREATED,
    SHIPMENT_EXPORTED,
    SHIPMENT_CANCELLED,
    SHIPMENT_UPDATED,
    SEGMENT_JOURNEY_CREATED,
    SEGMENT_JOURNEY_UPDATED,
    PACKAGE_DIMENSION_UPDATED,
    PACKAGE_DIMENSION_BULKUPDATED,
    COST_CREATED,
    COST_UPDATED,
    VENDOR_BOOKING_UPDATED;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

}
