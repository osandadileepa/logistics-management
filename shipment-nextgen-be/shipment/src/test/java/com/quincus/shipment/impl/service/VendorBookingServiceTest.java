package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.constant.BookingStatus;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.dto.VendorBookingUpdateRequest;
import com.quincus.shipment.api.dto.VendorBookingUpdateResponse;
import com.quincus.shipment.api.exception.SegmentNotFoundException;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import org.apache.commons.lang3.StringUtils;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorBookingServiceTest {

    @InjectMocks
    private VendorBookingService vendorBookingService;
    @Mock
    private PackageJourneySegmentRepository packageJourneySegmentRepository;
    @Mock
    private AssignmentStatusGenerator assignmentStatusGenerator;
    @Mock
    private VendorBookingPostProcessService vendorBookingPostProcessService;
    @Mock
    private AlertService alertService;
    @Mock
    private ShipmentFetchService shipmentFetchService;

    @ParameterizedTest
    @MethodSource("allBookingStatusAndExpectedAssignmentStatus")
    void givenVendorBookingUpdates_whenReceiveVendorBookingUpdatesFromApiG_shouldCorrectlySetToSegmentAndTriggerNeededService(BookingStatus bookingStatusArgument, String expectedAssignmentStatus) {
        // GIVE:
        VendorBookingUpdateRequest request = new VendorBookingUpdateRequest();
        request.setSegmentId("segment123");
        request.setShipmentJourneyId("journey123");
        request.setBookingStatus(bookingStatusArgument);
        request.setWaybillNumber("WB-111111");
        request.setBookingId("test internal booking id");
        request.setBookingVendorReferenceId("test external booking id");
        request.setRejectionReason("sample rejection reason");

        PackageJourneySegmentEntity entity = new PackageJourneySegmentEntity();
        entity.setId("segment123");
        entity.setShipmentJourneyId("journey123");

        when(packageJourneySegmentRepository.findById("segment123")).thenReturn(java.util.Optional.of(entity));
        when(assignmentStatusGenerator.generateAssignmentStatusByBookingStatus(bookingStatusArgument)).thenReturn(expectedAssignmentStatus);
        when(shipmentFetchService.findByJourneyIdOrThrowException(any(String.class))).thenReturn(List.of(new ShipmentEntity()));

        // WHEN:
        VendorBookingUpdateResponse response = vendorBookingService.receiveVendorBookingUpdatesFromApiG(request);

        // THEN:
        assertThat(response).isNotNull();
        assertThat(entity.getInternalBookingReference()).isEqualTo(request.getBookingId());
        assertThat(entity.getExternalBookingReference()).isEqualTo(request.getBookingVendorReferenceId());
        assertThat(entity.getBookingStatus()).isEqualTo(request.getBookingStatus());
        assertThat(entity.getAssignmentStatus()).isEqualTo(expectedAssignmentStatus);
        assertThat(entity.getMasterWaybill()).isEqualTo(request.getWaybillNumber());
        assertAlertGenerationTrigger(entity);
        assertMilestoneGenerationTrigger(request);
        verify(assignmentStatusGenerator, times(1)).generateAssignmentStatusByBookingStatus(request.getBookingStatus());
        verify(packageJourneySegmentRepository, times(1)).save(entity);
        verify(vendorBookingPostProcessService, times(1)).notifyOthersOnVendorBookingUpdate(any(List.class), any(PackageJourneySegment.class), any(PackageJourneySegment.class));
    }

    private void assertAlertGenerationTrigger(PackageJourneySegmentEntity packageJourneySegmentEntity) {
        if (packageJourneySegmentEntity.getBookingStatus() == BookingStatus.FAILED) {
            verify(alertService, times(1)).createVendorAssignmentFailedAlerts(packageJourneySegmentEntity);
        } else if (packageJourneySegmentEntity.getBookingStatus() == BookingStatus.REJECTED) {
            verify(alertService, times(1)).createVendorAssignmentRejectedAlerts(packageJourneySegmentEntity);
        } else {
            verifyNoInteractions(alertService);
        }
    }

    private void assertMilestoneGenerationTrigger(VendorBookingUpdateRequest request) {
        if (request.getBookingStatus() == BookingStatus.CONFIRMED) {
            verify(vendorBookingPostProcessService, times(1)).sendVendorBookingUpdateMilestone(any(List.class), any(PackageJourneySegment.class), eq(MilestoneCode.SHP_ASSIGNMENT_SCHEDULED));
        } else if (request.getBookingStatus() == BookingStatus.CANCELLED) {
            verify(vendorBookingPostProcessService, times(1)).sendVendorBookingUpdateMilestone(any(List.class), any(PackageJourneySegment.class), eq(MilestoneCode.SHP_ASSIGNMENT_CANCELLED));
        } else {
            verify(vendorBookingPostProcessService, times(0)).sendVendorBookingUpdateMilestone(any(List.class), any(PackageJourneySegment.class), any(MilestoneCode.class));
        }
    }

    @Test
    void givenVendorBookingUpdatesOfUnknowSegment_whenReceiveVendorBookingUpdatesFromApiG_shouldThrowSegmentNotFoundException() {
        // GIVEN:
        VendorBookingUpdateRequest request = new VendorBookingUpdateRequest();
        request.setSegmentId("invalidSegment");
        request.setShipmentJourneyId("journey123");

        when(packageJourneySegmentRepository.findById("invalidSegment")).thenReturn(java.util.Optional.empty());

        // WHEN THEN:
        assertThatThrownBy(() -> vendorBookingService.receiveVendorBookingUpdatesFromApiG(request))
                .isInstanceOf(SegmentNotFoundException.class);
    }

    private static Stream<Arguments> allBookingStatusAndExpectedAssignmentStatus() {
        return Stream.of(
                Arguments.of(BookingStatus.COMPLETED, StringUtils.EMPTY),
                Arguments.of(BookingStatus.CONFIRMED, "Confirmed"),
                Arguments.of(BookingStatus.REJECTED, "Rejected"),
                Arguments.of(BookingStatus.FAILED, "Failed"),
                Arguments.of(BookingStatus.PENDING, "Pending"),
                Arguments.of(BookingStatus.CANCELLED, null)
        );
    }
}