package com.quincus.ext.annotation.validator;

import com.quincus.ext.annotation.FileName;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class FileNameValidator implements ConstraintValidator<FileName, String> {

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-. (),;'+=!?@&#\\[\\]~^\"$%]+\\.[a-zA-Z0-9]+$");
    private static final int MAX_SIZE = 256;

    private boolean required;

    @Override
    public void initialize(FileName constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String fileName, ConstraintValidatorContext context) {
        // Case when fileName is required but is blank.
        if (required && StringUtils.isBlank(fileName)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Must be required and a valid file name format")
                    .addConstraintViolation();
            return false;
        }

        // Case when fileName is not required and is null or empty.
        if (!required && StringUtils.isEmpty(fileName)) {
            return true; // We allow null or empty fileName when it's not required.
        }

        return isValidFileName(fileName, context);
    }

    public static boolean isValidFileName(String fileName, ConstraintValidatorContext context) {
        // Case when fileName has a value and its length is greater than MAX_SIZE.
        if (fileName.length() > MAX_SIZE) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(String.format("File name is too long. It should be no more than %s characters.", MAX_SIZE))
                    .addConstraintViolation();
            return false;
        }

        // Check if fileName matches the desired pattern.
        if (!FILE_NAME_PATTERN.matcher(fileName.trim()).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid file name format. It must have an extension.")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

}