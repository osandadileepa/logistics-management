package com.quincus.shipment.impl.validator;

import com.quincus.qportal.model.QPortalDriver;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalVehicle;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.dto.csv.MilestoneCsv;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MilestoneCsvValidatorTest {

    @InjectMocks
    private MilestoneCsvValidator validator;

    private static Stream<Arguments> provideValidLocationCombinationsForMilestoneCsv() {
        MilestoneCsv milestoneCsv1 = new MilestoneCsv();
        milestoneCsv1.setLatitude("1");
        milestoneCsv1.setLongitude("2");

        MilestoneCsv milestoneCsv2 = new MilestoneCsv();
        milestoneCsv2.setFromCountry("COUNTRY1");
        milestoneCsv2.setFromState("STATE1");
        milestoneCsv2.setFromCity("CITY1");
        milestoneCsv2.setFromFacility("FACILITY1");
        milestoneCsv2.setToCountry("COUNTRY2");
        milestoneCsv2.setToState("STATE2");
        milestoneCsv2.setToCity("CITY2");
        milestoneCsv2.setToFacility("FACILITY2");

        MilestoneCsv milestoneCsv3 = new MilestoneCsv();
        milestoneCsv3.setFromCountry("COUNTRY3");
        milestoneCsv3.setFromState("STATE3");
        milestoneCsv3.setFromCity("CITY3");
        milestoneCsv3.setToCountry("COUNTRY4");
        milestoneCsv3.setToState("STATE4");
        milestoneCsv3.setToCity("CITY4");

        MilestoneCsv milestoneCsv4 = new MilestoneCsv();
        milestoneCsv4.setLatitude("1");
        milestoneCsv4.setLongitude("2");
        milestoneCsv4.setFromCountry("COUNTRY5");
        milestoneCsv4.setFromState("STATE5");
        milestoneCsv4.setFromCity("CITY5");
        milestoneCsv4.setFromFacility("FACILITY5");
        milestoneCsv4.setToCountry("COUNTRY6");
        milestoneCsv4.setToState("STATE6");
        milestoneCsv4.setToCity("CITY6");
        milestoneCsv4.setToFacility("FACILITY6");

        MilestoneCsv milestoneCsv5 = new MilestoneCsv();
        milestoneCsv5.setFromLatitude("3");
        milestoneCsv5.setFromLongitude("4");
        milestoneCsv5.setToLatitude("5");
        milestoneCsv5.setToLongitude("6");

        MilestoneCsv milestoneCsv6 = new MilestoneCsv();
        milestoneCsv6.setFromCountry("COUNTRY3");
        milestoneCsv6.setFromState("STATE3");
        milestoneCsv6.setFromCity("CITY3");
        milestoneCsv6.setFromWard("WARD3");
        milestoneCsv6.setFromDistrict("DISTRICT3");
        milestoneCsv6.setToCountry("COUNTRY4");
        milestoneCsv6.setToState("STATE4");
        milestoneCsv6.setToCity("CITY4");
        milestoneCsv6.setToWard("WARD4");
        milestoneCsv6.setToDistrict("DISTRICT4");

        MilestoneCsv milestoneCsv7 = new MilestoneCsv();

        return Stream.of(
                Arguments.of(Named.of("Latitude + Longitude combination, No other location", milestoneCsv1)),
                Arguments.of(Named.of("Locations with facility", milestoneCsv2)),
                Arguments.of(Named.of("Locations without facility", milestoneCsv3)),
                Arguments.of(Named.of("All Locations + Latitude & Longitude", milestoneCsv4)),
                Arguments.of(Named.of("From/To Coordinates", milestoneCsv5)),
                Arguments.of(Named.of("Locations with Ward and District", milestoneCsv6)),
                Arguments.of(Named.of("No location provided", milestoneCsv7))
        );
    }

    private static Stream<Arguments> provideInvalidLocationCombinationsForMilestoneCsv() {
        MilestoneCsv milestoneCsv1x = new MilestoneCsv();
        milestoneCsv1x.setLatitude("1");

        MilestoneCsv milestoneCsv2x = new MilestoneCsv();
        milestoneCsv2x.setLongitude("2");

        MilestoneCsv milestoneCsv3x = new MilestoneCsv();
        milestoneCsv3x.setFromCountry("COUNTRY1");
        milestoneCsv3x.setFromState("STATE1");
        milestoneCsv3x.setFromCity("CITY1");
        milestoneCsv3x.setFromFacility("FACILITY1");
        milestoneCsv3x.setToCountry("COUNTRY2");

        MilestoneCsv milestoneCsv4x = new MilestoneCsv();
        milestoneCsv4x.setFromState("STATE3");

        MilestoneCsv milestoneCsv5x = new MilestoneCsv();
        milestoneCsv5x.setFromCity("CITY5");

        MilestoneCsv milestoneCsv6x = new MilestoneCsv();
        milestoneCsv6x.setFromFacility("FACILITY5");

        MilestoneCsv milestoneCsv7x = new MilestoneCsv();
        milestoneCsv7x.setFromCountry("COUNTRY5");
        milestoneCsv7x.setFromState("STATE5");

        MilestoneCsv milestoneCsv8x = new MilestoneCsv();
        milestoneCsv8x.setFromCountry("COUNTRY5");
        milestoneCsv8x.setFromCity("CITY5");

        MilestoneCsv milestoneCsv9x = new MilestoneCsv();
        milestoneCsv9x.setToCountry("COUNTRY6");
        milestoneCsv9x.setToFacility("FACILITY6");

        MilestoneCsv milestoneCsv10x = new MilestoneCsv();
        milestoneCsv10x.setToState("STATE6");
        milestoneCsv10x.setToCity("CITY6");

        MilestoneCsv milestoneCsv11x = new MilestoneCsv();
        milestoneCsv11x.setToState("STATE6");
        milestoneCsv11x.setToFacility("FACILITY6");

        MilestoneCsv milestoneCsv12x = new MilestoneCsv();
        milestoneCsv12x.setToCity("CITY6");
        milestoneCsv12x.setToFacility("FACILITY6");

        MilestoneCsv milestoneCsv13x = new MilestoneCsv();
        milestoneCsv13x.setToCountry("COUNTRY6");
        milestoneCsv13x.setToState("STATE6");
        milestoneCsv13x.setToFacility("FACILITY6");

        MilestoneCsv milestoneCsv14x = new MilestoneCsv();
        milestoneCsv14x.setToCountry("COUNTRY6");
        milestoneCsv14x.setToCity("CITY6");
        milestoneCsv14x.setToFacility("FACILITY6");

        MilestoneCsv milestoneCsv15x = new MilestoneCsv();
        milestoneCsv15x.setToState("STATE6");
        milestoneCsv15x.setToCity("CITY6");
        milestoneCsv15x.setToFacility("FACILITY6");

        MilestoneCsv milestoneCsv16x = new MilestoneCsv();
        milestoneCsv16x.setToWard("WARD6");

        MilestoneCsv milestoneCsv17x = new MilestoneCsv();
        milestoneCsv17x.setToDistrict("DISTRICT6");

        MilestoneCsv milestoneCsv18x = new MilestoneCsv();
        milestoneCsv18x.setToWard("WARD7");
        milestoneCsv18x.setToDistrict("DISTRICT7");

        MilestoneCsv milestoneCsv19x = new MilestoneCsv();
        milestoneCsv19x.setToCity("CITY8");
        milestoneCsv19x.setToWard("WARD8");
        milestoneCsv19x.setToDistrict("DISTRICT8");

        MilestoneCsv milestoneCsv20x = new MilestoneCsv();
        milestoneCsv20x.setToState("STATE9");
        milestoneCsv20x.setToCity("CITY9");
        milestoneCsv20x.setToWard("WARD9");
        milestoneCsv20x.setToDistrict("DISTRICT9");

        MilestoneCsv milestoneCsv21x = new MilestoneCsv();
        milestoneCsv21x.setToCity("CITY9");
        milestoneCsv21x.setToDistrict("DISTRICT9");

        MilestoneCsv milestoneCsv22x = new MilestoneCsv();
        milestoneCsv22x.setToCity("CITY9");
        milestoneCsv22x.setToWard("DISTRICT9");

        MilestoneCsv milestoneCsv23x = new MilestoneCsv();
        milestoneCsv23x.setFromCountry("COUNTRY10");
        milestoneCsv23x.setFromCity("CITY10");
        milestoneCsv23x.setFromWard("WARD10");
        milestoneCsv23x.setFromDistrict("DISTRICT10");

        MilestoneCsv milestoneCsv24x = new MilestoneCsv();
        milestoneCsv24x.setFromCountry("COUNTRY11");
        milestoneCsv24x.setFromState("STATE11");
        milestoneCsv24x.setFromWard("WARD11");
        milestoneCsv24x.setFromDistrict("DISTRICT11");

        MilestoneCsv milestoneCsv25x = new MilestoneCsv();
        milestoneCsv25x.setFromState("STATE12");
        milestoneCsv25x.setFromCity("CITY12");
        milestoneCsv25x.setFromWard("WARD12");
        milestoneCsv25x.setFromDistrict("DISTRICT12");

        MilestoneCsv milestoneCsv26x = new MilestoneCsv();
        milestoneCsv26x.setFromCountry("COUNTRY13");
        milestoneCsv26x.setFromState("STATE13");
        milestoneCsv26x.setFromFacility("FACILITY13");
        milestoneCsv26x.setFromWard("WARD13");
        milestoneCsv26x.setFromDistrict("DISTRICT13");

        MilestoneCsv milestoneCsv27x = new MilestoneCsv();
        milestoneCsv27x.setFromCountry("COUNTRY14");
        milestoneCsv27x.setFromCity("CITY14");
        milestoneCsv27x.setFromFacility("FACILITY14");
        milestoneCsv27x.setFromWard("WARD14");
        milestoneCsv27x.setFromDistrict("DISTRICT14");

        MilestoneCsv milestoneCsv28x = new MilestoneCsv();
        milestoneCsv28x.setFromState("STATE15");
        milestoneCsv28x.setFromCity("CITY15");
        milestoneCsv28x.setFromFacility("FACILITY15");
        milestoneCsv28x.setFromWard("WARD15");
        milestoneCsv28x.setFromDistrict("DISTRICT15");

        return Stream.of(
                Arguments.of(Named.of("Malformed: Latitude only", milestoneCsv1x)),
                Arguments.of(Named.of("Malformed: Longitude only", milestoneCsv2x)),
                Arguments.of(Named.of("Malformed: Country only", milestoneCsv3x)),
                Arguments.of(Named.of("Malformed: State only", milestoneCsv4x)),
                Arguments.of(Named.of("Malformed: City only", milestoneCsv5x)),
                Arguments.of(Named.of("Malformed: Facility only", milestoneCsv6x)),
                Arguments.of(Named.of("Malformed: Country + State only", milestoneCsv7x)),
                Arguments.of(Named.of("Malformed: Country + City only", milestoneCsv8x)),
                Arguments.of(Named.of("Malformed: Country + Facility only", milestoneCsv9x)),
                Arguments.of(Named.of("Malformed: State + City only", milestoneCsv10x)),
                Arguments.of(Named.of("Malformed: State + Facility only", milestoneCsv11x)),
                Arguments.of(Named.of("Malformed: City + Facility only", milestoneCsv12x)),
                Arguments.of(Named.of("Malformed: Country + State + Facility", milestoneCsv13x)),
                Arguments.of(Named.of("Malformed: Country + City + Facility", milestoneCsv14x)),
                Arguments.of(Named.of("Malformed: State + City + Facility", milestoneCsv15x)),
                Arguments.of(Named.of("Malformed: Ward only", milestoneCsv16x)),
                Arguments.of(Named.of("Malformed: District only", milestoneCsv17x)),
                Arguments.of(Named.of("Malformed: Ward + District only", milestoneCsv18x)),
                Arguments.of(Named.of("Malformed: City + Ward + District only", milestoneCsv19x)),
                Arguments.of(Named.of("Malformed: State + City + Ward + District only", milestoneCsv20x)),
                Arguments.of(Named.of("Malformed: City + District only", milestoneCsv21x)),
                Arguments.of(Named.of("Malformed: City + Ward only", milestoneCsv22x)),
                Arguments.of(Named.of("Malformed: Country + City + Ward + District only", milestoneCsv23x)),
                Arguments.of(Named.of("Malformed: Country + State + Ward + District only", milestoneCsv24x)),
                Arguments.of(Named.of("Malformed: State + City + Ward + District only", milestoneCsv25x)),
                Arguments.of(Named.of("Malformed: Country + State + Facility + Ward + District only", milestoneCsv26x)),
                Arguments.of(Named.of("Malformed: Country + City + Facility + Ward + District only", milestoneCsv27x)),
                Arguments.of(Named.of("Malformed: State + City + Facility + Ward + District only", milestoneCsv28x))
        );
    }

    private static Stream<Arguments> provideValidLocationCombinations() {
        String countryName = "COUNTRY 1";
        String stateName = "STATE 1";
        String cityName = "CITY 1";
        String wardName = "WARD 1";
        String districtName = "DISTRICT 1";
        String facilityName = "FACILITY 1";

        List<QPortalLocation> refLocations = new ArrayList<>();
        QPortalLocation city1 = new QPortalLocation();
        city1.setId("city-1");
        city1.setName(cityName);
        city1.setLocationType("City");
        city1.setActive(true);
        city1.setAncestors(String.format("%s, %s", countryName, stateName));
        refLocations.add(city1);

        QPortalLocation facility1 = new QPortalLocation();
        facility1.setId("facility-1");
        facility1.setName(facilityName);
        facility1.setLocationType("Facility");
        facility1.setActive(true);
        facility1.setAncestors(String.format("%s, %s, %s", countryName, stateName, cityName));
        refLocations.add(facility1);

        QPortalLocation district1 = new QPortalLocation();
        district1.setId("district 1");
        district1.setName(districtName);
        district1.setLocationType("District");
        district1.setActive(true);
        district1.setAncestors(String.format("%s, %s, %s, %s", countryName, stateName, cityName, wardName));
        refLocations.add(district1);

        return Stream.of(
                Arguments.of(null, null, null, cityName, stateName, countryName, refLocations),
                Arguments.of(facilityName, null, null, cityName, stateName, countryName, refLocations),
                Arguments.of(null, districtName, wardName, cityName, stateName, countryName, refLocations),
                Arguments.of(null, null, null, null, null, null, refLocations)
        );
    }

    @Test
    void validateFixedColumnSize_sizeMatches_shouldDoNothing() {
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length);

        List<String> errorMessages = new ArrayList<>();
        validator.validateFixedColumnSize(milestoneCsv, errorMessages);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void validateFixedColumnSize_sizeMismatch_shouldPopulateErrorList() {
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setSize(MilestoneCsv.getCsvHeaders().length + 1);

        List<String> errorMessages = new ArrayList<>();
        validator.validateFixedColumnSize(milestoneCsv, errorMessages);
        assertThat(errorMessages).isNotEmpty().hasSize(1);
    }

    @Test
    void validateDataAnnotations_allRequiredFieldsAvailable_shouldDoNothing() {
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setShipmentTrackingId("SHP1");
        milestoneCsv.setMilestoneCode("1000");
        milestoneCsv.setMilestoneTime("2023-05-09T14:17:00+08:00");

        List<String> errorMessages = new ArrayList<>();
        validator.validateDataAnnotations(milestoneCsv, errorMessages);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void validateDataAnnotations_noRequiredFields_shouldPopulateErrorList() {
        MilestoneCsv milestoneCsv = new MilestoneCsv();

        List<String> errorMessages = new ArrayList<>();
        validator.validateDataAnnotations(milestoneCsv, errorMessages);
        assertThat(errorMessages).isNotEmpty()
                .contains("Field `DateTime` must not be blank")
                .contains("Field `Code` must not be blank")
                .contains("Field `Shipment ID` must not be blank");
    }

    @Test
    void validateMilestoneCode_validCode_shouldDoNothing() {
        List<String> errorMessages = new ArrayList<>();
        validator.validateMilestoneCode(MilestoneCode.OM_BOOKED.toString(), errorMessages);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void validateMilestoneCode_invalidCode_shouldPopulateErrorList() {
        List<String> errorMessages = new ArrayList<>();
        validator.validateMilestoneCode("ABCD", errorMessages);
        assertThat(errorMessages).isNotEmpty();
    }

    @Test
    void validateDateTimeFormat_validDateTime_shouldDoNothing() {
        List<String> errorMessages = new ArrayList<>();
        validator.validateDateTimeFormat("2023-05-09T14:17:00+08:00", errorMessages);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void validateDateTimeFormat_invalidDateTime_shouldPopulateErrorList() {
        List<String> errorMessages = new ArrayList<>();
        validator.validateDateTimeFormat("2023-05-09 14:17:00+08:00", errorMessages);
        assertThat(errorMessages).isNotEmpty();
    }

    @Test
    void validateDateTimeFormat_noTime_shouldDoNothing() {
        List<String> errorMessages = new ArrayList<>();
        validator.validateDateTimeFormat(null, errorMessages);
        assertThat(errorMessages).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideValidLocationCombinationsForMilestoneCsv")
    void validateLocationCombinationAndCoordinates_validCombination_shouldDoNothing(MilestoneCsv milestoneCsv) {
        List<String> errorMessages = new ArrayList<>();
        validator.validateLocationCombinationAndCoordinates(milestoneCsv, errorMessages);
        assertThat(errorMessages).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidLocationCombinationsForMilestoneCsv")
    void validateLocationCombinationAndCoordinates_invalidCombination_shouldPopulateErrorList(MilestoneCsv milestoneCsv) {
        List<String> errorMessages = new ArrayList<>();
        validator.validateLocationCombinationAndCoordinates(milestoneCsv, errorMessages);
        assertThat(errorMessages).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideValidLocationCombinations")
    void validateQPortalLocationCombination_locationFound_shouldDoNothing(String facility, String district, String ward,
                                                                          String city, String state, String country,
                                                                          List<QPortalLocation> refList) {
        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setFromCountry(country);
        milestoneCsv.setFromState(state);
        milestoneCsv.setFromCity(city);
        milestoneCsv.setFromWard(ward);
        milestoneCsv.setFromDistrict(district);
        milestoneCsv.setFromFacility(facility);
        milestoneCsv.setToCountry(country);
        milestoneCsv.setToState(state);
        milestoneCsv.setToCity(city);
        milestoneCsv.setToWard(ward);
        milestoneCsv.setToDistrict(district);
        milestoneCsv.setToFacility(facility);

        List<String> errorMessages = new ArrayList<>();
        validator.validateQPortalLocationCombination(milestoneCsv, refList, errorMessages);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void validateQPortalLocationCombination_locationNotFound_shouldPopulateErrorList() {
        String countryName = "COUNTRY X";
        String stateName = "STATE X";
        String cityName = "CITY X";
        String wardName = "WARD X";
        String districtName = "DISTRICT X";
        String facilityName = "FACILITY X";

        MilestoneCsv milestoneCsv = new MilestoneCsv();
        milestoneCsv.setFromCountry(countryName);
        milestoneCsv.setFromState(stateName);
        milestoneCsv.setFromCity(cityName);
        milestoneCsv.setFromWard(wardName);
        milestoneCsv.setFromDistrict(districtName);
        milestoneCsv.setFromFacility(facilityName);
        milestoneCsv.setToCountry(countryName);
        milestoneCsv.setToState(stateName);
        milestoneCsv.setToCity(cityName);
        milestoneCsv.setToWard(wardName);
        milestoneCsv.setToDistrict(districtName);
        milestoneCsv.setToFacility(facilityName);

        List<String> errorMessages = new ArrayList<>();
        validator.validateQPortalLocationCombination(milestoneCsv, Collections.emptyList(), errorMessages);
        assertThat(errorMessages).isNotEmpty();
    }

    @Test
    void validateQPortalLocation_dataFound_shouldDoNothing() {
        String facility = "FACILITY 1";
        List<QPortalLocation> refList = new ArrayList<>();
        QPortalLocation locationRef = new QPortalLocation();
        locationRef.setId("facility-1");
        locationRef.setName(facility);
        refList.add(locationRef);

        List<String> errorMessages = new ArrayList<>();
        validator.validateQPortalLocation(facility, refList, errorMessages);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void validateQPortalLocation_dataNotFound_shouldPopulateErrorList() {
        List<QPortalLocation> refList = new ArrayList<>();
        QPortalLocation locationRef = new QPortalLocation();
        locationRef.setId("facility-1");
        locationRef.setName("FACILITY 1");
        refList.add(locationRef);

        List<String> errorMessages = new ArrayList<>();
        validator.validateQPortalLocation("FACILITY X", refList, errorMessages);
        assertThat(errorMessages).isNotEmpty();
    }

    @Test
    void validateQPortalLocation_noName_shouldDoNothing() {
        List<QPortalLocation> refList = new ArrayList<>();
        QPortalLocation locationRef = new QPortalLocation();
        locationRef.setId("facility-1");
        locationRef.setName("FACILITY 1");
        refList.add(locationRef);

        List<String> errorMessages = new ArrayList<>();
        validator.validateQPortalLocation(null, refList, errorMessages);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void validateQPortalDriver_dataFound_shouldDoNothing() {
        String driver = "DRIVER 1";
        List<QPortalDriver> refList = new ArrayList<>();
        QPortalDriver driverRef = new QPortalDriver();
        driverRef.setId("driver-1");
        driverRef.setName(driver);
        refList.add(driverRef);

        List<String> errorMessages = new ArrayList<>();
        validator.validateQPortalDriver(driver, refList, errorMessages);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void validateQPortalDriver_dataNotFound_shouldPopulateErrorList() {
        List<QPortalDriver> refList = new ArrayList<>();
        QPortalDriver driverRef = new QPortalDriver();
        driverRef.setId("driver-1");
        driverRef.setName("DRIVER 1");
        refList.add(driverRef);

        List<String> errorMessages = new ArrayList<>();
        validator.validateQPortalDriver("DRIVER X", refList, errorMessages);
        assertThat(errorMessages).isNotEmpty();
    }

    @Test
    void validateQPortalDriver_noName_shouldDoNothing() {
        List<QPortalDriver> refList = new ArrayList<>();
        QPortalDriver driverRef = new QPortalDriver();
        driverRef.setId("driver-1");
        driverRef.setName("DRIVER 1");
        refList.add(driverRef);

        List<String> errorMessages = new ArrayList<>();
        validator.validateQPortalDriver(null, refList, errorMessages);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void validateQPortalVehicle_dataFound_shouldDoNothing() {
        String vehicle = "TRUCK 1";
        List<QPortalVehicle> refList = new ArrayList<>();
        QPortalVehicle vehicleRef = new QPortalVehicle();
        vehicleRef.setId("driver-1");
        vehicleRef.setName(vehicle);
        refList.add(vehicleRef);

        List<String> errorMessages = new ArrayList<>();
        validator.validateQPortalVehicle(vehicle, refList, errorMessages);
        assertThat(errorMessages).isEmpty();
    }

    @Test
    void validateQPortalVehicle_dataNotFound_shouldPopulateErrorList() {
        List<QPortalVehicle> refList = new ArrayList<>();
        QPortalVehicle vehicleRef = new QPortalVehicle();
        vehicleRef.setId("vehicle-1");
        vehicleRef.setName("TRUCK 1");
        refList.add(vehicleRef);

        List<String> errorMessages = new ArrayList<>();
        validator.validateQPortalVehicle("TRUCK X", refList, errorMessages);
        assertThat(errorMessages).isNotEmpty();
    }

    @Test
    void validateQPortalVehicle_noName_shouldDoNothing() {
        List<QPortalVehicle> refList = new ArrayList<>();
        QPortalVehicle vehicleRef = new QPortalVehicle();
        vehicleRef.setId("driver-1");
        vehicleRef.setName("TRUCK 1");
        refList.add(vehicleRef);

        List<String> errorMessages = new ArrayList<>();
        validator.validateQPortalVehicle(null, refList, errorMessages);
        assertThat(errorMessages).isEmpty();
    }
}
