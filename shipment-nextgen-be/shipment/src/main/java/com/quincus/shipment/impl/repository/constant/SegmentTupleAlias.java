package com.quincus.shipment.impl.repository.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class SegmentTupleAlias {
    public static final String SEGMENT_ID = "segment_id";
    public static final String JOURNEY_ID = "journey_id";
    public static final String REF_ID = "ref_id";
    public static final String SEQUENCE = "sequence";
    public static final String STATUS = "status";
    public static final String TRANSPORT_TYPE = "transport_type";
    public static final String LOCKOUT_TIME = "lock_out_time";
    public static final String LOCKOUT_TIMEZONE = "lock_out_timezone";
}
