package com.quincus.shipment.impl.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.ResponseCode;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.MilestoneUpdateRequest;
import com.quincus.shipment.api.dto.MilestoneUpdateResponse;
import com.quincus.shipment.api.exception.IllegalMilestoneException;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.web.common.exception.model.InvalidFieldTypeException;
import com.quincus.web.common.exception.model.MissingMandatoryFieldsException;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.logging.LoggingUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class VendorMilestoneService {

    private static final String ERROR_INVALID_MILESTONE_FROM_APIG = "Invalid milestone message received from APIG. JourneyId: {}, Milestone: {}";
    private static final String JOURNEY_ID = "journeyId";
    private static final String ORGANIZATION_ID = "organizationId";
    private static final String SEGMENT_ID = "segmentId";
    private static final String SHIPMENT_ID = "shipmentId";

    private MilestoneService milestoneService;
    private ShipmentRepository shipmentRepository;
    private PackageJourneySegmentRepository segmentRepository;
    private UserDetailsProvider userDetailsProvider;
    private AlertService alertService;
    private MilestonePostProcessService milestonePostProcessService;
    private ObjectMapper objectMapper;

    @Transactional
    public MilestoneUpdateResponse receiveMilestoneUpdateFromAPIG(final String journeyId, final MilestoneUpdateRequest request) {
        try {
            milestoneService.validate(request);
            final Milestone milestone = milestoneService.createMilestoneFromAPIG(request);
            final List<ShipmentEntity> shipmentEntityList = shipmentRepository.findByJourneyId(journeyId);

            validateJourneyIdAndShipmentIds(milestone, journeyId, shipmentEntityList, request.getShipmentIds());
            validateSegmentId(milestone, journeyId);
            processAPIGMilestone(shipmentEntityList, milestone);

            return milestoneService.createMilestoneUpdateResponse(request, ResponseCode.SCC0000, "");
        } catch (MissingMandatoryFieldsException e) {
            final MilestoneUpdateResponse response = milestoneService.createMilestoneUpdateResponse(request, ResponseCode.ERR0003, e.getMessage());
            throw new MissingMandatoryFieldsException(response, e.getMessage(), e);
        } catch (InvalidFieldTypeException e) {
            final MilestoneUpdateResponse response = milestoneService.createMilestoneUpdateResponse(request, ResponseCode.ERR0002, e.getMessage());
            throw new InvalidFieldTypeException(response, e.getMessage(), e);
        } catch (Exception e) {
            final MilestoneUpdateResponse response = milestoneService.createMilestoneUpdateResponse(request, ResponseCode.ERR9999, e.getMessage());
            throw new QuincusException(response, e.getMessage(), e);
        }
    }

    private void validateJourneyIdAndShipmentIds(final Milestone milestone, final String journeyId, final List<ShipmentEntity> shipmentEntityList, final List<String> shipmentIds) {
        final String organizationId = milestone.getOrganizationId();

        if (shipmentEntityList.isEmpty()) {
            log.error(ERROR_INVALID_MILESTONE_FROM_APIG, journeyId, milestone);
            throw new IllegalMilestoneException(
                    Map.of(
                            JOURNEY_ID, journeyId,
                            ORGANIZATION_ID, organizationId
                    ),
                    null
            );
        } else {
            final List<String> shipmentIdList = shipmentEntityList.stream().map(BaseEntity::getId).toList();
            shipmentIds.forEach(shpId -> {
                if (!shipmentIdList.contains(shpId)) {
                    log.error(ERROR_INVALID_MILESTONE_FROM_APIG, journeyId, milestone);
                    throw new IllegalMilestoneException(
                            Map.of(
                                    JOURNEY_ID, journeyId,
                                    ORGANIZATION_ID, organizationId,
                                    SHIPMENT_ID, shpId
                            ),
                            null
                    );
                }
            });
        }
    }

    private void validateSegmentId(final Milestone milestone, final String journeyId) {
        final String organizationId = userDetailsProvider.getCurrentOrganizationId();
        final Optional<PackageJourneySegmentEntity> optSegment = segmentRepository.findByIdAndOrganizationId(milestone.getSegmentId(), organizationId);

        if (optSegment.isEmpty()) {
            log.error(ERROR_INVALID_MILESTONE_FROM_APIG, journeyId, milestone);
            throw new IllegalMilestoneException(
                    Map.of(
                            SEGMENT_ID, milestone.getSegmentId(),
                            ORGANIZATION_ID, organizationId
                    ),
                    null
            );
        }
    }

    private void processAPIGMilestone(final List<ShipmentEntity> shipmentEntityList, final Milestone milestone) {
        shipmentEntityList.forEach(shipmentEntity -> {
            milestone.setShipmentId(shipmentEntity.getId());
            createAndProcessMilestone(milestone);

            Shipment shipment = ShipmentMapper.mapEntityToDomain(shipmentEntity, objectMapper);
            milestonePostProcessService.createAndSendShipmentMilestone(milestone, shipment);
            milestonePostProcessService.createAndSendAPIGWebhooks(milestone, shipment);
            milestonePostProcessService.createAndSendSegmentDispatch(milestone, shipment);
            milestonePostProcessService.createAndSendQShipSegment(milestone, shipment);
            milestonePostProcessService.createAndSendNotification(milestone, shipment);
        });
    }

    private void createAndProcessMilestone(final Milestone milestone) {
        final OffsetDateTime previousMilestoneTime = milestoneService.getMostRecentMilestoneTimeByShipmentId(milestone.getShipmentId());
        milestoneService.createMilestone(milestone);

        if (milestoneService.isNewMilestoneAfterMostRecentMilestone(milestone.getMilestoneTime(), previousMilestoneTime)
                && milestoneService.isFailedStatusCode(milestone.getMilestoneCode())) {
            alertService.createPickupDeliveryFailedAlert(milestone.getShipmentId());
        }

        if (!milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestone)) {
            milestone.setSegmentUpdatedFromMilestone(false);
            return;
        }

        milestoneService.updateMilestoneAndPackageJourneySegment(milestone, LoggingUtil.getTransactionId(), previousMilestoneTime);
    }
}
