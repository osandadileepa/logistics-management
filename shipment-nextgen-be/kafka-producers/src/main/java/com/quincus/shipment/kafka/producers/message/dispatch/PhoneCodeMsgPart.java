package com.quincus.shipment.kafka.producers.message.dispatch;

import lombok.Data;

@Data
public class PhoneCodeMsgPart {
    private String id;
    private String code;
    private String name;
}
