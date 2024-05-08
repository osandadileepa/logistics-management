package com.quincus.shipment.impl.web;

import com.quincus.shipment.AlertController;
import com.quincus.shipment.api.AlertApi;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class AlertControllerImpl implements AlertController {

    private AlertApi alertApi;

    @Override
    @PreAuthorize("hasAuthority('SHIPMENTS_EDIT')")
    @LogExecutionTime
    public void dismissAlert(final String alertId, boolean dismissed) {
        alertApi.dismiss(alertId, dismissed);
    }
}
