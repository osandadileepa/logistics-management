package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import com.quincus.shipment.api.constant.JobType;
import com.quincus.shipment.api.constant.TransportType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
public class CostSegment {
    @UUID
    private String segmentId;
    @Size(max = 45)
    private String sequenceNo;
    @Size(max = 50)
    private String refId;
    private TransportType transportType;
    private CostFacility startFacility;
    private CostFacility endFacility;
    private boolean firstSegment;
    private boolean lastSegment;
    private List<JobType> jobTypes;
}
