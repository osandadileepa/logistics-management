package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.constant.FileType;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@UtilityClass
public class FileTypeValidator {
    public static void validate(@NonNull MultipartFile file) {
        FileType fileType = FileType.fromMimeType(file.getContentType());

        if (fileType == null) {
            throw new QuincusValidationException("Invalid file or file format. Only CSV and Excel files are allowed.");
        }
    }

    public static String getFileTypeFromFile(@NonNull MultipartFile file) {
        return file.getContentType() != null ? file.getContentType() : "";
    }

}
