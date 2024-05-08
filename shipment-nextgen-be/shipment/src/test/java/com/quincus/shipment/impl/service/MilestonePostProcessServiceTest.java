package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.NotificationApi;
import com.quincus.shipment.api.constant.DspSegmentMsgUpdateSource;
import com.quincus.shipment.api.constant.MilestoneSource;
import com.quincus.shipment.api.constant.SegmentDispatchType;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static com.quincus.shipment.api.constant.MilestoneCode.DSP_ASSIGNMENT_CANCELED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_ASSIGNMENT_UPDATED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DISPATCH_SCHEDULED;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DRIVER_ARRIVED_FOR_DELIVERY;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DRIVER_ARRIVED_FOR_PICKUP;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.OM_BOOKED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestonePostProcessServiceTest {
    @InjectMocks
    private MilestonePostProcessService service;

    @Mock
    private ApiGatewayApi apiGatewayApi;

    @Mock
    private MessageApi messageApi;

    @Mock
    private NotificationApi notificationApi;

    @Mock
    private MilestoneService milestoneService;

    private static Stream<Arguments> transformMilestoneStreamAddSegment(Stream<Arguments> baseStream) {
        return baseStream.map(args -> {
            Milestone milestone = (Milestone) args.get()[0];
            PackageJourneySegment segment = new PackageJourneySegment();
            segment.setSegmentId(milestone.getSegmentId());

            return Arguments.of(milestone, segment);
        });
    }

    private static Stream<Arguments> provideMilestoneWithOrWithoutSegmentChange() {
        Milestone milestoneNoSegmentChange = new Milestone();
        milestoneNoSegmentChange.setMilestoneCode(OM_BOOKED);
        milestoneNoSegmentChange.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        milestoneNoSegmentChange.setShipmentId("shipment1");
        milestoneNoSegmentChange.setSegmentId("segment1");

        Milestone milestoneWithSegmentChange = new Milestone();
        milestoneWithSegmentChange.setMilestoneCode(OM_BOOKED);
        milestoneWithSegmentChange.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        milestoneWithSegmentChange.setShipmentId("shipment2");
        milestoneWithSegmentChange.setSegmentId("segment2");
        milestoneWithSegmentChange.setSegmentUpdatedFromMilestone(true);

        return Stream.of(
                Arguments.of(milestoneNoSegmentChange),
                Arguments.of(milestoneWithSegmentChange)
        );
    }

    private static Stream<Arguments> provideMilestoneAndSegmentWithOrWithoutChange() {
        return transformMilestoneStreamAddSegment(provideMilestoneWithOrWithoutSegmentChange());
    }

    private static Stream<Arguments> provideDSPSuccessfulMilestone() {
        Milestone pickupSuccessful = new Milestone();
        pickupSuccessful.setMilestoneCode(DSP_PICKUP_SUCCESSFUL);
        pickupSuccessful.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        pickupSuccessful.setSegmentUpdatedFromMilestone(true);
        pickupSuccessful.setShipmentId("shipment1");
        pickupSuccessful.setSegmentId("segment1");

        Milestone deliverySuccessful = new Milestone();
        deliverySuccessful.setMilestoneCode(DSP_DELIVERY_SUCCESSFUL);
        deliverySuccessful.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        deliverySuccessful.setSegmentUpdatedFromMilestone(true);
        deliverySuccessful.setShipmentId("shipment2");
        deliverySuccessful.setSegmentId("segment2");

        return Stream.of(
                Arguments.of(pickupSuccessful),
                Arguments.of(deliverySuccessful)
        );
    }

    private static Stream<Arguments> provideDSPSuccessfulMilestoneAndSegment() {
        return transformMilestoneStreamAddSegment(provideDSPSuccessfulMilestone());
    }

    private static Stream<Arguments> provideDriverUpdatesMilestone() {
        Milestone dispatchScheduled = new Milestone();
        dispatchScheduled.setMilestoneCode(DSP_DISPATCH_SCHEDULED);
        dispatchScheduled.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        dispatchScheduled.setSegmentUpdatedFromMilestone(true);
        dispatchScheduled.setShipmentId("shipment1");
        dispatchScheduled.setSegmentId("segment1");

        Milestone assignmentUpdated = new Milestone();
        assignmentUpdated.setMilestoneCode(DSP_ASSIGNMENT_UPDATED);
        assignmentUpdated.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        assignmentUpdated.setSegmentUpdatedFromMilestone(true);
        assignmentUpdated.setShipmentId("shipment2");
        assignmentUpdated.setSegmentId("segment2");

        Milestone assignmentCanceled = new Milestone();
        assignmentCanceled.setMilestoneCode(DSP_ASSIGNMENT_CANCELED);
        assignmentCanceled.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        assignmentCanceled.setSegmentUpdatedFromMilestone(true);
        assignmentCanceled.setShipmentId("shipment3");
        assignmentCanceled.setSegmentId("segment3");

        return Stream.of(
                Arguments.of(dispatchScheduled),
                Arguments.of(assignmentUpdated),
                Arguments.of(assignmentCanceled)
        );
    }

    private static Stream<Arguments> provideDriverUpdatesMilestoneAndSegment() {
        return transformMilestoneStreamAddSegment(provideDriverUpdatesMilestone());
    }

    private static Stream<Arguments> provideDriverArrivedMilestone() {
        Milestone pickupArrived = new Milestone();
        pickupArrived.setMilestoneCode(DSP_DRIVER_ARRIVED_FOR_PICKUP);
        pickupArrived.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        pickupArrived.setSegmentUpdatedFromMilestone(true);
        pickupArrived.setShipmentId("shipment1");
        pickupArrived.setSegmentId("segment1");

        Milestone deliveryArrived = new Milestone();
        deliveryArrived.setMilestoneCode(DSP_DRIVER_ARRIVED_FOR_DELIVERY);
        deliveryArrived.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        deliveryArrived.setSegmentUpdatedFromMilestone(true);
        deliveryArrived.setShipmentId("shipment2");
        deliveryArrived.setSegmentId("segment2");

        return Stream.of(
                Arguments.of(pickupArrived),
                Arguments.of(deliveryArrived)
        );
    }

    private static Stream<Arguments> provideDriverArrivedMilestoneAndSegment() {
        return transformMilestoneStreamAddSegment(provideDriverArrivedMilestone());
    }

    @ParameterizedTest
    @MethodSource("provideMilestoneWithOrWithoutSegmentChange")
    void createAndSendShipmentMilestone_segmentImplied_shouldCreateMilestoneMessage(Milestone milestone) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendShipmentMilestone(milestone, shipment));

        verify(messageApi, times(1)).sendMilestoneMessage(milestone, shipment);
    }

    @ParameterizedTest
    @MethodSource("provideMilestoneAndSegmentWithOrWithoutChange")
    void createAndSendShipmentMilestone_explicitSegment_shouldCreateMilestoneMessage(Milestone milestone,
                                                                                     PackageJourneySegment segment) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());

        TriggeredFrom from = TriggeredFrom.SHP;

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendShipmentMilestone(milestone, shipment, segment, from));

        verify(messageApi, times(1)).sendMilestoneMessage(milestone, shipment, segment, from);
    }

    @ParameterizedTest
    @MethodSource("provideDSPSuccessfulMilestone")
    void createAndSendAPIGWebhooks_segmentImpliedAndMilestoneDSPSuccessful_shouldSendToAPIG(Milestone milestone) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());

        when(milestoneService.isAllShipmentFromOrderHaveSameMilestone(any(), any())).thenReturn(true);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, times(1)).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDSPSuccessfulMilestone")
    void createAndSendAPIGWebhooks_segmentImpliedAndMilestoneDSPSuccessfulWithVendorSource_shouldSendToAPIG(Milestone milestone) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.VENDOR);

        when(milestoneService.isAllShipmentFromOrderHaveSameMilestone(any(), any())).thenReturn(true);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, times(1)).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }


    @ParameterizedTest
    @MethodSource("provideDSPSuccessfulMilestone")
    void createAndSendAPIGWebhooks_notAllShipmentsAreUpdated_shouldNotSendToAPIGOrderUpdateRequest(Milestone milestone) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());

        when(milestoneService.isAllShipmentFromOrderHaveSameMilestone(any(), any())).thenReturn(false);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDSPSuccessfulMilestone")
    void createAndSendAPIGWebhooks_notAllShipmentsAreUpdatedWithOrgSource_shouldNeverSendToAPIGOrderUpdateRequest(Milestone milestone) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.ORG);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }


    @ParameterizedTest
    @MethodSource("provideDSPSuccessfulMilestoneAndSegment")
    void createAndSendAPIGWebhooks_explicitSegmentAndMilestoneDSPSuccessful_shouldSendToAPIG(Milestone milestone,
                                                                                             PackageJourneySegment segment) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());

        when(milestoneService.isAllShipmentFromOrderHaveSameMilestone(any(), any())).thenReturn(true);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment, segment));

        verify(apiGatewayApi, times(1)).sendUpdateOrderProgress(any(Shipment.class),
                any(PackageJourneySegment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetailsWithRetry(any(Shipment.class),
                any(PackageJourneySegment.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDSPSuccessfulMilestoneAndSegment")
    void createAndSendAPIGWebhooks_explicitSegmentAndMilestoneDSPSuccessfulWithVendorSource_shouldSendToAPIG(Milestone milestone,
                                                                                             PackageJourneySegment segment) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.VENDOR);

        when(milestoneService.isAllShipmentFromOrderHaveSameMilestone(any(), any())).thenReturn(true);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment, segment));

        verify(apiGatewayApi, times(1)).sendUpdateOrderProgress(any(Shipment.class),
                any(PackageJourneySegment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetailsWithRetry(any(Shipment.class),
                any(PackageJourneySegment.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDSPSuccessfulMilestoneAndSegment")
    void createAndSendAPIGWebhooks_explicitSegmentAndMilestoneDSPSuccessfulWithOrgSource_shouldNeverSendToAPIG(Milestone milestone,
                                                                                                             PackageJourneySegment segment) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.ORG);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment, segment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(PackageJourneySegment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetailsWithRetry(any(Shipment.class),
                any(PackageJourneySegment.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverUpdatesMilestone")
    void createAndSendAPIGWebhooks_segmentImpliedAndMilestoneDriverUpdates_shouldSendToAPIG(Milestone milestone) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, times(1)).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverUpdatesMilestone")
    void createAndSendAPIGWebhooks_segmentImpliedAndMilestoneDriverUpdatesWithVendorSource_shouldSendToAPIG(Milestone milestone) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.VENDOR);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, times(1)).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverUpdatesMilestone")
    void createAndSendAPIGWebhooks_segmentImpliedAndMilestoneDriverUpdatesWithOrgSource_shouldNeverSendToAPIG(Milestone milestone) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.ORG);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverUpdatesMilestoneAndSegment")
    void createAndSendAPIGWebhooks_explicitSegmentAndMilestoneDriverUpdates_shouldSendToAPIG(Milestone milestone,
                                                                                             PackageJourneySegment segment)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment, segment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(PackageJourneySegment.class), any(Milestone.class));
        verify(apiGatewayApi, times(1)).sendAssignVendorDetailsWithRetry(any(Shipment.class),
                any(PackageJourneySegment.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverUpdatesMilestoneAndSegment")
    void createAndSendAPIGWebhooks_explicitSegmentAndMilestoneDriverUpdatesWithVendorSource_shouldSendToAPIG(Milestone milestone,
                                                                                             PackageJourneySegment segment)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.VENDOR);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment, segment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(PackageJourneySegment.class), any(Milestone.class));
        verify(apiGatewayApi, times(1)).sendAssignVendorDetailsWithRetry(any(Shipment.class),
                any(PackageJourneySegment.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverUpdatesMilestoneAndSegment")
    void createAndSendAPIGWebhooks_explicitSegmentAndMilestoneDriverUpdatesWithOrgSource_shouldNeverSendToAPIG(Milestone milestone,
                                                                                                             PackageJourneySegment segment)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.ORG);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment, segment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(PackageJourneySegment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetailsWithRetry(any(Shipment.class),
                any(PackageJourneySegment.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverArrivedMilestone")
    void createAndSendAPIGWebhooks_segmentImpliedAndMilestoneDriverArrivedWithVendorSource_shouldSendToAPIG(Milestone milestone)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.VENDOR);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, times(1)).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverArrivedMilestone")
    void createAndSendAPIGWebhooks_segmentImpliedAndMilestoneDriverArrivedWithOrgSource_shouldNeverSendToAPIG(Milestone milestone)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.ORG);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverArrivedMilestone")
    void createAndSendAPIGWebhooks_segmentImpliedAndMilestoneDriverArrived_shouldSendToAPIG(Milestone milestone)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, times(1)).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverArrivedMilestoneAndSegment")
    void createAndSendAPIGWebhooks_explicitSegmentAndMilestoneDriverArrived_shouldSendToAPIG(Milestone milestone,
                                                                                             PackageJourneySegment segment)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment, segment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(PackageJourneySegment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetailsWithRetry(any(Shipment.class),
                any(PackageJourneySegment.class));
        verify(apiGatewayApi, times(1)).sendCheckInDetails(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverArrivedMilestoneAndSegment")
    void createAndSendAPIGWebhooks_explicitSegmentAndMilestoneDriverArrivedWithVendorSource_shouldSendToAPIG(Milestone milestone,
                                                                                             PackageJourneySegment segment)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.VENDOR);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment, segment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(PackageJourneySegment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetailsWithRetry(any(Shipment.class),
                any(PackageJourneySegment.class));
        verify(apiGatewayApi, times(1)).sendCheckInDetails(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideDriverArrivedMilestoneAndSegment")
    void createAndSendAPIGWebhooks_explicitSegmentAndMilestoneDriverArrivedWithOrgSource_shouldNeverSendToAPIG(Milestone milestone,
                                                                                                             PackageJourneySegment segment)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.ORG);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment, segment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(PackageJourneySegment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetailsWithRetry(any(Shipment.class),
                any(PackageJourneySegment.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideMilestoneWithOrWithoutSegmentChange")
    void createAndSendAPIGWebhooks_segmentImpliedAndOtherMilestone_shouldNeverSendToAPIG(Milestone milestone)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideMilestoneWithOrWithoutSegmentChange")
    void createAndSendAPIGWebhooks_segmentImpliedAndOtherMilestoneWithVendorSource_shouldNeverSendToAPIG(Milestone milestone)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.VENDOR);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideMilestoneWithOrWithoutSegmentChange")
    void createAndSendAPIGWebhooks_segmentImpliedAndOtherMilestoneWithOrgSource_shouldNeverSendToAPIG(Milestone milestone)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.ORG);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetails(any(Shipment.class), any(Milestone.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideMilestoneAndSegmentWithOrWithoutChange")
    void createAndSendAPIGWebhooks_explicitSegmentAndOtherMilestone_shouldNeverSendToAPIG(Milestone milestone,
                                                                                          PackageJourneySegment segment)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment, segment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetailsWithRetry(any(Shipment.class),
                any(PackageJourneySegment.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideMilestoneAndSegmentWithOrWithoutChange")
    void createAndSendAPIGWebhooks_explicitSegmentAndOtherMilestoneWithVendorSource_shouldNeverSendToAPIG(Milestone milestone,
                                                                                          PackageJourneySegment segment)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.VENDOR);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment, segment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetailsWithRetry(any(Shipment.class),
                any(PackageJourneySegment.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
    }

    @ParameterizedTest
    @MethodSource("provideMilestoneAndSegmentWithOrWithoutChange")
    void createAndSendAPIGWebhooks_explicitSegmentAndOtherMilestoneWithOrgSource_shouldNeverSendToAPIG(Milestone milestone,
                                                                                                          PackageJourneySegment segment)
            throws JsonProcessingException {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        milestone.setSource(MilestoneSource.ORG);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendAPIGWebhooks(milestone, shipment, segment));

        verify(apiGatewayApi, never()).sendUpdateOrderProgress(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
        verify(apiGatewayApi, never()).sendAssignVendorDetailsWithRetry(any(Shipment.class),
                any(PackageJourneySegment.class));
        verify(apiGatewayApi, never()).sendCheckInDetails(any(Shipment.class), any(PackageJourneySegment.class),
                any(Milestone.class));
    }

    @Test
    void requestDispatchForMilestoneResend_shipmentArgument_shouldRequestDispatchResend() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");

        assertThatNoException()
                .isThrownBy(() -> service.requestDispatchForMilestoneResend(shipment));

        verify(messageApi, times(1)).sendSegmentDispatch(shipment, SegmentDispatchType.RESEND, DspSegmentMsgUpdateSource.CLIENT);
    }

    @Test
    void createAndSendSegmentDispatch_segmentImpliedAndChanged_shouldCreateSegmentDispatchMessage() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        milestone.setShipmentId(shipment.getId());
        milestone.setSegmentUpdatedFromMilestone(true);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendSegmentDispatch(milestone, shipment));

        verify(messageApi, times(1)).sendSegmentDispatch(shipment,
                SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
    }

    @Test
    void createAndSendSegmentDispatch_segmentImpliedNoChanges_shouldNeverCreateSegmentDispatchMessage() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        milestone.setShipmentId(shipment.getId());
        milestone.setSegmentUpdatedFromMilestone(false);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendSegmentDispatch(milestone, shipment));

        verify(messageApi, never()).sendSegmentDispatch(shipment,
                SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
    }

    @Test
    void createAndSendSegmentDispatch_explicitSegmentAndChanged_shouldCreateSegmentDispatchMessage() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");

        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId("segment1");

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        milestone.setShipmentId(shipment.getId());
        milestone.setSegmentId(segment.getSegmentId());
        milestone.setSegmentUpdatedFromMilestone(true);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendSegmentDispatch(milestone, shipment, segment));

        verify(messageApi, times(1)).sendSegmentDispatch(shipment, segment,
                SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
    }

    @Test
    void createAndSendSegmentDispatch_explicitSegmentNoChanges_shouldNeverCreateSegmentDispatchMessage() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");

        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId("segment1");

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        milestone.setShipmentId(shipment.getId());
        milestone.setSegmentId(segment.getSegmentId());
        milestone.setSegmentUpdatedFromMilestone(false);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendSegmentDispatch(milestone, shipment, segment));

        verify(messageApi, never()).sendSegmentDispatch(shipment, segment,
                SegmentDispatchType.JOURNEY_UPDATED, DspSegmentMsgUpdateSource.CLIENT);
    }

    @Test
    void createAndSendQShipSegment_segmentImpliedAndChanged_shouldCreateQShipSegmentMessage() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        milestone.setShipmentId(shipment.getId());
        milestone.setSegmentUpdatedFromMilestone(true);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendQShipSegment(milestone, shipment));

        verify(messageApi, times(1)).sendUpdatedSegmentFromShipment(shipment, milestone.getSegmentId());
    }

    @Test
    void createAndSendQShipSegment_segmentImpliedNoChanges_shouldNeverCreateQShipSegmentMessage() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        milestone.setShipmentId(shipment.getId());
        milestone.setSegmentUpdatedFromMilestone(false);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendQShipSegment(milestone, shipment));

        verify(messageApi, never()).sendUpdatedSegmentFromShipment(shipment, milestone.getSegmentId());
    }

    @Test
    void createAndSendQShipSegment_explicitSegmentAndChanged_shouldCreateQShipSegmentMessage() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");

        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId("segment1");

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        milestone.setShipmentId(shipment.getId());
        milestone.setSegmentId(segment.getSegmentId());
        milestone.setSegmentUpdatedFromMilestone(true);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendQShipSegment(milestone, shipment, segment));

        verify(messageApi, times(1)).sendUpdatedSegmentFromShipment(shipment, segment);
    }

    @Test
    void createAndSendQShipSegment_explicitSegmentNoChanges_shouldNeverCreateQShipSegmentMessage() {
        Shipment shipment = new Shipment();
        shipment.setId("shipment1");

        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId("segment1");

        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneTime(OffsetDateTime.now(Clock.systemUTC()));
        milestone.setShipmentId(shipment.getId());
        milestone.setSegmentId(segment.getSegmentId());
        milestone.setSegmentUpdatedFromMilestone(false);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendQShipSegment(milestone, shipment, segment));

        verify(messageApi, never()).sendUpdatedSegmentFromShipment(shipment, segment);
    }

    @ParameterizedTest
    @MethodSource("provideMilestoneWithOrWithoutSegmentChange")
    void createAndSendNotification_milestoneAndShipmentArgs_shouldSendNotification(Milestone milestone) {
        Shipment shipment = new Shipment();
        shipment.setId(milestone.getShipmentId());
        Organization organization = new Organization(UUID.randomUUID().toString());
        shipment.setOrganization(organization);

        assertThatNoException()
                .isThrownBy(() -> service.createAndSendNotification(milestone, shipment));

        verify(notificationApi, times(1)).sendNotification(any(NotificationRequest.class));
    }
}
