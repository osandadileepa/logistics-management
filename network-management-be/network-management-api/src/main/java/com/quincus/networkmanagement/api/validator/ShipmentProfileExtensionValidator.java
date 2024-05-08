package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.domain.ShipmentProfileExtension;
import com.quincus.networkmanagement.api.validator.constraint.ValidShipmentProfileExtension;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ShipmentProfileExtensionValidator implements ConstraintValidator<ValidShipmentProfileExtension, ShipmentProfileExtension> {
    @Override
    public boolean isValid(ShipmentProfileExtension shipmentProfile, ConstraintValidatorContext context) {
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
    private boolean isValidMinAndMax(ShipmentProfileExtension shipmentProfile, ConstraintValidatorContext context) {
        boolean isValid = true;

        if (shipmentProfile.getMaxSingleSide() != null && shipmentProfile.getMinSingleSide() != null &&
                shipmentProfile.getMaxSingleSide().compareTo(shipmentProfile.getMinSingleSide()) < 0) {
            context.buildConstraintViolationWithTemplate("max single side must be greater than min single side")
                    .addPropertyNode("maxSingleSide")
                    .addConstraintViolation();
            isValid = false;
        }

        if (shipmentProfile.getMaxLinearDim() != null && shipmentProfile.getMinLinearDim() != null &&
                shipmentProfile.getMaxLinearDim().compareTo(shipmentProfile.getMinLinearDim()) < 0) {
            context.buildConstraintViolationWithTemplate("max linear dim must be greater than min linear dim")
                    .addPropertyNode("maxLinearDim")
                    .addConstraintViolation();
            isValid = false;
        }

        if (shipmentProfile.getMaxVolume() != null && shipmentProfile.getMinVolume() != null &&
                shipmentProfile.getMaxVolume().compareTo(shipmentProfile.getMinVolume()) < 0) {
            context.buildConstraintViolationWithTemplate("max volume must be greater than min volume")
                    .addPropertyNode("maxVolume")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }

}
