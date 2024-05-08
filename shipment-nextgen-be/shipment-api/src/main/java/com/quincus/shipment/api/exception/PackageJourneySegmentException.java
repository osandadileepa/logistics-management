package com.quincus.shipment.api.exception;

import com.quincus.web.common.exception.model.QuincusException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class PackageJourneySegmentException extends QuincusException {
    private static final String PACKAGE_JOURNEY_SEGMENT_FIELD = "shipment_journey.package_journey_segments[%d].%s";
    @Getter
    private final List<String> errors;

    public PackageJourneySegmentException() {
        super("Invalid Package Journey Segment");
        errors = new ArrayList<>();
    }

    public void addError(int segmentPosition, String error, String field) {
        String formattedErrorMessage = String.format(PACKAGE_JOURNEY_SEGMENT_FIELD, segmentPosition, field);
        errors.add(String.format("%s %s", formattedErrorMessage, error));
    }
}
