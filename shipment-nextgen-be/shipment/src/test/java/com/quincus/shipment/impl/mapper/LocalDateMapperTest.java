package com.quincus.shipment.impl.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateMapperTest {


    @Test
    @DisplayName("GIVEN instant WHEN converting to local date time file THEN convert successfully")
    void shouldConvertToLocalDateTime() {
        assertThat(LocalDateMapper.toLocalDateTime(Instant.now())).isNotNull();
    }

    @Test
    @DisplayName("GIVEN null instant WHEN converting to local date time file THEN return null")
    void shouldReturnNull() {
        assertThat(LocalDateMapper.toLocalDateTime(null)).isNull();
    }
}
