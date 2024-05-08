package com.quincus.shipment.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quincus.shipment.api.domain.ShipmentJourney;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true, chain = true)
@Getter(onMethod = @__(@JsonProperty))
public class ShipmentJourneyUpdateResponse {
    private ShipmentJourney shipmentJourney;
    private List<String> updatedShipmentTrackingIds;
    private int totalShipmentsUpdated;
}