package e2e.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.apigateway.domain.FlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import com.quincus.apigateway.web.FlightControllerImpl;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.web.BaseControllerWebIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = {FlightControllerImpl.class})
@ContextConfiguration(classes = {FlightControllerImpl.class})
class FlightControllerWebControllerWebIT extends BaseControllerWebIT {

    private static final String FLIGHT_URL = "/flight";
    private static final String VALID_ORIGIN = "ABC";
    private static final String VALID_DESTINATION = "CBA";
    private static final String VALID_CARRIER = "123";

    @MockBean
    ApiGatewayApi gatewayApiMock;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Given a user, when a valid FlightScheduleSearchParameter is provided, the response should be OK.")
    void searchFlightSchedulesWhenValidDataThenShouldReturnSuccess() throws Exception {
        // GIVEN
        final FlightSchedule flightScheduleDummy = new FlightSchedule();
        flightScheduleDummy.setCarrier("given-carrier-01234");
        when(gatewayApiMock.searchFlights(any(FlightScheduleSearchParameter.class)))
                .thenReturn(List.of(flightScheduleDummy));

        final FlightScheduleSearchParameter flightScheduleSearchParam = new FlightScheduleSearchParameter();
        flightScheduleSearchParam.setOrigin(VALID_ORIGIN);
        flightScheduleSearchParam.setDestination(VALID_DESTINATION);
        flightScheduleSearchParam.setDepartureDate(LocalDate.now().minusDays(1));
        flightScheduleSearchParam.setCarrier(VALID_CARRIER);
        Request<FlightScheduleSearchParameter> request = new Request<>();
        request.setData(flightScheduleSearchParam);

        // WHEN
        final MvcResult result = performPostRequest(FLIGHT_URL + "/schedules", objectMapper.writeValueAsString(request));

        // THEN
        String responseContent = result.getResponse().getContentAsString();

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(JsonPath.parse(responseContent).read("$.data[*].carrier", String[].class))
                .contains(flightScheduleDummy.getCarrier());

    }

    @Test
    @DisplayName("Given origin & destination are more than max in FlightScheduleSearchParameter, should return BAD_REQUEST.")
    void searchFlightSchedulesWhenOriginDestinationMoreThanMaxThenShouldReturnBadRequest() throws Exception {
        // GIVEN
        final FlightScheduleSearchParameter flightScheduleSearchParam = new FlightScheduleSearchParameter();
        flightScheduleSearchParam.setOrigin("12345");
        flightScheduleSearchParam.setDestination("ABCDE");
        flightScheduleSearchParam.setCarrier(VALID_CARRIER);
        flightScheduleSearchParam.setDepartureDate(LocalDate.now().minusDays(1));
        Request<FlightScheduleSearchParameter> request = new Request<>();
        request.setData(flightScheduleSearchParam);

        // WHEN
        final MvcResult result = performPostRequest(FLIGHT_URL + "/schedules", objectMapper.writeValueAsString(request));

        // THEN
        assertThatErrorsContainMessages(
                result,
                new String[]{
                        "data.destination size must be between 1 and 3",
                        "data.origin size must be between 1 and 3"
                },
                2);
    }

    @Test
    @DisplayName("Given missing origin in FlightScheduleSearchParameter, should return BAD_REQUEST.")
    void searchFlightSchedulesWhenMissingOriginThenShouldReturnBadRequest() throws Exception {
        // GIVEN
        final FlightScheduleSearchParameter flightScheduleSearchParam = new FlightScheduleSearchParameter();
        flightScheduleSearchParam.setDestination(VALID_DESTINATION);
        flightScheduleSearchParam.setDepartureDate(LocalDate.now().minusDays(1));
        flightScheduleSearchParam.setCarrier(VALID_CARRIER);
        Request<FlightScheduleSearchParameter> request = new Request<>();
        request.setData(flightScheduleSearchParam);

        // WHEN
        final MvcResult result = performPostRequest(FLIGHT_URL + "/schedules", objectMapper.writeValueAsString(request));

        // THEN
        assertThatErrorsContainMessages(
                result,
                new String[]{
                        "data.origin must not be blank"
                },
                1);
    }

    @Test
    @DisplayName("Given missing destination in FlightScheduleSearchParameter, should return BAD_REQUEST.")
    void searchFlightSchedulesWhenMissingDestinationThenShouldReturnBadRequest() throws Exception {
        // GIVEN
        final FlightScheduleSearchParameter flightScheduleSearchParam = new FlightScheduleSearchParameter();
        flightScheduleSearchParam.setOrigin(VALID_ORIGIN);
        flightScheduleSearchParam.setDepartureDate(LocalDate.now().minusDays(1));
        flightScheduleSearchParam.setCarrier(VALID_CARRIER);
        Request<FlightScheduleSearchParameter> request = new Request<>();
        request.setData(flightScheduleSearchParam);

        // WHEN
        final MvcResult result = performPostRequest(FLIGHT_URL + "/schedules", objectMapper.writeValueAsString(request));

        // THEN
        assertThatErrorsContainMessages(
                result,
                new String[]{
                        "data.destination must not be blank"
                },
                1);
    }
    
    @Test
    @DisplayName("Given missing departure date in FlightScheduleSearchParameter, should return BAD_REQUEST.")
    void searchFlightSchedulesWhenMissingDepartureDateThenShouldReturnBadRequest() throws Exception {
        // GIVEN
        final FlightScheduleSearchParameter flightScheduleSearchParam = new FlightScheduleSearchParameter();
        flightScheduleSearchParam.setOrigin(VALID_ORIGIN);
        flightScheduleSearchParam.setDestination(VALID_DESTINATION);
        flightScheduleSearchParam.setCarrier(VALID_CARRIER);
        Request<FlightScheduleSearchParameter> request = new Request<>();
        request.setData(flightScheduleSearchParam);

        // WHEN
        final MvcResult result = performPostRequest(FLIGHT_URL + "/schedules", objectMapper.writeValueAsString(request));

        // THEN
        assertThatErrorsContainMessages(result, new String[]{"data.departureDate must not be null"}, 1);
    }

}
