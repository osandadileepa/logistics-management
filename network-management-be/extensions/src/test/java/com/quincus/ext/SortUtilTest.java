package com.quincus.ext;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SortUtilTest {

    private static Stream<Object[]> dataProvider() {
        return Stream.of(
                new Object[]{Arrays.asList("D", "BC", "A", "1A", "10001", "1", "2A", "2"), Arrays.asList("1", "1A", "2", "2A", "10001", "A", "BC", "D")},
                new Object[]{Arrays.asList("item10", "1", "item2"), Arrays.asList("1", "item2", "item10")},
                new Object[]{Arrays.asList("item1", "item10", "item2"), Arrays.asList("item1", "item2", "item10")},
                new Object[]{Arrays.asList("10015", "21", "29", "49"), Arrays.asList("21", "29", "49", "10015")}
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testSortListAlphanumerically(List<String> inputData, List<String> expectedData) {
        Function<String, String> identityFunction = Function.identity();

        SortUtil.sortListAlphanumerically(inputData, identityFunction);

        assertThat(inputData).isEqualTo(expectedData);
    }
}
