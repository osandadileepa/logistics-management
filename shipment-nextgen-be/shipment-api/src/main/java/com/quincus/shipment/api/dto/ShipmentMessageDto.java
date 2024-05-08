package com.quincus.shipment.api.dto;

import com.quincus.shipment.api.domain.Consignee;
import com.quincus.shipment.api.domain.OrderReference;
import com.quincus.shipment.api.domain.Sender;
import lombok.Data;

import java.util.List;

@Data
public class ShipmentMessageDto {
    private String id;
    private String orderId;
    private String organizationId;
    private List<String> shipmentReferenceId;
    private String externalOrderId;
    private String internalOrderId;
    private String customerOrderId;
    private List<OrderReference> orderReferences;
    private String packageId;
    private String packageRefId;
    private String userId;
    private String partnerId;
    private Sender sender;
    private Consignee consignee;
    private String orderIdLabel;
    private String orderTrackingUrl;
}
