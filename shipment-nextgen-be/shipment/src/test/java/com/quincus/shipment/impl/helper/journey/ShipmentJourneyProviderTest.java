package com.quincus.shipment.impl.helper.journey;

import com.quincus.order.api.domain.Root;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.helper.journey.generator.ShipmentJourneyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentJourneyProviderTest {
    private ShipmentJourneyProvider shipmentJourneyProvider;
    @Mock
    private ShipmentJourneyGenerator generator1;
    @Mock
    private ShipmentJourneyGenerator generator2;
    @Mock
    private ShipmentJourneyGenerator generator3;
    @Mock
    private Root root;

    @BeforeEach
    void setUp() {
        when(root.getId()).thenReturn("123123");
        shipmentJourneyProvider = new ShipmentJourneyProvider(List.of(generator1, generator2, generator3));
    }

    @Test
    void givenGenerator1HasResponse_whenGenerateJourney_thenNoInteractionWithTheRemainingGenerator() {
        when(generator1.generateShipmentJourney(root)).thenReturn(mock(ShipmentJourney.class));

        ShipmentJourney journey = shipmentJourneyProvider.generateShipmentJourney(root);

        assertThat(journey).isNotNull();
        verify(generator1, times(1)).generateShipmentJourney(root);
        verifyNoInteractions(generator2);
        verifyNoInteractions(generator3);
    }

    @Test
    void givenGenerator2HasResponse_whenGenerateJourney_thenNoInteractionWithTheGenerator3AndJournyIsFromGenertor2() {
        when(generator2.generateShipmentJourney(root)).thenReturn(mock(ShipmentJourney.class));

        ShipmentJourney journey = shipmentJourneyProvider.generateShipmentJourney(root);

        assertThat(journey).isNotNull();
        verify(generator1, times(1)).generateShipmentJourney(root);
        verifyNoInteractions(generator3);
    }

    @Test
    void givenGenerator3HasResponse_whenGenerateJourney_thenVerifyInteractionWithAllGeneratorButResultIsFromLastGenerator() {
        ShipmentJourney mockedJourney = mock(ShipmentJourney.class);
        when(generator3.generateShipmentJourney(root)).thenReturn(mockedJourney);

        ShipmentJourney journey = shipmentJourneyProvider.generateShipmentJourney(root);

        assertThat(journey).isNotNull().isEqualTo(mockedJourney);
        verify(generator1, times(1)).generateShipmentJourney(root);
        verify(generator2, times(1)).generateShipmentJourney(root);
        verify(generator3, times(1)).generateShipmentJourney(root);


    }
}
