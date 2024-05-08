package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.AlertApi;
import com.quincus.shipment.impl.service.AlertService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AlertApiImpl implements AlertApi {

    private final AlertService alertService;

    @Override
    public void dismiss(String alertId, boolean dismissed) {
        alertService.dismiss(alertId, dismissed);
    }

    @Override
    public void createPickupDeliveryFailedAlert(String shipmentId) {
        alertService.createPickupDeliveryFailedAlert(shipmentId);
    }
}
