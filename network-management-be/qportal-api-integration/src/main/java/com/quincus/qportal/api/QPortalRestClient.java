package com.quincus.qportal.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.quincus.qportal.config.QPortalProperties;
import com.quincus.qportal.model.QPortalCurrency;
import com.quincus.qportal.model.QPortalFacility;
import com.quincus.qportal.model.QPortalModel;
import com.quincus.qportal.model.QPortalOrganization;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.qportal.model.QPortalTag;
import com.quincus.qportal.model.QPortalUser;
import com.quincus.qportal.model.QPortalVehicleType;
import com.quincus.web.common.exception.model.ApiCallException;
import lombok.AllArgsConstructor;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
class QPortalRestClient {
    private static final String AUTHORIZATION = "X-API-AUTHORIZATION";
    private static final String ORGANIZATION_ID = "X-ORGANISATION-ID";
    private static final String ERR_QPORTAL_EXCEPTION = "Exception occurred while calling QPortal {} API. Error message: {}";
    private static final String ERR_JSON_MAPPING_FAILED = "Error occurred while mapping Json response to list. Error message: {}";
    private static final String ERR_LIST_FAILED = "Unable to retrieve `%s` from QPortal with organizationId `%s`";
    private static final String ERR_GET_FAILED = "Unable to retrieve `%s` from QPortal with organizationId `%s`";
    private static final String ERR_GET_FAILED_WITH_TOKEN = "Unable to retrieve `%s` from QPortal with token provided";
    private static final String CURRENT_USER = "get_my_profile";
    private static final String PARTNERS = "partners";
    private static final String USERS = "users";
    private static final String CURRENCIES = "currencies";
    private static final String LOCATIONS = "locations";
    private static final String CURRENCY = "currency";
    private static final String TAGS = "tags";
    private static final String VEHICLE_TYPES = "vehicle_types";
    private static final String LOCATION_TYPES = "location_types";
    private static final String ORGANISATIONS = "organisations";

    private final QPortalProperties qPortalProperties;
    @Qualifier("qPortalRestTemplate")
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    QPortalUser getCurrentUserProfile(String token) {
        URI uri = getUri(qPortalProperties.getUsersGetMyProfileApi());
        HttpEntity<String> request = createHttpRequest(token);
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class);
            JsonNode responseBody = response.getBody();
            if (responseBody == null) return null;
            return objectMapper.convertValue(responseBody, QPortalUser.class);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, CURRENT_USER, e.getMessage());
            throw new ApiCallException(String.format(ERR_GET_FAILED_WITH_TOKEN, CURRENT_USER), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, CURRENT_USER, e.getMessage());
            throw new ApiCallException(String.format(ERR_GET_FAILED_WITH_TOKEN, CURRENT_USER), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    List<QPortalPartner> listPartners(String organizationId) {
        URI uri = getUri(qPortalProperties.getPartnersAPI());
        return list(
                organizationId,
                uri,
                QPortalPartner.class,
                PARTNERS
        );
    }

    List<QPortalTag> listTags(String organizationId) {
        URI uri = getUri(qPortalProperties.getTagsAPI());
        return list(
                organizationId,
                uri,
                QPortalTag.class,
                TAGS
        );
    }

    List<QPortalCurrency> listCurrencies(String organizationId) {
        URI uri = getUri(qPortalProperties.getCurrenciesAPI());
        return list(
                organizationId,
                uri,
                QPortalCurrency.class,
                CURRENCIES
        );
    }

    List<QPortalVehicleType> listVehicleTypes(String organizationId) {
        URI uri = getUri(qPortalProperties.getVehicleTypesAPI());
        return list(
                organizationId,
                uri,
                QPortalVehicleType.class,
                VEHICLE_TYPES
        );
    }

    List<QPortalFacility> listFacilities(String organizationId) {
        String facilityLocationTypeId = getFacilityLocationTypeId(organizationId);
        URI uri = getUri(qPortalProperties.getLocationsAPI() + "?location_type_id=" + facilityLocationTypeId);
        return list(
                organizationId,
                uri,
                QPortalFacility.class,
                LOCATIONS
        );
    }

    QPortalOrganization getOrganizationById(String organizationId) {
        URI uri = getUri(qPortalProperties.getOrganizationAPI() + organizationId);
        return get(
                organizationId,
                uri,
                QPortalOrganization.class,
                ORGANISATIONS
        );
    }

    QPortalUser getUserById(String organizationId, String userId) {
        URI uri = getUri(qPortalProperties.getUsersAPI() + userId);
        return get(
                organizationId,
                uri,
                QPortalUser.class,
                USERS
        );
    }

    QPortalPartner getPartnerById(String organizationId, String partnerId) {
        URI uri = getUri(qPortalProperties.getPartnersAPI() + "?id=" + partnerId);
        return list(
                organizationId,
                uri,
                QPortalPartner.class,
                PARTNERS
        ).
                stream()
                .findFirst()
                .orElse(null);
    }

    QPortalPartner getPartnerByName(@NotBlank String organizationId, @NotBlank String partnerName) {
        URI uri = getUri(qPortalProperties.getPartnersAPI() + "?query=" + encode(partnerName));
        return list(
                organizationId,
                uri,
                QPortalPartner.class,
                PARTNERS
        )
                .stream()
                .filter(p -> p.getName().equals(partnerName))
                .findFirst()
                .orElse(null);
    }

    String getFacilityLocationTypeId(String organizationId) {
        URI uri = getUri(qPortalProperties.getLocationTypesAPI() + "?name=FACILITY");
        QPortalModel result = list(
                organizationId,
                uri,
                QPortalModel.class,
                LOCATION_TYPES
        ).
                stream()
                .findFirst()
                .orElse(null);
        return result != null ? result.getId() : null;
    }

    QPortalFacility getFacilityById(@NotBlank String organizationId, @NotBlank String facilityId) {
        URI uri = getUri(qPortalProperties.getLocationsAPI() + "?id=" + facilityId);
        return list(
                organizationId,
                uri,
                QPortalFacility.class,
                LOCATIONS
        ).
                stream()
                .findFirst()
                .orElse(null);
    }

    QPortalFacility getFacilityByName(@NotBlank String organizationId, @NotBlank String facilityName) {
        String facilityLocationTypeId = getFacilityLocationTypeId(organizationId);
        URI uri = getUri(qPortalProperties.getLocationsAPI()
                + "?location_type_id=" + facilityLocationTypeId
                + "&name=" + encode(facilityName));
        return list(
                organizationId,
                uri,
                QPortalFacility.class,
                LOCATIONS
        )
                .stream()
                .filter(f -> f.getName().equals(facilityName))
                .findFirst()
                .orElse(null);
    }

    QPortalCurrency getCurrencyById(@NotBlank String organizationId, @NotBlank String currencyId) {
        URI uri = getUri(qPortalProperties.getCurrenciesAPI() + currencyId);
        return getFromKey(
                organizationId,
                uri,
                QPortalCurrency.class,
                CURRENCY
        );
    }

    QPortalCurrency getCurrencyByCode(@NotBlank String organizationId, String currencyCode) {
        URI uri = getUri(qPortalProperties.getCurrenciesAPI());
        return list(
                organizationId,
                uri,
                QPortalCurrency.class,
                CURRENCIES
        )
                .stream()
                .filter(c -> c.getCode().equals(currencyCode))
                .findFirst()
                .orElse(null);
    }

    QPortalVehicleType getVehicleTypeById(@NotBlank String organizationId, @NotBlank String vehicleTypeId) {
        URI uri = getUri(qPortalProperties.getVehicleTypesAPI() + vehicleTypeId);
        return get(
                organizationId,
                uri,
                QPortalVehicleType.class,
                VEHICLE_TYPES
        );
    }

    QPortalVehicleType getVehicleTypeByName(@NotBlank String organizationId, String vehicleTypeName) {
        URI uri = getUri(qPortalProperties.getVehicleTypesAPI() + "?name=" + encode(vehicleTypeName));
        return list(
                organizationId,
                uri,
                QPortalVehicleType.class,
                VEHICLE_TYPES
        )
                .stream()
                .filter(v -> v.getName().equals(vehicleTypeName))
                .findFirst()
                .orElse(null);
    }

    QPortalTag getTagByName(@NotBlank String organizationId, String tagName) {
        URI uri = getUri(qPortalProperties.getTagsAPI() + "?query=" + encode(tagName));
        return list(
                organizationId,
                uri,
                QPortalTag.class,
                TAGS
        )
                .stream()
                .filter(t -> t.getName().equals(tagName))
                .findFirst()
                .orElse(null);
    }

    private <T> T get(
            String organizationId,
            URI uri,
            Class<T> recordType,
            String key) {
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            final JsonNode responseBody = objectMapper.readTree(response.getBody());
            return objectMapper.convertValue(responseBody, recordType);
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_GET_FAILED, key, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, key, e.getMessage());
            throw new ApiCallException(String.format(ERR_GET_FAILED, key, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, key, e.getMessage());
            throw new ApiCallException(String.format(ERR_GET_FAILED, key, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private <T> T getFromKey(
            String organizationId,
            URI uri,
            Class<T> recordType,
            String key) {
        HttpEntity<String> request = createS2sHttpRequest(organizationId);
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            final JsonNode responseBody = objectMapper.readTree(response.getBody());
            return objectMapper.convertValue(responseBody.get(key), recordType);
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_GET_FAILED, key, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, key, e.getMessage());
            throw new ApiCallException(String.format(ERR_GET_FAILED, key, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, key, e.getMessage());
            throw new ApiCallException(String.format(ERR_GET_FAILED, key, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private <T> List<T> list(
            String organizationId,
            URI uri,
            Class<T> recordType,
            String key) {
        try {
            HttpEntity<String> request = createS2sHttpRequest(organizationId);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            final JsonNode responseBody = objectMapper.readTree(response.getBody()).get(key);
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, recordType);
            return objectMapper.readValue(responseBody.traverse(), listType);
        } catch (final IOException e) {
            log.error(ERR_JSON_MAPPING_FAILED, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, key, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            log.error(ERR_QPORTAL_EXCEPTION, key, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, key, organizationId), e.getStatusCode());
        } catch (Exception e) {
            log.error(ERR_QPORTAL_EXCEPTION, key, e.getMessage());
            throw new ApiCallException(String.format(ERR_LIST_FAILED, key, organizationId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private URI getUri(String url) {
        return URI.create(qPortalProperties.getBaseUrl() + url);
    }

    private String encode(String string) {
        return UriUtils.encode(string, "UTF-8");
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
}
