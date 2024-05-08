package com.quincus.networkmanagement.impl.service.calculator;

import com.quincus.networkmanagement.api.constant.DistanceUnit;
import com.quincus.networkmanagement.api.domain.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyNode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DistanceCalculatorTest {

    DistanceCalculator distanceCalculator = new DistanceCalculator();

    private static Stream<Arguments> provideCoordinates() {
        return Stream.of(
                // Singapore to Delhi
                Arguments.of(28.66866, 77.10194, 1.37236, 103.93286, "4160.7920", "2585.3964"),
                // Manila to Makati
                Arguments.of(14.5813098, 120.9761612, 14.5567949, 121.0211226, "5.5537", "3.4509"),
                // Tokyo to Seoul
                Arguments.of(35.6840574, 139.7744912, 37.5666791, 126.9782914, "1159.9927", "720.7860"),
                // Paris to Bangkok
                Arguments.of(48.8588897, 2.320041, 13.7524938, 100.4935089, "9445.0414", "5868.8768"),
                // Kuala Lumpur to California
                Arguments.of(3.1516964, 101.6942371, 36.7014631, -118.755997, "13920.8398", "8650.0091"),
                // Shibuya to Osaka
                Arguments.of(35.6645956, 139.6987107, 34.661629, 135.4999268, "397.6053", "247.0605"),
                // Dasmarinas to Carmona
                Arguments.of(14.3270819, 120.9370871, 14.309243, 121.031708, "10.3857", "6.4533"),
                // Phuket to Boracay
                Arguments.of(7.9366015, 98.3529292, 11.9477338, 121.9392632, "2620.4007", "1628.2416"),
                // New York to London
                Arguments.of(40.7127281, -74.0060152, 51.5073359, -0.12765, "5570.2402", "3461.1869"),
                // Seattle to Hawaii
                Arguments.of(47.6038321, -122.330062, 19.5938015, -155.4283701, "4309.7343", "2677.9448"),
                // Hawaii to Seattle
                Arguments.of(19.5938015, -155.4283701, 47.6038321, -122.330062, "4309.7343", "2677.9448"),
                // Seattle to Seattle
                Arguments.of(47.6038321, -122.330062, 47.6038321, -122.330062, "0.0000", "0.0000"),
                // Same Lat
                Arguments.of(47.6038321, -122.330062, 47.6038321, -121.330062, "74.9729", "46.5860"),
                // Same Lon
                Arguments.of(47.6038321, -122.330062, 48.6038321, -122.330062, "111.1949", "69.0933")
        );
    }

    private static Stream<Arguments> provideInvalidScenarios() {
        return Stream.of(
                Arguments.of(null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                Arguments.of(BigDecimal.ZERO, null, BigDecimal.ZERO, BigDecimal.ZERO),
                Arguments.of(BigDecimal.ZERO, BigDecimal.ZERO, null, BigDecimal.ZERO),
                Arguments.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null),
                Arguments.of(BigDecimal.valueOf(47.6038321), BigDecimal.valueOf(-122.330062), BigDecimal.valueOf(47.6038321), BigDecimal.valueOf(-122.330062))
        );
    }


    @ParameterizedTest
    @MethodSource("provideCoordinates")
    @DisplayName("GIVEN node coordinates WHEN calculateDistance THEN return expected")
    void returnExpectedWhenCalculate(
            Double lat1,
            Double lon1,
            Double lat2,
            Double lon2,
            String expectedDistanceInKm,
            String expectedDistanceInMi
    ) {

        Node departureNode = dummyNode();
        departureNode.getFacility().setLat(BigDecimal.valueOf(lat1));
        departureNode.getFacility().setLon(BigDecimal.valueOf(lon1));
        Node arrivalNode = dummyNode();
        arrivalNode.getFacility().setLat(BigDecimal.valueOf(lat2));
        arrivalNode.getFacility().setLon(BigDecimal.valueOf(lon2));

        BigDecimal distanceInKm = distanceCalculator.calculateDistance(
                departureNode,
                arrivalNode,
                DistanceUnit.KILOMETERS
        );
        BigDecimal distanceInMi = distanceCalculator.calculateDistance(
                departureNode,
                arrivalNode,
                DistanceUnit.MILES
        );

        assertThat(distanceInKm).hasToString(expectedDistanceInKm);
        assertThat(distanceInMi).hasToString(expectedDistanceInMi);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidScenarios")
    @DisplayName("GIVEN invalid scenarios WHEN calculateDistance THEN return zero")
    void returnZeroWhenAnyIsNull(
            BigDecimal lat1,
            BigDecimal lon1,
            BigDecimal lat2,
            BigDecimal lon2
    ) {
        BigDecimal distanceInKilometers = distanceCalculator.calculateDistance(
                dummyNode(lat1, lon1),
                dummyNode(lat2, lon2),
                DistanceUnit.KILOMETERS
        );
        BigDecimal distanceInMiles = distanceCalculator.calculateDistance(
                dummyNode(lat1, lon1),
                dummyNode(lat2, lon2),
                DistanceUnit.MILES
        );

        assertThat(distanceInKilometers).hasToString("0.0000");
        assertThat(distanceInMiles).hasToString("0.0000");
    }

    @Test
    @DisplayName("GIVEN no facility WHEN calculateDistance THEN return zero")
    void returnZeroWhenNoFacility() {

        Node node = dummyNode();
        Node nodeWithoutFacility = dummyNode();
        nodeWithoutFacility.setFacility(null);

        BigDecimal noDepartureFacilityResult = distanceCalculator.calculateDistance(
                node,
                nodeWithoutFacility,
                DistanceUnit.KILOMETERS
        );
        BigDecimal noArrivalFacilityResult = distanceCalculator.calculateDistance(
                nodeWithoutFacility,
                node,
                DistanceUnit.MILES
        );

        assertThat(noDepartureFacilityResult).hasToString("0.0000");
        assertThat(noArrivalFacilityResult).hasToString("0.0000");
    }
}

