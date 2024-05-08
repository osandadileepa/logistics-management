package com.quincus.shipment.impl.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.service.MilestonePostProcessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MilestonePostProcessApiImplTest {
    @InjectMocks
    private MilestonePostProcessApiImpl milestonePostProcessApi;

    @Mock
    private MilestonePostProcessService milestonePostProcessService;

    @Test
    void createAndSendShipmentMilestone_genericArguments_shouldCallService() {
        milestonePostProcessApi.createAndSendShipmentMilestone(new Milestone(), new Shipment());
        verify(milestonePostProcessService, times(1))
                .createAndSendShipmentMilestone(any(Milestone.class), any(Shipment.class));
    }

    @Test
    void createAndSendShipmentMilestone_specificSegment_shouldCallService() {
        milestonePostProcessApi.createAndSendShipmentMilestone(new Milestone(), new Shipment(),
                new PackageJourneySegment(), TriggeredFrom.SHP);
        verify(milestonePostProcessService, times(1))
                .createAndSendShipmentMilestone(any(Milestone.class), any(Shipment.class),
                        any(PackageJourneySegment.class), eq(TriggeredFrom.SHP));
    }

    @Test
    void createAndSendAPIGWebhooks_genericArguments_shouldCallService() throws JsonProcessingException {
        milestonePostProcessApi.createAndSendAPIGWebhooks(new Milestone(), new Shipment());
        verify(milestonePostProcessService, times(1))
                .createAndSendAPIGWebhooks(any(Milestone.class), any(Shipment.class));
    }

    @Test
    void createAndSendAPIGWebhooks_specificSegment_shouldCallService() throws JsonProcessingException {
        milestonePostProcessApi.createAndSendAPIGWebhooks(new Milestone(), new Shipment(), new PackageJourneySegment());
        verify(milestonePostProcessService, times(1))
                .createAndSendAPIGWebhooks(any(Milestone.class), any(Shipment.class), any(PackageJourneySegment.class));
    }

    @Test
    void requestDispatchForMilestoneResend_shipmentArgument_shouldCallService() {
        milestonePostProcessApi.requestDispatchForMilestoneResend(new Shipment());
        verify(milestonePostProcessService, times(1))
                .requestDispatchForMilestoneResend(any(Shipment.class));
    }

    @Test
    void createAndSendSegmentDispatch_genericArguments_shouldCallService() {
        milestonePostProcessApi.createAndSendSegmentDispatch(new Milestone(), new Shipment());
        verify(milestonePostProcessService, times(1))
                .createAndSendSegmentDispatch(any(Milestone.class), any(Shipment.class));
    }

    @Test
    void createAndSendSegmentDispatch_specificSegment_shouldCallService() {
        milestonePostProcessApi.createAndSendSegmentDispatch(new Milestone(), new Shipment(), new PackageJourneySegment());
        verify(milestonePostProcessService, times(1))
                .createAndSendSegmentDispatch(any(Milestone.class), any(Shipment.class), any(PackageJourneySegment.class));
    }

    @Test
    void createAndSendQShipSegment_genericArguments_shouldCallService() {
        milestonePostProcessApi.createAndSendQShipSegment(new Milestone(), new Shipment());
        verify(milestonePostProcessService, times(1))
                .createAndSendQShipSegment(any(Milestone.class), any(Shipment.class));
    }

    @Test
    void createAndSendQShipSegment_specificSegment_shouldCallService() {
        milestonePostProcessApi.createAndSendQShipSegment(new Milestone(), new Shipment(), new PackageJourneySegment());
        verify(milestonePostProcessService, times(1))
                .createAndSendQShipSegment(any(Milestone.class), any(Shipment.class), any(PackageJourneySegment.class));
    }

    @Test
    void createAndSendNotification_milestoneAndShipmentArguments_shouldCallService() {
        milestonePostProcessApi.createAndSendNotification(new Milestone(), new Shipment());
        verify(milestonePostProcessService, times(1))
                .createAndSendNotification(any(Milestone.class), any(Shipment.class));
    }
}
