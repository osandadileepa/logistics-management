package e2e.web;

import com.quincus.shipment.api.FilterApi;
import com.quincus.shipment.impl.repository.entity.LocationHierarchyEntity_;
import com.quincus.shipment.impl.web.LocationHierarchyControllerImpl;
import com.quincus.shipment.impl.web.exception.ShipmentExceptionHandler;
import com.quincus.web.common.exception.model.QuincusError;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.web.BaseShipmentControllerWebIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.quincus.shipment.api.constant.ShipmentErrorCode.VALIDATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = {LocationHierarchyControllerImpl.class})
@ContextConfiguration(classes = {LocationHierarchyControllerImpl.class, ShipmentExceptionHandler.class})
class LocationHierarchyControllerWebIT extends BaseShipmentControllerWebIT {
    private static final String LOCATIONS_HIERARCHIES_URL = "/filter/location_hierarchies";
    private static final String LOCATIONS_STATES_URL = "/filter/states";
    private static final String LOCATIONS_CITIES_URL = "/filter/cities";
    private static final String LOCATIONS_FACILITIES_URL = "/filter/facilities";

    @MockBean
    private FilterApi filterApi;

    @Test
    @DisplayName("Given all valid parameters are supplied WHEN /filter/location_hierarchies THEN response should be OK")
    void shouldReturnOKForFindLocationHierarchies() throws Exception {
        //GIVEN
        String countryId = UUID.randomUUID().toString();
        String stateId = UUID.randomUUID().toString();
        String cityId = UUID.randomUUID().toString();
        String facilityId = UUID.randomUUID().toString();
        String key = "Key has max value of 2000";
        int level = 4;
        int page = 1;
        int per_page = 100;

        //WHEN
        final String apiUrl = LOCATIONS_HIERARCHIES_URL
                + "?country_id=" + countryId
                + "&state_id=" + stateId
                + "&city_id=" + cityId
                + "&facility_id=" + facilityId
                + "&key=" + URLEncoder.encode(key, StandardCharsets.UTF_8)
                + "&level=" + level
                + "&page=" + page
                + "&per_page=" + per_page;

        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Given all invalid parameters are supplied WHEN /filter/location_hierarchies THEN response should be BAD REQUEST")
    void shouldReturnBadRequestForFindLocationHierarchies() throws Exception {
        //GIVEN
        String countryId = "Invalid id";
        String stateId = "Invalid id";
        String cityId = "Invalid id";
        String facilityId = "Invalid id";
        String key = generateStringWithLength(2001);
        int level = 5;
        int page = 1;
        int per_page = 2000;

        //WHEN
        final String apiUrl = LOCATIONS_HIERARCHIES_URL
                + "?country_id=" + countryId
                + "&state_id=" + stateId
                + "&city_id=" + cityId
                + "&facility_id=" + facilityId
                + "&key=" + URLEncoder.encode(key, StandardCharsets.UTF_8)
                + "&level=" + level
                + "&page=" + page
                + "&per_page=" + per_page;

        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(7)
                .build();

        assertCommonErrorStructure(response, expectedError);
    }

    @Test
    @DisplayName("Given all valid parameters are supplied WHEN /filter/states THEN response should be OK")
    void shouldReturnOKForFindAllStatesByCountry() throws Exception {
        //GIVEN
        String countryId = UUID.randomUUID().toString();
        String sortBy = LocationHierarchyEntity_.FACILITY_NAME;
        int page = 1;
        int per_page = 100;

        //WHEN
        final String apiUrl = LOCATIONS_STATES_URL
                + "?country_id=" + countryId
                + "&page=" + page
                + "&per_page=" + per_page
                + "&sort_by" + sortBy;

        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Given all invalid parameters are supplied WHEN /filter/states THEN response should be BAD REQUEST")
    void shouldReturnBadRequestForFindAllStatesByCountry() throws Exception {
        //GIVEN
        String countryId = "Invalid name";
        String sortBy = generateStringWithLength(25);
        int page = 1;
        int per_page = 20000;

        //WHEN
        final String apiUrl = LOCATIONS_STATES_URL
                + "?country_id=" + countryId
                + "&page=" + page
                + "&per_page=" + per_page
                + "&sort_by=" + sortBy;

        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(3)
                .build();

        assertCommonErrorStructure(response, expectedError);
    }

    @Test
    @DisplayName("Given all valid parameters are supplied WHEN /filter/cities THEN response should be OK")
    void shouldReturnOKForFindAllCitiesByStateCountry() throws Exception {
        //GIVEN
        String countryId = UUID.randomUUID().toString();
        String stateId = UUID.randomUUID().toString();
        String sortBy = LocationHierarchyEntity_.FACILITY_NAME;
        int page = 1;
        int per_page = 100;

        //WHEN
        final String apiUrl = LOCATIONS_CITIES_URL
                + "?country_id=" + countryId
                + "&state_id=" + stateId
                + "&sort_by=" + sortBy
                + "&page=" + page
                + "&per_page=" + per_page;

        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Given all valid parameters are supplied WHEN /filter/cities THEN response should be BAD REQUEST")
    void shouldReturnBadRequestForFindAllCitiesByStateCountry() throws Exception {
        //GIVEN
        String countryId = "Invalid id";
        String stateId = "Invalid id";
        String sortBy = generateStringWithLength(2000);
        int page = 1;
        int per_page = 20000;

        //WHEN
        final String apiUrl = LOCATIONS_CITIES_URL
                + "?country_id=" + countryId
                + "&state_id=" + stateId
                + "&sort_by=" + sortBy
                + "&page=" + page
                + "&per_page=" + per_page;

        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(4)
                .build();

        assertCommonErrorStructure(response, expectedError);
    }

    @Test
    @DisplayName("Given all valid parameters are supplied WHEN /filter/facilities THEN response should be OK")
    void shouldReturnOKForFindAllFacilitiesByCityStateCountry() throws Exception {
        //GIVEN
        String countryId = UUID.randomUUID().toString();
        String stateId = UUID.randomUUID().toString();
        String cityId = UUID.randomUUID().toString();
        String sortBy = LocationHierarchyEntity_.FACILITY_NAME;
        int page = 1;
        int per_page = 100;

        //WHEN
        final String apiUrl = LOCATIONS_FACILITIES_URL
                + "?country_id=" + countryId
                + "&state_id=" + stateId
                + "&city_id=" + cityId
                + "&sort_by=" + sortBy
                + "&page=" + page
                + "&per_page=" + per_page;

        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Given all valid parameters are supplied WHEN /filter/facilities THEN response should be BAD REQUEST")
    void shouldReturnBadRequestForFindAllFacilitiesByCityStateCountry() throws Exception {
        //GIVEN
        String countryId = "Invalid id";
        String stateId = "Invalid id";
        String cityId = "Invalid id";
        String sortBy = generateStringWithLength(2000);
        int page = 1;
        int per_page = 20000;

        //WHEN
        final String apiUrl = LOCATIONS_FACILITIES_URL
                + "?country_id=" + countryId
                + "&state_id=" + stateId
                + "&city_id=" + cityId
                + "&sort_by=" + sortBy
                + "&page=" + page
                + "&per_page=" + per_page;

        final MvcResult result = performGetRequest(apiUrl);

        //THEN
        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(5)
                .build();

        assertCommonErrorStructure(response, expectedError);
    }

    private static String generateStringWithLength(int length) {
        return "a".repeat(length);
    }

}
