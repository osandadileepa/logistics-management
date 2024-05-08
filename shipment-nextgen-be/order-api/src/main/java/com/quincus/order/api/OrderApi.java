package com.quincus.order.api;

import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.OrderShipmentResponse;

import java.util.List;

public interface OrderApi {

    List<Shipment> createOrUpdateShipments(Order order, String orderPayload, String uuid);

    OrderShipmentResponse asyncCreateOrUpdateShipments(Order order, String orderPayload, String uuid);

    List<Shipment> createOrUpdateShipmentsLocal(String orderPayload, boolean segmentsUpdated);

    Order createOrderFromPayload(String orderPayload);

    boolean isOrderNotCancelled(Order order);
}
