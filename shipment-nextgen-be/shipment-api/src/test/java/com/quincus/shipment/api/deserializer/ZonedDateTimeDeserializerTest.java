package com.quincus.shipment.api.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.quincus.web.common.exception.model.QuincusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZonedDateTimeDeserializerTest {

    @InjectMocks
    private ZonedDateTimeDeserializer zonedDateTimeDeserializer;

    @Test
    void validZonedDateTime_deserialize_successDeserializeValue() throws IOException {
        JsonParser jsonParser = mock(JsonParser.class);
        when(jsonParser.getText()).thenReturn("2023-06-16T19:44:49-01:00");
        DeserializationContext deserializationContext = mock(DeserializationContext.class);
        ZonedDateTime parsedZoneTime = zonedDateTimeDeserializer.deserialize(jsonParser,deserializationContext);
        assertThat(parsedZoneTime.getMonthValue()).isEqualTo(6);
        assertThat(parsedZoneTime.getYear()).isEqualTo(2023);
        assertThat(parsedZoneTime.getDayOfMonth()).isEqualTo(16);
        assertThat(parsedZoneTime.getZone().getId()).isEqualTo("-01:00");
    }

    @Test
    void notValidZonedDateTime_deserialize_ThrowsQuincusException() throws IOException {
        JsonParser jsonParser = mock(JsonParser.class);
        when(jsonParser.getText()).thenReturn("2023-06-asd:44:49-01:00");
        DeserializationContext deserializationContext = mock(DeserializationContext.class);
        try (jsonParser) {
            assertThatThrownBy(() -> zonedDateTimeDeserializer.deserialize(jsonParser, deserializationContext))
                    .isInstanceOf(QuincusException.class);
        }
    }

    @Test
    void IoExceptionFromJson_deserialize_ThrowsQuincusException() throws IOException {
        JsonParser jsonParser = mock(JsonParser.class);
        when(jsonParser.getText()).thenThrow(new IOException("test error"));
        DeserializationContext deserializationContext = mock(DeserializationContext.class);
        assertThatThrownBy(() -> zonedDateTimeDeserializer.deserialize(jsonParser,deserializationContext))
                .isInstanceOf(QuincusException.class);
    }
}
