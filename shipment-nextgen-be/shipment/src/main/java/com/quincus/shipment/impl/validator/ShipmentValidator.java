package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.domain.Shipment;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentValidator {
    private static final String SHIPMENT_ERROR_VIOLATIONS_OCCURRED = "Shipment error violations occurred: ";
    private final Validator validator;
    private final PackageJourneySegmentValidator packageJourneySegmentValidator;

    public void validateShipment(Shipment shipment) {
        validateBaseShipment(shipment);
        packageJourneySegmentValidator.validatePackageJourneySegments(shipment.getShipmentJourney());
    }

    private void validateBaseShipment(Shipment shipment) {
        Set<ConstraintViolation<Shipment>> violations = validator.validate(shipment);

        String errorMessage = violations.stream()
                .filter(violation -> !isFromShipmentJourney(violation))
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining("\n"));

        if (!errorMessage.isEmpty()) {
            throw new QuincusValidationException(SHIPMENT_ERROR_VIOLATIONS_OCCURRED + errorMessage);
        }
    }

    private boolean isFromShipmentJourney(ConstraintViolation<Shipment> violation) {
        Class<?> leafBeanClass = violation.getLeafBean().getClass();
        return leafBeanClass.getName().matches(".*ShipmentJourney$") || leafBeanClass.getName().matches(".*PackageJourneySegment$");
    }
}
