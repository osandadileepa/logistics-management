package com.quincus.qportal.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.core.impl.exception.ApiCallException;
import com.quincus.qportal.config.QPortalProperties;
import com.quincus.qportal.model.QPortalCostType;
import com.quincus.qportal.model.QPortalCurrency;
import com.quincus.qportal.model.QPortalDriver;
import com.quincus.qportal.model.QPortalFacility;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalMilestone;
import com.quincus.qportal.model.QPortalNotification;
import com.quincus.qportal.model.QPortalNotificationResponse;
import com.quincus.qportal.model.QPortalPackageType;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.qportal.model.QPortalVehicle;
import com.quincus.qportal.model.QPortalVehicleType;
import lombok.AllArgsConstructor;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
class QPortalRestClient {
    private static final String AUTHORIZATION = "X-API-AUTHORIZATION";
    private static final String ORGANIZATION_ID = "X-ORGANISATION-ID";
    private static final String ID_QUERY_PARAM = "?id=";
    private static final String NAME_QUERY_PARAM = "?name=";
    private static final String ACTIVE = "Active";
    private static final String ERR_QPORTAL_EXCEPTION = "Exception occurred while calling QPortal {} API. Error message {}";
    private static final String ERR_JSON_MAPPING_FAILED = "Error occurred while mapping Json response to list. Error message {}";
    private static final String ERR_LIST_FAILED = "Unable to retrieve `%s` from QPortal with organization id `%s`";
    private static final String ERR_LIST_FAILED_TOKEN = "Unable to retrieve `%s` from QPortal with token provided.";
    private static final String LOCATION_NODE = "locations";
    private static final String QCOMM_NOTIFICATION = "QCOMM Notification";
    private static final String PARTNERS_NODE = "partners";
    private static final String USERS_NODE = "users";
    private static final String MILESTONES_NODE = "milestones";
    private static final String COST_TYPES_NODE = "cost_types";
    private static final String FACILITIES_NODE = "facilities";
    private static final String DRIVERS_NODE = "drivers";
    private static final String CURRENCIES_NODE = "currencies";
    private static final String CURRENCY_NODE = "currency";
    private static final String PACKAGE_TYPES_NODE = "package_types";
    private static final String VEHICLES_NODE = "vehicles";
    private static final String VEHICLE_TYPES_NODE = "vehicle_types";
    private static final String PAGE = "&page=";
    private static final String PER_PAGE = "&per_page=";
    private static final String QUERY = "?query=";

    private final QPortalProperties qPortalProperties;
    private final @Qualifier("qPortalRestTemplate") RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    List<QPortalPackageType> listPackageTypes(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getPackageTypesAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            final JsonNode qPortalPackageJsonNode = objectMapper.readTree(response.getBody()).get(PACKAGE_TYPES_NODE);
            return objectMapper.readValue(qPortalPackageJsonNode.traverse(), new TypeReference<>() {
            });
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, PACKAGE_TYPES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, PACKAGE_TYPES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, PACKAGE_TYPES_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, PACKAGE_TYPES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, PACKAGE_TYPES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    List<QPortalPartner> listPartners(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getPartnersAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            final JsonNode partnersArray = objectMapper.readTree(response.getBody()).get(PARTNERS_NODE);
            List<QPortalPartner> partners = objectMapper.readValue(partnersArray.traverse(), new TypeReference<>() {
            });
            return partners.stream().filter(this::isActiveQPortalPartner).toList();
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, PARTNERS_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, PARTNERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, PARTNERS_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, PARTNERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, PARTNERS_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isActiveQPortalPartner(QPortalPartner qPortalPartner) {
        return StringUtils.equalsIgnoreCase(qPortalPartner.getStatus(), ACTIVE) && StringUtils.isBlank(qPortalPartner.getDeletedTime());
    }

    QPortalPartner getPartnerById(@NotBlank String organizationId, @NotBlank String partnerId) {
        URI uri = getUri(qPortalProperties.getPartnersAPI() + partnerId);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class);
            JsonNode responseBody = response.getBody();
            if (responseBody == null) return null;
            return objectMapper.convertValue(responseBody, QPortalPartner.class);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, PARTNERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, PARTNERS_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, PARTNERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, PARTNERS_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    QPortalPartner getPartnerByName(@NotBlank String organizationId, @NotBlank String partnerName) {
        String encodedParameter = UriComponentsBuilder.fromUriString(partnerName).build().encode().toUriString();
        URI uri = getUri(qPortalProperties.getPartnersAPI() + QUERY + encodedParameter + PAGE + "1" + PER_PAGE + "5");
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            final JsonNode partnersArray = objectMapper.readTree(response.getBody()).get(PARTNERS_NODE);
            List<QPortalPartner> partners = objectMapper.readValue(partnersArray.traverse(), new TypeReference<>() {
            });
            return CollectionUtils.firstElement(partners);
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, PARTNERS_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, PARTNERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, PARTNERS_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, PARTNERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, PARTNERS_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    QPortalLocation getLocation(@NonNull final String organizationId, @NonNull final String locationId) {
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        URI uri = getUri(qPortalProperties.getLocationsAPI() + ID_QUERY_PARAM + locationId);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class);
            JsonNode responseBody = response.getBody();
            if (responseBody == null || responseBody.get(LOCATION_NODE) == null) return null;
            JsonNode location = responseBody.get(LOCATION_NODE).get(0);
            return objectMapper.convertValue(location, QPortalLocation.class);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, LOCATION_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, LOCATION_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, LOCATION_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, LOCATION_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    QPortalLocation getLocationByName(@NonNull final String organizationId, @NonNull final String locationName) {
        String encodedParameter = UriComponentsBuilder.fromUriString(locationName).build().encode().toUriString();
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        URI uri = getUri(qPortalProperties.getLocationsAPI() + NAME_QUERY_PARAM + encodedParameter);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class);
            JsonNode responseBody = response.getBody();
            if (responseBody == null || responseBody.get(LOCATION_NODE) == null) return null;
            JsonNode location = responseBody.get(LOCATION_NODE).get(0);
            return objectMapper.convertValue(location, QPortalLocation.class);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, LOCATION_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, LOCATION_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, LOCATION_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, LOCATION_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    List<QPortalUser> listUsers(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getListUsersAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);

        try {
            final JsonNode usersArray = objectMapper.readTree(response.getBody()).get(USERS_NODE);
            return objectMapper.readValue(usersArray.traverse(), new TypeReference<>() {
            });
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, USERS_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, USERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, USERS_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, USERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, USERS_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    QPortalUser getUserById(@NotBlank String organizationId, @NotBlank String userId) {
        URI uri = getUri(qPortalProperties.getUsersAPI() + userId);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class);
            JsonNode responseBody = response.getBody();
            if (responseBody == null) return null;
            return objectMapper.convertValue(responseBody, QPortalUser.class);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, USERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, USERS_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, USERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, USERS_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    QPortalUser getCurrentUserProfile(String token) {
        URI uri = getUri(qPortalProperties.getUsersGetMyProfileApi());
        HttpEntity<String> request = createHttpRequest(token);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class);
            JsonNode responseBody = response.getBody();
            if (responseBody == null) return null;
            return objectMapper.convertValue(responseBody, QPortalUser.class);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, USERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED_TOKEN, USERS_NODE), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, USERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED_TOKEN, USERS_NODE), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    List<QPortalMilestone> listMilestones(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getMilestonesAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            final JsonNode milestoneArray = objectMapper.readTree(response.getBody()).get(MILESTONES_NODE);
            return objectMapper.readValue(milestoneArray.traverse(), new TypeReference<>() {
            });
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, MILESTONES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, MILESTONES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, MILESTONES_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, MILESTONES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, MILESTONES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    List<QPortalCostType> listCostTypes(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getCostTypesAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        try {
            final JsonNode costTypeArray = objectMapper.readTree(response.getBody()).get(COST_TYPES_NODE);
            return objectMapper.readValue(costTypeArray.traverse(), new TypeReference<>() {
            });
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, COST_TYPES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, COST_TYPES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, COST_TYPES_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, COST_TYPES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, COST_TYPES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    QPortalCostType getCostType(@NotBlank String organizationId, @NotBlank String costTypeId) {
        URI uri = getUri(qPortalProperties.getCostTypesAPI() + costTypeId);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class);
            JsonNode responseBody = response.getBody();
            if (responseBody == null) return null;
            return objectMapper.convertValue(responseBody, QPortalCostType.class);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, COST_TYPES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, COST_TYPES_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, COST_TYPES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, COST_TYPES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    List<QPortalDriver> listDrivers(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getUsersAPI() + DRIVERS_NODE);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        try {
            final JsonNode driverArray = objectMapper.readTree(response.getBody()).get(DRIVERS_NODE);
            return objectMapper.readValue(driverArray.traverse(), new TypeReference<>() {
            });
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, DRIVERS_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, DRIVERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, DRIVERS_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, DRIVERS_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, DRIVERS_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    List<QPortalCurrency> listCurrencies(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getCurrenciesAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        try {
            final JsonNode currenciesArray = objectMapper.readTree(response.getBody()).get(CURRENCIES_NODE);
            return objectMapper.readValue(currenciesArray.traverse(), new TypeReference<>() {
            });
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, CURRENCY_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, CURRENCY_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, CURRENCY_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, CURRENCY_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, CURRENCY_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    QPortalCurrency getCurrency(@NotBlank String organizationId, @NotBlank String currencyId) {
        URI uri = getUri(qPortalProperties.getCurrenciesAPI() + currencyId);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);

            final JsonNode currencyJson = objectMapper.readTree(response.getBody()).get(CURRENCY_NODE);
            return objectMapper.convertValue(currencyJson, QPortalCurrency.class);
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, CURRENCY_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, CURRENCY_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, CURRENCY_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, CURRENCY_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, CURRENCY_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    List<QPortalVehicle> listVehicles(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getVehiclesAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        try {
            final JsonNode vehiclesArray = objectMapper.readTree(response.getBody()).get(VEHICLES_NODE);
            return objectMapper.readValue(vehiclesArray.traverse(), new TypeReference<>() {
            });
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, VEHICLES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, VEHICLES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, VEHICLES_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, VEHICLES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, VEHICLES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    List<QPortalVehicleType> listVehicleTypes(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getVehicleTypesAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        try {
            final JsonNode vehicleTypesArray = objectMapper.readTree(response.getBody()).get(VEHICLE_TYPES_NODE);
            return objectMapper.readValue(vehicleTypesArray.traverse(), new TypeReference<>() {
            });
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, VEHICLE_TYPES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, VEHICLES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, VEHICLE_TYPES_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, VEHICLES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, VEHICLE_TYPES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    QPortalVehicleType getVehicleType(@NotBlank String organizationId, @NotBlank String vehicleTypeId) {
        URI uri = getUri(qPortalProperties.getVehicleTypesAPI() + vehicleTypeId);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);

            final JsonNode vehicleTypeJson = objectMapper.readTree(response.getBody());
            return objectMapper.convertValue(vehicleTypeJson, QPortalVehicleType.class);
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, VEHICLE_TYPES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, VEHICLE_TYPES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, VEHICLE_TYPES_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, VEHICLE_TYPES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, VEHICLE_TYPES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    List<QPortalLocation> listLocations(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getLocationsAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        try {
            final JsonNode locationsArray = objectMapper.readTree(response.getBody()).get(LOCATION_NODE);
            return objectMapper.readValue(locationsArray.traverse(), new TypeReference<>() {
            });
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, LOCATION_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, LOCATION_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, LOCATION_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, LOCATION_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, LOCATION_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    QPortalNotificationResponse sendNotification(String organizationId, QPortalNotification qPortalNotification) {
        HttpEntity<QPortalNotification> request = new HttpEntity<>(qPortalNotification, createHeadersHttpPost(organizationId));
        URI uri = getUri(qPortalProperties.getNotificationAPI());
        try {
            ResponseEntity<QPortalNotificationResponse> response = restTemplate.exchange(uri, HttpMethod.POST, request, QPortalNotificationResponse.class);
            QPortalNotificationResponse responseBody = response.getBody();
            if (responseBody == null) return null;
            log.info(QCOMM_NOTIFICATION + " trigger success. Response was: {}", responseBody);
            return responseBody;
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, QCOMM_NOTIFICATION, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, QCOMM_NOTIFICATION, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, QCOMM_NOTIFICATION, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, QCOMM_NOTIFICATION, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    List<QPortalFacility> listFacilities(@NotBlank String organizationId) {
        URI uri = getUri(qPortalProperties.getFacilitiesAPI());
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        try {
            final JsonNode facilityArray = objectMapper.readTree(response.getBody()).get(FACILITIES_NODE);
            return objectMapper.readValue(facilityArray.traverse(), new TypeReference<>() {
            });
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, FACILITIES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, FACILITIES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, FACILITIES_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, FACILITIES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, FACILITIES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    QPortalFacility getFacility(@NotBlank String organizationId, @NotBlank String facilityId) {
        URI uri = getUri(qPortalProperties.getFacilitiesAPI() + facilityId);
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class);
            JsonNode responseBody = response.getBody();
            if (responseBody == null) return null;
            return objectMapper.convertValue(responseBody, QPortalFacility.class);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, FACILITIES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, FACILITIES_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, FACILITIES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, FACILITIES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    QPortalFacility getFacilityByName(@NotBlank String organizationId, @NotBlank String facilityName) {
        String encodedParameter = UriComponentsBuilder.fromUriString(facilityName).build().encode().toUriString();
        URI uri = getUri(qPortalProperties.getFacilitiesAPI() + QUERY + encodedParameter + PAGE + "1" + PER_PAGE + "5");
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            String responseBody = response.getBody();
            if (responseBody == null) return null;
            final JsonNode facilityArray = objectMapper.readTree(responseBody).get(FACILITIES_NODE);
            List<QPortalFacility> facilities = objectMapper.readValue(facilityArray.traverse(), new TypeReference<>() {
            });
            return CollectionUtils.firstElement(facilities);
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, FACILITIES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, FACILITIES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, FACILITIES_NODE, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, FACILITIES_NODE, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, FACILITIES_NODE, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
}
