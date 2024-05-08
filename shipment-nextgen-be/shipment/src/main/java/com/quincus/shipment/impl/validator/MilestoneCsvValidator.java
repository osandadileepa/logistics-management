package com.quincus.shipment.impl.validator;

import com.quincus.qportal.api.QPortalUtils;
import com.quincus.qportal.model.QPortalDriver;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalVehicle;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.dto.csv.MilestoneCsv;
import com.quincus.shipment.api.exception.InvalidEnumValueException;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.quincus.shipment.api.dto.csv.MilestoneCsv.HEADER_MILESTONE_CODE;
import static com.quincus.shipment.api.dto.csv.MilestoneCsv.HEADER_MILESTONE_DATE_TIME;
import static com.quincus.shipment.api.dto.csv.MilestoneCsv.HEADER_SHIPMENT_TRACKING_ID;

@Component
@AllArgsConstructor
public class MilestoneCsvValidator {
    public static final String ERROR_MESSAGE_DELIMITER = " | ";
    public static final String ADDRESS_DELIMITER = ", ";
    public static final String DATA_ANNOTATION_ERROR = "Field `%s` %s";
    public static final String MALFORMED_COORDINATION_ERROR = "Invalid coordinates. Either both %s Latitude and %s Longitude should be provided, or neither should be present.";
    public static final String MALFORMED_LOCATION_ERROR = "Malformed Location.";
    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validatorFactory.getValidator();
    private static final Map<String, String> FIELD_NAME_HEADER_MAP = Map.of(
            ShipmentEntity_.SHIPMENT_TRACKING_ID, HEADER_SHIPMENT_TRACKING_ID,
            MilestoneEntity_.MILESTONE_CODE, HEADER_MILESTONE_CODE,
            MilestoneEntity_.MILESTONE_TIME, HEADER_MILESTONE_DATE_TIME
    );

    public void validateFixedColumnSize(MilestoneCsv data, List<String> errorMessages) {
        int allowedSize = MilestoneCsv.getCsvHeaders().length;
        if (data.getSize() != allowedSize) {
            String errorMessage = String.format("Malformed entry. Expected size = %d. Actual size = %d.",
                    allowedSize, data.getSize());
            errorMessages.add(errorMessage);
        }
    }

    public void validateDataAnnotations(MilestoneCsv data, List<String> errorMessages) {
        Set<ConstraintViolation<MilestoneCsv>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            errorMessages.addAll(violations.stream().map(v -> {
                String fieldName = FIELD_NAME_HEADER_MAP.get(v.getPropertyPath().toString());
                return String.format(DATA_ANNOTATION_ERROR, fieldName != null ? fieldName : v.getPropertyPath().toString(), v.getMessage());
            }).toList());
        }
    }

    public void validateMilestoneCode(@NonNull String milestoneCode, List<String> errorMessages) {
        try {
            MilestoneCode.fromValue(milestoneCode);
        } catch (InvalidEnumValueException e) {
            String errorMessage = String.format("Invalid milestone code `%s`", milestoneCode);
            errorMessages.add(errorMessage);
        }
    }

    public void validateDateTimeFormat(String dateTimeStr, List<String> errorMessages) {
        if (dateTimeStr != null) {
            try {
                OffsetDateTime.parse(dateTimeStr);
            } catch (DateTimeParseException e) {
                String errorMessage = String.format("Invalid milestone time `%s`", dateTimeStr);
                errorMessages.add(errorMessage);
            }
        }
    }

    public void validateLocationCombinationAndCoordinates(MilestoneCsv data, List<String> errorMessages) {
        validateCoordinates(data.getLatitude(), data.getLongitude(), "Current", errorMessages);
        validateCoordinates(data.getFromLatitude(), data.getFromLongitude(), "From", errorMessages);
        validateCoordinates(data.getToLatitude(), data.getToLongitude(), "To", errorMessages);

        validateFacilityCombination(data.getFromFacility(), data.getFromDistrict(), data.getFromWard(),
                data.getFromCity(), data.getFromState(), data.getFromCountry(), errorMessages);
        validateDistrictCombination(data.getFromDistrict(), data.getFromWard(), data.getFromCity(), data.getFromState(),
                data.getFromCountry(), errorMessages);
        validateCityCombination(data.getFromCity(), data.getFromState(), data.getFromCountry(), errorMessages);
        validateLocationBaseCombination(data.getFromState(), data.getFromCountry(), errorMessages);

        validateFacilityCombination(data.getToFacility(), data.getToDistrict(), data.getToWard(),
                data.getToCity(), data.getToState(), data.getToCountry(), errorMessages);
        validateDistrictCombination(data.getToDistrict(), data.getToWard(), data.getToCity(), data.getToState(),
                data.getToCountry(), errorMessages);
        validateCityCombination(data.getToCity(), data.getToState(), data.getToCountry(), errorMessages);
        validateLocationBaseCombination(data.getToState(), data.getToCountry(), errorMessages);
    }

    public void validateQPortalLocationCombination(MilestoneCsv data, @NonNull List<QPortalLocation> refLocationList,
                                                   List<String> errorMessages) {
        String fromLocationErrorMessage = getQPortalCombinationErrorMessage(data.getFromFacility(),
                data.getFromDistrict(), data.getFromWard(), data.getFromCity(), data.getFromState(),
                data.getFromCountry(), refLocationList);
        if (fromLocationErrorMessage != null) {
            errorMessages.add(fromLocationErrorMessage);
        }
        String toLocationErrorMessage = getQPortalCombinationErrorMessage(data.getToFacility(), data.getToDistrict(),
                data.getToWard(), data.getToCity(), data.getToState(), data.getToCountry(), refLocationList);
        if (toLocationErrorMessage != null) {
            errorMessages.add(toLocationErrorMessage);
        }
    }

    private String getQPortalCombinationErrorMessage(String facility, String district, String ward, String city,
                                                     String state, String country,
                                                     @NonNull List<QPortalLocation> refLocationList) {
        if (isLocationCombinationEmpty(city, state, country)) {
            return null;
        }

        List<String> ancestors = createAncestorList(facility, district, ward, city, state, country);
        String locationName = getLocationName(facility, district, ward, city);

        String locationId = QPortalUtils.lookupLocationIdFromName(locationName, ancestors, refLocationList);
        if (locationId == null) {
            return String.format("Location `%s%s%s` not found.",
                    String.join(ADDRESS_DELIMITER, ancestors), ADDRESS_DELIMITER, locationName);
        }
        return null;
    }

    public void validateQPortalLocation(String qPortalLocationName, @NonNull List<QPortalLocation> refLocationList,
                                        List<String> errorMessages) {
        if (qPortalLocationName != null) {
            String locationId = QPortalUtils.lookupIdFromName(qPortalLocationName, refLocationList);
            if (locationId == null) {
                String errorMessage = String.format("Location `%s` not found.", qPortalLocationName);
                errorMessages.add(errorMessage);
            }
        }
    }

    public void validateQPortalDriver(String qPortalDriverName, @NonNull List<QPortalDriver> refDriverList,
                                      List<String> errorMessages) {
        if (qPortalDriverName != null) {
            String driverId = QPortalUtils.lookupIdFromName(qPortalDriverName, refDriverList);
            if (driverId == null) {
                String errorMessage = String.format("Driver `%s` not found.", qPortalDriverName);
                errorMessages.add(errorMessage);
            }
        }
    }

    public void validateQPortalVehicle(String qPortalVehicleName, @NonNull List<QPortalVehicle> refVehicleList,
                                       List<String> errorMessages) {
        if (qPortalVehicleName != null) {
            String vehicleId = QPortalUtils.lookupIdFromName(qPortalVehicleName, refVehicleList);
            if (vehicleId == null) {
                String errorMessage = "Vehicle `%s` not found.";
                errorMessages.add(errorMessage);
            }
        }
    }

    private void validateFacilityCombination(String location, String district, String ward, String city, String state,
                                             String country, List<String> errorMessages) {
        if (location != null) {
            String errorMessage;
            if (city == null || state == null || country == null) {
                errorMessage = "Invalid location combination for Facility `%s`. Country, State/Province, and City must be provided.";
                errorMessages.add(String.format(errorMessage, location));
                return;
            }
            if ((district == null) != (ward == null)) {
                errorMessage = "Invalid location combination for Facility `%s`. Country, State/Province, City, Ward, and District must be provided.";
                errorMessages.add(String.format(errorMessage, location));
            }
        }
    }

    private void validateDistrictCombination(String district, String ward, String city, String state, String country,
                                             List<String> errorMessages) {
        if (district != null) {
            if (ward == null || city == null || state == null || country == null) {
                String errorMessage = "Invalid location combination for District `%s`. Country, State/Province, City, and Ward must be provided.";
                errorMessages.add(String.format(errorMessage, district));
            }
        } else if (ward != null) {
            String errorMessage = "Malformed Location. Ward must have District.";
            errorMessages.add(errorMessage);
        }
    }

    private void validateCityCombination(String city, String state, String country, List<String> errorMessages) {
        if (city != null) {
            if (state == null || country == null) {
                String errorMessage = "Invalid location combination for City `%s`. Country, and State/Province must be provided.";
                errorMessages.add(String.format(errorMessage, city));
            }
        } else if ((state != null) || (country != null)) {
            errorMessages.add(MALFORMED_LOCATION_ERROR);
        }
    }

    private void validateLocationBaseCombination(String state, String country, List<String> errorMessages) {
        if ((state != null) ^ (country != null)) {
            errorMessages.add(MALFORMED_LOCATION_ERROR);
        }
    }

    private boolean isLocationCombinationEmpty(String city, String state, String country) {
        return city == null && state == null && country == null;
    }

    private List<String> createAncestorList(String facility, String district, String ward, String city, String state, String country) {
        List<String> ancestors = new ArrayList<>(Arrays.asList(country, state));
        if (facility != null) {
            ancestors.add(city);
            if (ward != null) {
                ancestors.add(ward);
            }
            if (district != null) {
                ancestors.add(district);
            }
            return ancestors;
        }

        if (ward != null) {
            ancestors.add(city);
            if (district != null) {
                ancestors.add(ward);
            }
        }

        return ancestors;
    }

    private String getLocationName(String facility, String district, String ward, String city) {
        return Optional.ofNullable(facility)
                .orElse(Optional.ofNullable(district)
                        .orElse(Optional.ofNullable(ward).orElse(city)));
    }

    private void validateCoordinates(String latitude, String longitude, String prefix, List<String> errorMessages) {
        if ((latitude == null) != (longitude == null)) {
            String errorMessage = createMalformedCoordinateErrorMessage(prefix);
            errorMessages.add(errorMessage);
        }
    }

    private String createMalformedCoordinateErrorMessage(String prefix) {
        return String.format(MALFORMED_COORDINATION_ERROR, prefix, prefix);
    }
}
