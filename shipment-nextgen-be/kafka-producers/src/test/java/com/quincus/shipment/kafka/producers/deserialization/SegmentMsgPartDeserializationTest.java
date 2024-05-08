package com.quincus.shipment.kafka.producers.deserialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentMsgPart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class SegmentMsgPartDeserializationTest {
    private static ObjectMapper OBJECTMAPPER;

    @BeforeAll
    static void initialize() {
        final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.modules(new JavaTimeModule());
        final ObjectMapper objectMapper = builder.build();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECTMAPPER = objectMapper;
    }

    @Test
    void deserialize_segmentMsgPart_shouldHaveDateTimeWithCorrectTimeZone() throws JsonProcessingException {
        String segmentMsgPartJsonData = "{ " +
                "\"pick_up_start_time\": \"2023-06-16T19:44:49-01:00\"," +
                " \"pick_up_commit_time\": \"2023-06-17T19:44:49-01:00\", " +
                "\"pickup_time_zone\": \"UTC-01:00\", " +
                "\"drop_off_start_time\": \"2023-06-16T20:56:50+08:00\"," +
                " \"drop_off_commit_time\": \"2023-06-16T20:56:50+08:00\", " +
                "\"drop_off_time_zone\": \"UTC+08:00\"  }";
        SegmentMsgPart segmentsDispatchMessage = OBJECTMAPPER.readValue(segmentMsgPartJsonData, SegmentMsgPart.class);
        assertThat(segmentsDispatchMessage.getPickUpStartTime().getZone().getId()).isEqualTo("-01:00");
        assertThat(segmentsDispatchMessage.getPickUpCommitTime().getZone().getId()).isEqualTo("-01:00");
        assertThat(segmentsDispatchMessage.getDropOffStartTime().getZone().getId()).isEqualTo("+08:00");
        assertThat(segmentsDispatchMessage.getDropOffCommitTime().getZone().getId()).isEqualTo("+08:00");
    }
}
