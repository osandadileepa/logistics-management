package com.quincus.shipment.kafka.producers.message.dispatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.quincus.shipment.api.domain.OrderReference;
import com.quincus.shipment.api.serializer.OffsetDateTimeSerializer;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class OrderMsgPart {
    private String id;
    private String note;
    private List<String> customerReferences;
    private List<JsonNode> attachments;
    private List<String> tagsList;
    private ShipperMsgPart shipper;
    private AddressDetailsMsgPart origin;
    private ConsigneeMsgPart consignee;
    private AddressDetailsMsgPart destination;
    private PricingInfoMsgPart pricingInfo;
    private String opsType;
    private String serviceTypeId;
    private String serviceTypeName;
    private int numberOfShipments;
    private String code;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime pickupStartTime;
    private String pickupStartTimezone;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime pickupCommitTime;
    private String pickupCommitTimezone;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime deliveryStartTime;
    private String deliveryStartTimezone;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime deliveryCommitTime;
    private String deliveryCommitTimezone;
    private List<OrderReference> orderReferences;
}
