package com.quincus.shipment.impl.web;

import com.quincus.shipment.impl.web.exception.ShipmentExceptionHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentExceptionHelperTest {

    @ParameterizedTest
    @MethodSource("provideStringsForToSnakeCase")
    void testToSnakeCase(String input, String expected) {
        String result = ShipmentExceptionHelper.toSnakeCase(input);
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> provideStringsForToSnakeCase() {
        return Stream.of(
                Arguments.of("thisIsCamelCase", "this_is_camel_case"),
                Arguments.of("data.networkLaneSegments[0].duration", "data.network_lane_segments[0].duration"),
                Arguments.of("this_is_already_snake_case", "this_is_already_snake_case"),
                Arguments.of("Data", "data"),
                Arguments.of("", "")
        );
    }

}
