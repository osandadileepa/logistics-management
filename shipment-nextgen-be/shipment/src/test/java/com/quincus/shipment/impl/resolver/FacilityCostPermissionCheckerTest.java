package com.quincus.shipment.impl.resolver;

import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.impl.repository.AlertRepository;
import com.quincus.shipment.impl.repository.ShipmentRepository;
import com.quincus.web.common.multitenant.QuincusLocationCoverage;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityCostPermissionCheckerTest {

    private static Map<String, QuincusLocationCoverage.LocationType> supportedLocationTypes;

    @InjectMocks
    private FacilityLocationPermissionChecker facilityLocationPermissionChecker;
    @Spy
    private UserDetailsProvider userDetailsProvider = new UserDetailsProvider(new UserDetailsContextHolder());
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private AlertRepository alertRepository;

    @BeforeAll
    static void initializeLocationTypes() {
        supportedLocationTypes = new HashMap<>();

        QuincusLocationCoverage.LocationType countryLocationType = new QuincusLocationCoverage.LocationType();
        countryLocationType.setId("uuid_location_type_01");
        countryLocationType.setName(QuincusLocationCoverage.LOCATION_TYPE_COUNTRY);
        supportedLocationTypes.put(QuincusLocationCoverage.LOCATION_TYPE_COUNTRY, countryLocationType);

        QuincusLocationCoverage.LocationType stateLocationType = new QuincusLocationCoverage.LocationType();
        stateLocationType.setId("uuid_location_type_02");
        stateLocationType.setName(QuincusLocationCoverage.LOCATION_TYPE_STATE);
        supportedLocationTypes.put(QuincusLocationCoverage.LOCATION_TYPE_STATE, stateLocationType);

        QuincusLocationCoverage.LocationType cityLocationType = new QuincusLocationCoverage.LocationType();
        cityLocationType.setId("uuid_location_type_03");
        cityLocationType.setName(QuincusLocationCoverage.LOCATION_TYPE_CITY);
        supportedLocationTypes.put(QuincusLocationCoverage.LOCATION_TYPE_CITY, cityLocationType);

        QuincusLocationCoverage.LocationType facilityLocationType = new QuincusLocationCoverage.LocationType();
        facilityLocationType.setId("uuid_location_type_0N");
        facilityLocationType.setName(QuincusLocationCoverage.LOCATION_TYPE_FACILITY);
        supportedLocationTypes.put(QuincusLocationCoverage.LOCATION_TYPE_FACILITY, facilityLocationType);
    }

    private static QuincusLocationCoverage.LocationType getLocationTypeCountry() {
        return supportedLocationTypes.get(QuincusLocationCoverage.LOCATION_TYPE_COUNTRY);
    }

    private static QuincusLocationCoverage.LocationType getLocationTypeState() {
        return supportedLocationTypes.get(QuincusLocationCoverage.LOCATION_TYPE_STATE);
    }

    private static QuincusLocationCoverage.LocationType getLocationTypeCity() {
        return supportedLocationTypes.get(QuincusLocationCoverage.LOCATION_TYPE_CITY);
    }

    private static QuincusLocationCoverage.LocationType getLocationTypeFacility() {
        return supportedLocationTypes.get(QuincusLocationCoverage.LOCATION_TYPE_FACILITY);
    }

    private static Stream<Arguments> provideLocationCoverageAndFacilityCovered() {
        QuincusLocationCoverage facilityOnlyCoverage = new QuincusLocationCoverage();
        facilityOnlyCoverage.setId("uuid_facility_0001");
        facilityOnlyCoverage.setName("FACILITY 01");
        facilityOnlyCoverage.setLocationType(getLocationTypeFacility());
        facilityOnlyCoverage.setCityId("uuid_city_0001");
        facilityOnlyCoverage.setStateProvinceId("uuid_state_0001");
        facilityOnlyCoverage.setCountryId("uuid_country_0001");

        QuincusLocationCoverage cityWideCoverage = new QuincusLocationCoverage();
        cityWideCoverage.setId("uuid_city_0002");
        cityWideCoverage.setName("CITY 02");
        cityWideCoverage.setLocationType(getLocationTypeCity());
        cityWideCoverage.setStateProvinceId("uuid_state_0002");
        cityWideCoverage.setCountryId("uuid_country_0002");

        QuincusLocationCoverage stateWideCoverage = new QuincusLocationCoverage();
        stateWideCoverage.setId("uuid_state_0003");
        stateWideCoverage.setName("STATE 03");
        stateWideCoverage.setLocationType(getLocationTypeState());
        stateWideCoverage.setCountryId("uuid_country_0003");

        QuincusLocationCoverage countryWideCoverage = new QuincusLocationCoverage();
        countryWideCoverage.setId("uuid_country_0004");
        countryWideCoverage.setName("COUNTRY 04");
        countryWideCoverage.setLocationType(getLocationTypeCountry());

        Facility facility1 = new Facility();
        facility1.setExternalId("uuid_facility_0001");

        Facility facility2 = new Facility();
        facility2.setExternalId("uuid_facility_0002");
        Address facility2Location = new Address();
        facility2Location.setCityId("uuid_city_0002");
        facility2.setLocation(facility2Location);

        Facility facility3 = new Facility();
        facility3.setExternalId("uuid_facility_0003");
        Address facility3Location = new Address();
        facility3Location.setCityId("uuid_city_0003");
        facility3Location.setStateId("uuid_state_0003");
        facility3.setLocation(facility3Location);

        Facility facility4 = new Facility();
        facility4.setExternalId("uuid_facility_0004");
        Address facility4Location = new Address();
        facility4Location.setCityId("uuid_city_0004");
        facility4Location.setStateId("uuid_state_0004");
        facility4Location.setCountryId("uuid_country_0004");
        facility4.setLocation(facility4Location);

        return Stream.of(
                Arguments.of(Named.of("Covered Facility", List.of(facilityOnlyCoverage)), facility1),
                Arguments.of(Named.of("Covered City", List.of(cityWideCoverage)), facility2),
                Arguments.of(Named.of("Covered State", List.of(stateWideCoverage)), facility3),
                Arguments.of(Named.of("Covered Country", List.of(countryWideCoverage)), facility4)
        );
    }

    private static Stream<Arguments> provideLocationCoverageAndFacilityNotCovered() {
        QuincusLocationCoverage facilityOnlyCoverage = new QuincusLocationCoverage();
        facilityOnlyCoverage.setId("uuid_facility_0011");
        facilityOnlyCoverage.setName("FACILITY 11");
        facilityOnlyCoverage.setLocationType(getLocationTypeFacility());
        facilityOnlyCoverage.setCityId("uuid_city_0011");
        facilityOnlyCoverage.setStateProvinceId("uuid_state_0011");
        facilityOnlyCoverage.setCountryId("uuid_country_0011");

        QuincusLocationCoverage cityWideCoverage = new QuincusLocationCoverage();
        cityWideCoverage.setId("uuid_city_0012");
        cityWideCoverage.setName("CITY 12");
        cityWideCoverage.setLocationType(getLocationTypeCity());
        cityWideCoverage.setStateProvinceId("uuid_state_0012");
        cityWideCoverage.setCountryId("uuid_country_0012");

        QuincusLocationCoverage stateWideCoverage = new QuincusLocationCoverage();
        stateWideCoverage.setId("uuid_state_0013");
        stateWideCoverage.setName("STATE 13");
        stateWideCoverage.setLocationType(getLocationTypeState());
        stateWideCoverage.setCountryId("uuid_country_0013");

        QuincusLocationCoverage countryWideCoverage = new QuincusLocationCoverage();
        countryWideCoverage.setId("uuid_country_0014");
        countryWideCoverage.setName("COUNTRY 01");
        countryWideCoverage.setLocationType(getLocationTypeCountry());

        Facility facilityX1 = new Facility();
        facilityX1.setExternalId("uuid_facility_00X1");
        Address facilityX1Location = new Address();
        facilityX1Location.setCityId("uuid_city_0011");
        facilityX1Location.setStateId("uuid_state_0011");
        facilityX1Location.setCountryId("uuid_country_0011");
        facilityX1.setLocation(facilityX1Location);

        Facility facilityX2 = new Facility();
        facilityX2.setExternalId("uuid_facility_00X2");
        Address facilityX2Location = new Address();
        facilityX2Location.setCityId("uuid_city_00X2");
        facilityX2Location.setStateId("uuid_state_0012");
        facilityX2Location.setCountryId("uuid_country_0012");
        facilityX2.setLocation(facilityX2Location);

        Facility facilityX3 = new Facility();
        facilityX3.setExternalId("uuid_facility_00X3");
        Address facilityX3Location = new Address();
        facilityX3Location.setCityId("uuid_city_00X3");
        facilityX3Location.setStateId("uuid_state_00X3");
        facilityX3Location.setCountryId("uuid_country_0013");
        facilityX3.setLocation(facilityX3Location);

        Facility invalidFacility = new Facility();
        invalidFacility.setExternalId("uuid_facility_no_location");

        List<QuincusLocationCoverage> multipleLocationCoverage = List.of(facilityOnlyCoverage, cityWideCoverage,
                stateWideCoverage, stateWideCoverage);
        return Stream.of(
                Arguments.of(Named.of("Not Covered by City", multipleLocationCoverage), facilityX1),
                Arguments.of(Named.of("Not Covered by State", multipleLocationCoverage), facilityX2),
                Arguments.of(Named.of("Not Covered by Country", multipleLocationCoverage), facilityX3),
                Arguments.of(Named.of("Facility has no location", multipleLocationCoverage), invalidFacility)
        );
    }

    @Test
    void isShipmentIdAnySegmentLocationCovered_shipmentIdArgumentAndLocationCoverageProvided_shouldReturnTrue() {
        List<QuincusLocationCoverage> locationCoverages = new ArrayList<>();
        QuincusLocationCoverage facilityOnlyCoverage = new QuincusLocationCoverage();
        facilityOnlyCoverage.setId("uuid_facility_01");
        facilityOnlyCoverage.setName("FACILITY-01");
        facilityOnlyCoverage.setLocationType(getLocationTypeFacility());
        facilityOnlyCoverage.setCityId("uuid_city_01");
        facilityOnlyCoverage.setStateProvinceId("uuid_state_01");
        facilityOnlyCoverage.setCountryId("uuid_country_01");
        locationCoverages.add(facilityOnlyCoverage);

        String shipmentId = "shipment-1";

        doReturn(locationCoverages).when(userDetailsProvider).getCurrentLocationCoverages();
        doReturn(false).when(userDetailsProvider).isFromPreAuthenticatedSource();
        when(shipmentRepository.isShipmentIdAnySegmentLocationCovered(eq(shipmentId), anySet(), anySet(), anySet(),
                anySet())).thenReturn(true);

        assertThat(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(shipmentId)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void isShipmentIdAnySegmentLocationCovered_locationCoverageNotProvided_shouldReturnFalse(List<QuincusLocationCoverage> locationCoverages) {
        doReturn(false).when(userDetailsProvider).isFromPreAuthenticatedSource();
        doReturn(locationCoverages).when(userDetailsProvider).getCurrentLocationCoverages();

        assertThat(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered("shipment-1")).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void isShipmentIdAnySegmentLocationCovered_noShipmentId_shouldReturnFalse(String nullOrBlankId) {
        doReturn(false).when(userDetailsProvider).isFromPreAuthenticatedSource();
        assertThat(facilityLocationPermissionChecker.isShipmentIdAnySegmentLocationCovered(nullOrBlankId)).isFalse();
    }

    @Test
    void isShipmentTrackingIdAnySegmentLocationCovered_shipmentIdArgumentAndLocationCoverageProvided_shouldReturnTrue() {
        List<QuincusLocationCoverage> locationCoverages = new ArrayList<>();
        QuincusLocationCoverage facilityOnlyCoverage = new QuincusLocationCoverage();
        facilityOnlyCoverage.setId("uuid_facility_01");
        facilityOnlyCoverage.setName("FACILITY-01");
        facilityOnlyCoverage.setLocationType(getLocationTypeFacility());
        facilityOnlyCoverage.setCityId("uuid_city_01");
        facilityOnlyCoverage.setStateProvinceId("uuid_state_01");
        facilityOnlyCoverage.setCountryId("uuid_country_01");
        locationCoverages.add(facilityOnlyCoverage);

        String shipmentTrackingId = "shipment-tracking-id-1";

        doReturn(false).when(userDetailsProvider).isFromPreAuthenticatedSource();
        doReturn(locationCoverages).when(userDetailsProvider).getCurrentLocationCoverages();
        when(shipmentRepository.isShipmentTrackingIdAnySegmentLocationCovered(eq(shipmentTrackingId), anySet(), anySet(),
                anySet(), anySet())).thenReturn(true);

        assertThat(facilityLocationPermissionChecker.isShipmentTrackingIdAnySegmentLocationCovered(shipmentTrackingId)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void isShipmentTrackingIdAnySegmentLocationCovered_locationCoverageNotProvided_shouldReturnFalse(List<QuincusLocationCoverage> locationCoverages) {
        doReturn(false).when(userDetailsProvider).isFromPreAuthenticatedSource();
        doReturn(locationCoverages).when(userDetailsProvider).getCurrentLocationCoverages();

        assertThat(facilityLocationPermissionChecker.isShipmentTrackingIdAnySegmentLocationCovered("shipment-1")).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void isShipmentTrackingIdAnySegmentLocationCovered_noShipmentTrackingId_shouldReturnFalse(String nullOrBlankId) {
        doReturn(false).when(userDetailsProvider).isFromPreAuthenticatedSource();
        assertThat(facilityLocationPermissionChecker.isShipmentTrackingIdAnySegmentLocationCovered(nullOrBlankId)).isFalse();
    }

    @Test
    void isAlertIdAnySegmentLocationCovered_alertIdArgumentAndLocationCoverageProvided_shouldReturnTrue() {
        List<QuincusLocationCoverage> locationCoverages = new ArrayList<>();
        QuincusLocationCoverage facilityOnlyCoverage = new QuincusLocationCoverage();
        facilityOnlyCoverage.setId("uuid_facility_01");
        facilityOnlyCoverage.setName("FACILITY-01");
        facilityOnlyCoverage.setLocationType(getLocationTypeFacility());
        facilityOnlyCoverage.setCityId("uuid_city_01");
        facilityOnlyCoverage.setStateProvinceId("uuid_state_01");
        facilityOnlyCoverage.setCountryId("uuid_country_01");
        locationCoverages.add(facilityOnlyCoverage);

        String alertId = "alert-id-1";

        doReturn(false).when(userDetailsProvider).isFromPreAuthenticatedSource();
        doReturn(locationCoverages).when(userDetailsProvider).getCurrentLocationCoverages();
        when(alertRepository.isAlertFromSegmentLocationCovered(eq(alertId), anySet(), anySet(), anySet(), anySet()))
                .thenReturn(true);

        assertThat(facilityLocationPermissionChecker.isAlertIdAnySegmentLocationCovered(alertId)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void isAlertIdAnySegmentLocationCovered_locationCoverageNotProvided_shouldReturnFalse(List<QuincusLocationCoverage> locationCoverages) {
        doReturn(locationCoverages).when(userDetailsProvider).getCurrentLocationCoverages();
        doReturn(false).when(userDetailsProvider).isFromPreAuthenticatedSource();

        assertThat(facilityLocationPermissionChecker.isAlertIdAnySegmentLocationCovered("alert-1")).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void isAlertIdAnySegmentLocationCovered_noAlertId_shouldReturnFalse(String nullOrBlankId) {
        doReturn(false).when(userDetailsProvider).isFromPreAuthenticatedSource();
        assertThat(facilityLocationPermissionChecker.isAlertIdAnySegmentLocationCovered(nullOrBlankId)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideLocationCoverageAndFacilityCovered")
    void isFacilityLocationCovered_facilityCovered_shouldReturnTrue(List<QuincusLocationCoverage> locationCoverage,
                                                                    Facility facility) {
        doReturn(locationCoverage).when(userDetailsProvider).getCurrentLocationCoverages();
        assertThat(facilityLocationPermissionChecker.isFacilityLocationCovered(facility)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideLocationCoverageAndFacilityNotCovered")
    void isFacilityLocationCovered_facilityNotCovered_shouldReturnFalse(List<QuincusLocationCoverage> locationCoverage,
                                                                        Facility facility) {
        doReturn(locationCoverage).when(userDetailsProvider).getCurrentLocationCoverages();
        assertThat(facilityLocationPermissionChecker.isFacilityLocationCovered(facility)).isFalse();
    }

    @Test
    void isFacilityLocationCovered_noFacility_shouldReturnFalse() {
        assertThat(facilityLocationPermissionChecker.isFacilityLocationCovered(null)).isFalse();
    }

    @Test
    void isFacilityLocationCovered_unsupportedLocationCoverage_shouldReturnFalse() {
        QuincusLocationCoverage.LocationType locationType = new QuincusLocationCoverage.LocationType();
        locationType.setId("0");
        locationType.setName("OTHER");

        QuincusLocationCoverage unsupportedCoverage = new QuincusLocationCoverage();
        unsupportedCoverage.setId("uuid_level_x_0001");
        unsupportedCoverage.setName("X 01");
        unsupportedCoverage.setLocationType(locationType);
        unsupportedCoverage.setCityId("uuid_city_000X");
        unsupportedCoverage.setStateProvinceId("uuid_state_000X");
        unsupportedCoverage.setCountryId("uuid_country_000X");

        Facility facilityX1 = new Facility();
        facilityX1.setExternalId("uuid_facility_00X1");
        Address facilityX1Location = new Address();
        facilityX1Location.setCityId("uuid_city_0011");
        facilityX1Location.setStateId("uuid_state_0011");
        facilityX1Location.setCountryId("uuid_country_0011");
        facilityX1.setLocation(facilityX1Location);

        doReturn(List.of(unsupportedCoverage)).when(userDetailsProvider).getCurrentLocationCoverages();
        assertThat(facilityLocationPermissionChecker.isFacilityLocationCovered(facilityX1)).isFalse();
    }

    @Test
    void isFacilityLocationCovered_noLocationCoverage_shouldReturnFalse() {
        Facility facility1 = new Facility();
        facility1.setExternalId("uuid_facility_x");

        doReturn(Collections.emptyList()).when(userDetailsProvider).getCurrentLocationCoverages();
        assertThat(facilityLocationPermissionChecker.isFacilityLocationCovered(facility1)).isFalse();
    }
}
