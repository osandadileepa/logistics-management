package com.quincus.shipment.kafka.producers.message.dispatch;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quincus.shipment.api.serializer.BigDecimalAsFloatSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PricingInfoMsgPart {
    private String id;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal cod;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal tax;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal total;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal discount;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal surcharge;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal baseTariff;
    private String currencyCode;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal insuranceCharge;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal serviceTypeCharge;
    private CurrencyMsgPart currency;
}
