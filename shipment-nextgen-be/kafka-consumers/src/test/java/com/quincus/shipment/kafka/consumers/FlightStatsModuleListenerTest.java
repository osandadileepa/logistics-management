package com.quincus.shipment.kafka.consumers;

import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.shipment.api.FlightStatsApi;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import test.util.TestUtil;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

@ExtendWith(MockitoExtension.class)
class FlightStatsModuleListenerTest {
    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    FlightStatsModuleListener flightStatsModuleListener;
    @Mock
    FlightStatsApi flightStatsApi;

    @Test
    void listen_withValidParameters_shouldHaveNoErrors() {
        String topic = "local-v2-gateway-flightstats-inbound";
        JsonNode value = testUtil.getDataFromFile("samplepayload/flight-subscribe-rs.json");

        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 0, 0, "key", value.asText());

        assertThatNoException().isThrownBy(() -> flightStatsModuleListener.listen(consumerRecord));
    }
}
