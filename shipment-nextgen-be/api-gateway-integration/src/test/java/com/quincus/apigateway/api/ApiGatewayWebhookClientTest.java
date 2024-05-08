package com.quincus.apigateway.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.apigateway.config.ApiGatewayRestProperties;
import com.quincus.apigateway.web.model.ApiGatewayAssignVendorDetailRequest;
import com.quincus.apigateway.web.model.ApiGatewayCheckInRequest;
import com.quincus.apigateway.web.model.ApiGatewayUpdateOrderProgressRequest;
import com.quincus.apigateway.web.model.ApiResponse;
import com.quincus.web.common.exception.model.ApiCallException;
import com.quincus.web.common.exception.model.ApiNetworkIssueException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiGatewayWebhookClientTest {
    @InjectMocks
    private ApiGatewayWebhookClient apiGatewayWebHookClient;
    @Mock
    private ApiGatewayRestProperties apiGatewayRestProperties;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ResponseEntity<ApiResponse> apiResponse;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    void assignVendorDetails_shouldCallWebhookUsingRestTemplate() throws JsonProcessingException {
        ApiGatewayAssignVendorDetailRequest request = new ApiGatewayAssignVendorDetailRequest();
        request.setOrderNo("orderNo");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ApiResponse.class))).thenReturn(apiResponse);
        when(objectMapper.writeValueAsString(request)).thenReturn(request.toString());

        apiGatewayWebHookClient.assignVendorDetails("organization-id", request);

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(ApiResponse.class));
        verify(objectMapper, times(1)).writeValueAsString(request);
    }

    @Test
    void updateOrderProgress_shouldCallWebhookUsingRestTemplate() throws JsonProcessingException {
        ApiGatewayUpdateOrderProgressRequest request = new ApiGatewayUpdateOrderProgressRequest();
        request.setOrderNo("ORDER-ONE");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ApiResponse.class))).thenReturn(apiResponse);
        when(objectMapper.writeValueAsString(request)).thenReturn(request.toString());

        apiGatewayWebHookClient.updateOrderProgress("ORG-ID-ONE", request);

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(ApiResponse.class));
        verify(objectMapper, times(1)).writeValueAsString(request);
    }

    @Test
    void checkIn_shouldCallWebhookUsingRestTemplate() throws JsonProcessingException {
        ApiGatewayCheckInRequest request = new ApiGatewayCheckInRequest();
        request.setOrderNo("ORDER-ONE");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ApiResponse.class))).thenReturn(apiResponse);
        when(objectMapper.writeValueAsString(request)).thenReturn(request.toString());

        apiGatewayWebHookClient.checkIn("ORG-ID-ONE", request);

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(ApiResponse.class));
        verify(objectMapper, times(1)).writeValueAsString(request);
    }

    @Test
    void assignVendorDetails_shouldHandleResourceAccessException() throws JsonProcessingException {
        ApiGatewayAssignVendorDetailRequest request = new ApiGatewayAssignVendorDetailRequest();

        when(objectMapper.writeValueAsString(request)).thenReturn(request.toString());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ApiResponse.class)))
                .thenThrow(ResourceAccessException.class);

        assertThatThrownBy(() -> apiGatewayWebHookClient.assignVendorDetails("org-id", request))
                .isInstanceOf(ApiNetworkIssueException.class);
    }

    @Test
    void assignVendorDetails_shouldHandleHttpClientErrorException() throws JsonProcessingException {
        ApiGatewayAssignVendorDetailRequest request = new ApiGatewayAssignVendorDetailRequest();

        when(objectMapper.writeValueAsString(request)).thenReturn(request.toString());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ApiResponse.class)))
                .thenThrow(HttpClientErrorException.class);

        assertThatThrownBy(() -> apiGatewayWebHookClient.assignVendorDetails("org-id", request))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void assignVendorDetails_shouldHandleJsonProcessingException() throws JsonProcessingException {
        ApiGatewayAssignVendorDetailRequest request = new ApiGatewayAssignVendorDetailRequest();

        when(objectMapper.writeValueAsString(request)).thenThrow(JsonProcessingException.class);

        assertThatThrownBy(() -> apiGatewayWebHookClient.assignVendorDetails("org-id", request))
                .isInstanceOf(QuincusValidationException.class);
    }

    @Test
    void updateOrderProgress_shouldHandleResourceAccessException() throws JsonProcessingException {
        ApiGatewayUpdateOrderProgressRequest request = new ApiGatewayUpdateOrderProgressRequest();
        request.setOrderNo("ORDER-ONE");

        when(objectMapper.writeValueAsString(request)).thenReturn(request.toString());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ApiResponse.class)))
                .thenThrow(ResourceAccessException.class);

        assertThatThrownBy(() -> apiGatewayWebHookClient.updateOrderProgress("ORG-ID-ONE", request))
                .isInstanceOf(ApiNetworkIssueException.class);
    }

    @Test
    void updateOrderProgress_shouldHandleHttpClientErrorException() throws JsonProcessingException {
        ApiGatewayUpdateOrderProgressRequest request = new ApiGatewayUpdateOrderProgressRequest();
        request.setOrderNo("ORDER-ONE");

        when(objectMapper.writeValueAsString(request)).thenReturn(request.toString());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ApiResponse.class)))
                .thenThrow(HttpClientErrorException.class);

        assertThatThrownBy(() -> apiGatewayWebHookClient.updateOrderProgress("ORG-ID-ONE", request))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void updateOrderProgress_shouldHandleJsonProcessingException() throws JsonProcessingException {
        ApiGatewayUpdateOrderProgressRequest request = new ApiGatewayUpdateOrderProgressRequest();
        request.setOrderNo("ORDER-ONE");

        when(objectMapper.writeValueAsString(request)).thenThrow(JsonProcessingException.class);

        assertThatThrownBy(() -> apiGatewayWebHookClient.updateOrderProgress("ORG-ID-ONE", request))
                .isInstanceOf(QuincusValidationException.class);
    }

    @Test
    void checkIn_shouldHandleResourceAccessException() throws JsonProcessingException {
        ApiGatewayCheckInRequest request = new ApiGatewayCheckInRequest();
        request.setOrderNo("ORDER-ONE");

        when(objectMapper.writeValueAsString(request)).thenReturn(request.toString());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ApiResponse.class)))
                .thenThrow(ResourceAccessException.class);

        assertThatThrownBy(() -> apiGatewayWebHookClient.checkIn("ORG-ID-ONE", request))
                .isInstanceOf(ApiNetworkIssueException.class);
    }

    @Test
    void checkIn_shouldHandleHttpClientErrorException() throws JsonProcessingException {
        ApiGatewayCheckInRequest request = new ApiGatewayCheckInRequest();
        request.setOrderNo("ORDER-ONE");

        when(objectMapper.writeValueAsString(request)).thenReturn(request.toString());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ApiResponse.class)))
                .thenThrow(HttpClientErrorException.class);

        assertThatThrownBy(() -> apiGatewayWebHookClient.checkIn("ORG-ID-ONE", request))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void checkIn_shouldHandleJsonProcessingException() throws JsonProcessingException {
        ApiGatewayCheckInRequest request = new ApiGatewayCheckInRequest();
        request.setOrderNo("ORDER-ONE");

        when(objectMapper.writeValueAsString(request)).thenThrow(JsonProcessingException.class);

        assertThatThrownBy(() -> apiGatewayWebHookClient.checkIn("ORG-ID-ONE", request))
                .isInstanceOf(QuincusValidationException.class);
    }
}
