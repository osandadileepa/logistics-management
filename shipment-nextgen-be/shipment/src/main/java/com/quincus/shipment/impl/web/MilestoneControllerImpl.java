package com.quincus.shipment.impl.web;

import com.quincus.shipment.MilestoneController;
import com.quincus.shipment.api.MilestoneApi;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.dto.MilestoneUpdateTimeRequest;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class MilestoneControllerImpl implements MilestoneController {

    private final MilestoneApi milestoneApi;

    @Override
    @LogExecutionTime
    public Response<MilestoneUpdateResponse> add(Request<MilestoneUpdateRequest> request) {
        final MilestoneUpdateRequest milestoneUpdateRequest = request.getData();
        return new Response<>(milestoneApi.add(milestoneUpdateRequest));
    }

    @Override
    @PreAuthorize("hasAuthority('SHIPMENTS_EDIT')")
    @LogExecutionTime
    public Response<Milestone> partialUpdate(Request<Milestone> request) {
        final Milestone milestone = request.getData();
        return new Response<>(milestoneApi.partialUpdate(milestone));
    }

    @Override
    @LogExecutionTime
    public Response<Milestone> updateMilestoneTime(String id, Request<MilestoneUpdateTimeRequest> request) {
        MilestoneUpdateTimeRequest milestoneUpdateTimeRequest = request.getData();
        milestoneUpdateTimeRequest.setId(id);
        return new Response<>(milestoneApi.updateMilestoneTime(milestoneUpdateTimeRequest));
    }
}
