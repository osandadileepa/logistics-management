package com.quincus.shipment.impl.helper.journey.generator;

import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.domain.ShipmentJourney;

public interface ShipmentJourneyGenerator {

    ShipmentJourney generateShipmentJourney(Root omMessage);
}
