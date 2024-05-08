package com.quincus.shipment.impl.helper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentOrderDateUtilTest {
    
    @ParameterizedTest
    @MethodSource("provideOrderDateSample")
    void givenSampleOrderDates_whenModifyOrderDateToSegmentDate_thenExpectCorrectedDateFormat(String orderDate, String expectedSegmentDate) {
        assertThat(ShipmentOrderDateUtil.modifyOrderDateToSegmentDate(orderDate)).isEqualTo(expectedSegmentDate);
    }

    private static Stream<Arguments> provideOrderDateSample() {
        return Stream.of(
                Arguments.of("2023-05-23 00:00:00 GMT-07:00", "2023-05-23 00:00:00 -0700")
                , Arguments.of("2023-05-24 11:59:00 GMT-07:00", "2023-05-24 11:59:00 -0700")
                , Arguments.of("2023-05-30 00:00:00 GMT+08:00", "2023-05-30 00:00:00 +0800")
                , Arguments.of("2023-05-31 11:59:00 GMT+08:00", "2023-05-31 11:59:00 +0800")
        );
    }
}
