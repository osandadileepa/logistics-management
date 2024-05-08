package com.quincus.shipment.api;

import com.quincus.shipment.api.dto.FailedMessage;

public interface FailedMessageApi {
    void sendToDlq(FailedMessage failedMessage);
}
