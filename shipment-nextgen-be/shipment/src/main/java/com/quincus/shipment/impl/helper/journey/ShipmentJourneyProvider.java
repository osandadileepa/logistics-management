package com.quincus.shipment.impl.helper.journey;

import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.helper.journey.generator.ShipmentJourneyGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class ShipmentJourneyProvider {

    private final List<ShipmentJourneyGenerator> shipmentJourneyGeneratorList;

    public ShipmentJourney generateShipmentJourney(Root omMessage) {
        for (ShipmentJourneyGenerator generator : shipmentJourneyGeneratorList) {
            ShipmentJourney generatedJourney = generator.generateShipmentJourney(omMessage);
            if (generatedJourney != null) {
                log.debug("Generated Journey from: {} for OM id: {}", generator.getClass().getName(), omMessage.getId());
                return generatedJourney;
            }
        }
        return null;
    }
}
