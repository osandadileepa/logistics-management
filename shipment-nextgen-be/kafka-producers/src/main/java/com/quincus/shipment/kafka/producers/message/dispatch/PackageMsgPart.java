package com.quincus.shipment.kafka.producers.message.dispatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.serializer.BigDecimalAsFloatSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PackageMsgPart {
    private String id;
    private String code;
    private String refId;
    private String typeRefId;
    private String type;
    private String note;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal valueOfGoods;
    private Integer itemCount;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal height;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal width;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal length;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal grossWeight;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal volumeWeight;
    @JsonSerialize(using = BigDecimalAsFloatSerializer.class)
    private BigDecimal chargeableWeight;
    private MeasurementUnit measurement;
    private String additionalData1;
    private List<CommodityMsgPart> commodities;
    @JsonProperty("is_custom")
    private Boolean custom;
}
