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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorBookingPostProcessServiceTest {

    @InjectMocks
    private VendorBookingPostProcessService vendorBookingPostProcessService;
    @Mock
    private MessageApi messageApi;
    @Mock
    private QLoggerAPI qLoggerAPI;
    @Mock
    private MilestoneService milestoneService;

    @Test
    void givenShipmentAndSegmentToNotify_whenNotifyOthersOnVendorBookingUpdate_thenTriggerMessageApi() {
        PackageJourneySegment prevPackageJourneySegment = mock(PackageJourneySegment.class);
        PackageJourneySegment updatedPackageJourneySegment = mock(PackageJourneySegment.class);
        List<Shipment> shipments = List.of(new Shipment(), new Shipment());

        vendorBookingPostProcessService.notifyOthersOnVendorBookingUpdate(shipments, prevPackageJourneySegment, updatedPackageJourneySegment);

        verify(messageApi, times(2)).sendUpdatedSegmentFromShipment(any(Shipment.class), any(PackageJourneySegment.class));
        verify(messageApi, times(1)).sendSegmentDispatch(any(Shipment.class), any(PackageJourneySegment.class), any(SegmentDispatchType.class), any(DspSegmentMsgUpdateSource.class));
        verify(qLoggerAPI, times(1)).publishVendorBookingUpdateEvent(anyString(), any(Shipment.class), any(PackageJourneySegment.class), any(PackageJourneySegment.class));
    }


    @ParameterizedTest
    @MethodSource("vendorUpdateMilestone")
    void givenAssignmentScheduledMilestone_whenSendVendorBookingUpdateMilestone_thenTriggerSendMilestoneMessage(MilestoneCode milestoneCode) {
        MilestoneCode assignmentScheduledMilestone = MilestoneCode.SHP_ASSIGNMENT_SCHEDULED;
        PackageJourneySegment updatedPackageJourneySegment = mock(PackageJourneySegment.class);
        List<Shipment> shipments = List.of(new Shipment(), new Shipment());

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(milestoneCode);

        when(milestoneService.createVendorUpdateMilestone(any(Shipment.class), any(PackageJourneySegment.class), any(MilestoneCode.class))).thenReturn(milestone);

        vendorBookingPostProcessService.sendVendorBookingUpdateMilestone(shipments, updatedPackageJourneySegment, assignmentScheduledMilestone);

        verify(messageApi, times(2)).sendMilestoneMessage(any(Milestone.class), any(Shipment.class), any(PackageJourneySegment.class), any(TriggeredFrom.class));
    }

    private static Stream<Arguments> vendorUpdateMilestone() {
        return Stream.of(
                Arguments.of(MilestoneCode.SHP_ASSIGNMENT_SCHEDULED, MilestoneCode.SHP_ASSIGNMENT_CANCELLED));
    }
}
