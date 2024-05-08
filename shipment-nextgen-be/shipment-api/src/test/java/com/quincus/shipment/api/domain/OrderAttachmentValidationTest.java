package com.quincus.shipment.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderAttachmentValidationTest extends ValidationTest {

    @Test
    void orderAttachment_withMissingMandatoryFields_shouldHaveViolations() {
        assertThat(validateModel(new OrderAttachment())).isNotEmpty();
    }

    @Test
    void orderAttachment_withValidFields_shouldHaveNoViolations() {
        OrderAttachment attachment = new OrderAttachment();
        attachment.setId("attachment-1");
        attachment.setFileName("name");
        attachment.setFileUrl("url");
        attachment.setFileSize(1234L);
        assertThat(validateModel(attachment)).isEmpty();
    }

    @Test
    void orderAttachment_withBlankFields_shouldHaveViolations() {
        OrderAttachment attachment = new OrderAttachment();
        attachment.setId("");
        attachment.setFileName("");
        attachment.setFileUrl("");
        attachment.setFileSize(1234L);
        assertThat(validateModel(attachment)).isNotEmpty();
    }

    @Test
    void orderAttachment_withEmptyFields_shouldHaveViolations() {
        OrderAttachment attachment = new OrderAttachment();
        attachment.setId(" ");
        attachment.setFileName(" ");
        attachment.setFileUrl(" ");
        attachment.setFileSize(1234L);
        assertThat(validateModel(attachment)).isNotEmpty();
    }
}
