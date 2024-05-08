package com.quincus.shipment.impl.helper;

import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@UtilityClass
public class CsvValidator {
    private static final String TEXT_CSV = "text/csv";

    public static void validate(@NonNull MultipartFile file) throws QuincusValidationException {
        if (!StringUtils.equals(TEXT_CSV, file.getContentType())) {
            throw new QuincusValidationException("Invalid file or file format. Only CSV files are allowed.");
        }
    }
}
