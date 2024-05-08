package com.quincus.apigateway.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.apigateway.config.ApiGatewayRestProperties;
import com.quincus.apigateway.web.model.ApiGatewayAssignVendorDetailRequest;
import com.quincus.apigateway.web.model.ApiGatewayCheckInRequest;
import com.quincus.apigateway.web.model.ApiGatewayUpdateOrderAdditionalChargesRequest;
import com.quincus.apigateway.web.model.ApiGatewayUpdateOrderProgressRequest;
import com.quincus.apigateway.web.model.ApiGatewayWebhookResponse;
import com.quincus.apigateway.web.model.ApiResponse;
import com.quincus.web.common.exception.model.ApiCallException;
import com.quincus.web.common.exception.model.ApiNetworkIssueException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Service
@Slf4j
public class ApiGatewayWebhookClient {
    private static final String AUTHORIZATION = "X-API-AUTHORIZATION";
    private static final String ORGANIZATION_ID = "X-ORGANISATION-ID";
    private static final String ASSIGN_VENDOR_DETAILS = "assignVendorDetails";
    private static final String UPDATE_ORDER_PROGRESS = "updateOrderProgress";
    private static final String UPDATE_ORDER_ADDITIONAL_CHARGES = "updateOrderAdditionalCharges";
    private static final String CHECK_IN = "checkIn";
    private static final String REQUEST_PARSING_ERROR_MSG = "Error occurred while converting request to JSON string";
    private static final String CALLING_WEBHOOK_SEGMENT_PARAM = "Calling `{}` webhook for orderNo `{}` segmentId `{}`";
    private static final String WEBHOOK_CALL_ERROR = "Encountered an issue while making an APIG Webhook call to `%s` for organization id `%s`. Error message: `%s`.";
    private static final String WEBHOOK_RESPONSE = "Response received for orderNo `{}` response `{}`";
    private final RestTemplate restTemplate;
    private final ApiGatewayRestProperties apiGatewayRestProperties;
    private final ObjectMapper objectMapper;

    public ApiGatewayWebhookClient(ApiGatewayRestProperties apiGatewayRestProperties,
                                   @Qualifier("apiGatewayObjectMapper") ObjectMapper objectMapper,
                                   @Qualifier("apiGatewayRestTemplate") RestTemplate restTemplate) {
        this.apiGatewayRestProperties = apiGatewayRestProperties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    public ApiGatewayWebhookResponse assignVendorDetails(final String organizationId,
                                                         final ApiGatewayAssignVendorDetailRequest apiGatewayAssignVendorDetailRequest) {
        log.info(CALLING_WEBHOOK_SEGMENT_PARAM, ASSIGN_VENDOR_DETAILS, apiGatewayAssignVendorDetailRequest.getOrderNo(),
                apiGatewayAssignVendorDetailRequest.getSegmentId());
        String url = getUrl(apiGatewayRestProperties.getAssignVendorDetailsWebhook());
        HttpEntity<String> request = generateHttpRequest(organizationId, apiGatewayAssignVendorDetailRequest);
        try {
            ResponseEntity<? extends ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    ApiResponse.class);
            log.info(WEBHOOK_RESPONSE, apiGatewayAssignVendorDetailRequest.getOrderNo(), response);
            return objectMapper.convertValue(response.getBody(), ApiGatewayWebhookResponse.class);
        } catch (final ResourceAccessException | HttpServerErrorException e) {
            log.error(String.format(WEBHOOK_CALL_ERROR, url, organizationId, e.getMessage()), e);
            throw new ApiNetworkIssueException(String.format(WEBHOOK_CALL_ERROR, ASSIGN_VENDOR_DETAILS, organizationId, e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (final HttpClientErrorException e) {
            log.error(String.format(WEBHOOK_CALL_ERROR, url, organizationId, e.getMessage()), e);
            throw new ApiCallException(String.format(WEBHOOK_CALL_ERROR, ASSIGN_VENDOR_DETAILS, organizationId, e.getMessage()), e.getStatusCode());
        } catch (final Exception e) {
            log.error(String.format(WEBHOOK_CALL_ERROR, url, organizationId, e.getMessage()), e);
            throw new ApiCallException(String.format(WEBHOOK_CALL_ERROR, ASSIGN_VENDOR_DETAILS, organizationId, e.getMessage()), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public ApiGatewayWebhookResponse updateOrderProgress(final String organizationId,
                                                         final ApiGatewayUpdateOrderProgressRequest apiGatewayUpdateOrderProgressRequest) {
        log.info(CALLING_WEBHOOK_SEGMENT_PARAM, UPDATE_ORDER_PROGRESS, apiGatewayUpdateOrderProgressRequest.getOrderNo(),
                apiGatewayUpdateOrderProgressRequest.getSegmentId());
        String url = getUrl(apiGatewayRestProperties.getUpdateOrderProgressWebhook());
        HttpEntity<String> request = generateHttpRequest(organizationId, apiGatewayUpdateOrderProgressRequest);
        try {
            ResponseEntity<? extends ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    ApiResponse.class);
            log.info(WEBHOOK_RESPONSE, apiGatewayUpdateOrderProgressRequest.getOrderNo(), response);
            return objectMapper.convertValue(response.getBody(), ApiGatewayWebhookResponse.class);
        } catch (final ResourceAccessException | HttpServerErrorException e) {
            log.error(String.format(WEBHOOK_CALL_ERROR, url, organizationId, e.getMessage()), e);
            throw new ApiNetworkIssueException(String.format(WEBHOOK_CALL_ERROR, UPDATE_ORDER_PROGRESS, organizationId, e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (HttpClientErrorException e) {
            log.error(String.format(WEBHOOK_CALL_ERROR, url, organizationId, e.getMessage()), e);
            throw new ApiCallException(String.format(WEBHOOK_CALL_ERROR, UPDATE_ORDER_PROGRESS, organizationId, e.getMessage()), e.getStatusCode());
        } catch (final Exception e) {
            log.error(String.format(WEBHOOK_CALL_ERROR, url, organizationId, e.getMessage()), e);
            throw new ApiCallException(String.format(WEBHOOK_CALL_ERROR, UPDATE_ORDER_PROGRESS, organizationId, e.getMessage()), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public ApiGatewayWebhookResponse updateOrderAdditionalCharges(final String organizationId,
                                                                  final ApiGatewayUpdateOrderAdditionalChargesRequest apiGatewayUpdateOrderAdditionalChargesRequest) throws JsonProcessingException {
        log.info(CALLING_WEBHOOK_SEGMENT_PARAM, UPDATE_ORDER_ADDITIONAL_CHARGES, apiGatewayUpdateOrderAdditionalChargesRequest.getOrderNo(),
                apiGatewayUpdateOrderAdditionalChargesRequest.getSegmentId());
        String url = getUrl(apiGatewayRestProperties.getUpdateOrderAdditionalChargesWebhook());
        HttpEntity<String> request = generateHttpRequest(organizationId, apiGatewayUpdateOrderAdditionalChargesRequest);
        try {
            ResponseEntity<? extends ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    ApiResponse.class);
            log.info(WEBHOOK_RESPONSE, apiGatewayUpdateOrderAdditionalChargesRequest.getOrderNo(), response);
            return objectMapper.convertValue(response.getBody(), ApiGatewayWebhookResponse.class);
        } catch (final ResourceAccessException | HttpServerErrorException e) {
            log.error(String.format(WEBHOOK_CALL_ERROR, url, organizationId, e.getMessage()), e);
            throw new ApiNetworkIssueException(String.format(WEBHOOK_CALL_ERROR, UPDATE_ORDER_ADDITIONAL_CHARGES, organizationId, e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (HttpClientErrorException e) {
            log.error(String.format(WEBHOOK_CALL_ERROR, url, organizationId, e.getMessage()), e);
            throw new ApiCallException(String.format(WEBHOOK_CALL_ERROR, UPDATE_ORDER_ADDITIONAL_CHARGES, organizationId, e.getMessage()), e.getStatusCode());
        } catch (final Exception e) {
            log.error(String.format(WEBHOOK_CALL_ERROR, url, organizationId, e.getMessage()), e);
            throw new ApiCallException(String.format(WEBHOOK_CALL_ERROR, UPDATE_ORDER_ADDITIONAL_CHARGES, organizationId, e.getMessage()), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public ApiGatewayWebhookResponse checkIn(final String organizationId,
                                             final ApiGatewayCheckInRequest apiGatewayCheckinRequest) {
        log.info(CALLING_WEBHOOK_SEGMENT_PARAM, CHECK_IN, apiGatewayCheckinRequest.getOrderNo(),
                apiGatewayCheckinRequest.getSegmentId());
        String url = getUrl(apiGatewayRestProperties.getCheckInWebhook());
        HttpEntity<String> request = generateHttpRequest(organizationId, apiGatewayCheckinRequest);
        try {
            ResponseEntity<? extends ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    ApiResponse.class);
            log.info(WEBHOOK_RESPONSE, apiGatewayCheckinRequest.getOrderNo(), response);
            return objectMapper.convertValue(response.getBody(), ApiGatewayWebhookResponse.class);
        } catch (final ResourceAccessException | HttpServerErrorException e) {
            log.error(String.format(WEBHOOK_CALL_ERROR, url, organizationId, e.getMessage()), e);
            throw new ApiNetworkIssueException(String.format(WEBHOOK_CALL_ERROR, CHECK_IN, organizationId, e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (HttpClientErrorException e) {
            log.error(String.format(WEBHOOK_CALL_ERROR, url, organizationId, e.getMessage()), e);
            throw new ApiCallException(String.format(WEBHOOK_CALL_ERROR, CHECK_IN, organizationId, e.getMessage()), e.getStatusCode());
        } catch (Exception e) {
            log.error(String.format(WEBHOOK_CALL_ERROR, url, organizationId, e.getMessage()), e);
            throw new ApiCallException(String.format(WEBHOOK_CALL_ERROR, CHECK_IN, organizationId, e.getMessage()), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private String getUrl(String api) {
        return apiGatewayRestProperties.getWebhookBaseUrl() + api;
    }

    private HttpEntity<String> generateHttpRequest(String organizationId, Object request) {
        try {
            final String body = objectMapper.writeValueAsString(request);
            HttpHeaders headers = generateHeaders(organizationId, body);
            return new HttpEntity<>(body, headers);
        } catch (JsonProcessingException e) {
            log.error(REQUEST_PARSING_ERROR_MSG, e);
            throw new QuincusValidationException(REQUEST_PARSING_ERROR_MSG + e.getMessage());
        }
    }

    private HttpHeaders generateHeaders(final String organizationId, final String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, apiGatewayRestProperties.getS2sToken());
        if (organizationId != null) {
            headers.add(ORGANIZATION_ID, organizationId);
        }
        headers.setContentLength(body.length());
        return headers;
    }
}
