package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.validator.constraint.ValidRRule;
import org.apache.commons.lang3.ObjectUtils;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RRuleValidator implements ConstraintValidator<ValidRRule, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            RecurrenceRule rule = new RecurrenceRule(value.replace("RRULE:", ""));

            if (ObjectUtils.anyNull(
                    rule.getByPart(RecurrenceRule.Part.BYHOUR),
                    rule.getByPart(RecurrenceRule.Part.BYMINUTE)
            )) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("must have BYHOUR and BYMINUTE").addConstraintViolation();
                return false;
            }
            return true;
        } catch (InvalidRecurrenceRuleException ex) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("must be a valid RRULE format").addConstraintViolation();
            return false;
        }
    }
}