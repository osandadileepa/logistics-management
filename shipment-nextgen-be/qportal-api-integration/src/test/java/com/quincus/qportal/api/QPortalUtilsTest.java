package com.quincus.qportal.api;

import com.quincus.qportal.model.QPortalDriver;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalModel;
import com.quincus.qportal.model.QPortalVehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QPortalUtilsTest {

    private static List<QPortalLocation> initializeRefLocationList() {
        QPortalLocation locCountry1 = new QPortalLocation();
        locCountry1.setActive(true);
        locCountry1.setId("country1-id");
        locCountry1.setName("COUNTRY1");
        locCountry1.setLocationType("Country");
        locCountry1.setAncestors("");
        QPortalLocation locState11 = new QPortalLocation();
        locState11.setActive(true);
        locState11.setId("state11-id");
        locState11.setName("STATE11");
        locState11.setLocationType("State/province");
        locState11.setAncestors("COUNTRY1");
        QPortalLocation locCity111 = new QPortalLocation();
        locCity111.setActive(true);
        locCity111.setId("city111-id");
        locCity111.setName("CITY111");
        locCity111.setLocationType("City");
        locCity111.setAncestors("COUNTRY1, STATE11");
        QPortalLocation facility1111 = new QPortalLocation();
        facility1111.setActive(true);
        facility1111.setId("facility1111-id");
        facility1111.setName("FACILITY1111");
        facility1111.setLocationType("Facility");
        facility1111.setAncestors("COUNTRY1, STATE11, CITY111");

        return List.of(locCountry1, locState11, locCity111, facility1111);
    }

    private static List<QPortalDriver> initializeDriverList() {
        QPortalDriver driver1 = new QPortalDriver();
        driver1.setId("driver1-id");
        driver1.setName("DRIVER 1");
        QPortalDriver driver2 = new QPortalDriver();
        driver2.setId("driver2-id");
        driver2.setName("DRIVER 2");

        return List.of(driver1, driver2);
    }

    private static List<QPortalVehicle> initializeVehicleList() {
        QPortalVehicle v1 = new QPortalVehicle();
        v1.setId("vehicle1-id");
        v1.setName("CAR 1");
        QPortalVehicle v2 = new QPortalVehicle();
        v2.setId("vehicle2-id");
        v2.setName("TRUCK 2");

        return List.of(v1, v2);
    }

    private static Stream<Arguments> provideLocationCombination() {
        return Stream.of(
                Arguments.of("COUNTRY1", null, "country1-id"),
                Arguments.of("COUNTRY1", Collections.emptyList(), "country1-id"),
                Arguments.of("COUNTRY1", List.of("COUNTRY1"), null),
                Arguments.of("STATE11", null, null),
                Arguments.of("STATE11", Collections.emptyList(), null),
                Arguments.of("STATE11", List.of("COUNTRY1"), "state11-id"),
                Arguments.of("CITY111", null, null),
                Arguments.of("CITY111", Collections.emptyList(), null),
                Arguments.of("CITY111", List.of("COUNTRY1"), null),
                Arguments.of("CITY111", List.of("COUNTRY1", "STATE11"), "city111-id"),
                Arguments.of("FACILITY1111", null, null),
                Arguments.of("FACILITY1111", Collections.emptyList(), null),
                Arguments.of("FACILITY1111", List.of("COUNTRY1"), null),
                Arguments.of("FACILITY1111", List.of("COUNTRY1", "STATE11"), null),
                Arguments.of("FACILITY1111", List.of("COUNTRY1", "STATE11", "CITY111"), "facility1111-id"),
                Arguments.of("FACILITY_X", List.of("COUNTRY1", "STATE11", "CITY111"), null)
        );
    }

    private static Stream<Arguments> provideQPortalNameAndList() {
        List<QPortalLocation> locationList = initializeRefLocationList();
        List<QPortalDriver> driverList = initializeDriverList();
        List<QPortalVehicle> vehicleList = initializeVehicleList();

        return Stream.of(
                Arguments.of("COUNTRY1", locationList, "country1-id"),
                Arguments.of("state11", locationList, "state11-id"),
                Arguments.of("city_x", locationList, null),
                Arguments.of(null, locationList, null),
                Arguments.of("DRIVER 1", driverList, "driver1-id"),
                Arguments.of("driver 2", driverList, "driver2-id"),
                Arguments.of("driver X", driverList, null),
                Arguments.of(null, driverList, null),
                Arguments.of("CAR 1", vehicleList, "vehicle1-id"),
                Arguments.of("truck 2", vehicleList, "vehicle2-id"),
                Arguments.of("truck X", vehicleList, null),
                Arguments.of(null, vehicleList, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideLocationCombination")
    void lookupLocationIdFromName_locationNameProvided_shouldReturnIdOrNull(String locationName,
                                                                            List<String> ancestors, String expected) {
        List<QPortalLocation> refLocationList = initializeRefLocationList();
        assertThat(QPortalUtils.lookupLocationIdFromName(locationName, ancestors, refLocationList)).isEqualTo(expected);
    }

    @Test
    void lookupLocationIdFromName_noLocationName_shouldReturnNull() {
        List<QPortalLocation> refLocationList = initializeRefLocationList();
        assertThat(QPortalUtils.lookupLocationIdFromName(null, null, refLocationList)).isNull();
    }

    @ParameterizedTest
    @MethodSource("provideQPortalNameAndList")
    void lookupIdFromName_nameProvided_shouldReturnIdOrNull(String name, List<? extends QPortalModel> qPortalList,
                                                            String expected) {
        assertThat(QPortalUtils.lookupIdFromName(name, qPortalList)).isEqualTo(expected);
    }
}
