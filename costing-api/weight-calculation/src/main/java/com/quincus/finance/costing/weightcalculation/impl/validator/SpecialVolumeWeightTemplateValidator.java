package com.quincus.finance.costing.weightcalculation.impl.validator;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpecialVolumeWeightTemplateValidator implements ConstraintValidator<SpecialVolumeWeightTemplateConstraint, MultipartFile> {

    private static final String ERR_UNSUPPORTED_FILE_FORMAT = "The file uploaded file should be in either .CSV, .XLS or .XLSX format";
    private static final String ERR_FILE_TOO_LARGE = "Attached filed should not exceed 500kb";

    private static final Long FILE_SIZE_LIMIT = 1024 * 500L;

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {

        constraintValidatorContext.disableDefaultConstraintViolation();
        List<String> errors = new ArrayList<>();

        if(multipartFile != null && !isSupportedContentType(multipartFile.getContentType())) {
            errors.add(ERR_UNSUPPORTED_FILE_FORMAT);
        }

        if(multipartFile != null && multipartFile.getSize() > FILE_SIZE_LIMIT) {
            errors.add(ERR_FILE_TOO_LARGE);
        }

        if(!errors.isEmpty()) {
            for(String error : errors) {
                constraintValidatorContext.buildConstraintViolationWithTemplate(error).addConstraintViolation();
            }
            return false;
        }

        return true;
    }

    private boolean isSupportedContentType(String contentType) {
        return "text/csv".equals(contentType)
            || "application/vnd.ms-excel".equals(contentType)
            || "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType);
    }

}
