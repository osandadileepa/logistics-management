package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.constant.BookingStatus;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.VendorBookingUpdateRequest;
import com.quincus.shipment.api.dto.VendorBookingUpdateResponse;
import com.quincus.shipment.api.exception.SegmentNotFoundException;
import com.quincus.shipment.impl.mapper.PackageJourneySegmentMapper;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class VendorBookingService {
    private static final String ERR_SEGMENT_NOT_FOUND = "Segment with Id: %s not found";
    private final PackageJourneySegmentRepository packageJourneySegmentRepository;
    private final AssignmentStatusGenerator assignmentStatusGenerator;
    private final VendorBookingPostProcessService vendorBookingPostProcessService;
    private final ShipmentFetchService shipmentFetchService;
    private final AlertService alertService;

    @Transactional
    public VendorBookingUpdateResponse receiveVendorBookingUpdatesFromApiG(VendorBookingUpdateRequest vendorBookingUpdateRequest) {
        PackageJourneySegmentEntity packageJourneySegmentEntity = packageJourneySegmentRepository.findById(vendorBookingUpdateRequest.getSegmentId()).orElse(null);
        if (packageJourneySegmentEntity == null || !packageJourneySegmentEntity.getShipmentJourneyId().equalsIgnoreCase(vendorBookingUpdateRequest.getShipmentJourneyId())) {
            throw new SegmentNotFoundException(String.format(ERR_SEGMENT_NOT_FOUND, vendorBookingUpdateRequest.getSegmentId()));
        }
        PackageJourneySegment prevPackageJourneySegment = PackageJourneySegmentMapper.mapEntityToDomain(packageJourneySegmentEntity);
        updateSegmentWithVendorBookingUpdateRequest(packageJourneySegmentEntity, vendorBookingUpdateRequest);
        createAlertsForFailedAndRejectedVendorBooking(packageJourneySegmentEntity);
        packageJourneySegmentRepository.save(packageJourneySegmentEntity);

        List<Shipment> shipments = findShipmentsByJourneyIdForMessageSending(packageJourneySegmentEntity.getShipmentJourneyId());
        PackageJourneySegment updatedPackageJourneySegment = PackageJourneySegmentMapper.mapEntityToDomain(packageJourneySegmentEntity);
        createAndSendMilestone(shipments, updatedPackageJourneySegment);
        vendorBookingPostProcessService.notifyOthersOnVendorBookingUpdate(shipments, prevPackageJourneySegment, updatedPackageJourneySegment);
        return new VendorBookingUpdateResponse(updatedPackageJourneySegment);
    }

    private List<Shipment> findShipmentsByJourneyIdForMessageSending(String shipmentJourneyId) {
        List<ShipmentEntity> shipmentEntities = shipmentFetchService.findByJourneyIdOrThrowException(shipmentJourneyId);
        return shipmentEntities.stream().map(ShipmentMapper::toShipmentForShipmentJourneyUpdate).toList();
    }

    private void updateSegmentWithVendorBookingUpdateRequest(PackageJourneySegmentEntity packageJourneySegmentEntity, VendorBookingUpdateRequest vendorBookingUpdateRequest) {
        packageJourneySegmentEntity.setInternalBookingReference(vendorBookingUpdateRequest.getBookingId());
        packageJourneySegmentEntity.setExternalBookingReference(vendorBookingUpdateRequest.getBookingVendorReferenceId());
        packageJourneySegmentEntity.setBookingStatus(vendorBookingUpdateRequest.getBookingStatus());
        packageJourneySegmentEntity.setMasterWaybill(vendorBookingUpdateRequest.getWaybillNumber());
        packageJourneySegmentEntity.setRejectionReason(vendorBookingUpdateRequest.getRejectionReason());
        packageJourneySegmentEntity.setAssignmentStatus(assignmentStatusGenerator.generateAssignmentStatusByBookingStatus(vendorBookingUpdateRequest.getBookingStatus()));
    }

    private void createAlertsForFailedAndRejectedVendorBooking(PackageJourneySegmentEntity packageJourneySegmentEntity) {
        if (packageJourneySegmentEntity.getBookingStatus() == BookingStatus.FAILED) {
            alertService.createVendorAssignmentFailedAlerts(packageJourneySegmentEntity);
        } else if (packageJourneySegmentEntity.getBookingStatus() == BookingStatus.REJECTED) {
            alertService.createVendorAssignmentRejectedAlerts(packageJourneySegmentEntity);
        }
    }

    private void createAndSendMilestone(List<Shipment> shipments, PackageJourneySegment updatedPackageJourneySegment) {
        if (updatedPackageJourneySegment.getBookingStatus() == BookingStatus.CONFIRMED) {
            vendorBookingPostProcessService.sendVendorBookingUpdateMilestone(shipments, updatedPackageJourneySegment, MilestoneCode.SHP_ASSIGNMENT_SCHEDULED);
        } else if (updatedPackageJourneySegment.getBookingStatus() == BookingStatus.CANCELLED) {
            vendorBookingPostProcessService.sendVendorBookingUpdateMilestone(shipments, updatedPackageJourneySegment, MilestoneCode.SHP_ASSIGNMENT_CANCELLED);
        }
    }


}
