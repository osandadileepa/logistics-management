package com.quincus.shipment.api.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Setter;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class BigDecimalSerializerTest {
    private static Stream<Arguments> provideDummiesWithBigDecimal() {
        DummyDomain dummy1 = new DummyDomain();
        dummy1.setAmount(new BigDecimal("10"));

        DummyDomain dummy2 = new DummyDomain();
        dummy2.setAmount(BigDecimal.valueOf(10));

        DummyDomain dummy3 = new DummyDomain();
        dummy3.setAmount(new BigDecimal("20.99"));

        DummyDomain dummy4 = new DummyDomain();
        dummy4.setAmount(BigDecimal.valueOf(20.99));

        DummyDomain dummy5 = new DummyDomain();
        dummy5.setAmount(new BigDecimal("10.0"));

        DummyDomain dummy6 = new DummyDomain();
        dummy6.setAmount(new BigDecimal("0"));

        DummyDomain dummy7 = new DummyDomain();
        dummy7.setAmount(new BigDecimal("0.0"));

        DummyDomain dummy8 = new DummyDomain();
        dummy8.setAmount(new BigDecimal("-10"));

        DummyDomain dummy9 = new DummyDomain();
        dummy9.setAmount(new BigDecimal("-0.0000123"));

        DummyDomain dummy10 = new DummyDomain();
        dummy10.setAmount(new BigDecimal("12.2345678"));

        return Stream.of(
                Arguments.of(Named.of("new BigDecimal(10)", dummy1), "{\"amount\":10}"),
                Arguments.of(Named.of("BigDecimal.valueOf(10)", dummy2), "{\"amount\":10}"),
                Arguments.of(Named.of("new BigDecimal(20.99)", dummy3), "{\"amount\":20.99}"),
                Arguments.of(Named.of("BigDecimal.valueOf(20.99)", dummy4), "{\"amount\":20.99}"),
                Arguments.of(Named.of("new BigDecimal(10.0)", dummy5), "{\"amount\":10}"),
                Arguments.of(Named.of("new BigDecimal(0)", dummy6), "{\"amount\":0}"),
                Arguments.of(Named.of("new BigDecimal(0.0)", dummy7), "{\"amount\":0}"),
                Arguments.of(Named.of("new BigDecimal(-10)", dummy8), "{\"amount\":-10}"),
                Arguments.of(Named.of("new BigDecimal(-0.0000123)", dummy9), "{\"amount\":-0.0000123}"),
                Arguments.of(Named.of("new BigDecimal(12.2345678)", dummy10), "{\"amount\":12.2345678}")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDummiesWithBigDecimal")
    void serialize_variousBigDecimalInputs_shouldSerializeAsIs(DummyDomain dummy, String expectedJsonLine) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        assertThat(mapper.writeValueAsString(dummy)).isEqualTo(expectedJsonLine);
    }

    @Setter
    public static class DummyDomain {
        @JsonSerialize(using = BigDecimalSerializer.class)
        private BigDecimal amount;
    }
}
