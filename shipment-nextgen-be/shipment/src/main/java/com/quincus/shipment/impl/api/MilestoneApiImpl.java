package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.MilestoneApi;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.dto.MilestoneUpdateTimeRequest;
import com.quincus.shipment.impl.service.MilestoneService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MilestoneApiImpl implements MilestoneApi {

    private MilestoneService milestoneService;

    @Override
    public boolean isFailedStatusCode(MilestoneCode code) {
        return milestoneService.isFailedStatusCode(code);
    }

    @Override
    public MilestoneUpdateResponse add(MilestoneUpdateRequest milestoneUpdateRequest) {
        return milestoneService.saveMilestoneFromAPIG(milestoneUpdateRequest);
    }

    @Override
    public Milestone partialUpdate(Milestone milestone) {
        return milestoneService.partialUpdate(milestone);
    }

    @Override
    public Milestone updateMilestoneTime(MilestoneUpdateTimeRequest milestoneUpdateTimeRequest) {
        return milestoneService.updateMilestoneTime(milestoneUpdateTimeRequest);
    }
}
