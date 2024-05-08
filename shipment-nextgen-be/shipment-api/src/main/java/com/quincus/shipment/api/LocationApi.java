package com.quincus.shipment.api;

public interface LocationApi {
    void receiveLocationMessage(String payload, String transactionId);
}
