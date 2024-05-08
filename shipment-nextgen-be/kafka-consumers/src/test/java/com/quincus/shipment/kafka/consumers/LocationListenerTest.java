package com.quincus.shipment.kafka.consumers;

import com.quincus.shipment.api.LocationApi;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

@ExtendWith(MockitoExtension.class)
class LocationListenerTest {
    @InjectMocks
    LocationListener locationListener;
    @Mock
    LocationApi locationApi;

    @Test
    void listen_withValidParameters_shouldHaveNoErrors() {
        String topic = "local-v2-gateway-location-inbound";
        String value = "";
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 0, 0, "key", value);

        assertThatNoException().isThrownBy(() -> locationListener.listen(consumerRecord));
    }
}
