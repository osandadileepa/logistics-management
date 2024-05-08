package com.quincus.shipment.api;

import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;

import javax.validation.constraints.NotNull;

public interface MilestonePostProcessApi {
    void createAndSendShipmentMilestone(@NotNull Milestone milestone, @NotNull Shipment shipment);

    void createAndSendShipmentMilestone(@NotNull Milestone milestone, @NotNull Shipment shipment,
                                        @NotNull PackageJourneySegment segment, @NotNull TriggeredFrom from);

    void createAndSendAPIGWebhooks(@NotNull Milestone milestone, @NotNull Shipment shipment);

    void createAndSendAPIGWebhooks(@NotNull Milestone milestone, @NotNull Shipment shipment,
                                   @NotNull PackageJourneySegment segment);

    void requestDispatchForMilestoneResend(@NotNull Shipment shipment);

    void createAndSendSegmentDispatch(@NotNull Milestone milestone, @NotNull Shipment shipment);

    void createAndSendSegmentDispatch(@NotNull Milestone milestone, @NotNull Shipment shipment,
                                      @NotNull PackageJourneySegment segment);

    void createAndSendQShipSegment(@NotNull Milestone milestone, @NotNull Shipment shipment);

    void createAndSendQShipSegment(@NotNull Milestone milestone, @NotNull Shipment shipment,
                                   @NotNull PackageJourneySegment segment);

    void createAndSendNotification(@NotNull Milestone milestone, @NotNull Shipment shipment);
}
