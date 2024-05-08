package com.quincus.ext.annotation.validator;

import com.quincus.ext.annotation.FileNames;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class FileNamesValidator implements ConstraintValidator<FileNames, List<String>> {

    @Override
    public boolean isValid(List<String> fileNames, ConstraintValidatorContext context) {
        if (CollectionUtils.isEmpty(fileNames)) {
            return false;
        }
        return fileNames.stream().anyMatch(fileName -> FileNameValidator.isValidFileName(fileName, context));
    }
}
