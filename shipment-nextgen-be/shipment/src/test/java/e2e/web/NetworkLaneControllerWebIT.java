package e2e.web;

import com.quincus.shipment.api.NetworkLaneApi;
import com.quincus.shipment.api.filter.NetworkLaneFilter;
import com.quincus.shipment.api.filter.NetworkLaneLocationFilter;
import com.quincus.shipment.api.filter.ServiceTypeFilter;
import com.quincus.shipment.impl.web.NetworkLaneControllerImpl;
import com.quincus.shipment.impl.web.exception.ShipmentExceptionHandler;
import com.quincus.web.common.exception.model.QuincusError;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.web.BaseShipmentControllerWebIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;

import java.util.List;

import static com.quincus.shipment.api.constant.ShipmentErrorCode.VALIDATION_ERROR;

@WebMvcTest(controllers = {NetworkLaneControllerImpl.class})
@ContextConfiguration(classes = {NetworkLaneControllerImpl.class, ShipmentExceptionHandler.class})
class NetworkLaneControllerWebIT extends BaseShipmentControllerWebIT {

    private static final String NETWORK_LANE_URL = "/network-lane";
    @MockBean
    private NetworkLaneApi networkLaneApi;

    @Override
    protected MockMvcConfigurer applySpringSecurity() {
        return SecurityMockMvcConfigurers.springSecurity();
    }

    @Test
    @WithMockUser
    void validateNetworkLanePaginationFilterMinimumOne() throws Exception {
        NetworkLaneFilter networkLaneFilter = new NetworkLaneFilter();
        networkLaneFilter.setSize(0);
        networkLaneFilter.setPageNumber(0);
        final String apiUrl = NETWORK_LANE_URL + "/list";
        final MvcResult result = performPostRequest(apiUrl, createRequest(networkLaneFilter));
        //THEN
        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(2)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("data.page_number", "must be greater than or equal to 1"),
                new FieldError("data.size", "must be greater than or equal to 1")
        );
        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser
    void validateNetworkLanePaginationFilterMaxOneHundred() throws Exception {
        NetworkLaneFilter networkLaneFilter = new NetworkLaneFilter();
        networkLaneFilter.setSize(101);
        networkLaneFilter.setPageNumber(2);// use 2 cause getter always returns value - 1
        final String apiUrl = NETWORK_LANE_URL + "/list";
        final MvcResult result = performPostRequest(apiUrl, createRequest(networkLaneFilter));
        //THEN
        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(1)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("data.size", "must be less than or equal to 100")
        );
        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser
    void validateNetworkLaneFilterCorrectUUIDForLocationFilter() throws Exception {
        NetworkLaneFilter networkLaneFilter = new NetworkLaneFilter();
        networkLaneFilter.setSize(100);
        networkLaneFilter.setPageNumber(2);// use 2 cause getter always returns value - 1

        NetworkLaneLocationFilter locationFilter = new NetworkLaneLocationFilter();
        locationFilter.setFacilityIds(List.of("xxx"));
        locationFilter.setCountryIds(List.of("xxx"));
        locationFilter.setStateIds(List.of("xxx"));
        locationFilter.setCityIds(List.of("xxx"));
        networkLaneFilter.setOriginLocations(locationFilter);
        networkLaneFilter.setDestinationLocations(locationFilter);
        networkLaneFilter.setFacilityLocations(locationFilter);

        final String apiUrl = NETWORK_LANE_URL + "/list";
        final MvcResult result = performPostRequest(apiUrl, createRequest(networkLaneFilter));
        //THEN
        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(12)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("data.facility_locations.country_ids[0]", "must be a valid UUIDv4 format")
                , new FieldError("data.facility_locations.state_ids[0]", "must be a valid UUIDv4 format")
                , new FieldError("data.facility_locations.city_ids[0]", "must be a valid UUIDv4 format")
                , new FieldError("data.facility_locations.facility_ids[0]", "must be a valid UUIDv4 format")
                , new FieldError("data.destination_locations.country_ids[0]", "must be a valid UUIDv4 format")
                , new FieldError("data.destination_locations.state_ids[0]", "must be a valid UUIDv4 format")
                , new FieldError("data.destination_locations.city_ids[0]", "must be a valid UUIDv4 format")
                , new FieldError("data.destination_locations.facility_ids[0]", "must be a valid UUIDv4 format")
                , new FieldError("data.origin_locations.country_ids[0]", "must be a valid UUIDv4 format")
                , new FieldError("data.origin_locations.state_ids[0]", "must be a valid UUIDv4 format")
                , new FieldError("data.origin_locations.city_ids[0]", "must be a valid UUIDv4 format")
                , new FieldError("data.origin_locations.facility_ids[0]", "must be a valid UUIDv4 format")
        );
        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    @Test
    @WithMockUser
    void validateNetworkLaneFilterCorrectServiceTypeFilter() throws Exception {
        NetworkLaneFilter networkLaneFilter = new NetworkLaneFilter();
        networkLaneFilter.setSize(100);
        networkLaneFilter.setPageNumber(2);// use 2 cause getter always returns value - 1

        ServiceTypeFilter serviceTypeFilter = new ServiceTypeFilter();
        serviceTypeFilter.setId("xxx");
        serviceTypeFilter.setName("thisIsAServiceTypeThatIsMoreThanOneHundredTwentyEightCharactersThatIsTheLimitForTheStringNameCriteriaSetForServiceTypeJustToTest!");
        networkLaneFilter.setServiceTypes(List.of(serviceTypeFilter));

        final String apiUrl = NETWORK_LANE_URL + "/list";
        final MvcResult result = performPostRequest(apiUrl, createRequest(networkLaneFilter));
        //THEN
        Response<QuincusError> response = extractErrorResponse(result);
        ExpectedError expectedError = ExpectedError.builder()
                .message("There is a validation error in your request")
                .status(HttpStatus.BAD_REQUEST)
                .errorCode(VALIDATION_ERROR.name())
                .fieldErrorSize(2)
                .build();

        List<FieldError> expectedFieldErrors = buildErrorList(
                new FieldError("data.service_types[0].id", "must be a valid UUIDv4 format")
                , new FieldError("data.service_types[0].name", "size must be between 0 and 128")
        );
        assertCommonErrorStructure(response, expectedError);
        assertFieldErrors(response.getData().fieldErrors(), expectedFieldErrors);
    }

    private Request<NetworkLaneFilter> createRequest(NetworkLaneFilter networkLaneFilter) {
        Request<NetworkLaneFilter> request = new Request<>();
        request.setData(networkLaneFilter);
        return request;
    }
}
