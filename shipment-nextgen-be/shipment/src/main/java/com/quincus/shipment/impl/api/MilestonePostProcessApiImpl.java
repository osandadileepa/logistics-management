package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.MilestonePostProcessApi;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.service.MilestonePostProcessService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Service
@AllArgsConstructor
public class MilestonePostProcessApiImpl implements MilestonePostProcessApi {
    private final MilestonePostProcessService milestonePostProcessService;

    @Override
    public void createAndSendShipmentMilestone(@NotNull Milestone milestone, @NotNull Shipment shipment) {
        milestonePostProcessService.createAndSendShipmentMilestone(milestone, shipment);
    }

    @Override
    public void createAndSendShipmentMilestone(@NotNull Milestone milestone, @NotNull Shipment shipment,
                                               @NotNull PackageJourneySegment segment, @NotNull TriggeredFrom from) {
        milestonePostProcessService.createAndSendShipmentMilestone(milestone, shipment, segment, from);
    }

    @Override
    public void createAndSendAPIGWebhooks(@NotNull Milestone milestone, @NotNull Shipment shipment) {
        milestonePostProcessService.createAndSendAPIGWebhooks(milestone, shipment);
    }

    @Override
    public void createAndSendAPIGWebhooks(@NotNull Milestone milestone, @NotNull Shipment shipment,
                                          @NotNull PackageJourneySegment segment) {
        milestonePostProcessService.createAndSendAPIGWebhooks(milestone, shipment, segment);
    }

    @Override
    public void requestDispatchForMilestoneResend(@NotNull Shipment shipment) {
        milestonePostProcessService.requestDispatchForMilestoneResend(shipment);
    }

    @Override
    public void createAndSendSegmentDispatch(@NotNull Milestone milestone, @NotNull Shipment shipment) {
        milestonePostProcessService.createAndSendSegmentDispatch(milestone, shipment);
    }

    @Override
    public void createAndSendSegmentDispatch(@NotNull Milestone milestone, @NotNull Shipment shipment,
                                             @NotNull PackageJourneySegment segment) {
        milestonePostProcessService.createAndSendSegmentDispatch(milestone, shipment, segment);
    }

    @Override
    public void createAndSendQShipSegment(@NotNull Milestone milestone, @NotNull Shipment shipment) {
        milestonePostProcessService.createAndSendQShipSegment(milestone, shipment);
    }

    @Override
    public void createAndSendQShipSegment(@NotNull Milestone milestone, @NotNull Shipment shipment,
                                          @NotNull PackageJourneySegment segment) {
        milestonePostProcessService.createAndSendQShipSegment(milestone, shipment, segment);
    }

    @Override
    public void createAndSendNotification(@NotNull Milestone milestone, @NotNull Shipment shipment) {
        milestonePostProcessService.createAndSendNotification(milestone, shipment);
    }
}
