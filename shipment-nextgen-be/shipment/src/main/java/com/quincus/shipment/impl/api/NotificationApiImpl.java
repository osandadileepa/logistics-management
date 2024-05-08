package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.NotificationApi;
import com.quincus.shipment.api.dto.NotificationRequest;
import com.quincus.shipment.impl.service.QPortalNotificationService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NotificationApiImpl implements NotificationApi {

    private final QPortalNotificationService qPortalNotificationService;

    @Override
    @Async("threadPoolTaskExecutor")
    public void sendNotification(NotificationRequest notificationRequest) {
        qPortalNotificationService.sendNotification(notificationRequest);
    }
}
