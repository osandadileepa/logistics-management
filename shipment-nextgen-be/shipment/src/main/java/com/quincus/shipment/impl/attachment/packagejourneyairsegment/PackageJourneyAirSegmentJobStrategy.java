package com.quincus.shipment.impl.attachment.packagejourneyairsegment;

import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.apigateway.domain.FlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv;
import com.quincus.shipment.api.exception.JobRecordExecutionException;
import com.quincus.shipment.impl.attachment.JobTemplateStrategy;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.service.FlightStatsEventService;
import com.quincus.shipment.impl.service.JobMetricsService;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import com.quincus.shipment.impl.service.PartnerService;
import com.quincus.shipment.impl.service.ShipmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class PackageJourneyAirSegmentJobStrategy extends JobTemplateStrategy<PackageJourneyAirSegmentCsv> {

    public static final String SHIPMENT_SEGMENTS_NOT_FOUND_ERROR_MESSAGE = "Shipment ID does not exist or does not have 'Planned' or 'In Progress' air segments to apply updates.";
    public static final String SEGMENT_UPDATE_ERROR_MESSAGE = "Failed to update the segment with ID `%s` for the Shipment ID `%s`";
    public static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred while processing the Shipment ID `%s`";
    private static final String FLIGHT_ERROR_MESSAGE = "The flight cannot be tracked as it is not found on Flightstats. Ensure airline code, flight number, and departure datetime is correct.";
    private final PackageJourneyAirSegmentCsvValidator packageJourneyAirSegmentCsvValidator;
    private final PackageJourneySegmentService packageJourneySegmentService;
    private final ShipmentService shipmentService;
    private final PartnerService partnerService;
    private final FlightStatsEventService flightStatsEventService;
    private final ApiGatewayApi gatewayApi;

    public PackageJourneyAirSegmentJobStrategy(final JobMetricsService<PackageJourneyAirSegmentCsv> jobMetricsService,
                                               final PackageJourneyAirSegmentCsvValidator packageJourneyAirSegmentCsvValidator,
                                               final PackageJourneySegmentService packageJourneySegmentService,
                                               final ShipmentService shipmentService, final PartnerService partnerService,
                                               final FlightStatsEventService flightStatsEventService,
                                               final ApiGatewayApi gatewayApi) {
        super(jobMetricsService);
        this.packageJourneyAirSegmentCsvValidator = packageJourneyAirSegmentCsvValidator;
        this.packageJourneySegmentService = packageJourneySegmentService;
        this.shipmentService = shipmentService;
        this.partnerService = partnerService;
        this.flightStatsEventService = flightStatsEventService;
        this.gatewayApi = gatewayApi;
    }

    @Override
    @Transactional(noRollbackFor = JobRecordExecutionException.class)
    public void execute(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv) throws JobRecordExecutionException {
        try {
            if (packageJourneyAirSegmentCsvValidator.isValid(packageJourneyAirSegmentCsv)) {
                String shipmentTrackingId = packageJourneyAirSegmentCsv.getShipmentId();
                String organizationId = packageJourneyAirSegmentCsv.getOrganizationId();

                List<PackageJourneySegment> earliestActiveAirSegments = getAirSegmentWithOriginOrEarliestActive(shipmentTrackingId, organizationId,
                        packageJourneyAirSegmentCsv.getOriginFacility(), packageJourneyAirSegmentCsv.getOriginCity(),
                        packageJourneyAirSegmentCsv.getOriginState(), packageJourneyAirSegmentCsv.getOriginCountry());

                if (CollectionUtils.isEmpty(earliestActiveAirSegments)) {
                    packageJourneyAirSegmentCsv.addErrorMessage(SHIPMENT_SEGMENTS_NOT_FOUND_ERROR_MESSAGE);
                } else {
                    updateEarliestActiveAirSegments(packageJourneyAirSegmentCsv, earliestActiveAirSegments);
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format(UNEXPECTED_ERROR_MESSAGE, packageJourneyAirSegmentCsv.getShipmentId());
            log.warn(errorMessage, e.getMessage(), e);
            packageJourneyAirSegmentCsv.addErrorMessage(errorMessage);
        } finally {
            handleFailedReason(packageJourneyAirSegmentCsv);
        }
    }

    private void updateEarliestActiveAirSegment(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv, PackageJourneySegment earliestActiveAirSegment) {
        Optional<PackageJourneySegmentEntity> earliestActiveAirSegmentEntityOptional = packageJourneySegmentService.findBySegmentId(earliestActiveAirSegment.getSegmentId());
        if (earliestActiveAirSegmentEntityOptional.isPresent()) {
            PackageJourneySegmentEntity earliestActiveAirSegmentEntityToUpdate = earliestActiveAirSegmentEntityOptional.get();
            try {
                String departureDatetime = packageJourneyAirSegmentCsv.getDepartureDatetime();
                if (!checkValidFlightAndSetAirline(packageJourneyAirSegmentCsv, earliestActiveAirSegmentEntityToUpdate, departureDatetime)) {
                    return;
                }

                Optional.ofNullable(packageJourneyAirSegmentCsv.getInstructionContent())
                        .filter(StringUtils::isNotBlank)
                        .ifPresent(earliestActiveAirSegmentEntityToUpdate::setInstruction);

                Optional.ofNullable(packageJourneyAirSegmentCsv.getAirWayBill())
                        .filter(StringUtils::isNotBlank)
                        .ifPresent(earliestActiveAirSegmentEntityToUpdate::setMasterWaybill);

                if (StringUtils.isNotBlank(packageJourneyAirSegmentCsv.getVendor()) && StringUtils.isNotBlank(packageJourneyAirSegmentCsv.getVendorId())) {
                    earliestActiveAirSegmentEntityToUpdate.setPartner(partnerService.findOrCreatePartner(packageJourneyAirSegmentCsv.getVendorId()));
                }

                updateDateTimeInformation(packageJourneyAirSegmentCsv, earliestActiveAirSegment, earliestActiveAirSegmentEntityToUpdate, departureDatetime);
                earliestActiveAirSegmentEntityToUpdate.setAirlineCode(packageJourneyAirSegmentCsv.getAirlineCode());
                earliestActiveAirSegmentEntityToUpdate.setFlightNumber(packageJourneyAirSegmentCsv.getFlightNumber());
                flightStatsEventService.subscribeFlight(List.of(earliestActiveAirSegmentEntityToUpdate));
                updateOrCreateAirSegmentInstruction(earliestActiveAirSegmentEntityToUpdate, packageJourneyAirSegmentCsv);
                packageJourneySegmentService.update(earliestActiveAirSegmentEntityToUpdate);

            } catch (Exception e) {
                String errorMessage = String.format(SEGMENT_UPDATE_ERROR_MESSAGE, earliestActiveAirSegmentEntityToUpdate.getId(), packageJourneyAirSegmentCsv.getShipmentId());
                log.warn(errorMessage, e);
                packageJourneyAirSegmentCsv.addErrorMessage(errorMessage);
            }
        }
    }

    private void updateDateTimeInformation(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv, PackageJourneySegment earliestActiveAirSegment, PackageJourneySegmentEntity earliestActiveAirSegmentEntityToUpdate, String departureDatetime) {
        if (StringUtils.isNotBlank(packageJourneyAirSegmentCsv.getLockoutDatetime())) {
            String lockoutTimezone = determineTimezone(packageJourneyAirSegmentCsv.getLockoutTimezone(), earliestActiveAirSegment.getDepartureTimezone());
            earliestActiveAirSegmentEntityToUpdate.setLockOutTime(
                    parseAirSegmentCsvDateAndTimezoneToStandardFormat(packageJourneyAirSegmentCsv.getLockoutDatetime(), lockoutTimezone)
            );
            earliestActiveAirSegmentEntityToUpdate.setLockOutTimezone(lockoutTimezone);
        }
        if (StringUtils.isNotBlank(departureDatetime)) {
            String departureTimezone = determineTimezone(packageJourneyAirSegmentCsv.getDepartureTimezone(), earliestActiveAirSegment.getDepartureTimezone());
            earliestActiveAirSegmentEntityToUpdate.setDepartureTime(
                    parseAirSegmentCsvDateAndTimezoneToStandardFormat(departureDatetime, departureTimezone)
            );
            earliestActiveAirSegmentEntityToUpdate.setDepartureTimezone(departureTimezone);
        }
        if (StringUtils.isNotBlank(packageJourneyAirSegmentCsv.getArrivalDatetime())) {
            String arrivalTimezone = determineTimezone(packageJourneyAirSegmentCsv.getArrivalTimezone(), earliestActiveAirSegment.getArrivalTimezone());
            earliestActiveAirSegmentEntityToUpdate.setArrivalTime(
                    parseAirSegmentCsvDateAndTimezoneToStandardFormat(packageJourneyAirSegmentCsv.getArrivalDatetime(), arrivalTimezone)
            );
            earliestActiveAirSegmentEntityToUpdate.setArrivalTimezone(arrivalTimezone);
        }
        if (StringUtils.isNotBlank(packageJourneyAirSegmentCsv.getRecoveryDatetime())) {
            String recoveryTimezone = determineTimezone(packageJourneyAirSegmentCsv.getRecoveryTimezone(), earliestActiveAirSegment.getArrivalTimezone());
            earliestActiveAirSegmentEntityToUpdate.setRecoveryTime(
                    parseAirSegmentCsvDateAndTimezoneToStandardFormat(packageJourneyAirSegmentCsv.getRecoveryDatetime(), recoveryTimezone)
            );
            earliestActiveAirSegmentEntityToUpdate.setRecoveryTimezone(recoveryTimezone);
        }
    }

    private boolean checkValidFlightAndSetAirline(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv,
                                                  PackageJourneySegmentEntity earliestActiveAirSegmentEntityToUpdate,
                                                  String departureDatetime) {
        if (StringUtils.isBlank(departureDatetime)) return false;
        LocalDateTime departureLocalDateTime = DateTimeUtil.convertStringToLocalDateTime(departureDatetime);
        if (departureLocalDateTime == null) return false;

        FlightScheduleSearchParameter parameter = buildFlightScheduleSearchParameter(packageJourneyAirSegmentCsv, earliestActiveAirSegmentEntityToUpdate, departureLocalDateTime);
        final List<FlightSchedule> flightScheduleList = gatewayApi.searchFlights(parameter);
        if (CollectionUtils.isEmpty(flightScheduleList)) {
            packageJourneyAirSegmentCsv.addErrorMessage(FLIGHT_ERROR_MESSAGE);
            return false;
        }
        earliestActiveAirSegmentEntityToUpdate.setAirline(flightScheduleList.get(0).getCarrierName());
        return true;
    }

    private FlightScheduleSearchParameter buildFlightScheduleSearchParameter(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv,
                                                                             PackageJourneySegmentEntity earliestActiveAirSegmentEntityToUpdate,
                                                                             LocalDateTime departureLocalDateTime) {
        FlightScheduleSearchParameter parameter = new FlightScheduleSearchParameter();
        parameter.setOrigin(earliestActiveAirSegmentEntityToUpdate.getFlightOrigin());
        parameter.setDestination(earliestActiveAirSegmentEntityToUpdate.getFlightDestination());
        parameter.setCarrier(packageJourneyAirSegmentCsv.getAirlineCode());
        parameter.setDepartureDate(departureLocalDateTime.toLocalDate());
        return parameter;
    }

    private String determineTimezone(String csvTimezoneInput, String defaultTimeZone) {
        return Optional.ofNullable(csvTimezoneInput).filter(StringUtils::isNotBlank).orElse(defaultTimeZone);
    }

    /**
     * @param datetime - sample expected - 2023-09-19T07:00:00
     * @param timezone - sample expected - UTC+08:00
     *
     * @return sample expected return - 2023-09-19T07:00:00+08:00
     */
    private String parseAirSegmentCsvDateAndTimezoneToStandardFormat(String datetime, String timezone) {
        if (StringUtils.isBlank(datetime) || StringUtils.isBlank(timezone)) {
            return null;
        }

        String parsedTimezone = timezone.replace("UTC", "");
        return datetime.concat(parsedTimezone);

    }

    private List<PackageJourneySegment> getAirSegmentWithOriginOrEarliestActive(String shipmentTrackingId, String organizationId, String facilityName, String cityName, String stateName, String countryName) {
        Map<String, List<PackageJourneySegment>> activeAirSegmentsMap = shipmentService.findActiveAirSegmentsMap(shipmentTrackingId, organizationId);
        if (CollectionUtils.isEmpty(activeAirSegmentsMap)) {
            return Collections.emptyList();
        }
        return activeAirSegmentsMap.values().stream()
                .flatMap(list -> list.stream().sorted(Comparator.comparing(PackageJourneySegment::getSequence))
                        .filter(segment ->
                                segment.getStartFacility().getName().equalsIgnoreCase(facilityName) ||
                                        segment.getStartFacility().getLocation().getCityName().equalsIgnoreCase(cityName) ||
                                        segment.getStartFacility().getLocation().getStateName().equalsIgnoreCase(stateName) ||
                                        segment.getStartFacility().getLocation().getCountryName().equalsIgnoreCase(countryName))
                        .limit(1)
                        .findFirst()
                        .or(() -> list.stream().filter(segment -> segment.getStatus() == SegmentStatus.PLANNED || segment.getStatus() == SegmentStatus.IN_PROGRESS)
                                .min(Comparator.comparing(PackageJourneySegment::getSequence)))
                        .stream()).toList();
    }

    private void updateEarliestActiveAirSegments(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv, List<PackageJourneySegment> earliestActiveAirSegments) {
        earliestActiveAirSegments.forEach(segment -> updateEarliestActiveAirSegment(packageJourneyAirSegmentCsv, segment));
    }

    private void handleFailedReason(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv) throws JobRecordExecutionException {
        packageJourneyAirSegmentCsv.buildFailedReason();
        if (StringUtils.isNotBlank(packageJourneyAirSegmentCsv.getFailedReason())) {
            throw new JobRecordExecutionException(packageJourneyAirSegmentCsv.getFailedReason());
        }
    }

    private void updateOrCreateAirSegmentInstruction(PackageJourneySegmentEntity airSegment,
                                                     PackageJourneyAirSegmentCsv airSegmentContext) {
        String segmentId = airSegment.getId();
        String organizationId = airSegmentContext.getOrganizationId();
        String instruction = airSegmentContext.getInstructionContent();

        List<InstructionEntity> instructionEntities = Optional.ofNullable(airSegment.getInstructions())
                .orElse(new ArrayList<>());

        InstructionEntity instructionEntity = instructionEntities.stream()
                .filter(this::isSegmentInstruction).findFirst()
                .map(i -> updateAirSegmentInstruction(i, instruction, airSegment))
                .orElse(createAirSegmentInstruction(segmentId, instruction, organizationId, airSegment));

        instructionEntities.removeIf(this::isSegmentInstruction);
        instructionEntities.add(instructionEntity);
        airSegment.setInstructions(instructionEntities);
    }

    private boolean isSegmentInstruction(InstructionEntity instructionEntity) {
        return InstructionApplyToType.SEGMENT == instructionEntity.getApplyTo();
    }

    private InstructionEntity updateAirSegmentInstruction(InstructionEntity instructionEntity,
                                                          String segmentInstruction,
                                                          PackageJourneySegmentEntity airSegment) {
        instructionEntity.setValue(segmentInstruction);
        instructionEntity.setLabel(getSegmentInstructionLabel(airSegment.getRefId()));
        instructionEntity.setUpdatedAt(Instant.now(Clock.systemUTC()).toString());
        return instructionEntity;
    }

    private InstructionEntity createAirSegmentInstruction(String segmentId, String segmentInstruction,
                                                          String organizationId,
                                                          PackageJourneySegmentEntity airSegment) {
        if (StringUtils.isBlank(segmentInstruction)) {
            return null;
        }

        Instant currentTime = Instant.now(Clock.systemUTC());
        InstructionEntity newInstruction = new InstructionEntity();
        newInstruction.setExternalId(segmentId);
        newInstruction.setOrganizationId(organizationId);
        newInstruction.setValue(segmentInstruction);
        newInstruction.setLabel(getSegmentInstructionLabel(airSegment.getRefId()));
        newInstruction.setSource(Instruction.SOURCE_SEGMENT);
        newInstruction.setApplyTo(InstructionApplyToType.SEGMENT);
        newInstruction.setCreatedAt(currentTime.toString());
        newInstruction.setUpdatedAt(currentTime.toString());
        return newInstruction;
    }

    private String getSegmentInstructionLabel(String refId) {
        return String.format("Segment %s instruction", refId);
    }
}