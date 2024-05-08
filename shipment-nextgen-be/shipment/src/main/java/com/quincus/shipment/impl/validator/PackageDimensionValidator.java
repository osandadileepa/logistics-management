package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.constant.PackageDimensionUpdateImportHeader;
import com.quincus.shipment.api.exception.InvalidEnumValueException;
import com.quincus.shipment.api.exception.PackageDimensionException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import static java.util.Objects.isNull;

@Component
public class PackageDimensionValidator {

    public static final String ERR_MSG_INVALID_FILE_FORMAT = "Invalid file format. Please upload a CSV file.";
    public static final String ERR_MSG_FILE_SIZE_LIMIT = "File size exceeds the limit. Please upload a file that is no larger than 10MB.";
    public static final String ERR_MSG_FILE_INVALID_TEMPLATE_FORMAT = "Invalid file template. Please ensure that you are using the correct file template and try again.";
    public static final String ERR_MSG_GENERIC_FILE_UPLOAD = "There seems to be a system error at the moment. Please try again later.";
    private static final String CSV_FILE_EXTENSION = "csv";
    private static final int FILE_SIZE_LIMIT = 10;
    private static final int BYTE_TO_MB_FORMULA = 1024 * 1024;

    private static void verifyHeader(String headers) {
        try {
            Arrays.stream(headers.split(",")).forEach(PackageDimensionUpdateImportHeader::fromValue);
        } catch (InvalidEnumValueException e) {
            throw new PackageDimensionException(ERR_MSG_FILE_INVALID_TEMPLATE_FORMAT);
        }
    }

    private static long getFileSizeInMB(File file) {
        return file.length() / BYTE_TO_MB_FORMULA;
    }

    public void validatePackageDimensionUpdateFile(File file) {
        try {
            if (!FilenameUtils.isExtension(file.getName(), CSV_FILE_EXTENSION)) {
                throw new PackageDimensionException(ERR_MSG_INVALID_FILE_FORMAT);
            } else if (getFileSizeInMB(file) > FILE_SIZE_LIMIT) {
                throw new PackageDimensionException(ERR_MSG_FILE_SIZE_LIMIT);
            }
            validateFileHeader(file);
        } catch (IOException e) {
            throw new PackageDimensionException(ERR_MSG_GENERIC_FILE_UPLOAD);
        }
    }

    private void validateFileHeader(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headers = reader.readLine();
            if (isNull(headers)) {
                throw new PackageDimensionException(ERR_MSG_FILE_INVALID_TEMPLATE_FORMAT);
            }
            verifyHeader(headers);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
}
