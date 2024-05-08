package com.quincus.shipment.impl.service;

import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.NotificationApi;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.MilestoneSource;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.NotificationRequest;
import com.quincus.shipment.api.helper.MilestoneCodeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DRIVER_ARRIVED_FOR_DELIVERY;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DRIVER_ARRIVED_FOR_PICKUP;

@Service
@AllArgsConstructor
@Slf4j
public class MilestonePostProcessService {
    private static final String DEBUG_SENDING_MILESTONE_TO_APIG = "Sending milestone update from DSP to APIG {}";
    private static final String INFO_RESENDING_SHIPMENT_TO_DISPATCH = "Resending shipment record to dispatch topic";
    private final ApiGatewayApi apiGatewayApi;
    private final MessageApi messageApi;
    private final NotificationApi notificationApi;
    private final MilestoneService milestoneService;

    public void createAndSendShipmentMilestone(@NotNull Milestone milestone, @NotNull Shipment shipment) {
        messageApi.sendMilestoneMessage(milestone, shipment);
    }

    public void createAndSendShipmentMilestone(@NotNull Milestone milestone, @NotNull Shipment shipment,
                                               @NotNull PackageJourneySegment segment, @NotNull TriggeredFrom from) {
        messageApi.sendMilestoneMessage(milestone, shipment, segment, from);
    }

    public void createAndSendAPIGWebhooks(@NotNull Milestone milestone, @NotNull Shipment shipment) {
        if (!milestone.isSegmentUpdatedFromMilestone() || MilestoneSource.ORG == milestone.getSource()) {
            return;
        }

        sendUpdateOrderProgressToAPIG(shipment, milestone);
        sendAssignVendorDetails(shipment, milestone);
        sendCheckInDetails(shipment, milestone);
    }

    public void createAndSendAPIGWebhooks(@NotNull Milestone milestone, @NotNull Shipment shipment,
                                          @NotNull PackageJourneySegment segment) {
        if (!milestone.isSegmentUpdatedFromMilestone() || MilestoneSource.ORG == milestone.getSource()) {
            return;
        }

        sendUpdateOrderProgressToAPIG(shipment, milestone, segment);
        sendAssignVendorDetails(shipment, milestone, segment);
        sendCheckInDetails(shipment, milestone, segment);
    }

    public void requestDispatchForMilestoneResend(@NotNull Shipment shipment) {
        log.info(INFO_RESENDING_SHIPMENT_TO_DISPATCH);
        messageApi.sendSegmentDispatch(shipment, SegmentDispatchType.RESEND, DspSegmentMsgUpdateSource.CLIENT);
    }

    public void createAndSendSegmentDispatch(@NotNull Milestone milestone, @NotNull Shipment shipment) {
        if (!milestone.isSegmentUpdatedFromMilestone()) {
            return;
        }
        messageApi.sendSegmentDispatch(shipment, SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
    }

    public void createAndSendSegmentDispatch(@NotNull Milestone milestone, @NotNull Shipment shipment,
                                             @NotNull PackageJourneySegment segment) {
        if (!milestone.isSegmentUpdatedFromMilestone()) {
            return;
        }
        messageApi.sendSegmentDispatch(shipment, segment, SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
    }

    public void createAndSendQShipSegment(@NotNull Milestone milestone, @NotNull Shipment shipment) {
        if (!milestone.isSegmentUpdatedFromMilestone()) {
            return;
        }
        messageApi.sendUpdatedSegmentFromShipment(shipment, milestone.getSegmentId());
    }

    public void createAndSendQShipSegment(@NotNull Milestone milestone, @NotNull Shipment shipment,
                                          @NotNull PackageJourneySegment segment) {
        if (!milestone.isSegmentUpdatedFromMilestone()) {
            return;
        }
        messageApi.sendUpdatedSegmentFromShipment(shipment, segment);
    }

    public void createAndSendNotification(@NotNull Milestone milestone, @NotNull Shipment shipment) {
        notificationApi.sendNotification(NotificationRequest.ofMilestoneNotification(shipment, milestone, shipment.getOrganization().getId()));
    }

    private void sendUpdateOrderProgressToAPIG(Shipment shipment, Milestone milestone) {
        if (milestoneService.isAllShipmentFromOrderHaveSameMilestone(milestone, shipment)) {
            MilestoneCode code = milestone.getMilestoneCode();
            if (MilestoneCodeUtil.isCodePickupOrDeliveryRelated(code)) {
                log.debug(DEBUG_SENDING_MILESTONE_TO_APIG, milestone);
                apiGatewayApi.sendUpdateOrderProgress(shipment, milestone);
            }
        }
    }

    private void sendUpdateOrderProgressToAPIG(Shipment shipment, Milestone milestone, PackageJourneySegment segment) {
        if (milestoneService.isAllShipmentFromOrderHaveSameMilestone(milestone, shipment)) {
            MilestoneCode code = milestone.getMilestoneCode();
            if (MilestoneCodeUtil.isCodePickupOrDeliveryRelated(code)) {
                log.debug(DEBUG_SENDING_MILESTONE_TO_APIG, milestone);
                apiGatewayApi.sendUpdateOrderProgress(shipment, segment, milestone);
            }
        }
    }

    private void sendAssignVendorDetails(Shipment shipment, Milestone milestone) {
        if (MilestoneCodeUtil.isCodeUpdatingDriver(milestone.getMilestoneCode())) {
            log.debug(DEBUG_SENDING_MILESTONE_TO_APIG, milestone);
            apiGatewayApi.sendAssignVendorDetails(shipment, milestone);
        }
    }

    private void sendAssignVendorDetails(Shipment shipment, Milestone milestone, PackageJourneySegment segment) {
        if (MilestoneCodeUtil.isCodeUpdatingDriver(milestone.getMilestoneCode())) {
            log.debug(DEBUG_SENDING_MILESTONE_TO_APIG, milestone);
            apiGatewayApi.sendAssignVendorDetailsWithRetry(shipment, segment);
        }
    }

    private void sendCheckInDetails(Shipment shipment, Milestone milestone) {
        if (milestone.getMilestoneCode() == DSP_DRIVER_ARRIVED_FOR_PICKUP
                || milestone.getMilestoneCode() == DSP_DRIVER_ARRIVED_FOR_DELIVERY) {
            log.debug(DEBUG_SENDING_MILESTONE_TO_APIG, milestone);
            apiGatewayApi.sendCheckInDetails(shipment, milestone);
        }
    }

    private void sendCheckInDetails(Shipment shipment, Milestone milestone, PackageJourneySegment segment) {
        if (milestone.getMilestoneCode() == DSP_DRIVER_ARRIVED_FOR_PICKUP
                || milestone.getMilestoneCode() == DSP_DRIVER_ARRIVED_FOR_DELIVERY) {
            log.debug(DEBUG_SENDING_MILESTONE_TO_APIG, milestone);
            apiGatewayApi.sendCheckInDetails(shipment, segment, milestone);
        }
    }
}
