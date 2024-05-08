package com.quincus.shipment.impl.service;

import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class VendorBookingPostProcessService {

    private static final String QLOGGER_UPDATE_VENDOR_EVENT_SOURCE = "VendorBookingPostProcessService#notifyOthersOnVendorBookingUpdate";

    private MilestoneService milestoneService;
    private final MessageApi messageApi;
    private final QLoggerAPI qLoggerAPI;

    public void notifyOthersOnVendorBookingUpdate(List<Shipment> shipments,
                                                  PackageJourneySegment prevPackageJourneySegment,
                                                  PackageJourneySegment updatedPackageJourneySegment) {

        Shipment shipment = shipments.stream().findFirst().orElse(null);
        shipments.forEach(s -> messageApi.sendUpdatedSegmentFromShipment(s, updatedPackageJourneySegment));
        messageApi.sendSegmentDispatch(shipment, updatedPackageJourneySegment, SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.VENDOR);
        qLoggerAPI.publishVendorBookingUpdateEvent(QLOGGER_UPDATE_VENDOR_EVENT_SOURCE, shipment, prevPackageJourneySegment, updatedPackageJourneySegment);
    }

    public void sendVendorBookingUpdateMilestone(List<Shipment> shipments, PackageJourneySegment updatedPackageJourneySegment, MilestoneCode milestoneCode) {
        shipments.forEach(shipment -> {
            Milestone assigmentScheduledMilestone = milestoneService.createVendorUpdateMilestone(shipment, updatedPackageJourneySegment, milestoneCode);
            messageApi.sendMilestoneMessage(assigmentScheduledMilestone, shipment, updatedPackageJourneySegment, TriggeredFrom.APIG);
        });
    }
}
