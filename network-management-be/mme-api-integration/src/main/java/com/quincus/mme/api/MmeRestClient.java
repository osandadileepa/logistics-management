package com.quincus.mme.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.mme.config.MmeProperties;
import com.quincus.mme.model.MmeGetTrainedModelRequest;
import com.quincus.mme.model.MmeTrainModelResponse;
import com.quincus.mme.model.MmeTrainingStatus;
import com.quincus.web.common.exception.model.ApiCallException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class MmeRestClient {
    private static final String AUTHORIZATION = "Authorization";
    private static final String NW_TRAINING_REQUEST_ID = "NW-TRAINING-REQUEST-ID";
    private static final String X_USER_ID = "X-USER-ID";
    private static final String ORGANIZATION_NAME = "X-ORGANISATION-NAME";
    private static final String ORGANIZATION_ID = "X-ORGANISATION-ID";
    private static final String ERR_MME_EXCEPTION = "Exception occurred while trigger Train MME {} API. Error message: {}";
    private static final String ERR_MME_API_FAILED = "Failed to communicate with MME using endpoint '%s' for organizationId '%s'";

    private MmeProperties mmeProperties;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public MmeTrainModelResponse trainModel(
            JsonNode payload,
            String organizationId,
            String organizationName,
            String userId
    ) {
        URI uri = getUri(mmeProperties.getTrainModel());
        HttpEntity<JsonNode> request = getRequestEntity(payload, organizationId, organizationName, userId, UUID.randomUUID().toString());
        String path = uri.getPath();

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
            logRequestAndResponse(request, response, uri);
            JsonNode responseBody = objectMapper.readTree(response.getBody());
            if (responseBody == null) return new MmeTrainModelResponse(MmeTrainingStatus.FAILED, null);
            MmeTrainModelResponse trainResponse = objectMapper.convertValue(responseBody, MmeTrainModelResponse.class);
            trainResponse.setStatus(MmeTrainingStatus.INITIATED);
            setTraceFromHeaders(response.getHeaders(), trainResponse);
            return trainResponse;
        } catch (HttpClientErrorException e) {
            log.error(ERR_MME_EXCEPTION, path, e.getMessage());
            throw new ApiCallException(String.format(ERR_MME_API_FAILED, path, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_MME_EXCEPTION, path, e.getMessage());
            throw new ApiCallException(String.format(ERR_MME_API_FAILED, path, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public MmeTrainModelResponse getTrainedModel(
            MmeGetTrainedModelRequest payload,
            String organizationId,
            String organizationName,
            String userId,
            String trainingRequestId
    ) {
        URI uri = getUri(mmeProperties.getCheckTrainModel());
        HttpEntity<MmeGetTrainedModelRequest> request = getRequestEntity(payload, organizationId, organizationName, userId, trainingRequestId);
        String path = uri.getPath();
        String uniqueId = payload.getUniqueId();
        MmeTrainModelResponse trainResponse = new MmeTrainModelResponse(MmeTrainingStatus.COMPLETED, uniqueId);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
            logRequestAndResponse(request, response, uri);
            setTraceFromHeaders(response.getHeaders(), trainResponse);
            if (response.getStatusCode() == HttpStatus.OK) {
                return trainResponse;
            }
            trainResponse.setStatus(MmeTrainingStatus.PENDING);
            return trainResponse;
        } catch (HttpClientErrorException e) {
            log.error(ERR_MME_EXCEPTION, path, e.getMessage());
            throw new ApiCallException(String.format(ERR_MME_API_FAILED, path, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_MME_EXCEPTION, path, e.getMessage());
            throw new ApiCallException(String.format(ERR_MME_API_FAILED, path, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private HttpHeaders generateHeaders(
            String organizationId,
            String organizationName,
            String userId,
            String trainingRequestId
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(AUTHORIZATION, mmeProperties.getAuthValue());
        headers.add(NW_TRAINING_REQUEST_ID, trainingRequestId);
        headers.add(ORGANIZATION_ID, organizationId);
        headers.add(ORGANIZATION_NAME, organizationName);
        headers.add(X_USER_ID, userId);
        return headers;
    }

    private void setTraceFromHeaders(HttpHeaders headers, MmeTrainModelResponse trainModelResponse) {
        if (headers != null && !headers.isEmpty()) {
            List<String> nwRequestHeader = headers.get(NW_TRAINING_REQUEST_ID);
            List<String> userId = headers.get(X_USER_ID);
            trainModelResponse.setNwTrainRequestId((nwRequestHeader != null && !nwRequestHeader.isEmpty()) ? nwRequestHeader.get(0) : null);
            trainModelResponse.setUserId((userId != null && !userId.isEmpty()) ? userId.get(0) : null);
        }
    }

    private URI getUri(String url) {
        return URI.create(mmeProperties.getBaseUrl() + url);
    }

    private <T> HttpEntity<T> getRequestEntity(
            T payload,
            String organizationId,
            String organizationName,
            String userId,
            String trainingRequestId
    ) {
        return new HttpEntity<>(payload, generateHeaders(organizationId, organizationName, userId, trainingRequestId));
    }

    private void logRequestAndResponse(HttpEntity<?> request, ResponseEntity<String> response, URI uri) {
        log.info("Sending request to MME. uri: {}, headers: {}", uri, request.getHeaders());
        log.debug("MME request body: {}", request.getBody());
        log.info("Received response from MME. status: {}, headers: {}", response.getStatusCode(), response.getHeaders());
        log.debug("MME response body: {}", response.getBody());
    }
}
