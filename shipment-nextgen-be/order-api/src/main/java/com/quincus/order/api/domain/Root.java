package com.quincus.order.api.domain;

import com.quincus.shipment.api.domain.OrderReference;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class Root {
    public static final String STATUS_CREATED = "created";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String DISTANCE_UOM_IMPERIAL = "imperial";
    public static final String DISTANCE_UOM_METRIC = "metric";

    private String transactionId;
    @NotNull
    private String userId;
    private String id;
    private String status;
    private String bookingMode;
    private String orderIdLabel;
    private String partnerId;
    private String createdAt;
    private String updatedAt;
    private boolean selfPickup;
    private String referenceId;
    private List<Instruction> instructions;
    private String handleFacilityId;
    private String opsType;
    private String deletedAt;
    private String latestEditorId;
    private String isSegment;
    private List<SegmentsPayload> segmentsPayloads;
    private String note;
    private String cancelReason;
    private String returnReason;
    private String distanceUom;
    private String shipmentCode;
    @NotNull
    private String organisationId;
    private String createdBy;
    private String pickupStartTime;
    private String pickupCommitTime;
    private String pickupTimezone;
    private String deliveryStartTime;
    private String deliveryCommitTime;
    private String deliveryTimezone;
    @NotNull
    private Shipper shipper;
    @Valid
    @NotNull
    private Origin origin;
    @NotNull
    private Consignee consignee;
    @Valid
    @NotNull
    private Destination destination;
    private List<Package> shipments;
    @NotNull
    private String serviceType;
    private PricingInfo pricingInfo;
    private List<String> orderLocationIds;
    private List<String> tagList;
    private String measurementUnits;
    private String orderType;
    private List<CustomerReference> customerReferences;
    private String trackingUrl;
    @NotNull
    private String serviceTypeId;
    private List<Attachment> attachments;
    private BulkOrder bulkOrder;
    private boolean isException;
    private String type;
    private boolean returnOrderCreatable;
    private String internalOrderId;
    private String externalOrderId;
    private String customerOrderId;
    private List<OrderReference> orderReferences;
    private String orderSetRunInfo;
    private boolean usedOpenApi;
    private String statusUpdatedAt;
    private boolean segmentsUpdated;
}
