package com.quincus.shipment.api;

public interface AlertApi {
    void dismiss(String alertId, boolean dismissed);
    
    void createPickupDeliveryFailedAlert(String shipmentId);
}
