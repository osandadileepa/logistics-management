package com.quincus.shipment.impl.helper;

import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class CsvValidatorTest {
    
    @Test
    @DisplayName("Given an invalid file format, when validating, then throw a QuincusValidationException")
    void givenInvalidFileFormatWhenValidatingThenThrowQuincusValidationException() {
        MultipartFile file = new MockMultipartFile("file.txt", "Hello World".getBytes());

        assertThatThrownBy(() -> CsvValidator.validate(file))
                .isInstanceOf(QuincusValidationException.class);
    }

    @Test
    @DisplayName("Given a valid CSV file, when validating, then no exception should be thrown")
    void givenValidCsvFileWhenValidatingThenNoExceptionThrown() {
        byte[] csvContent = "CSV content".getBytes();
        MultipartFile file = new MockMultipartFile("file.csv", "file.csv", "text/csv", csvContent);

        assertThatCode(() -> CsvValidator.validate(file))
                .doesNotThrowAnyException();
    }
}
