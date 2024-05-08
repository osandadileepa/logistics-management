package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class CostShipment {
    @UUID
    private String id;
    @Size(max = 256)
    private String shipmentTrackingId;
    @Size(max = 64)
    private String origin;
    @Size(max = 64)
    private String destination;
    @UUID(required = false)
    private String orderId;
    @Size(max = 64)
    private String orderIdLabel;
    @UUID(required = false)
    private String partnerId;
    @Size(max = 64)
    private String externalOrderId;
    @Size(min = 1)
    private List<@Valid CostSegment> segments;
    @Max(value = 100000)
    private int totalSegments;
    @Size(max = 64)
    private String orderStatus;

    public List<CostSegment> getSegments() {
        if (CollectionUtils.isEmpty(segments)) {
            return Collections.emptyList();
        }
        return segments;
    }
}
