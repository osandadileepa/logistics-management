package com.quincus.shipment.kafka.producers.message.dispatch;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quincus.shipment.api.serializer.BigDecimalAsFloatSerializer;
import com.quincus.shipment.api.serializer.InstantSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CurrencyMsgPart {
    private String id;
    private String code;
    private String name;
    private Boolean deleted;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant updatedAt;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal exchangeRate;
    private String organisationId;
    private Boolean isDefaultCurrency;
}
