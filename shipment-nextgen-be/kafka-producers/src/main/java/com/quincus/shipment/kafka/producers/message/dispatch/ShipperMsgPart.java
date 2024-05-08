package com.quincus.shipment.kafka.producers.message.dispatch;

import lombok.Data;

@Data
public class ShipperMsgPart {
    private String name;
    private String email;
    private String phone;
    private PhoneCodeMsgPart phoneCode;
}
