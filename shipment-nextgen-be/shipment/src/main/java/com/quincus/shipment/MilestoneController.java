package com.quincus.shipment;

import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.dto.MilestoneUpdateTimeRequest;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/milestones")
@Tag(name = "milestones", description = "This endpoint allows to manage milestones related transactions.")
public interface MilestoneController {

    @PostMapping
    @Operation(summary = "Create Milestone from Client Milestone Update API",
            description = "Create a new Milestone based on milestone update information from client.", tags = "milestones")
    Response<MilestoneUpdateResponse> add(@RequestBody final Request<MilestoneUpdateRequest> request);

    @PatchMapping("/partial-update")
    @Operation(summary = "Update Milestone API", description = "Partial Update Milestone by segment id and milestone code.", tags = "milestones")
    Response<Milestone> partialUpdate(@RequestBody final Request<Milestone> request);

    @PatchMapping("/{id}/update-milestone-time")
    @Operation(summary = "Update Milestone Time API", description = "Updates the milestone time", tags = "milestones")
    Response<Milestone> updateMilestoneTime(@PathVariable("id") final String id, @RequestBody final Request<MilestoneUpdateTimeRequest> request);
}
