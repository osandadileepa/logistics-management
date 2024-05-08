package com.quincus.shipment.impl.attachment.networklane;

import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.dto.csv.NetworkLaneCsv;
import com.quincus.shipment.api.dto.csv.NetworkLaneSegmentCsv;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class NetworkLaneCsvValidator {

    private static final String MANDATORY_DESTINATION = "Destination Location Tree Level %s is Mandatory";
    private static final String MANDATORY_ORIGIN = "Origin Location Tree Level %s is Mandatory";
    private static final String SEGMENT_FIELD_MANDATORY = "Segment %d: `%s` is Mandatory";
    private static final String SEGMENT_VALUE_NOT_VALID = "Segment %d: `%s` is not a valid value for field `%s`";
    private static final String SEGMENT_DATE_FORMAT_NOT_VALID = "Segment %d: Invalid format for field `%s`. Format example: 2022-12-30T16:27:02+07:00";
    private static final String NO_NETWORK_LANE_SEGMENT_ERROR = "No Network Network Lane Connection found";
    private static final String MANDATORY_SERVICE_TYPE = "Service Type is Mandatory";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";

    public void validate(NetworkLaneCsv networkLaneCsv) {
        List<String> errorMessages = new ArrayList<>();
        if (StringUtils.isBlank(networkLaneCsv.getServiceType())) {
            errorMessages.add(MANDATORY_SERVICE_TYPE);
        }
        if (StringUtils.isBlank(networkLaneCsv.getDestinationLocationTreeLevel1())) {
            errorMessages.add(String.format(MANDATORY_DESTINATION, "1"));
        }
        if (StringUtils.isBlank(networkLaneCsv.getDestinationLocationTreeLevel2())) {
            errorMessages.add(String.format(MANDATORY_DESTINATION, "2"));
        }
        if (StringUtils.isBlank(networkLaneCsv.getDestinationLocationTreeLevel3())) {
            errorMessages.add(String.format(MANDATORY_DESTINATION, "3"));
        }
        if (StringUtils.isBlank(networkLaneCsv.getOriginLocationTreeLevel1())) {
            errorMessages.add(String.format(MANDATORY_ORIGIN, "1"));
        }
        if (StringUtils.isBlank(networkLaneCsv.getOriginLocationTreeLevel2())) {
            errorMessages.add(String.format(MANDATORY_ORIGIN, "2"));
        }
        if (StringUtils.isBlank(networkLaneCsv.getOriginLocationTreeLevel3())) {
            errorMessages.add(String.format(MANDATORY_ORIGIN, "3"));
        }

        validateNetworkLaneSegments(errorMessages, networkLaneCsv.getNetworkLaneSegments());

        if (CollectionUtils.isNotEmpty(errorMessages)) {
            String combinedErrorMessage = String.join(" | ", errorMessages);
            networkLaneCsv.setFailedReason(combinedErrorMessage);
            throw new QuincusValidationException(combinedErrorMessage);
        }
    }

    private void validateNetworkLaneSegments(List<String> errorMessages, List<NetworkLaneSegmentCsv> networkLaneSegmentCsvs) {
        if (CollectionUtils.isEmpty(networkLaneSegmentCsvs)) {
            errorMessages.add(String.format(NO_NETWORK_LANE_SEGMENT_ERROR));
            return;
        }
        IntStream.range(0, networkLaneSegmentCsvs.size()).forEach(index -> {
            int segmentLineNumber = index + 1;
            NetworkLaneSegmentCsv networkLaneSegmentCsv = networkLaneSegmentCsvs.get(index);
            if (isCsvLineSegmentIgnorable(networkLaneSegmentCsv)) {
                networkLaneSegmentCsv.setIgnoreRecord(true);
                return;
            }
            if (StringUtils.isBlank(networkLaneSegmentCsv.getPickupFacilityName()) && index != 0) {
                errorMessages.add(String.format(SEGMENT_FIELD_MANDATORY, segmentLineNumber, "Pickup Facility Name"));
            }
            if (StringUtils.isBlank(networkLaneSegmentCsv.getDropOffFacilityName()) && !isLastSegment(index, networkLaneSegmentCsvs.size(), networkLaneSegmentCsvs)) {
                errorMessages.add(String.format(SEGMENT_FIELD_MANDATORY, segmentLineNumber, "Drop Off Facility Name"));
            }
            if (StringUtils.isBlank(networkLaneSegmentCsv.getSequenceNumber())) {
                errorMessages.add(String.format(SEGMENT_FIELD_MANDATORY, segmentLineNumber, "Sequence Number"));
            }
            if (StringUtils.isBlank(networkLaneSegmentCsv.getPartnerName())) {
                errorMessages.add(String.format(SEGMENT_FIELD_MANDATORY, segmentLineNumber, "Partner Name"));
            }
            if (!isTransportTypeValue(networkLaneSegmentCsv.getTransportCategory())) {
                errorMessages.add(String.format(SEGMENT_VALUE_NOT_VALID, segmentLineNumber, networkLaneSegmentCsv.getDurationUnit(), "Duration Unit"));
            }
            validateUnitOfMeasurement(networkLaneSegmentCsv.getDurationUnit(), segmentLineNumber, "Duration Unit", errorMessages);
            validateUnitOfMeasurement(networkLaneSegmentCsv.getCalculatedMileageUnit(), segmentLineNumber, "Calculated Mileage Unit", errorMessages);
            validateDateTimeFormat(networkLaneSegmentCsv.getLockOutTime(), segmentLineNumber, "Lockout Time", errorMessages);
            validateDateTimeFormat(networkLaneSegmentCsv.getDepartureTime(), segmentLineNumber, "Departure Time", errorMessages);
            validateDateTimeFormat(networkLaneSegmentCsv.getArrivalTime(), segmentLineNumber, "Arrival Time", errorMessages);
            validateDateTimeFormat(networkLaneSegmentCsv.getRecoveryTime(), segmentLineNumber, "Recovery Time", errorMessages);
        });
    }

    private boolean isLastSegment(int currentIndex, int segmentSize, List<NetworkLaneSegmentCsv> networkLaneSegmentCsvs) {
        //To know if its last segment, either check current index is equal to size - 1
        // or next index of csv record is ignorable which means no data.
        // Ignorable records are due to handling of different segments size when reprocess is needed for error network lane import
        return currentIndex == (segmentSize - 1) || isCsvLineSegmentIgnorable(networkLaneSegmentCsvs.get(currentIndex + 1));
    }

    private boolean isCsvLineSegmentIgnorable(NetworkLaneSegmentCsv networkLaneSegmentCsv) {
        return Stream.of(networkLaneSegmentCsv.getSequenceNumber(), networkLaneSegmentCsv.getTransportCategory()
                        , networkLaneSegmentCsv.getPartnerName(), networkLaneSegmentCsv.getVehicleInfo()
                        , networkLaneSegmentCsv.getFlightNumber(), networkLaneSegmentCsv.getAirline()
                        , networkLaneSegmentCsv.getAirlineCode(), networkLaneSegmentCsv.getMasterWaybill()
                        , networkLaneSegmentCsv.getPickupFacilityName(), networkLaneSegmentCsv.getDropOffFacilityName()
                        , networkLaneSegmentCsv.getPickupInstruction(), networkLaneSegmentCsv.getDropOffInstruction()
                        , networkLaneSegmentCsv.getDuration(), networkLaneSegmentCsv.getDurationUnit()
                        , networkLaneSegmentCsv.getLockOutTime(), networkLaneSegmentCsv.getDepartureTime()
                        , networkLaneSegmentCsv.getArrivalTime(), networkLaneSegmentCsv.getRecoveryTime()
                        , networkLaneSegmentCsv.getCalculatedMileage(), networkLaneSegmentCsv.getCalculatedMileageUnit())
                .allMatch(StringUtils::isBlank);

    }

    private void validateUnitOfMeasurement(String value, int segmentLineNumber, String fieldName, List<String> errorMessages) {
        if (isUnitOfMeasurementInvalid(value)) {
            errorMessages.add(String.format(SEGMENT_VALUE_NOT_VALID, segmentLineNumber, value, fieldName));
        }
    }

    private void validateDateTimeFormat(String value, int segmentLineNumber, String fieldName, List<String> errorMessages) {
        if (isInvalidDateTimeFormat(value)) {
            errorMessages.add(String.format(SEGMENT_DATE_FORMAT_NOT_VALID, segmentLineNumber, fieldName));
        }
    }

    private boolean isTransportTypeValue(String transportCategory) {
        try {
            if (StringUtils.isBlank(transportCategory)) {
                return true;
            }
            Enum.valueOf(TransportType.class, transportCategory);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private boolean isUnitOfMeasurementInvalid(String unitOfMeasurement) {
        try {
            if (StringUtils.isBlank(unitOfMeasurement)) {
                return false;
            }
            Enum.valueOf(UnitOfMeasure.class, unitOfMeasurement);
        } catch (IllegalArgumentException e) {
            return true;
        }
        return false;
    }

    private boolean isInvalidDateTimeFormat(String dateTimeString) {
        if (StringUtils.isBlank(dateTimeString)) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
            OffsetDateTime.parse(dateTimeString, formatter);
            return false;
        } catch (DateTimeParseException e) {
            return true;
        }
    }

}
