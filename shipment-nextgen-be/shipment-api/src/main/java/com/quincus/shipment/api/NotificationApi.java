package com.quincus.shipment.api;

import com.quincus.shipment.api.dto.NotificationRequest;

public interface NotificationApi {
    void sendNotification(NotificationRequest notificationRequest);
}
