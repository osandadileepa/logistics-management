package com.quincus.qportal.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.qportal.config.QPortalProperties;
import com.quincus.qportal.model.QPortalCostType;
import com.quincus.qportal.model.QPortalCurrency;
import com.quincus.qportal.model.QPortalDriver;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalMilestone;
import com.quincus.qportal.model.QPortalNotificationRequest;
import com.quincus.qportal.model.QPortalNotificationResponse;
import com.quincus.qportal.model.QPortalOrganization;
import com.quincus.qportal.model.QPortalPackageType;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.qportal.model.QPortalUserRequest;
import com.quincus.qportal.model.QPortalVehicle;
import com.quincus.qportal.model.QPortalVehicleType;
import com.quincus.web.common.exception.model.ApiCallException;
import com.quincus.web.common.exception.model.ApiNetworkIssueException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
class QPortalRestClient {
    private static final String AUTHORIZATION = "X-API-AUTHORIZATION";
    private static final String ORGANIZATION_ID = "X-ORGANISATION-ID";
    private static final String ID_QUERY_PARAM = "?id=";
    private static final String NAME_QUERY_PARAM = "?name=";
    private static final String COMMON_QUERY_PARAM = "?existent=true&sort_by=name";
    private static final String SORT_PARAM = "?sort_by=name";
    private static final String PAGE_QUERY_PARAM = "&page=%s&per_page=%s";
    private static final String NAME_SEARCH_QUERY_PARAM = "&query=%s";
    private static final String USER_ID_SEARCH_QUERY_PARAM = "&user_ids=%s";
    private static final String CURRENT_USER = "current user";
    private static final String NOTIFICATION_QCOMMS = "QPortal Notification - (QComms)";
    private static final String LOCATION_NODE = "locations";
    private static final String VEHICLE_TYPES_NODE = "vehicle_types";
    private static final String PARTNERS_NODE = "partners";
    private static final String USERS_NODE = "users";
    private static final String MILESTONES_NODE = "milestones";
    private static final String COST_TYPES_NODE = "cost_types";
    private static final String DRIVERS_NODE = "drivers";
    private static final String CURRENCIES_NODE = "currencies";
    private static final String CURRENCY_NODE = "currency";
    private static final String PACKAGE_TYPES_NODE = "package_types";
    private static final String VEHICLES_NODE = "vehicles";
    private static final String ORGANISATIONS = "organisations";
    private static final String API_CALL_ERROR = "Encountered an issue while making a QPortal call to `%s` for organization id `%s`. Error message: `%s`.";
    private static final String JSON_MAPPING_ERROR = "Issue occurred during the conversion of `%s` for organization id `%s`. Error message: `%s`.";
    private final QPortalProperties qPortalProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public QPortalRestClient(QPortalProperties qPortalProperties, ObjectMapper objectMapper, @Qualifier("qPortalRestTemplate") RestTemplate restTemplate) {
        this.qPortalProperties = qPortalProperties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    List<QPortalPackageType> listPackageTypes(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getPackageTypesAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode qPortalPackageJsonNode = objectMapper.readTree(response.getBody()).get(PACKAGE_TYPES_NODE);
            return objectMapper.readValue(qPortalPackageJsonNode.traverse(), new TypeReference<>() {});
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, PACKAGE_TYPES_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    List<QPortalPartner> listPartners(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getPartnersAPI() + COMMON_QUERY_PARAM);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode partnersArray = objectMapper.readTree(response.getBody()).get(PARTNERS_NODE);
            return objectMapper.readValue(partnersArray.traverse(), new TypeReference<>() {
            });
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, PARTNERS_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    List<QPortalPartner> listPartnersWithSearchAndPagination(@NotBlank String organizationId, @NotBlank String userId, @NotNull Integer perPage, @NotNull Integer page, String key) {
        URI uri = getUri(qPortalProperties.getPartnersAPI() + generateCommonQueryParameter(perPage, page, key, userId));

        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode partnersArray = objectMapper.readTree(response.getBody()).get(PARTNERS_NODE);
            return objectMapper.readValue(partnersArray.traverse(), new TypeReference<>() {
            });
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, PARTNERS_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e.getMessage(), e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private String generateCommonQueryParameter(Integer perPage, Integer page, String key, String userId) {
        String parameter = COMMON_QUERY_PARAM + String.format(PAGE_QUERY_PARAM, page, perPage);
        if (StringUtils.isNotBlank(key)) {
            String encodedKey = UriComponentsBuilder.fromUriString(key).build().encode().toUriString();
            parameter = parameter + String.format(NAME_SEARCH_QUERY_PARAM, encodedKey);
        }
        if (StringUtils.isNotBlank(userId)) {
            String encodedKey = UriComponentsBuilder.fromUriString(userId).build().encode().toUriString();
            parameter = parameter + String.format(USER_ID_SEARCH_QUERY_PARAM, encodedKey);
        }
        return parameter;
    }


    QPortalPartner getPartnerById(@NotBlank String organizationId, @NotBlank String partnerId) {
        URI uri = getUri(qPortalProperties.getPartnersAPI() + ID_QUERY_PARAM + partnerId);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode partnersArray = objectMapper.readTree(response.getBody()).get(PARTNERS_NODE);
            List<QPortalPartner> partners = objectMapper.readValue(partnersArray.traverse(), new TypeReference<>() {});
            return CollectionUtils.firstElement(partners);
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, PARTNERS_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    QPortalPartner getPartnerByName(@NotBlank String organizationId, @NotBlank String partnerName) {
        String encodedParameter = UriComponentsBuilder.fromUriString(partnerName).build().encode().toUriString();
        URI uri = getUri(qPortalProperties.getPartnersAPI() + NAME_QUERY_PARAM + encodedParameter);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode partnersArray = objectMapper.readTree(response.getBody()).get(PARTNERS_NODE);
            List<QPortalPartner> partners = objectMapper.readValue(partnersArray.traverse(), new TypeReference<>() {});
            return CollectionUtils.firstElement(partners);
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, PARTNERS_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    QPortalLocation getLocation(@NonNull final String organizationId, @NonNull final String locationId) {
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        URI uri = getUri(qPortalProperties.getLocationsAPI() + ID_QUERY_PARAM + locationId);
        ResponseEntity<JsonNode> response = performGet(uri, request, JsonNode.class);
        JsonNode responseBody = response.getBody();
        if (responseBody == null || responseBody.get(LOCATION_NODE) == null) return null;
        try {
            JsonNode location = responseBody.get(LOCATION_NODE).get(0);
            return objectMapper.convertValue(location, QPortalLocation.class);
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, LOCATION_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    List<QPortalLocation> getLocationsByName(@NonNull final String organizationId, @NonNull final String locationName) {
        String encodedParameter = UriComponentsBuilder.fromUriString(locationName).build().encode().toUriString();
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        URI uri = getUri(qPortalProperties.getLocationsAPI() + NAME_QUERY_PARAM + encodedParameter);
        ResponseEntity<JsonNode> response = performGet(uri, request, JsonNode.class);
        JsonNode responseBody = response.getBody();
        if (responseBody == null || responseBody.get(LOCATION_NODE) == null) return Collections.emptyList();
        try {
            return objectMapper.readValue(responseBody.get(LOCATION_NODE).traverse(), new TypeReference<>() {});
        } catch (Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, LOCATION_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    List<QPortalUser> listUsers(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getListUsersAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode usersArray = objectMapper.readTree(response.getBody()).get(USERS_NODE);
            return objectMapper.readValue(usersArray.traverse(), new TypeReference<>() {});
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, USERS_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    QPortalUser getUserById(@NotBlank String organizationId, @NotBlank String userId) {
        URI uri = getUri(qPortalProperties.getUsersAPI() + userId);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<JsonNode> response = performGet(uri, request, JsonNode.class);
        JsonNode responseBody = response.getBody();
        if (responseBody == null) return null;
        try {
            return objectMapper.convertValue(responseBody, QPortalUser.class);
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, CURRENT_USER, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    QPortalUser getCurrentUserProfile(String token) {
        URI uri = getUri(qPortalProperties.getUsersGetMyProfileApi());
        HttpEntity<String> request = createHttpRequest(token);
        ResponseEntity<JsonNode> response = performGet(uri, request, JsonNode.class);
        JsonNode responseBody = response.getBody();
        if (responseBody == null) return null;
        try {
            return objectMapper.convertValue(responseBody, QPortalUser.class);
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, CURRENT_USER, "token", e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    List<QPortalMilestone> listMilestones(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getMilestonesAPI() + SORT_PARAM);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode milestoneArray = objectMapper.readTree(response.getBody()).get(MILESTONES_NODE);
            return objectMapper.readValue(milestoneArray.traverse(), new TypeReference<>() {});
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, MILESTONES_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    List<QPortalMilestone> listMilestonesWithSearchAndPagination(@NotBlank String organizationId, @NotNull Integer perPage, @NotNull Integer page, String key) {
        URI uri = getUri(qPortalProperties.getMilestonesAPI() + generateCommonQueryParameter(perPage, page, key, null));
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode partnersArray = objectMapper.readTree(response.getBody()).get(MILESTONES_NODE);
            return objectMapper.readValue(partnersArray.traverse(), new TypeReference<>() {
            });
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, MILESTONES_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e.getMessage(), e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    List<QPortalCostType> listCostTypes(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getCostTypesAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode costTypeArray = objectMapper.readTree(response.getBody()).get(COST_TYPES_NODE);
            return objectMapper.readValue(costTypeArray.traverse(), new TypeReference<>() {});
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, COST_TYPES_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    QPortalCostType getCostType(@NotBlank String organizationId, @NotBlank String costTypeId) {
        URI uri = getUri(qPortalProperties.getCostTypesAPI() + costTypeId);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<JsonNode> response = performGet(uri, request, JsonNode.class);
        JsonNode responseBody = response.getBody();
        if (responseBody == null) return null;
        try {
            return objectMapper.convertValue(responseBody, QPortalCostType.class);
        } catch (Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, COST_TYPES_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    List<QPortalDriver> listDrivers(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getUsersAPI() + DRIVERS_NODE);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode driverArray = objectMapper.readTree(response.getBody()).get(DRIVERS_NODE);
            return objectMapper.readValue(driverArray.traverse(), new TypeReference<>() {});
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, DRIVERS_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    List<QPortalDriver> listDriversByPartners(@NotBlank String organizationId, QPortalUserRequest userRequest) {
        HttpEntity<QPortalUserRequest> request = new HttpEntity<>(userRequest, createHeadersHttpPost(organizationId));
        URI uri = getUri(qPortalProperties.getListUsersAPI());
        ResponseEntity<String> response = performPost(uri, request, String.class);
        try {
            final JsonNode driverArray = objectMapper.readTree(response.getBody()).get(USERS_NODE);
            return objectMapper.readValue(driverArray.traverse(), new TypeReference<>() {});
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, DRIVERS_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    List<QPortalCurrency> listCurrencies(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getCurrenciesAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode costTypeArray = objectMapper.readTree(response.getBody()).get(CURRENCIES_NODE);
            return objectMapper.readValue(costTypeArray.traverse(), new TypeReference<>() {});
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, CURRENCIES_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    QPortalCurrency getCurrency(@NotBlank String organizationId, @NotBlank String currencyId) {
        URI uri = getUri(qPortalProperties.getCurrenciesAPI() + currencyId);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode currencyJson = objectMapper.readTree(response.getBody()).get(CURRENCY_NODE);
            return objectMapper.convertValue(currencyJson, QPortalCurrency.class);
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, CURRENCY_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    List<QPortalVehicle> listVehicles(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getVehiclesAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode costTypeArray = objectMapper.readTree(response.getBody()).get(VEHICLES_NODE);
            return objectMapper.readValue(costTypeArray.traverse(), new TypeReference<>() {});
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, VEHICLES_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    List<QPortalLocation> listLocations(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getLocationsAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode costTypeArray = objectMapper.readTree(response.getBody()).get(LOCATION_NODE);
            return objectMapper.readValue(costTypeArray.traverse(), new TypeReference<>() {});
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, LOCATION_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public List<QPortalVehicleType> listVehicleTypes(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getVehicleTypesAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = performGet(uri, request, String.class);
        try {
            final JsonNode vehicleTypesArray = objectMapper.readTree(response.getBody()).get(VEHICLE_TYPES_NODE);
            return objectMapper.readValue(vehicleTypesArray.traverse(), new TypeReference<>() {});
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, VEHICLE_TYPES_NODE, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    QPortalNotificationResponse sendNotification(@NotBlank String organizationId, @NotNull QPortalNotificationRequest notificationRequest) {
        HttpEntity<QPortalNotificationRequest> request = new HttpEntity<>(notificationRequest, createHeadersHttpPost(organizationId));
        URI uri = getUri(qPortalProperties.getNotificationAPI());
        ResponseEntity<QPortalNotificationResponse> response = performPost(uri, request, QPortalNotificationResponse.class);

        QPortalNotificationResponse responseBody = response.getBody();

        if (!isValidQPortalNotificationResponse(responseBody)) {
            String errorDetail = getErrorDetailFromQPortalNotificationResponse(Optional.ofNullable(responseBody));
            log.error("`{}` Request encountered an issue. Details: `{}`", NOTIFICATION_QCOMMS, errorDetail);
            return null;
        }
        log.info("`{}` Request completed successfully. The received response: `{}`", NOTIFICATION_QCOMMS, responseBody);
        return responseBody;
    }

    QPortalOrganization getOrganizationById(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getOrganizationAPI() + organizationId);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<JsonNode> response = performGet(uri, request, JsonNode.class);
        JsonNode responseBody = response.getBody();
        if (responseBody == null) return null;
        try {
            return objectMapper.convertValue(responseBody, QPortalOrganization.class);
        } catch (final Exception e) {
            String errorMessage = String.format(JSON_MAPPING_ERROR, ORGANISATIONS, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private <T> ResponseEntity<T> performGet(URI uri, HttpEntity<?> request, Class<T> responseType) {
        return executeExchange(uri, HttpMethod.GET, request, responseType);
    }

    private <T> ResponseEntity<T> performPost(URI uri, HttpEntity<?> request, Class<T> responseType) {
        return executeExchange(uri, HttpMethod.POST, request, responseType);
    }

    private <T> ResponseEntity<T> executeExchange(URI uri, HttpMethod method, HttpEntity<?> request, Class<T> responseType) {
        String path = uri.getPath();
        String organizationId = extractOrganizationIdFromHttpEntity(request);
        try {
            return restTemplate.exchange(uri, method, request, responseType);
        } catch (final ResourceAccessException | HttpServerErrorException e) {
            String errorMessage = String.format(API_CALL_ERROR, path, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiNetworkIssueException(errorMessage, HttpStatus.SERVICE_UNAVAILABLE);
        } catch (final HttpClientErrorException e) {
            String errorMessage = String.format(API_CALL_ERROR, path, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, e.getStatusCode());
        } catch (final Exception e) {
            String errorMessage = String.format(API_CALL_ERROR, path, organizationId, e.getMessage());
            log.error(errorMessage, e);
            throw new ApiCallException(errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private String getErrorDetailFromQPortalNotificationResponse(Optional<QPortalNotificationResponse> optResponse) {
        return optResponse
                .map(qPortalNotificationResponse -> String.format("Encountered error: %s", qPortalNotificationResponse.getError()))
                .orElse("Response body is null.");
    }

    private boolean isValidQPortalNotificationResponse(QPortalNotificationResponse response) {
        return Optional.ofNullable(response)
                .map(body -> Objects.isNull(body.getError()))
                .orElse(false);
    }

    private URI getUri(String api) {
        return URI.create(qPortalProperties.getBaseUrl() + api);
    }

    private HttpEntity<String> createS2sHttpRequest(String organizationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(AUTHORIZATION, qPortalProperties.getS2sToken());
        headers.add(ORGANIZATION_ID, organizationId);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<String> createHttpRequest(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add("Authorization", "Bearer " + token);
        return new HttpEntity<>(headers);
    }

    private HttpHeaders createHeadersHttpPost(String organizationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(AUTHORIZATION, qPortalProperties.getS2sToken());
        headers.add(ORGANIZATION_ID, organizationId);
        return headers;
    }

    private String extractOrganizationIdFromHttpEntity(HttpEntity<?> httpEntity) {
        return Optional.ofNullable(httpEntity)
                .map(HttpEntity::getHeaders)
                .map(headers -> headers.get(ORGANIZATION_ID))
                .filter(organizationIds -> !CollectionUtils.isEmpty(organizationIds))
                .map(organizationIds -> organizationIds.get(0))
                .orElse(null);
    }
}