package com.quincus.ext.annotation.validator;

import com.quincus.ext.DateTimeUtil;
import com.quincus.ext.annotation.ISODateTime;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ISODatetimeValidator implements ConstraintValidator<ISODateTime, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        return isValidDatetimeFormat(value);
    }

    private boolean isValidDatetimeFormat(String value) {
        try {
//          OffsetDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));  to be use once FE has adjusted the format
            DateTimeUtil.parseZonedDateTime(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}