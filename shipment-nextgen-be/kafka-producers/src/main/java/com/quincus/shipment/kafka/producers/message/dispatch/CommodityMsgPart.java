package com.quincus.shipment.kafka.producers.message.dispatch;

import lombok.Data;

@Data
public class CommodityMsgPart {
    private String id;
    private String name;
    private String description;
    private String code;
    private String hsCode;
    private String note;
    private String packagingType;
}
