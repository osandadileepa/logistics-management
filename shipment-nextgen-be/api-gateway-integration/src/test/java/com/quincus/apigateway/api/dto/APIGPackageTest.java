package com.quincus.apigateway.api.dto;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class APIGPackageTest {

    private static Stream<Arguments> provideEqualObjects() {
        APIGPackage noPackageNo1 = new APIGPackage();
        noPackageNo1.setAdditionalData1("1_1");
        APIGPackage noPackageNo2 = new APIGPackage();
        noPackageNo2.setAdditionalData1("1_1");

        APIGPackage withPackageNo1 = new APIGPackage();
        withPackageNo1.setPackageNo("1");
        withPackageNo1.setAdditionalData1("1_1");
        APIGPackage withPackageNo2 = new APIGPackage();
        withPackageNo2.setPackageNo("1");
        withPackageNo2.setAdditionalData1("1_1");

        APIGPackage none1 = new APIGPackage();
        APIGPackage none2 = new APIGPackage();

        return Stream.of(
                Arguments.of(noPackageNo1, noPackageNo2),
                Arguments.of(withPackageNo1, withPackageNo2),
                Arguments.of(none1, none2)
        );
    }

    private static Stream<Arguments> provideObjectsAscendingOrder() {
        APIGPackage apigPackage1 = new APIGPackage();
        apigPackage1.setAdditionalData1("1_1");
        APIGPackage apigPackage2 = new APIGPackage();
        apigPackage2.setAdditionalData1("1_2");

        APIGPackage apigPackage3 = new APIGPackage();
        apigPackage3.setPackageNo("1");
        apigPackage3.setAdditionalData1("1_1");
        APIGPackage apigPackage4 = new APIGPackage();
        apigPackage4.setPackageNo("2");
        apigPackage4.setAdditionalData1("1_2");

        APIGPackage apigPackage5 = new APIGPackage();
        APIGPackage apigPackage6 = new APIGPackage();
        apigPackage6.setAdditionalData1("1_1");

        return Stream.of(
                Arguments.of(apigPackage1, apigPackage2),
                Arguments.of(apigPackage3, apigPackage4),
                Arguments.of(apigPackage5, apigPackage6)
        );
    }

    @ParameterizedTest
    @MethodSource("provideEqualObjects")
    void compareTo_equals_shouldReturnZero(APIGPackage o1, APIGPackage o2) {
        assertThat(o1).isEqualByComparingTo(o2);
    }

    @ParameterizedTest
    @MethodSource("provideObjectsAscendingOrder")
    void compareTo_ascendingOrder_shouldReturnNegative(APIGPackage o1, APIGPackage o2) {
        assertThat(o1).isLessThan(o2);
    }

    @ParameterizedTest
    @MethodSource("provideObjectsAscendingOrder")
    void compareTo_descendingOrder_shouldReturnPositive(APIGPackage o1, APIGPackage o2) {
        assertThat(o2).isGreaterThan(o1);
    }
}
