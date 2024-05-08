package com.quincus.shipment.api.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;


class HostedFileValidationTest extends ValidationTest {

    @Test
    void hostedFile_withMissingMandatoryFields_shouldHaveViolations() {
        assertThat(validateModel(new HostedFile())).isNotEmpty();
    }

    @Test
    void hostedFile_withValidFields_shouldHaveNoViolations() {
        HostedFile hostedFile = new HostedFile();
        hostedFile.setId("attachment-1");
        hostedFile.setFileName("name");
        hostedFile.setFileUrl("url");
        hostedFile.setFileSize(1234L);
        hostedFile.setFileTimestamp(OffsetDateTime.now());
        assertThat(validateModel(hostedFile)).isEmpty();
    }

    @Test
    void hostedFile_withBlankFields_shouldHaveViolations() {
        HostedFile hostedFile = new HostedFile();
        hostedFile.setId("");
        hostedFile.setFileName("");
        hostedFile.setFileUrl("");
        hostedFile.setFileSize(1234L);
        hostedFile.setFileTimestamp(OffsetDateTime.now());
        assertThat(validateModel(hostedFile)).isNotEmpty();
    }

    @Test
    void hostedFile_withEmptyFields_shouldHaveViolations() {
        HostedFile hostedFile = new HostedFile();
        hostedFile.setId(" ");
        hostedFile.setFileName(" ");
        hostedFile.setFileUrl(" ");
        hostedFile.setFileSize(1234L);
        hostedFile.setFileTimestamp(OffsetDateTime.now());
        assertThat(validateModel(hostedFile)).isNotEmpty();
    }
}
