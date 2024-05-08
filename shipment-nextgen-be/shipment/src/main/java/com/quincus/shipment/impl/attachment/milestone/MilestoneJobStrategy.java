package com.quincus.shipment.impl.attachment.milestone;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.api.QPortalUtils;
import com.quincus.qportal.model.QPortalDriver;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalMilestone;
import com.quincus.qportal.model.QPortalVehicle;
import com.quincus.shipment.api.MilestonePostProcessApi;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.csv.MilestoneCsv;
import com.quincus.shipment.api.exception.JobRecordExecutionException;
import com.quincus.shipment.impl.attachment.JobTemplateStrategy;
import com.quincus.shipment.impl.helper.MilestoneHubLocationHandler;
import com.quincus.shipment.impl.helper.MilestoneTimezoneHelper;
import com.quincus.shipment.impl.helper.ShipmentUtil;
import com.quincus.shipment.impl.mapper.MilestoneMapper;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.service.JobMetricsService;
import com.quincus.shipment.impl.service.MilestoneService;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import com.quincus.shipment.impl.service.ShipmentService;
import com.quincus.shipment.impl.validator.MilestoneCsvValidator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.quincus.shipment.impl.validator.MilestoneCsvValidator.ERROR_MESSAGE_DELIMITER;

@Component
@Slf4j
public class MilestoneJobStrategy extends JobTemplateStrategy<MilestoneCsv> {
    static final String ERR_SHIPMENT_NOT_FOUND = "Milestone entry refers to a shipment which does not exist or has no active segment.";
    private static final String ERR_LINE_MESSAGE_FMT = "Line %d: %s";
    private final ShipmentService shipmentService;
    private final PackageJourneySegmentService segmentService;
    private final MilestoneService milestoneService;
    private final MilestoneTimezoneHelper milestoneTimezoneHelper;
    private final MilestoneCsvValidator validator;
    private final MilestoneMapper milestoneMapper;
    private final QPortalApi qPortalApi;
    private final MilestonePostProcessApi milestonePostProcessApi;
    private final MilestoneHubLocationHandler milestoneHubLocationHandler;

    MilestoneJobStrategy(JobMetricsService<MilestoneCsv> jobMetricsService, ShipmentService shipmentService,
                         PackageJourneySegmentService segmentService, MilestoneService milestoneService,
                         MilestoneCsvValidator validator, MilestoneMapper milestoneMapper, QPortalApi qPortalApi,
                         MilestonePostProcessApi milestonePostProcessApi, MilestoneHubLocationHandler milestoneHubLocationHandler,
                         MilestoneTimezoneHelper milestoneTimezoneHelper) {
        super(jobMetricsService);
        this.shipmentService = shipmentService;
        this.segmentService = segmentService;
        this.milestoneService = milestoneService;
        this.validator = validator;
        this.milestoneMapper = milestoneMapper;
        this.qPortalApi = qPortalApi;
        this.milestonePostProcessApi = milestonePostProcessApi;
        this.milestoneHubLocationHandler = milestoneHubLocationHandler;
        this.milestoneTimezoneHelper = milestoneTimezoneHelper;
    }

    @Override
    @Transactional(noRollbackFor = JobRecordExecutionException.class)
    public void execute(MilestoneCsv data) throws JobRecordExecutionException {
        validateDataCount(data);
        validateRequiredFields(data);
        validateConditionalOrCombinationFields(data);

        String organizationId = data.getOrganizationId();
        String userId = data.getUserId();
        List<QPortalLocation> refLocationList = qPortalApi.listLocations(organizationId);
        List<QPortalDriver> refDriverList = qPortalApi.listDrivers(organizationId);
        List<QPortalVehicle> refVehicleList = qPortalApi.listVehicles(organizationId);
        List<QPortalMilestone> refMilestoneList = qPortalApi.listMilestones(organizationId);

        validateQPortalFields(data, refLocationList, refDriverList, refVehicleList);

        Milestone milestone = milestoneMapper.toDomain(data);
        Optional.ofNullable(data.getFromCountry())
                .ifPresent(country -> milestone.setFromCountryId(QPortalUtils.lookupIdFromName(country, refLocationList)));
        Optional.ofNullable(data.getFromState())
                .ifPresent(state -> milestone.setFromStateId(QPortalUtils.lookupIdFromName(state, refLocationList)));
        Optional.ofNullable(data.getFromCity())
                .ifPresent(city -> milestone.setFromCityId(QPortalUtils.lookupIdFromName(city, refLocationList)));
        Optional.ofNullable(data.getFromFacility())
                .ifPresent(facility -> milestone.setFromLocationId(QPortalUtils.lookupIdFromName(facility, refLocationList)));

        Optional.ofNullable(data.getToCountry())
                .ifPresent(country -> milestone.setToCountryId(QPortalUtils.lookupIdFromName(country, refLocationList)));
        Optional.ofNullable(data.getToState())
                .ifPresent(state -> milestone.setToStateId(QPortalUtils.lookupIdFromName(state, refLocationList)));
        Optional.ofNullable(data.getToCity())
                .ifPresent(city -> milestone.setToCityId(QPortalUtils.lookupIdFromName(city, refLocationList)));
        Optional.ofNullable(data.getToFacility())
                .ifPresent(facility -> milestone.setToLocationId(QPortalUtils.lookupIdFromName(facility, refLocationList)));

        // use hub details in csv else use user location as hub_id
        Optional.ofNullable(data.getHub())
                .ifPresentOrElse(hub -> {
                            milestone.setHubId(QPortalUtils.lookupIdFromName(hub, refLocationList));
                            milestoneHubLocationHandler.enrichMilestoneHubIdWithLocationIds(milestone);
                        }
                        , () -> milestoneHubLocationHandler.configureMilestoneHubWithUserHubInfo(milestone));
        milestoneTimezoneHelper.supplyMilestoneTimezoneFromHubTimezone(milestone);
        Optional.ofNullable(data.getDriverName())
                .ifPresent(driver -> milestone.setDriverId(QPortalUtils.lookupIdFromName(driver, refDriverList)));
        Optional.ofNullable(data.getVehicleName())
                .ifPresent(vehicle -> milestone.setVehicleId(QPortalUtils.lookupIdFromName(vehicle, refVehicleList)));

        Shipment relatedShipment = shipmentService.findShipmentFromTrackingIdForMilestoneBatch(data.getShipmentTrackingId(), data.getOrganizationId());
        if ((relatedShipment == null) || (relatedShipment.getShipmentJourney() == null)
                || CollectionUtils.isEmpty(relatedShipment.getShipmentJourney().getPackageJourneySegments())) {
            setFailedReasonAndThrowException(data, ERR_SHIPMENT_NOT_FOUND);
        }

        PackageJourneySegment activeSegment = ShipmentUtil.getActiveSegment(relatedShipment);

        MilestoneEntity forUpsertMilestone;
        MilestoneEntity existingMilestone = milestoneService.findMilestoneFromShipmentAndSegment(milestone.getMilestoneCode(), relatedShipment, activeSegment);
        if (existingMilestone != null) {
            forUpsertMilestone = milestoneMapper.toEntity(existingMilestone, milestone);
        } else {
            milestone.setShipmentId(relatedShipment.getId());
            milestone.setOrganizationId(organizationId);
            milestone.setUserId(userId);
            milestone.setSegmentId(activeSegment.getSegmentId());
            enrichMilestoneWithQPortalInfo(milestone, refMilestoneList);

            forUpsertMilestone = milestoneMapper.toEntity(milestone);
        }
        milestoneTimezoneHelper.supplyEtaAndProofOfDeliveryTimezoneFromSegmentEndFacilityTimezone(milestone, activeSegment);
        OffsetDateTime mostRecentMilestone = milestoneService.getMostRecentMilestoneTimeByShipmentId(relatedShipment.getId());

        try {
            saveMilestone(forUpsertMilestone);
        } catch (Exception e) {
            String errorMessage = String.format("Exception encountered while attempting to persist milestone. Error message: %s",
                    e.getMessage());
            setFailedReasonAndThrowException(data, errorMessage);
        }

        PackageJourneySegment refSegment;
        if (!milestoneService.isAllShipmentFromOrderHaveSameMilestoneOrMilestoneCodePickupSuccessful(milestone.getMilestoneCode(),
                relatedShipment, activeSegment.getSegmentId())) {
            refSegment = activeSegment;
            milestone.setSegmentUpdatedFromMilestone(false);
        } else {
            refSegment = updateSegment(data, activeSegment, milestone, mostRecentMilestone);
        }

        sendMilestoneMessage(data, milestone, relatedShipment, refSegment);

        if (existingMilestone == null) {
            sendUpdatesToAPIG(data, milestone, relatedShipment, refSegment);
            sendUpdatesToDSP(data, milestone, relatedShipment, refSegment);
            sendUpdatesToQSHP(data, milestone, relatedShipment, refSegment);

            milestonePostProcessApi.createAndSendNotification(milestone, relatedShipment);
        }
    }

    private void validateDataCount(MilestoneCsv data) throws JobRecordExecutionException {
        List<String> validationErrors = new ArrayList<>();
        validator.validateFixedColumnSize(data, validationErrors);
        setFailedReasonAndThrowException(data, validationErrors);
    }

    private void validateRequiredFields(MilestoneCsv data) throws JobRecordExecutionException {
        List<String> validationErrors = new ArrayList<>();
        validator.validateDataAnnotations(data, validationErrors);
        setFailedReasonAndThrowException(data, validationErrors);
    }

    private void validateConditionalOrCombinationFields(MilestoneCsv data) throws JobRecordExecutionException {
        List<String> validationErrorMessages = new ArrayList<>();
        validator.validateMilestoneCode(data.getMilestoneCode(), validationErrorMessages);
        validator.validateDateTimeFormat(data.getMilestoneTime(), validationErrorMessages);
        validator.validateDateTimeFormat(data.getEta(), validationErrorMessages);
        validator.validateLocationCombinationAndCoordinates(data, validationErrorMessages);

        if (!validationErrorMessages.isEmpty()) {
            String errorMessage = String.join(ERROR_MESSAGE_DELIMITER, validationErrorMessages);
            setFailedReasonAndThrowException(data, errorMessage);
        }
    }

    private void validateQPortalFields(MilestoneCsv data, @NonNull List<QPortalLocation> refLocationList,
                                       @NonNull List<QPortalDriver> refDriverList,
                                       @NonNull List<QPortalVehicle> refVehicleList) throws JobRecordExecutionException {
        List<String> validationErrorMessages = new ArrayList<>();
        validator.validateQPortalLocationCombination(data, refLocationList, validationErrorMessages);
        validator.validateQPortalLocation(data.getHub(), refLocationList, validationErrorMessages);
        validator.validateQPortalDriver(data.getDriverName(), refDriverList, validationErrorMessages);
        validator.validateQPortalVehicle(data.getVehicleName(), refVehicleList, validationErrorMessages);

        if (!validationErrorMessages.isEmpty()) {
            String errorMessage = String.join(ERROR_MESSAGE_DELIMITER, validationErrorMessages);
            setFailedReasonAndThrowException(data, errorMessage);
        }
    }

    private void setFailedReasonAndThrowException(MilestoneCsv data, String errorMessage) {
        String lineErrorMessage = String.format(ERR_LINE_MESSAGE_FMT, data.getRecordNumber(), errorMessage);
        data.setFailedReason(lineErrorMessage);
        throw new JobRecordExecutionException(lineErrorMessage);
    }

    private void saveMilestone(MilestoneEntity milestoneEntity) {
        milestoneService.save(milestoneEntity);
    }

    private void setFailedReasonAndThrowException(MilestoneCsv data, List<String> errorMessages) {
        if (CollectionUtils.isEmpty(errorMessages)) {
            return;
        }

        String lineErrorMessage = String.format(ERR_LINE_MESSAGE_FMT, data.getRecordNumber(),
                String.join(ERROR_MESSAGE_DELIMITER, errorMessages));
        data.setFailedReason(lineErrorMessage);
        throw new JobRecordExecutionException(lineErrorMessage);
    }

    private void enrichMilestoneWithQPortalInfo(Milestone milestone, List<QPortalMilestone> qPortalRefMilestoneList) {
        QPortalMilestone refMilestone = lookupMilestoneCode(milestone.getMilestoneCode(), qPortalRefMilestoneList);
        if (refMilestone != null) {
            milestone.setMilestoneRefId(refMilestone.getId());
            if (milestone.getMilestoneName() == null) {
                milestone.setMilestoneName(refMilestone.getName());
            }
        }
    }

    private QPortalMilestone lookupMilestoneCode(MilestoneCode milestoneCode, List<QPortalMilestone> qPortalRefMilestoneList) {
        for (QPortalMilestone qPortalMilestone : qPortalRefMilestoneList) {
            if (qPortalMilestone.getCode().equals(milestoneCode.toString())) {
                return qPortalMilestone;
            }
        }
        return null;
    }

    private PackageJourneySegment updateSegment(MilestoneCsv data, PackageJourneySegment activeSegment,
                                                Milestone milestone, OffsetDateTime mostRecentMilestone) {
        PackageJourneySegmentEntity segmentEntity = segmentService.findBySegmentId(activeSegment.getSegmentId()).orElse(null);
        if (segmentEntity == null) {
            String errorMessage = "Data inconsistency - previously existing segment ID is not found.";
            log.warn(errorMessage);
            setFailedReasonAndThrowException(data, errorMessage);
        }

        boolean isSegmentUpdated = false;
        if ((mostRecentMilestone != null) && milestoneService.isNewMilestoneAfterMostRecentMilestone(milestone.getMilestoneTime(), mostRecentMilestone)) {
            isSegmentUpdated = segmentService.updateSegmentStatusByMilestone(segmentEntity, milestone);
        }
        isSegmentUpdated = isSegmentUpdated || segmentService.updateSegmentDriverAndVehicleFromMilestone(segmentEntity, milestone);
        isSegmentUpdated = isSegmentUpdated || segmentService.updateOnSiteTimeFromMilestone(segmentEntity, milestone);
        isSegmentUpdated = isSegmentUpdated || segmentService.updateActualTimeFromMilestone(segmentEntity, milestone);
        milestone.setSegmentUpdatedFromMilestone(isSegmentUpdated);
        if (isSegmentUpdated) {
            return segmentService.save(segmentEntity);
        }
        return activeSegment;
    }

    private void sendMilestoneMessage(MilestoneCsv data, Milestone milestone, Shipment shipment,
                                      PackageJourneySegment segment) {
        try {
            milestonePostProcessApi.createAndSendShipmentMilestone(milestone, shipment, segment, TriggeredFrom.SHP);
        } catch (Exception e) {
            String errorMessage = String.format("Exception encountered while sending shipment-milestone. Error message: %s",
                    e.getMessage());
            setFailedReasonAndThrowException(data, errorMessage);
        }
    }

    private void sendUpdatesToAPIG(MilestoneCsv data, Milestone milestone, Shipment shipment, PackageJourneySegment segment) {
        try {
            milestonePostProcessApi.createAndSendAPIGWebhooks(milestone, shipment, segment);
        } catch (Exception e) {
            String errorMessage = String.format("Exception encountered while sending milestone webhook to API-G. Error message: %s",
                    e.getMessage());
            setFailedReasonAndThrowException(data, errorMessage);
        }
    }

    private void sendUpdatesToDSP(MilestoneCsv data, Milestone milestone, Shipment shipment, PackageJourneySegment segment) {
        try {
            milestonePostProcessApi.createAndSendSegmentDispatch(milestone, shipment, segment);
        } catch (Exception e) {
            String errorMessage = String.format("Exception encountered while sending updated segment to DSP. Error message: %s",
                    e.getMessage());
            setFailedReasonAndThrowException(data, errorMessage);
        }
    }

    private void sendUpdatesToQSHP(MilestoneCsv data, Milestone milestone, Shipment shipment, PackageJourneySegment segment) {
        try {
            milestonePostProcessApi.createAndSendQShipSegment(milestone, shipment, segment);
        } catch (Exception e) {
            String errorMessage = String.format("Exception encountered while sending updated segment to QSHIP. Error message: %s",
                    e.getMessage());
            setFailedReasonAndThrowException(data, errorMessage);
        }
    }
}
