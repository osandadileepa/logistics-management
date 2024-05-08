package com.quincus.apigateway.api;

import com.quincus.apigateway.api.dto.APIGFlightSchedule;
import com.quincus.apigateway.api.dto.APIGFlightScheduleSearchParameter;
import com.quincus.apigateway.config.ApiGatewayRestProperties;
import com.quincus.apigateway.domain.FlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import com.quincus.apigateway.mapper.FlightScheduleMapper;
import com.quincus.apigateway.web.model.ApiRequest;
import com.quincus.apigateway.web.model.ApiResponse;
import com.quincus.web.common.exception.model.ApiCallException;
import com.quincus.web.common.exception.model.ApiNetworkIssueException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ApiGatewayRestClient {

    public static final String APIG_REST_CALL_ERROR = "Encountered an issue while making an APIG Rest call to `%s` Flight Schedules.. Error message: `%s`.";
    private final ApiGatewayRestProperties apiGatewayRestProperties;

    private final FlightScheduleMapper flightScheduleMapper;

    private final RestTemplate restTemplate;

    public ApiGatewayRestClient(ApiGatewayRestProperties apiGatewayRestProperties,
                                FlightScheduleMapper flightScheduleMapper,
                                @Qualifier("apiGatewayRestTemplate") RestTemplate restTemplate) {
        this.apiGatewayRestProperties = apiGatewayRestProperties;
        this.flightScheduleMapper = flightScheduleMapper;
        this.restTemplate = restTemplate;
    }

    private String getFlightBaseUrl() {
        return apiGatewayRestProperties.getBaseUrl() + apiGatewayRestProperties.getFlightPath();
    }

    public List<FlightSchedule> searchFlights(FlightScheduleSearchParameter parameter) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        APIGFlightScheduleSearchParameter parameterDto = flightScheduleMapper.mapDomainToDto(parameter);

        ApiRequest<APIGFlightScheduleSearchParameter> requestBody = new ApiRequest<>(parameterDto);
        HttpEntity<ApiRequest<APIGFlightScheduleSearchParameter>> httpRequest = new HttpEntity<>(requestBody, headers);
        log.info("Request for API Gateway search Flight Schedules: {}", httpRequest);

        String url = getFlightBaseUrl() + apiGatewayRestProperties.getFlightSchedulePath();

        ResponseEntity<ApiResponse<List<APIGFlightSchedule>>> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpRequest,
                    new ParameterizedTypeReference<>() {});
            log.info("Response from API Gateway search Flight Schedules: {}", responseEntity);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new ApiCallException(url, httpRequest, responseEntity);
            }

            ApiResponse<List<APIGFlightSchedule>> responseBody = responseEntity.getBody();
            if (responseBody == null) {
                return Collections.emptyList();
            }
            return flightScheduleMapper.mapDtoListToDomainList(responseBody.getData());
        } catch (final ResourceAccessException | HttpServerErrorException e) {
            log.error(String.format(APIG_REST_CALL_ERROR, url, e.getMessage()), e);
            throw new ApiNetworkIssueException(String.format(APIG_REST_CALL_ERROR, url, e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (final HttpClientErrorException e) {
            log.error(String.format(APIG_REST_CALL_ERROR, url, e.getMessage()), e);
            throw new ApiNetworkIssueException(String.format(APIG_REST_CALL_ERROR, url, e.getMessage()), e.getStatusCode());
        } catch (final Exception e) {
            log.error(String.format(APIG_REST_CALL_ERROR, url, e.getMessage()), e);
            throw new ApiNetworkIssueException(String.format(APIG_REST_CALL_ERROR, url, e.getMessage()), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}
