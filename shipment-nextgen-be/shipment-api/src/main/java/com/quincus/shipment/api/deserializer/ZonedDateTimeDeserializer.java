package com.quincus.shipment.api.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.quincus.ext.DateTimeUtil;
import com.quincus.web.common.exception.model.QuincusException;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Slf4j
public class ZonedDateTimeDeserializer extends StdScalarDeserializer<ZonedDateTime> {

    private static final String ERROR_DESERIALIZING = "Error Deserializing Date Time";

    public ZonedDateTimeDeserializer() {
        super(ZonedDateTime.class);
    }

    @Override
    public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        try {
            return DateTimeUtil.parseZonedDateTime(jsonParser.getText());
        } catch (Exception ex) {
            throw new QuincusException(ERROR_DESERIALIZING);
        }
    }
}
