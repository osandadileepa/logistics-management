package com.quincus.shipment.kafka.producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.kafka.producers.message.MilestoneMessage;
import com.quincus.web.common.validator.PostProcessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageApiImplTest {

    @InjectMocks
    private MessageApiImpl messageApi;

    @Mock
    private MessageService messageService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PostProcessValidator<ShipmentJourney> journeyValidator;

    @Mock
    private PostProcessValidator<PackageJourneySegment> segmentValidator;

    @Nested
    class SegmentDispatchQshipValidationTrueTest {
        @BeforeEach
        void initializeCommonMocks() {
            lenient().when(journeyValidator.isValid(any())).thenReturn(true);
            lenient().when(segmentValidator.isValid(any())).thenReturn(true);
        }

        @Test
        void sendSegmentDispatch_withJourneyParam_shouldCallService() {
            messageApi.sendSegmentDispatch(List.of(new Shipment()), new ShipmentJourney(), SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
            verify(messageService, times(1)).sendSegmentDispatch(anyList(), any(ShipmentJourney.class),
                    eq(SegmentDispatchType.JOURNEY_UPDATED), eq(DspSegmentMsgUpdateSource.CLIENT));
        }

        @Test
        void sendSegmentDispatch_withSegmentParam_shouldCallService() {
            messageApi.sendSegmentDispatch(new Shipment(), new PackageJourneySegment(), SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
            verify(messageService, times(1)).sendSegmentsDispatch(any(Shipment.class),
                    any(PackageJourneySegment.class), eq(SegmentDispatchType.JOURNEY_UPDATED), eq(DspSegmentMsgUpdateSource.CLIENT));
        }

        @Test
        void sendSegmentDispatch_withSingleShipmentArgument_shouldCallService() {
            Shipment shipment = new Shipment();
            shipment.setId("shipment1");
            ShipmentJourney journey = new ShipmentJourney();
            journey.setJourneyId("journey1");
            shipment.setShipmentJourney(journey);
            messageApi.sendSegmentDispatch(shipment, SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
            verify(messageService, times(1)).sendSegmentsDispatch(any(Shipment.class),
                    eq(SegmentDispatchType.JOURNEY_UPDATED), eq(DspSegmentMsgUpdateSource.CLIENT));
        }
    }

    @Nested
    class SegmentDispatchValidationFalseTest {
        @BeforeEach
        void initializeCommonMocks() {
            lenient().when(journeyValidator.isValid(any())).thenReturn(false);
            lenient().when(segmentValidator.isValid(any())).thenReturn(false);
        }

        //TODO: Enable once DSP fixes SHPV2-5945 counterpart
        @Disabled("bypassed")
        @Test
        void sendSegmentDispatch_withJourneyParam_shouldSkip() {
            messageApi.sendSegmentDispatch(List.of(new Shipment()), new ShipmentJourney(), SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
            verify(messageService, never()).sendSegmentDispatch(anyList(), any(ShipmentJourney.class),
                    eq(SegmentDispatchType.JOURNEY_UPDATED), eq(DspSegmentMsgUpdateSource.CLIENT));
        }

        //TODO: Enable once DSP fixes SHPV2-5945 counterpart
        @Disabled("bypassed")
        @Test
        void sendSegmentDispatch_withSegmentParam_shouldSkip() {
            messageApi.sendSegmentDispatch(new Shipment(), new PackageJourneySegment(), SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
            verify(messageService, never()).sendSegmentsDispatch(any(Shipment.class),
                    any(PackageJourneySegment.class), eq(SegmentDispatchType.JOURNEY_UPDATED), eq(DspSegmentMsgUpdateSource.CLIENT));
        }

        //TODO: Enable once DSP fixes SHPV2-5945 counterpart
        @Disabled("bypassed")
        @Test
        void sendSegmentDispatch_withSingleShipmentArgument_shouldSkip() {
            Shipment shipment = new Shipment();
            shipment.setId("shipment1");
            ShipmentJourney journey = new ShipmentJourney();
            journey.setJourneyId("journey1");
            shipment.setShipmentJourney(journey);
            messageApi.sendSegmentDispatch(shipment, SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
            verify(messageService, never()).sendSegmentsDispatch(any(Shipment.class),
                    eq(SegmentDispatchType.JOURNEY_UPDATED), eq(DspSegmentMsgUpdateSource.CLIENT));
        }
    }

    @Test
    void sendMilestoneMessage_shouldNotThrowNPE() {
        messageApi.sendMilestoneMessage(null, TriggeredFrom.SHP);

        Shipment shipment = new Shipment();
        messageApi.sendMilestoneMessage(shipment, TriggeredFrom.SHP);

        ShipmentJourney journey = new ShipmentJourney();
        shipment.setShipmentJourney(journey);
        messageApi.sendMilestoneMessage(shipment, TriggeredFrom.SHP);

        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setStatus(SegmentStatus.COMPLETED);
        journey.addPackageJourneySegment(segment);
        messageApi.sendMilestoneMessage(shipment, TriggeredFrom.SHP);

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(MilestoneCode.OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now());
        shipment.setMilestone(milestone);
        messageApi.sendMilestoneMessage(shipment, TriggeredFrom.SHP);

        segment.setStatus(SegmentStatus.PLANNED);
        Package shipmentPackage = new Package();
        Organization organization = new Organization();
        shipment.setShipmentPackage(shipmentPackage);
        shipment.setOrganization(organization);
        messageApi.sendMilestoneMessage(shipment, TriggeredFrom.SHP);

        verify(messageService, times(1)).sendMilestoneMessage(any(Shipment.class), any(MilestoneMessage.class), any(TriggeredFrom.class));
    }

    @Test
    void sendMilestoneMessage_withSegmentArgument_shouldSendMessage() {
        String shipmentId = "shipment1";
        Shipment shipment = new Shipment();
        Organization organization = new Organization("org1");
        shipment.setOrganization(organization);
        Package shipmentPackage = new Package();
        shipmentPackage.setId("package1");
        shipment.setShipmentPackage(shipmentPackage);

        String segmentId = "segment1";
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);

        shipment.setId(shipmentId);
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(MilestoneCode.OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setShipmentId(shipmentId);
        milestone.setSegmentId(segmentId);

        messageApi.sendMilestoneMessage(milestone, shipment, segment, TriggeredFrom.SHP);

        verify(messageService, times(1)).sendMilestoneMessage(eq(shipment),
                any(MilestoneMessage.class), eq(TriggeredFrom.SHP));

    }

    @Test
    void sendFlightMilestoneMessage_validArguments_shouldSendMessage() {
        String shipmentId = "shipment-id-1";
        String segmentId = "segment-id-1";
        ShipmentMessageDto shipment = new ShipmentMessageDto();
        shipment.setId(shipmentId);
        Organization organization = new Organization();
        organization.setId("organization-id-1");
        shipment.setOrganizationId(organization.getId());
        Package shipmentPackage = new Package();
        shipmentPackage.setId("package-id-1");
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(segmentId);
        Milestone milestone = new Milestone();
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setShipmentId(shipmentId);
        milestone.setSegmentId(segmentId);

        messageApi.sendFlightMilestoneMessage(milestone, shipment, segment, TriggeredFrom.APIG);

        verify(messageService, times(1)).sendMilestoneMessage(any());
    }
}
