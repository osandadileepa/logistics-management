package com.quincus.shipment.api;

import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.dto.MilestoneUpdateTimeRequest;

public interface MilestoneApi {

    boolean isFailedStatusCode(MilestoneCode code);

    MilestoneUpdateResponse add(MilestoneUpdateRequest milestoneUpdateRequest);

    Milestone partialUpdate(Milestone milestone);

    Milestone updateMilestoneTime(MilestoneUpdateTimeRequest milestoneUpdateTimeRequest);
}
