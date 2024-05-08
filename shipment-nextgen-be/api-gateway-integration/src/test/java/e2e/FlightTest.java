package e2e;

import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.apigateway.domain.FlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import com.quincus.apigateway.test_utils.TestUtil;
import com.quincus.apigateway.web.FlightControllerImpl;
import com.quincus.apigateway.web.model.ApiResponse;
import com.quincus.web.common.config.JacksonConfiguration;
import com.quincus.web.common.exception.CommonExceptionHandler;
import com.quincus.web.common.exception.model.ApiCallException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = {FlightControllerImpl.class}, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {CommonExceptionHandler.class, FlightControllerImpl.class, JacksonConfiguration.class})
class FlightTest {
    @MockBean
    ApiGatewayApi apiGatewayApi;

    TestUtil testUtil = TestUtil.getInstance();

    @Test
    void searchFlightsApiGatewayReturnedFailureShouldRespondApiGatewayResponseCode(@Autowired MockMvc mvc) throws Exception {
        var urlDummy = "https://example/search/flight/schedules";
        var requestJson = testUtil.searchFlightSchedulesJson();
        var httpEntity = new HttpEntity<>(requestJson);
        var responseStatusDummy = HttpStatus.BAD_GATEWAY;
        var responseDummy = new ApiResponse<List<FlightSchedule>>();
        responseDummy.setMessage("Test Only.");
        var responseEntityDummy = new ResponseEntity<>(responseDummy, responseStatusDummy);

        given(apiGatewayApi.searchFlights(any(FlightScheduleSearchParameter.class)))
                .willThrow(new ApiCallException(urlDummy, httpEntity, responseEntityDummy));

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post("/flight/schedules")
                        .content(requestJson.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadGateway())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).contains(urlDummy);
    }

    @Test
    void searchFlightsApiGatewayHttpRequestEncounteredExceptionShouldRespondInternalServerError(@Autowired MockMvc mvc) throws Exception {
        var urlDummy = "https://example/search/flight/schedules";
        var requestJson = testUtil.searchFlightSchedulesJson();
        var httpEntity = new HttpEntity<>(requestJson);
        var errorMsgDummy = "Error Encountered. Expected, Test Only.";
        var exceptionDummy = new RestClientException("Expected Error, Test Only.");

        given(apiGatewayApi.searchFlights(any(FlightScheduleSearchParameter.class)))
                .willThrow(new ApiCallException(urlDummy, httpEntity, errorMsgDummy, exceptionDummy));

        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post("/flight/schedules")
                        .content(requestJson.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains(urlDummy, errorMsgDummy);
    }
}
