package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.LocationApi;
import com.quincus.shipment.impl.service.LocationHierarchyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LocationApiImpl implements LocationApi {

    private final LocationHierarchyService locationHierarchyService;

    @Override
    public void receiveLocationMessage(String payload, String transactionId) {
        locationHierarchyService.receiveLocationMessage(payload, transactionId);
    }
}
