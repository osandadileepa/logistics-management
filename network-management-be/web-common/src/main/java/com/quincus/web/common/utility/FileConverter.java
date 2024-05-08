package com.quincus.web.common.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

import static java.util.Objects.isNull;

@Slf4j
@NoArgsConstructor(access = AccessLevel.NONE)
public class FileConverter {
    // Define the list of potentially malicious characters or sequences to be removed
    private static final String[] MALICIOUS_CHARACTERS = {"\\", "/", ":", "*", "?", "\"", "<", ">", "|"};

    public static File convertMultipartToFile(MultipartFile file) {
        if (isNull(file)) {
            return null;
        }
        String originalFileName = sanitizeFilename(file.getOriginalFilename());
        File convFile = new File(Objects.requireNonNull(originalFileName));

        try (FileOutputStream stream = new FileOutputStream(convFile)) {
            stream.write(file.getBytes());
        } catch (Exception e) {
            log.warn("Failed to convert multipart file: `{}` into a file. Error was: {}", originalFileName, e.getMessage());
        }
        return convFile;
    }

    private static String sanitizeFilename(String filename) {
        for (String maliciousChar : MALICIOUS_CHARACTERS) {
            if (!StringUtils.isEmpty(filename))
                filename = filename.replace(maliciousChar, "");
        }
        return filename;
    }
}
