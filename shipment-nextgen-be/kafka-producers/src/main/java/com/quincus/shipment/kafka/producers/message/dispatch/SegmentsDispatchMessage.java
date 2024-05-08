package com.quincus.shipment.kafka.producers.message.dispatch;

import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import lombok.Data;

import java.util.List;

@Data
public class SegmentsDispatchMessage {

    public static final String MSG_SHP_VERSION = "2";
    public static final String ZONED_DATE_TIME_FMT = "yyyy-MM-dd'T'HH:mm:ssxxx";
    private String id;
    private String shpVersion;
    private String organisationId;
    private String userId;
    private SegmentDispatchType type;
    private OrderMsgPart order;
    private List<ShipmentMsgPart> shipments;
    private String journeyId;
    private List<SegmentMsgPart> segments;
    private String internalOrderId;
    private String externalOrderId;
    private String customerOrderId;
    private DspSegmentMsgUpdateSource updateSource;
}
