package com.quincus.apigateway.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.apigateway.api.dto.APIGFlightScheduleSearchParameter;
import com.quincus.apigateway.config.ApiGatewayRestProperties;
import com.quincus.apigateway.domain.FlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import com.quincus.apigateway.mapper.FlightScheduleMapper;
import com.quincus.apigateway.web.model.ApiResponse;
import com.quincus.web.common.exception.model.ApiNetworkIssueException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiGatewayRestClientTest {

    final String baseUrlDummy = "https://example.com";
    final String flightPathDummy = "/flight_path";
    final String flightSchedulePathDummy = "/schedules";
    final String expectedUrl = baseUrlDummy + flightPathDummy + flightSchedulePathDummy;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    private ApiGatewayRestClient apiGatewayRestClient;
    @Mock
    private ApiGatewayRestProperties apiGatewayRestProperties;
    @Mock
    private FlightScheduleMapper flightScheduleMapper;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void mockWhenApiGatewayRestPropertiesAreResolved() {
        when(apiGatewayRestProperties.getBaseUrl()).thenReturn(baseUrlDummy);
        when(apiGatewayRestProperties.getFlightPath()).thenReturn(flightPathDummy);
        when(apiGatewayRestProperties.getFlightSchedulePath()).thenReturn(flightSchedulePathDummy);
    }

    @AfterEach
    public void validateExpectedURL() {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate, times(1)).exchange(urlCaptor.capture(), any(HttpMethod.class),
                any(HttpEntity.class), any(ParameterizedTypeReference.class));
        assertThat(urlCaptor.getValue()).withFailMessage("URL value mismatch.").isEqualTo(expectedUrl);
    }

    @Test
    void searchFlights_apiCallSuccess_shouldReturnFlightScheduleList() {
        APIGFlightScheduleSearchParameter searchParamEntityDummy = new APIGFlightScheduleSearchParameter();
        when(flightScheduleMapper.mapDomainToDto(any(FlightScheduleSearchParameter.class)))
                .thenReturn(searchParamEntityDummy);

        FlightSchedule flightScheduleDummy = new FlightSchedule();
        when(flightScheduleMapper.mapDtoListToDomainList(anyList())).thenReturn(List.of(flightScheduleDummy));

        ResponseEntity responseEntityMock = mock(ResponseEntity.class);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntityMock);
        when(responseEntityMock.getStatusCode()).thenReturn(HttpStatus.OK);

        ApiResponse responseBodyDummy = new ApiResponse<>();
        responseBodyDummy.setData(List.of(new FlightSchedule()));
        when(responseEntityMock.getBody()).thenReturn(responseBodyDummy);

        final List<FlightSchedule> flightScheduleList = apiGatewayRestClient.searchFlights(new FlightScheduleSearchParameter());

        assertThat(flightScheduleList).isNotEmpty();
    }

    @Test
    void searchFlights_apiCallFailure_shouldReturnThrowApiGatewayCallException() {

        APIGFlightScheduleSearchParameter searchParamEntityDummy = new APIGFlightScheduleSearchParameter();
        when(flightScheduleMapper.mapDomainToDto(any(FlightScheduleSearchParameter.class)))
                .thenReturn(searchParamEntityDummy);

        ResponseEntity responseEntityMock = mock(ResponseEntity.class);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntityMock);
        when(responseEntityMock.getStatusCode()).thenReturn(HttpStatus.UNPROCESSABLE_ENTITY);

        ApiResponse responseBodyDummy = new ApiResponse<>();
        responseBodyDummy.setData(List.of(new FlightSchedule()));
        when(responseEntityMock.getBody()).thenReturn(responseBodyDummy);

        FlightScheduleSearchParameter param = new FlightScheduleSearchParameter();
        assertThatThrownBy(() -> apiGatewayRestClient.searchFlights(param))
                .isInstanceOf(ApiNetworkIssueException.class);
    }

    @Test
    void searchFlights_exceptionOnApiCall_shouldReturnThrowApiGatewayCallException() {

        APIGFlightScheduleSearchParameter searchParamEntityDummy = new APIGFlightScheduleSearchParameter();
        when(flightScheduleMapper.mapDomainToDto(any(FlightScheduleSearchParameter.class)))
                .thenReturn(searchParamEntityDummy);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new RestClientException("Error. Test Only."));

        FlightScheduleSearchParameter param = new FlightScheduleSearchParameter();
        assertThatThrownBy(() -> apiGatewayRestClient.searchFlights(param))
                .isInstanceOf(ApiNetworkIssueException.class);
    }
}
