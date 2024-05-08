package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.validator.constraint.ValidNode;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NodeValidator implements ConstraintValidator<ValidNode, Node> {

    @Override
    public boolean isValid(Node node, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        return isValidAddress(node, context);
    }

    private boolean isValidAddress(Node node, ConstraintValidatorContext context) {

        if (StringUtils.isNotBlank(node.getAddressLine2()) && StringUtils.isBlank(node.getAddressLine1())) {
            context.buildConstraintViolationWithTemplate("address line1 is required when address line2 is provided")
                    .addPropertyNode("addressLine1")
                    .addConstraintViolation();
            return false;
        }

        if (StringUtils.isNotBlank(node.getAddressLine3()) && StringUtils.isBlank(node.getAddressLine2())) {
            context.buildConstraintViolationWithTemplate("address line1 and address line2 are required when address line3 is provided")
                    .addPropertyNode("addressLine2")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

}
