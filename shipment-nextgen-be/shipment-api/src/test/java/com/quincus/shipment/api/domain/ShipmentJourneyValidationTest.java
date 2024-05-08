package com.quincus.shipment.api.domain;

import com.quincus.shipment.api.constant.JourneyStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentJourneyValidationTest extends ValidationTest {

    @Test
    void shipmentJourney_WithMandatoryFields_ShouldHaveNoViolations() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setJourneyId(UUID.randomUUID().toString());
        shipmentJourney.setOrderId(UUID.randomUUID().toString());
        shipmentJourney.setShipmentId(UUID.randomUUID().toString());
        shipmentJourney.setStatus(JourneyStatus.PLANNED);
        assertThat(validateModel(shipmentJourney)).isEmpty();
    }
}
