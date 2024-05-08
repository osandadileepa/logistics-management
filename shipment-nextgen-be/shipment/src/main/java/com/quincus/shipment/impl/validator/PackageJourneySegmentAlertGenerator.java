package com.quincus.shipment.impl.validator;

import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.AlertMessage;
import com.quincus.shipment.api.constant.AlertType;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Alert;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.helper.FieldUtil;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity_;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Component
@AllArgsConstructor
public class PackageJourneySegmentAlertGenerator {
    private static final String START_FACILITY = "start_facility";
    private static final String END_FACILITY = "end_facility";
    private static final String MAWB_PATTERN = "\\b(\\d{3}-?)(\\d{7})(\\d)\\b";
    private static final Pattern PATTERN = Pattern.compile(MAWB_PATTERN);
    private static final String SEGMENT_NO = "[Segment %s]";

    public void generateAlertPackageJourneySegments(ShipmentJourney shipmentJourney, boolean isShipmentJourneyUpdate) {
        List<PackageJourneySegment> packageJourneySegments = shipmentJourney.getPackageJourneySegments();
        int segmentsSize = packageJourneySegments.size();
        List<String> missingMandatoryFieldsSegments = new ArrayList<>();
        List<String> overlapInTimeSegments = new ArrayList<>();
        List<String> invalidWaybillSegments = new ArrayList<>();
        List<String> mismatchLocationInSegments = new ArrayList<>();

        IntStream.range(0, segmentsSize).forEach(
                index -> {
                    PackageJourneySegment segment = packageJourneySegments.get(index);

                    List<Alert> alerts = new ArrayList<>();
                    int segmentNumber = index + 1;

                    validateMandatoryFieldsAndAddAlert(segment, segmentsSize, alerts, missingMandatoryFieldsSegments, segmentNumber, isShipmentJourneyUpdate);

                    if (segmentsSize > 1 && index != 0) {
                        PackageJourneySegment previousSegment = packageJourneySegments.get(index - 1);
                        validateOverlappingSegmentsAndAddAlert(segment, previousSegment, alerts, overlapInTimeSegments, mismatchLocationInSegments, segmentNumber);
                    }

                    if (segment.getTransportType() == TransportType.AIR
                            && StringUtils.isNotBlank(segment.getMasterWaybill())) {
                        validateMasterAWBAndAddAlert(segment.getMasterWaybill(), alerts, invalidWaybillSegments, segmentNumber);
                    }
                    if (!CollectionUtils.isEmpty(alerts)) {
                        segment.setAlerts(alerts);
                    }
                }
        );

        setJourneyAlerts(shipmentJourney, missingMandatoryFieldsSegments, overlapInTimeSegments, invalidWaybillSegments, mismatchLocationInSegments);
    }

    private void setJourneyAlerts(ShipmentJourney shipmentJourney, List<String> missingMandatoryFieldsSegments,
                                  List<String> overlapInTimeSegments, List<String> invalidWaybillSegments, List<String> mismatchLocationInSegments) {
        List<Alert> journeyAlerts = shipmentJourney.getAlerts();

        if (CollectionUtils.isEmpty(journeyAlerts)) {
            journeyAlerts = new ArrayList<>();
        } else {
            journeyAlerts.clear();
        }

        if (!CollectionUtils.isEmpty(missingMandatoryFieldsSegments)) {
            journeyAlerts.add(new Alert(AlertMessage.MISSING_MANDATORY_FIELDS, missingMandatoryFieldsSegments, AlertType.ERROR));
        }
        if (!CollectionUtils.isEmpty(overlapInTimeSegments)) {
            journeyAlerts.add(new Alert(AlertMessage.TIME_OVERLAP_ACROSS_SEGMENTS, overlapInTimeSegments, AlertType.WARNING));
        }
        if (!CollectionUtils.isEmpty(invalidWaybillSegments)) {
            journeyAlerts.add(new Alert(AlertMessage.MAWB_NO_CHECKSUM_VALIDATION, invalidWaybillSegments, AlertType.WARNING));
        }
        if (!CollectionUtils.isEmpty(mismatchLocationInSegments)) {
            journeyAlerts.add(new Alert(AlertMessage.JOURNEY_SEGMENT_LOCATIONS_MISMATCH, mismatchLocationInSegments, AlertType.ERROR));
        }
        shipmentJourney.setAlerts(journeyAlerts);
    }

    private void validateMandatoryFieldsAndAddAlert(PackageJourneySegment packageJourneySegment, int segmentsSize,
                                                    List<Alert> alerts, List<String> missingMandatoryFieldsSegments,
                                                    int segmentNumber, boolean isShipmentJourneyUpdate) {
        SegmentType segmentType = packageJourneySegment.getType();
        List<String> missingMandatoryFields = new ArrayList<>();

        if (StringUtils.isBlank(packageJourneySegment.getOpsType()) && !isShipmentJourneyUpdate) {
            missingMandatoryFields.add(FieldUtil.camelToSnake(PackageJourneySegmentEntity_.OPS_TYPE));
        }

        if (packageJourneySegment.getTransportType() == TransportType.GROUND) {
            validateGroundSegmentMandatoryFields(packageJourneySegment, missingMandatoryFields, isShipmentJourneyUpdate);
        }

        if (segmentsSize > 1) {
            validateFacilities(segmentType, packageJourneySegment, missingMandatoryFields);
        } else {
            validateStartFacility(packageJourneySegment, missingMandatoryFields);
            validateEndFacility(packageJourneySegment, missingMandatoryFields);
        }

        if (packageJourneySegment.getTransportType() == TransportType.AIR) {
            validateAirSegmentMandatoryFields(packageJourneySegment, missingMandatoryFields);
        }

        if (packageJourneySegment.getPartner() == null && !isShipmentJourneyUpdate) {
            missingMandatoryFields.add(PackageJourneySegmentEntity_.PARTNER);
        }

        if (!CollectionUtils.isEmpty(missingMandatoryFields)) {
            missingMandatoryFieldsSegments.add(String.format(SEGMENT_NO, segmentNumber));
            alerts.add(new Alert(AlertMessage.MISSING_MANDATORY_FIELDS, AlertType.ERROR, missingMandatoryFields));
        }
    }

    private void validateGroundSegmentMandatoryFields(PackageJourneySegment packageJourneySegment, List<String> missingMandatoryFields, boolean isShipmentJourneyUpdate) {
        if (StringUtils.isBlank(packageJourneySegment.getPickUpTime()) && !isShipmentJourneyUpdate) {
            missingMandatoryFields.add(FieldUtil.camelToSnake(PackageJourneySegmentEntity_.PICK_UP_TIME));
        }
        if (StringUtils.isBlank(packageJourneySegment.getDropOffTime()) && !isShipmentJourneyUpdate) {
            missingMandatoryFields.add(FieldUtil.camelToSnake(PackageJourneySegmentEntity_.DROP_OFF_TIME));
        }
    }

    private void validateFacilities(SegmentType segmentType, PackageJourneySegment packageJourneySegment,
                                    List<String> missingFields) {
        if (segmentType == SegmentType.FIRST_MILE) {
            validateEndFacility(packageJourneySegment, missingFields);
        } else if (segmentType == SegmentType.LAST_MILE) {
            validateStartFacility(packageJourneySegment, missingFields);
        } else if (segmentType == SegmentType.MIDDLE_MILE) {
            validateStartFacility(packageJourneySegment, missingFields);
            validateEndFacility(packageJourneySegment, missingFields);
        }
    }

    private void validateStartFacility(PackageJourneySegment packageJourneySegment, List<String> missingFields) {
        if (packageJourneySegment.getStartFacility() == null) {
            missingFields.add(START_FACILITY);
        }
    }

    private void validateEndFacility(PackageJourneySegment packageJourneySegment, List<String> missingFields) {
        if (packageJourneySegment.getEndFacility() == null) {
            missingFields.add(END_FACILITY);
        }
    }

    private void validateOverlappingSegmentsAndAddAlert(PackageJourneySegment segment, PackageJourneySegment previousSegment,
                                                        List<Alert> alerts, List<String> overlapInTimeSegments,
                                                        List<String> mismatchLocationInSegments, int segmentNumber) {
        TransportType currentSegmentTransportType = segment.getTransportType();
        TransportType previousSegmentTransportType = previousSegment.getTransportType();
        String pickUpOrLockOutTime = "";
        String dropOffOrRecoveryTime = "";

        List<String> invalidFields = new ArrayList<>();

        if (currentSegmentTransportType == TransportType.GROUND) {
            pickUpOrLockOutTime = segment.getPickUpTime();
            invalidFields.add(FieldUtil.camelToSnake(PackageJourneySegmentEntity_.PICK_UP_TIME));
        } else if (currentSegmentTransportType == TransportType.AIR) {
            pickUpOrLockOutTime = segment.getLockOutTime();
            invalidFields.add(FieldUtil.camelToSnake(PackageJourneySegmentEntity_.LOCK_OUT_TIME));
        }

        if (previousSegmentTransportType == TransportType.GROUND) {
            dropOffOrRecoveryTime = previousSegment.getDropOffTime();
            invalidFields.add(FieldUtil.camelToSnake(PackageJourneySegmentEntity_.DROP_OFF_TIME));
        } else if (previousSegmentTransportType == TransportType.AIR) {
            dropOffOrRecoveryTime = previousSegment.getRecoveryTime();
            invalidFields.add(FieldUtil.camelToSnake(PackageJourneySegmentEntity_.RECOVERY_TIME));
        }

        if (StringUtils.isNotEmpty(pickUpOrLockOutTime) && StringUtils.isNotEmpty(dropOffOrRecoveryTime)) {
            ZonedDateTime currentPickUpOrLockOutDateTime = DateTimeUtil.parseZonedDateTime(pickUpOrLockOutTime);
            ZonedDateTime previousDropOffOrRecoveryDateTime = DateTimeUtil.parseZonedDateTime(dropOffOrRecoveryTime);
            if (currentPickUpOrLockOutDateTime != null
                    && (currentPickUpOrLockOutDateTime.isEqual(previousDropOffOrRecoveryDateTime)
                    || currentPickUpOrLockOutDateTime.isBefore(previousDropOffOrRecoveryDateTime))) {
                alerts.add(new Alert(AlertMessage.TIME_OVERLAP_ACROSS_SEGMENTS, AlertType.WARNING, invalidFields));
                overlapInTimeSegments.add(String.format(SEGMENT_NO, segmentNumber));
            }
        }

        if (previousSegment.getEndFacility() != null && segment.getStartFacility() != null &&
                !StringUtils.equals(previousSegment.getEndFacility().getExternalId(), segment.getStartFacility().getExternalId())) {
            alerts.add(new Alert(AlertMessage.JOURNEY_SEGMENT_LOCATIONS_MISMATCH, AlertType.ERROR, List.of(START_FACILITY)));
            mismatchLocationInSegments.add(String.format(SEGMENT_NO, segmentNumber));
        }
    }

    private void validateMasterAWBAndAddAlert(String masterAWB, List<Alert> alerts, List<String> invalidWaybillSegments, int segmentNumber) {
        Matcher matcher = PATTERN.matcher(masterAWB.trim());
        if (!matcher.matches() || Integer.parseInt(matcher.group(2)) % 7 != Integer.parseInt(matcher.group(3))) {
            alerts.add(new Alert(AlertMessage.MAWB_NO_CHECKSUM_VALIDATION, AlertType.WARNING, List.of(FieldUtil.camelToSnake(PackageJourneySegmentEntity_.MASTER_WAYBILL))));
            invalidWaybillSegments.add(String.format(SEGMENT_NO, segmentNumber));
        }
    }

    private void validateAirSegmentMandatoryFields(PackageJourneySegment packageJourneySegment, List<String> missingMandatoryFields) {
        if (StringUtils.isEmpty(packageJourneySegment.getFlightNumber())) {
            missingMandatoryFields.add(FieldUtil.camelToSnake(PackageJourneySegmentEntity_.FLIGHT_NUMBER));
        }
        if (StringUtils.isEmpty(packageJourneySegment.getAirlineCode())) {
            missingMandatoryFields.add(FieldUtil.camelToSnake(PackageJourneySegmentEntity_.AIRLINE_CODE));
        }
        if (StringUtils.isBlank(packageJourneySegment.getDepartureTime())) {
            missingMandatoryFields.add(FieldUtil.camelToSnake(PackageJourneySegmentEntity_.DEPARTURE_TIME));
        }
    }
}
