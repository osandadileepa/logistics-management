package com.quincus.shipment.impl.service;


import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.web.common.exception.model.InvalidFieldTypeException;
import com.quincus.web.common.exception.model.MissingMandatoryFieldsException;
import com.quincus.web.common.exception.model.QuincusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorMilestoneServiceTest {

    @InjectMocks
    private VendorMilestoneService vendorMilestoneService;

    @Mock
    private MilestoneService milestoneService;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private PackageJourneySegmentRepository segmentRepository;

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Mock
    private AlertService alertService;

    @Mock
    private MilestonePostProcessService milestonePostProcessService;



    @Test
    void givenMilestoneUpdateRequestWithMissingMandatoryField_whenReceiveMilestoneUpdateFromAPIG_shouldThrowMissingMandatoryFieldsException() {
        String journeyId = "1bbd7255-46e8-4052-aed4-c2615a89fbba";
        MilestoneUpdateRequest request = new MilestoneUpdateRequest();

        doThrow(new MissingMandatoryFieldsException("")).when(milestoneService).validate(any(MilestoneUpdateRequest.class));

        assertThrows(MissingMandatoryFieldsException.class, () -> vendorMilestoneService.receiveMilestoneUpdateFromAPIG(journeyId, request));
    }

    @Test
    void givenMilestoneUpdateRequestWithInvalidFieldType_whenReceiveMilestoneUpdateFromAPIG_shouldThrowInvalidFieldTypeException() {
        String journeyId = "1bbd7255-46e8-4052-aed4-c2615a89fbba";
        MilestoneUpdateRequest request = new MilestoneUpdateRequest();

        doThrow(new InvalidFieldTypeException("")).when(milestoneService).validate(any(MilestoneUpdateRequest.class));

        assertThrows(InvalidFieldTypeException.class, () -> vendorMilestoneService.receiveMilestoneUpdateFromAPIG(journeyId, request));
    }

    @Test
    void givenMilestoneUpdateRequestWithError_whenReceiveMilestoneUpdateFromAPIG_shouldThrowQuincusException() {
        String journeyId = "1bbd7255-46e8-4052-aed4-c2615a89fbba";
        MilestoneUpdateRequest request = new MilestoneUpdateRequest();
        Set<ConstraintViolation<Object>> constraintViolations = new HashSet<>();

        doThrow(new ConstraintViolationException("", constraintViolations)).when(milestoneService).validate(any(MilestoneUpdateRequest.class));

        assertThrows(QuincusException.class, () -> vendorMilestoneService.receiveMilestoneUpdateFromAPIG(journeyId, request));
    }

    @Test
    void givenMilestoneUpdateRequestWithNoneExistingJourneyId_whenReceiveMilestoneUpdateFromAPIG_shouldThrowQuincusException() {
        String journeyId = "1bbd7255-46e8-4052-aed4-c2615a89fbba";
        MilestoneUpdateRequest request = new MilestoneUpdateRequest();

        when(milestoneService.createMilestoneFromAPIG(request)).thenReturn(new Milestone());
        when(shipmentRepository.findByJourneyId(journeyId)).thenReturn(Collections.emptyList());

        assertThrows(QuincusException.class, () -> vendorMilestoneService.receiveMilestoneUpdateFromAPIG(journeyId, request));
    }

    @Test
    void givenMilestoneUpdateRequestWithNoneExistingShipmentId_whenReceiveMilestoneUpdateFromAPIG_shouldThrowQuincusException() {
        String journeyId = "1bbd7255-46e8-4052-aed4-c2615a89fbba";
        MilestoneUpdateRequest request = new MilestoneUpdateRequest();
        request.setShipmentIds(List.of("1", "2", "3"));

        List<ShipmentEntity> entities = new ArrayList<>();
        ShipmentEntity shipment1 = new ShipmentEntity();
        shipment1.setId("1");
        entities.add(shipment1);

        ShipmentEntity shipment2 = new ShipmentEntity();
        shipment2.setId("2");
        entities.add(shipment2);

        when(milestoneService.createMilestoneFromAPIG(request)).thenReturn(new Milestone());
        when(shipmentRepository.findByJourneyId(journeyId)).thenReturn(entities);

        assertThrows(QuincusException.class, () -> vendorMilestoneService.receiveMilestoneUpdateFromAPIG(journeyId, request));
    }

    @Test
    void givenMilestoneUpdateRequestWithNoneExistingSegmentId_whenReceiveMilestoneUpdateFromAPIG_shouldThrowQuincusException() {
        String journeyId = "1bbd7255-46e8-4052-aed4-c2615a89fbba";
        String organizationId = "af6784b6-ee92-47ab-b1d2-351c3d143e5f";
        String segmentId = "1bbd7255-46e8-4052-aed4-c2615a89fbbx";

        MilestoneUpdateRequest request = new MilestoneUpdateRequest();
        request.setSegmentId(segmentId);
        request.setShipmentIds(List.of("1", "2"));

        List<ShipmentEntity> entities = new ArrayList<>();
        ShipmentEntity shipment1 = new ShipmentEntity();
        shipment1.setId("1");
        entities.add(shipment1);

        ShipmentEntity shipment2 = new ShipmentEntity();
        shipment2.setId("2");
        entities.add(shipment2);

        Milestone milestone = new Milestone();
        milestone.setSegmentId(segmentId);

        when(milestoneService.createMilestoneFromAPIG(request)).thenReturn(milestone);
        when(shipmentRepository.findByJourneyId(journeyId)).thenReturn(entities);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(segmentRepository.findByIdAndOrganizationId(request.getSegmentId(), organizationId)).thenReturn(null);

        assertThrows(QuincusException.class, () -> vendorMilestoneService.receiveMilestoneUpdateFromAPIG(journeyId, request));
    }

    @Test
    void givenMilestoneUpdateRequestWithFailedStatusMilestone_whenReceiveMilestoneUpdateFromAPIG_shouldProcessAndCreateAlert() {
        String journeyId = "1bbd7255-46e8-4052-aed4-c2615a89fbba";
        String organizationId = "af6784b6-ee92-47ab-b1d2-351c3d143e5f";
        String segmentId = "1bbd7255-46e8-4052-aed4-c2615a89fbbx";

        MilestoneUpdateRequest request = new MilestoneUpdateRequest();
        request.setSegmentId(segmentId);
        request.setShipmentIds(List.of("1", "2"));
        request.setMilestone("1502");

        List<ShipmentEntity> entities = new ArrayList<>();
        ShipmentEntity shipment1 = new ShipmentEntity();
        shipment1.setId("1");
        entities.add(shipment1);

        ShipmentEntity shipment2 = new ShipmentEntity();
        shipment2.setId("2");
        entities.add(shipment2);

        Milestone milestone = new Milestone();
        milestone.setSegmentId(segmentId);
        milestone.setShipmentId("1");
        milestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_FAILED);
        milestone.setMilestoneTime(OffsetDateTime.now());

        when(milestoneService.createMilestoneFromAPIG(request)).thenReturn(milestone);
        when(shipmentRepository.findByJourneyId(journeyId)).thenReturn(entities);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(segmentRepository.findByIdAndOrganizationId(segmentId, organizationId)).thenReturn(Optional.of(new PackageJourneySegmentEntity()));
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId(any())).thenReturn(OffsetDateTime.now());
        when(milestoneService.isNewMilestoneAfterMostRecentMilestone(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(true);
        when(milestoneService.isFailedStatusCode(any(MilestoneCode.class))).thenReturn(true);
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestone)).thenReturn(true);

        vendorMilestoneService.receiveMilestoneUpdateFromAPIG(journeyId, request);

        verify(alertService, times(2)).createPickupDeliveryFailedAlert(anyString());
        verify(milestoneService, times(2)).updateMilestoneAndPackageJourneySegment(any(), any(), any());
        verify(milestonePostProcessService, times(2)).createAndSendShipmentMilestone(any(Milestone.class), any());
        verify(milestonePostProcessService, times(2)).createAndSendAPIGWebhooks(any(Milestone.class), any());
        verify(milestonePostProcessService, times( 2)).createAndSendSegmentDispatch(any(Milestone.class), any());
        verify(milestonePostProcessService, times(2)).createAndSendQShipSegment(any(Milestone.class), any());
        verify(milestonePostProcessService, times(2)).createAndSendNotification(any(Milestone.class), any());
    }

    @Test
    void givenHaveSameMilestoneOrMilestoneCodePickupSuccessful_whenReceiveMilestoneUpdateFromAPIG_shouldProcess() {
        String journeyId = "1bbd7255-46e8-4052-aed4-c2615a89fbba";
        String organizationId = "af6784b6-ee92-47ab-b1d2-351c3d143e5f";
        String segmentId = "1bbd7255-46e8-4052-aed4-c2615a89fbbx";

        MilestoneUpdateRequest request = new MilestoneUpdateRequest();
        request.setSegmentId(segmentId);
        request.setShipmentIds(List.of("1", "2"));
        request.setMilestone("1502");

        List<ShipmentEntity> entities = new ArrayList<>();
        ShipmentEntity shipment1 = new ShipmentEntity();
        shipment1.setId("1");
        entities.add(shipment1);

        ShipmentEntity shipment2 = new ShipmentEntity();
        shipment2.setId("2");
        entities.add(shipment2);

        Milestone milestone = new Milestone();
        milestone.setSegmentId(segmentId);
        milestone.setShipmentId("1");
        milestone.setMilestoneCode(MilestoneCode.DSP_PICKUP_SUCCESSFUL);
        milestone.setMilestoneTime(OffsetDateTime.now());

        when(milestoneService.createMilestoneFromAPIG(request)).thenReturn(milestone);
        when(shipmentRepository.findByJourneyId(journeyId)).thenReturn(entities);
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn(organizationId);
        when(segmentRepository.findByIdAndOrganizationId(segmentId, organizationId)).thenReturn(Optional.of(new PackageJourneySegmentEntity()));
        when(milestoneService.isNewMilestoneAfterMostRecentMilestone(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(true);
        when(milestoneService.getMostRecentMilestoneTimeByShipmentId(any())).thenReturn(OffsetDateTime.now());
        when(milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestone)).thenReturn(false);

        vendorMilestoneService.receiveMilestoneUpdateFromAPIG(journeyId, request);

        verify(alertService, times(0)).createPickupDeliveryFailedAlert(anyString());
        verify(milestoneService, times(0)).updateMilestoneAndPackageJourneySegment(any(), any(), any());
        verify(milestonePostProcessService, times(2)).createAndSendShipmentMilestone(any(Milestone.class), any());
        verify(milestonePostProcessService, times(2)).createAndSendAPIGWebhooks(any(Milestone.class), any());
        verify(milestonePostProcessService, times( 2)).createAndSendSegmentDispatch(any(Milestone.class), any());
        verify(milestonePostProcessService, times(2)).createAndSendQShipSegment(any(Milestone.class), any());
        verify(milestonePostProcessService, times(2)).createAndSendNotification(any(Milestone.class), any());
    }
}
