package com.quincus.shipment.impl.attachment.packagejourneyairsegment;

import com.quincus.qportal.api.QPortalApi;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Component
public class PackageJourneyAirSegmentCsvValidator {
    public static final String SHIPMENT_ID_ERROR_MESSAGE = "Shipment ID must not be blank.";
    public static final String AIRLINE_CODE_ERROR_MESSAGE = "Airline Code must not be blank.";
    public static final String FLIGHT_NUMBER_ERROR_MESSAGE = "Flight Number must not be blank.";
    public static final String EMPTY_DATE_ERROR_MESSAGE = "%s Date must not be blank.";
    public static final String DATE_FORMAT_ERROR_MESSAGE = "Invalid %s Date format. (e.g. 2023-05-10T08:00:00).";
    public static final String TIMEZONE_FORMAT_ERROR_MESSAGE = "Invalid %s Timezone format. (e.g. UTC+08:00).";
    public static final String INSTRUCTION_ERROR_MESSAGE = "Max 4000 characters allowed for segment note. Please reduce.";
    public static final String AIRWAY_BILL_ERROR_MESSAGE = "Airway bill does not have a checksum validation.";
    public static final String VENDOR_ERROR_MESSAGE = "Vendor %s does not exist. Input a valid vendor";
    public static final String TIMEZONE_FORMAT = "UTC[+-]\\d{2}:\\d{2}";
    public static final Pattern TIMEZONE_PATTERN = Pattern.compile(TIMEZONE_FORMAT);
    public static final String DEPARTURE = "Departure";
    public static final String LOCKOUT = "Lockout";
    public static final String ARRIVAL = "Arrival";
    public static final String RECOVERY = "Recovery";
    private static final int MAXIMUM_INSTRUCTION_LENGTH = 4000;
    private static final String DATE_PATTERN_ACCEPTED = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}";
    private static final String AIRWAY_BILL_FORMAT = "\\b(\\d{3}-?)(\\d{7})(\\d)\\b";
    private static final Pattern AIRWAY_BILL_PATTERN = Pattern.compile(AIRWAY_BILL_FORMAT);
    public final QPortalApi qPortalApi;

    public boolean isValid(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv) {
        if (StringUtils.isBlank(packageJourneyAirSegmentCsv.getShipmentId())) {
            packageJourneyAirSegmentCsv.addErrorMessage(SHIPMENT_ID_ERROR_MESSAGE);
        }
        if (StringUtils.isBlank(packageJourneyAirSegmentCsv.getAirlineCode())) {
            packageJourneyAirSegmentCsv.addErrorMessage(AIRLINE_CODE_ERROR_MESSAGE);
        }
        if (StringUtils.isBlank(packageJourneyAirSegmentCsv.getFlightNumber())) {
            packageJourneyAirSegmentCsv.addErrorMessage(FLIGHT_NUMBER_ERROR_MESSAGE);
        }
        validateVendorAndSupplyExternalId(packageJourneyAirSegmentCsv);
        validateInstruction(packageJourneyAirSegmentCsv);
        validateAirwayBill(packageJourneyAirSegmentCsv);
        validateDepartureDateTime(packageJourneyAirSegmentCsv);
        validateLockoutDateTime(packageJourneyAirSegmentCsv);
        validateArrivalDateTime(packageJourneyAirSegmentCsv);
        validateRecoveryDateTime(packageJourneyAirSegmentCsv);

        return CollectionUtils.isEmpty(packageJourneyAirSegmentCsv.getErrorMessages());
    }

    private void validateVendorAndSupplyExternalId(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv) {
        String vendor = packageJourneyAirSegmentCsv.getVendor();
        if (StringUtils.isNotBlank(vendor)) {
            try {
                QPortalPartner qPortalPartner = qPortalApi.getPartnerByName(packageJourneyAirSegmentCsv.getOrganizationId(), vendor);
                if (qPortalPartner == null) {
                    packageJourneyAirSegmentCsv.addErrorMessage(String.format(VENDOR_ERROR_MESSAGE, vendor));
                    return;
                }
                packageJourneyAirSegmentCsv.setVendorId(qPortalPartner.getId());
            } catch (Exception e) {
                packageJourneyAirSegmentCsv.addErrorMessage("Error validating vendor");
            }
        }
    }

    private void validateInstruction(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv) {
        String instruction = packageJourneyAirSegmentCsv.getInstructionContent();
        if (StringUtils.isNotBlank(instruction) && instruction.length() > MAXIMUM_INSTRUCTION_LENGTH) {
            packageJourneyAirSegmentCsv.addErrorMessage(INSTRUCTION_ERROR_MESSAGE);
        }
    }

    private void validateAirwayBill(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv) {
        String airwayBill = packageJourneyAirSegmentCsv.getAirWayBill();
        if (StringUtils.isBlank(airwayBill)) return;
        Matcher matcher = AIRWAY_BILL_PATTERN.matcher(airwayBill.trim());
        if (!matcher.matches() || Integer.parseInt(matcher.group(2)) % 7 != Integer.parseInt(matcher.group(3))) {
            packageJourneyAirSegmentCsv.addErrorMessage(AIRWAY_BILL_ERROR_MESSAGE);
        }
    }

    private void validateDepartureDateTime(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv) {
        String departureDatetime = packageJourneyAirSegmentCsv.getDepartureDatetime();
        if (StringUtils.isBlank(departureDatetime)) {
            packageJourneyAirSegmentCsv.addErrorMessage(String.format(EMPTY_DATE_ERROR_MESSAGE, DEPARTURE));
        }
        validateDateTime(packageJourneyAirSegmentCsv, departureDatetime, packageJourneyAirSegmentCsv.getDepartureTimezone(), DEPARTURE);
    }

    private void validateLockoutDateTime(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv) {
        validateDateTime(packageJourneyAirSegmentCsv, packageJourneyAirSegmentCsv.getLockoutDatetime(),
                packageJourneyAirSegmentCsv.getLockoutTimezone(), LOCKOUT);
    }

    private void validateArrivalDateTime(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv) {
        validateDateTime(packageJourneyAirSegmentCsv, packageJourneyAirSegmentCsv.getArrivalDatetime(),
                packageJourneyAirSegmentCsv.getArrivalTimezone(), ARRIVAL);
    }

    private void validateRecoveryDateTime(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv) {
        validateDateTime(packageJourneyAirSegmentCsv, packageJourneyAirSegmentCsv.getRecoveryDatetime(),
                packageJourneyAirSegmentCsv.getRecoveryTimezone(), RECOVERY);
    }

    private void validateDateTime(PackageJourneyAirSegmentCsv packageJourneyAirSegmentCsv, String dateTime, String timezone, String message) {
        if (StringUtils.isNotBlank(dateTime) && !isValidDDate(dateTime)) {
            packageJourneyAirSegmentCsv.addErrorMessage(String.format(DATE_FORMAT_ERROR_MESSAGE, message));
        }
        if (!isValidTimezone(timezone)) {
            packageJourneyAirSegmentCsv.addErrorMessage(String.format(TIMEZONE_FORMAT_ERROR_MESSAGE, message));
        }
    }

    private boolean isValidDDate(String date) {
        try {
            return Pattern.matches(DATE_PATTERN_ACCEPTED, date);
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }

    private boolean isValidTimezone(String timezone) {
        if (StringUtils.isBlank(timezone)) return true;
        Matcher matcher = TIMEZONE_PATTERN.matcher(timezone);
        return matcher.matches();
    }
}