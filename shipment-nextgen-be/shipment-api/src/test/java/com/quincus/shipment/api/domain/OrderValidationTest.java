package com.quincus.shipment.api.domain;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class OrderValidationTest extends ValidationTest {
    private final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

    @Test
    void order_withMissingMandatoryFields_shouldHaveViolations() {
        assertThat(validateModel(new Order())).isNotEmpty();
    }

    @Test
    void order_withValidFields_shouldHaveNoViolations() {
        Order order = new Order();
        order.setId("ID");
        order.setStatus("Status");
        order.setOrderIdLabel("ID-LABEL");
        order.setPickupStartTime(OffsetDateTime.now().format(ISO_FORMATTER));
        order.setPickupCommitTime(OffsetDateTime.now().plusHours(1).format(ISO_FORMATTER));
        order.setPickupTimezone("GMT+08:00");
        order.setDeliveryStartTime(OffsetDateTime.now().plusDays(1).format(ISO_FORMATTER));
        order.setDeliveryCommitTime(OffsetDateTime.now().plusDays(1).plusHours(1).format(ISO_FORMATTER));
        order.setDeliveryTimezone("GMT+08:00");
        order.setNotes("Notes");
        assertThat(validateModel(order)).isEmpty();
    }

    @Test
    void order_withBlankFields_shouldHaveViolations() {
        Order order = new Order();
        order.setId(" ");
        order.setOrderIdLabel(" ");
        assertThat(validateModel(order)).isNotEmpty();
    }

    @Test
    void order_withEmptyFields_shouldHaveViolations() {
        Order order = new Order();
        order.setId("");
        order.setOrderIdLabel("");
        assertThat(validateModel(order)).isNotEmpty();
    }

    @Test
    void order_notesExceedLimit_shouldHaveViolation() {
        int limit = 2000;
        String longNote = RandomStringUtils.randomAlphabetic(limit + 1);
        Order order = new Order();
        order.setId("ID");
        order.setStatus("Status");
        order.setOrderIdLabel("ID-LABEL");
        order.setPickupStartTime(OffsetDateTime.now().format(ISO_FORMATTER));
        order.setPickupCommitTime(OffsetDateTime.now().plusHours(1).format(ISO_FORMATTER));
        order.setPickupTimezone("GMT+08:00");
        order.setDeliveryStartTime(OffsetDateTime.now().plusDays(1).format(ISO_FORMATTER));
        order.setDeliveryCommitTime(OffsetDateTime.now().plusDays(1).plusHours(1).format(ISO_FORMATTER));
        order.setDeliveryTimezone("GMT+08:00");
        order.setNotes(longNote);
        assertThat(validateModel(order)).isNotEmpty();
    }
}
