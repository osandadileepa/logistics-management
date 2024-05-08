package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.domain.ShipmentProfile;
import com.quincus.networkmanagement.api.validator.constraint.ValidShipmentProfile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ShipmentProfileValidator implements ConstraintValidator<ValidShipmentProfile, ShipmentProfile> {
    @Override
    public boolean isValid(ShipmentProfile shipmentProfile, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        return isValidMinAndMax(shipmentProfile, context);
    }

    /**
     * Due to the nature of validators whereby custom validation may trigger prior to standard validation,
     * Certain fields may be null, causing an exception to occur during the isValid call
     * Resolution:
     * Bypass sonar warning regarding certain expressions always evaluating to true
     */
    @SuppressWarnings("squid:S2589")
    private boolean isValidMinAndMax(ShipmentProfile shipmentProfile, ConstraintValidatorContext context) {
        boolean isValid = true;

        if (shipmentProfile.getMaxLength() != null && shipmentProfile.getMinLength() != null &&
                shipmentProfile.getMaxLength().compareTo(shipmentProfile.getMinLength()) < 0) {
            context.buildConstraintViolationWithTemplate("max length must greater than min length")
                    .addPropertyNode("maxLength")
                    .addConstraintViolation();
            isValid = false;
        }

        if (shipmentProfile.getMaxWidth() != null && shipmentProfile.getMinWidth() != null &&
                shipmentProfile.getMaxWidth().compareTo(shipmentProfile.getMinWidth()) < 0) {
            context.buildConstraintViolationWithTemplate("max width must greater than min width")
                    .addPropertyNode("maxWidth")
                    .addConstraintViolation();
            isValid = false;
        }

        if (shipmentProfile.getMaxHeight() != null && shipmentProfile.getMinHeight() != null &&
                shipmentProfile.getMaxHeight().compareTo(shipmentProfile.getMinHeight()) < 0) {
            context.buildConstraintViolationWithTemplate("max height must greater than min height")
                    .addPropertyNode("maxHeight")
                    .addConstraintViolation();
            isValid = false;
        }

        if (shipmentProfile.getMaxWeight() != null && shipmentProfile.getMinWeight() != null &&
                shipmentProfile.getMaxWeight().compareTo(shipmentProfile.getMinWeight()) < 0) {
            context.buildConstraintViolationWithTemplate("max weight must greater than min weight")
                    .addPropertyNode("maxWeight")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
