package com.quincus.qportal.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.qportal.config.QPortalProperties;
import com.quincus.qportal.model.QPortalCostType;
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
import com.quincus.web.common.exception.model.ApiCallException;
import com.quincus.web.common.exception.model.ApiNetworkIssueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QPortalRestClientTest {
    @InjectMocks
    QPortalRestClient qPortalRestClient;
    @Mock
    QPortalProperties qPortalProperties;
    @Mock
    RestTemplate restTemplate;
    @Mock
    ObjectMapper objectMapper;
    String baseUrl;
    String s2sToken;
    HttpHeaders headers;
    HttpEntity<String> request;
    String json;

    @BeforeEach
    void before() {
        baseUrl = "https://api.test.quincus.com/";
        s2sToken = "DUMMY-AUTH";
        headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add("X-API-AUTHORIZATION", s2sToken);
        headers.add("X-ORGANISATION-ID", "organizationId");
        request = new HttpEntity<>(headers);
        json = "{\"partners\": [{\"id\": \"59af783c-a102-462a-9e17-4d866795438f\", \"name\": \"AZ Test Courier\"}]}";
    }

    @Test
    void getPackageTypes_ShouldReturnResult() throws IOException {
        String api = "api/open_api/v1/package_types";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getPackageTypesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalPackageType()));
        assertThat(qPortalRestClient.listPackageTypes("organizationId")).isNotNull();
    }

    @Test
    void getPackageTypes_WhenParseException_ShouldThrowApiCallException() throws IOException {
        String api = "api/open_api/v1/package_types";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getPackageTypesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenThrow(new RuntimeException("test error"));
        assertThatThrownBy(() -> qPortalRestClient.listPackageTypes("organizationId")).isInstanceOf(ApiCallException.class);
    }

    @Test
    void getPartners_ShouldReturnResult() throws IOException {
        String api = "api/open_api/v1/partners";
        URI uri = URI.create(baseUrl + api + "?existent=true&sort_by=name");
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getPartnersAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalPartner()));
        assertThat(qPortalRestClient.listPartners("organizationId")).isNotNull();
    }

    @Test
    void listMilestone_ShouldHaveCorrectUriAndReturnResult() throws IOException {
        String api = "api/open_api/v1/milestones.json?";
        URI uri = URI.create(baseUrl + api + "?sort_by=name");
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getMilestonesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalMilestone()));
        assertThat(qPortalRestClient.listMilestones("organizationId")).isNotNull();
    }

    @Test
    void listMilestone_WhenParseException_ShouldThrowApiCallException() throws IOException {
        String api = "api/open_api/v1/milestones.json?";
        URI uri = URI.create(baseUrl + api + "?sort_by=name");
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getMilestonesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("Sample error"));

        assertThatThrownBy(() -> qPortalRestClient.listMilestones("organizationId")).isInstanceOf(ApiCallException.class)
                .hasMessage("Issue occurred during the conversion of `milestones` for organization id `organizationId`. Error message: `Sample error`.");
    }

    @Test
    void listMilestone_whenHttpServerErrorException_ShouldThrowApiNetworkIssueException() {
        String api = "api/open_api/v1/milestones.json?";
        URI uri = URI.create(baseUrl + api + "?sort_by=name");
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getMilestonesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenThrow(new HttpServerErrorException(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> qPortalRestClient.listMilestones("organizationId")).isInstanceOf(ApiNetworkIssueException.class);
    }

    @Test
    void listMilestone_whenHttpClientErrorException_ShouldThrowApiCallException() {
        String api = "api/open_api/v1/milestones.json?";
        URI uri = URI.create(baseUrl + api + "?sort_by=name");
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getMilestonesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> qPortalRestClient.listMilestones("organizationId")).isInstanceOf(ApiCallException.class);
    }

    @Test
    void listMilestone_whenGeneralException_ShouldThrowApiCallException() {
        String api = "api/open_api/v1/milestones.json?";
        URI uri = URI.create(baseUrl + api + "?sort_by=name");
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getMilestonesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenThrow(new RuntimeException("test error"));

        assertThatThrownBy(() -> qPortalRestClient.listMilestones("organizationId")).isInstanceOf(ApiCallException.class);
    }

    @Test
    void listCurrencies_ShouldHaveCorrectUriAndReturnResult() throws IOException {
        String api = "api/v1/currencies/";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getCurrenciesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalMilestone()));
        assertThat(qPortalRestClient.listCurrencies("organizationId")).isNotNull();
    }

    @Test
    void listCurrencies_WhenParseException_ShouldThrowApiCallException() throws IOException {
        String api = "api/v1/currencies/";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getCurrenciesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenThrow(new RuntimeException("test error"));
        assertThatThrownBy(() -> qPortalRestClient.listCurrencies("organizationId"))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void getUserByUd_WhenParseException_ShouldThrowApiCallException() {
        String api = "api/v1/users/";
        String useId = "userId";
        URI uri = URI.create(baseUrl + api + useId);

        JsonNode node1 = mock(JsonNode.class);
        ResponseEntity<JsonNode> response = new ResponseEntity<>(node1, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getUsersAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class)).thenReturn(response);

        when(objectMapper.convertValue(any(JsonNode.class), eq(QPortalUser.class))).thenThrow(new RuntimeException("testError"));
        assertThatThrownBy(() -> qPortalRestClient.getUserById("organizationId", useId))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void getUserByUd_ShouldHaveCorrectUriAndReturnResult() throws IOException {
        String api = "api/v1/users/";
        String useId = "userId";
        URI uri = URI.create(baseUrl + api + useId);

        JsonNode node1 = mock(JsonNode.class);
        ResponseEntity<JsonNode> response = new ResponseEntity<>(node1, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getUsersAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class)).thenReturn(response);

        when(objectMapper.convertValue(any(JsonNode.class), eq(QPortalUser.class))).thenReturn(new QPortalUser());
        assertThat(qPortalRestClient.getUserById("organizationId", useId)).isNotNull();
    }

    @Test
    void getPartnersWithPaginationParams_ShouldReturnResult() throws IOException {
        int page = 1;
        int perPage = 10;
        String userId = UUID.randomUUID().toString();
        String query = "test";
        String api = "api/open_api/v1/partners";
        URI uri = URI.create(baseUrl + api + "?existent=true&sort_by=name&page=" + page + "&per_page=" + perPage + "&query=" + query + "&user_ids=" + userId);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getPartnersAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalPartner()));
        assertThat(qPortalRestClient.listPartnersWithSearchAndPagination("organizationId", userId, perPage, page, query)).isNotNull();
    }

    @Test
    void listPartnerWithPagination_whenParseException_ShouldThrowApiCallException() throws IOException {
        int page = 1;
        int perPage = 10;
        String userId = UUID.randomUUID().toString();
        String query = "test here";
        String api = "api/open_api/v1/partners";
        String encodedQuery = UriComponentsBuilder.fromUriString(query).build().encode().toUriString();
        URI uri = URI.create(baseUrl + api + "?existent=true&sort_by=name&page=" + page + "&per_page=" + perPage + "&query=" + encodedQuery + "&user_ids=" + userId);

        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getPartnersAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("test error"));
        assertThatThrownBy(() -> qPortalRestClient.listPartnersWithSearchAndPagination("organizationId", userId, perPage, page, query))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void listPartner_whenParseException_ShouldThrowApiCallException() throws IOException {
        String api = "api/open_api/v1/partners";
        URI uri = URI.create(baseUrl + api + "?existent=true&sort_by=name");

        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getPartnersAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("test error"));
        assertThatThrownBy(() -> qPortalRestClient.listPartners("organizationId"))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void getPartnersById_ShouldInvokeCorrectUrlAndReturnNotNull() throws IOException {
        String api = "api/open_api/v1/partners";
        String partnerId = "partnerId";
        URI uri = URI.create(baseUrl + api + "?id=" + partnerId);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getPartnersAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalPartner()));
        assertThat(qPortalRestClient.getPartnerById("organizationId", partnerId)).isNotNull();
    }

    @Test
    void getPartnersById_WhenParseException_ShouldThrowApiCallException() throws IOException {
        String api = "api/open_api/v1/partners";
        String partnerId = "partnerId";
        URI uri = URI.create(baseUrl + api + "?id=" + partnerId);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getPartnersAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenThrow(new RuntimeException("test error"));
        assertThatThrownBy(() -> qPortalRestClient.getPartnerById("organizationId", partnerId)).isInstanceOf(ApiCallException.class);
    }

    @Test
    void getPartnersByName_ShouldInvokeCorrectUrlAndReturnNotNull() throws IOException {
        String api = "api/open_api/v1/partners";
        String partnerName = "partnerName";
        URI uri = URI.create(baseUrl + api + "?name=" + partnerName);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getPartnersAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalPartner()));
        assertThat(qPortalRestClient.getPartnerByName("organizationId", partnerName)).isNotNull();
    }

    @Test
    void getPartnersByName_WhenParseException_ShouldThrowApiCallException() throws IOException {
        String api = "api/open_api/v1/partners";
        String partnerName = "partnerName";
        URI uri = URI.create(baseUrl + api + "?name=" + partnerName);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getPartnersAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenThrow(new RuntimeException("test error"));
        assertThatThrownBy(() -> qPortalRestClient.getPartnerByName("organizationId", partnerName))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void getLocations_ShouldReturnResult() {
        String api = "api/open_api/v1/locations";
        URI uri = URI.create(baseUrl + api + "?id=" + "locationId");
        ResponseEntity<JsonNode> response = mock(ResponseEntity.class);
        JsonNode body = mock(JsonNode.class);
        JsonNode locations = mock(JsonNode.class);
        when(response.getBody()).thenReturn(body);
        when(body.get(anyString())).thenReturn(locations);
        when(locations.get(0)).thenReturn(mock(JsonNode.class));
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getLocationsAPI()).thenReturn(api);
        when(objectMapper.convertValue(any(), eq(QPortalLocation.class))).thenReturn(new QPortalLocation());
        when(restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class)).thenReturn(response);
        assertThat(qPortalRestClient.getLocation("organizationId", "locationId")).isNotNull();
    }

    @Test
    void getLocations_WhenParseException_ShouldThrowApiCallException() {
        String api = "api/open_api/v1/locations";
        URI uri = URI.create(baseUrl + api + "?id=" + "locationId");
        ResponseEntity<JsonNode> response = mock(ResponseEntity.class);
        JsonNode body = mock(JsonNode.class);
        JsonNode locations = mock(JsonNode.class);
        when(response.getBody()).thenReturn(body);
        when(body.get(anyString())).thenReturn(locations);
        when(locations.get(0)).thenReturn(mock(JsonNode.class));
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getLocationsAPI()).thenReturn(api);
        when(objectMapper.convertValue(any(), eq(QPortalLocation.class))).thenThrow(new RuntimeException("test error"));
        when(restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class)).thenReturn(response);
        assertThatThrownBy(() -> qPortalRestClient.getLocation("organizationId", "locationId"))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void getLocationByName_ShouldInvokeCorrectUrlAndReturnNotNull() throws IOException {
        String api = "api/open_api/v1/locations";
        String locationName = "locName";
        URI uri = URI.create(baseUrl + api + "?name=" + locationName);
        ResponseEntity<JsonNode> response = mock(ResponseEntity.class);
        JsonNode body = mock(JsonNode.class);
        JsonNode locations = mock(JsonNode.class);
        when(response.getBody()).thenReturn(body);
        when(body.get(anyString())).thenReturn(locations);
        when(locations.traverse()).thenReturn(mock(JsonParser.class));
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getLocationsAPI()).thenReturn(api);
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalLocation()));
        when(restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class)).thenReturn(response);
        assertThat(qPortalRestClient.getLocationsByName("organizationId", locationName)).isNotNull();
    }

    @Test
    void getLocationByName_WhenParseException_ShouldThrowApiCallException() throws IOException {
        String api = "api/open_api/v1/locations";
        String locationName = "locName";
        URI uri = URI.create(baseUrl + api + "?name=" + locationName);
        ResponseEntity<JsonNode> response = mock(ResponseEntity.class);
        JsonNode body = mock(JsonNode.class);
        JsonNode locations = mock(JsonNode.class);
        when(response.getBody()).thenReturn(body);
        when(body.get(anyString())).thenReturn(locations);
        when(locations.traverse()).thenReturn(mock(JsonParser.class));
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getLocationsAPI()).thenReturn(api);
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenThrow(new RuntimeException());
        when(restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class)).thenReturn(response);
        assertThatThrownBy(() -> qPortalRestClient.getLocationsByName("organizationId", locationName)).isInstanceOf(ApiCallException.class);
    }

    @Test
    void getCostTypes_ShouldReturnResult() throws IOException {
        String api = "api/open_api/v1/cost_types";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getCostTypesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalCostType()));
        assertThat(qPortalRestClient.listCostTypes("organizationId")).isNotNull();
    }

    @Test
    void getCostTypes_whenParseException_shouldThrowApiCallException() throws IOException {
        String api = "api/open_api/v1/cost_types";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getCostTypesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenThrow(new RuntimeException("test error"));
        assertThatThrownBy(() -> qPortalRestClient.listCostTypes("organizationId"))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void listLocations_ShouldReturnResult() throws IOException {
        String api = "api/open_api/v1/locations.json/";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getLocationsAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalLocation()));
        assertThat(qPortalRestClient.listLocations("organizationId")).isNotNull();
    }

    @Test
    void listLocations_WhenParseException_ThenShouldThrowApiCallException() throws IOException {
        String api = "api/open_api/v1/locations.json/";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getLocationsAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenThrow(new RuntimeException("Test error"));
        assertThatThrownBy(() -> qPortalRestClient.listLocations("organizationId")).isInstanceOf(ApiCallException.class);
    }

    @Test
    void listDrivers_ShouldReturnResult() throws IOException {
        String baseApi = "api/v1/users/";
        String api = "api/v1/users/drivers";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getUsersAPI()).thenReturn(baseApi);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalDriver()));
        assertThat(qPortalRestClient.listDrivers("organizationId")).isNotNull();
    }

    @Test
    void listDrivers_WhenParseException_ThenShouldThrowApiCallException() throws IOException {
        String baseApi = "api/v1/users/";
        String api = "api/v1/users/drivers";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getUsersAPI()).thenReturn(baseApi);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenThrow(new RuntimeException("test exception"));
        assertThatThrownBy(() -> qPortalRestClient.listDrivers("organizationId")).isInstanceOf(ApiCallException.class);
    }

    @Test
    void listDriversByPartners_ShouldReturnResult() throws IOException {
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<QPortalUserRequest> requestPost = new HttpEntity<>(new QPortalUserRequest(), headers);
        String api = "api/v1/users/";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getListUsersAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.POST, requestPost, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalDriver()));
        assertThat(qPortalRestClient.listDriversByPartners("organizationId", new QPortalUserRequest())).isNotNull();
    }

    @Test
    void listDriversByPartners_WhenParseException_ThenShouldThrowApiCallException() throws IOException {
        QPortalUserRequest userRequest = new QPortalUserRequest();
        userRequest.setAccessiblePartnerIds(List.of("partner1", "partner2"));
        userRequest.setDriver(true);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<QPortalUserRequest> requestPost = new HttpEntity<>(userRequest, headers);
        String api = "api/v1/users/";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getListUsersAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.POST, requestPost, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenThrow(new RuntimeException("test exception"));
        assertThatThrownBy(
                () -> qPortalRestClient.listDriversByPartners("organizationId", userRequest)
        ).isInstanceOf(ApiCallException.class);
    }

    @Test
    void listVehicles_ShouldReturnResult() throws IOException {
        String api = "api/open_api/v1/vehicles";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getVehiclesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalVehicle()));
        assertThat(qPortalRestClient.listVehicles("organizationId")).isNotNull();
    }

    @Test
    void listVehicles_whenParseException_thenShouldThrowApiCallException() throws IOException {
        String api = "api/open_api/v1/vehicles";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getVehiclesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenThrow(new RuntimeException());
        assertThatThrownBy(() -> qPortalRestClient.listVehicles("organizationId"))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void triggerNotificationQcomm() {
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<QPortalNotificationRequest> requestPost = new HttpEntity<>(new QPortalNotificationRequest(), headers);

        String api = "api/open_api/v1/notification";
        URI uri = URI.create(baseUrl + api);
        QPortalNotificationResponse responseBody = mock(QPortalNotificationResponse.class);
        ResponseEntity<QPortalNotificationResponse> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getNotificationAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.POST, requestPost, QPortalNotificationResponse.class)).thenReturn(response);

        assertThat(qPortalRestClient.sendNotification("organizationId", new QPortalNotificationRequest())).isNotNull();
    }

    @Test
    void getOrganisations_whenParseException_thenShouldThrowApiCallException() {
        String organizationId = "organizationId";
        String api = "api/open_api/v1/organisations/";
        URI uri = URI.create(baseUrl + api + organizationId);
        ResponseEntity<JsonNode> response = mock(ResponseEntity.class);
        JsonNode body = mock(JsonNode.class);

        when(response.getBody()).thenReturn(body);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getOrganizationAPI()).thenReturn(api);
        when(objectMapper.convertValue(any(), eq(QPortalOrganization.class))).thenThrow(new RuntimeException("test exception"));
        when(restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class)).thenReturn(response);

        assertThatThrownBy(() -> qPortalRestClient.getOrganizationById(organizationId)).isInstanceOf(ApiCallException.class);
    }

    @Test
    void getOrganisations_ShouldReturnResult() {
        String organizationId = "organizationId";
        String api = "api/open_api/v1/organisations/";
        URI uri = URI.create(baseUrl + api + organizationId);
        ResponseEntity<JsonNode> response = mock(ResponseEntity.class);
        JsonNode body = mock(JsonNode.class);

        when(response.getBody()).thenReturn(body);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getOrganizationAPI()).thenReturn(api);
        when(objectMapper.convertValue(any(), eq(QPortalOrganization.class))).thenReturn(new QPortalOrganization());
        when(restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class)).thenReturn(response);

        assertThat(qPortalRestClient.getOrganizationById(organizationId)).isNotNull();
    }

    @Test
    void listMilestones_ShouldReturnResult() throws IOException {
        String api = "api/open_api/v1/milestones";
        URI uri = URI.create(baseUrl + api + "?sort_by=name");
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getMilestonesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalMilestone()));
        assertThat(qPortalRestClient.listMilestones("organizationId")).isNotNull();
    }

    @Test
    void getMilestonesWithPaginationParams_ShouldReturnResult() throws IOException {
        int page = 1;
        int perPage = 10;
        String query = "test test";
        String api = "api/open_api/v1/milestones";
        String encodedQuery = UriComponentsBuilder.fromUriString(query).build().encode().toUriString();
        URI uri = URI.create(baseUrl + api + "?existent=true&sort_by=name&page=" + page + "&per_page=" + perPage + "&query=" + encodedQuery);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getMilestonesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalPartner()));
        assertThat(qPortalRestClient.listMilestonesWithSearchAndPagination("organizationId", perPage, page, query)).isNotNull();
    }

    @Test
    void listMilestonesWithPagination_whenParseException_ShouldThrowApiCallException() throws IOException {
        int page = 1;
        int perPage = 10;
        String query = "test";
        String api = "api/open_api/v1/milestones";
        URI uri = URI.create(baseUrl + api + "?existent=true&sort_by=name&page=" + page + "&per_page=" + perPage + "&query=" + query);

        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getMilestonesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("test error"));
        assertThatThrownBy(() -> qPortalRestClient.listMilestonesWithSearchAndPagination("organizationId", perPage, page, query))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void getCostType_ShouldReturnResult() {
        String costTypeId = "costTypeId";
        String api = "api/v1/cost_types/";
        URI uri = URI.create(baseUrl + api + costTypeId);
        ResponseEntity<JsonNode> response = mock(ResponseEntity.class);
        JsonNode body = mock(JsonNode.class);
        when(response.getBody()).thenReturn(body);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getCostTypesAPI()).thenReturn(api);
        when(objectMapper.convertValue(any(), eq(QPortalCostType.class))).thenReturn(new QPortalCostType());
        when(restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class)).thenReturn(response);
        assertThat(qPortalRestClient.getCostType("organizationId", "costTypeId")).isNotNull();
    }

    @Test
    void getCostType_whenParseException_thenShouldThrowApiCallException() {
        String costTypeId = "costTypeId";
        String api = "api/v1/cost_types/";
        URI uri = URI.create(baseUrl + api + costTypeId);
        ResponseEntity<JsonNode> response = mock(ResponseEntity.class);
        JsonNode body = mock(JsonNode.class);
        when(response.getBody()).thenReturn(body);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getCostTypesAPI()).thenReturn(api);
        when(objectMapper.convertValue(any(), eq(QPortalCostType.class))).thenThrow(new RuntimeException("test execption"));
        when(restTemplate.exchange(uri, HttpMethod.GET, request, JsonNode.class)).thenReturn(response);
        assertThatThrownBy(() -> qPortalRestClient.getCostType("organizationId", "costTypeId"))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    void listVehicleTypes_ShouldReturnResult() throws IOException {
        String api = "api/open_api/v1/vehicle_types.json/";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getVehicleTypesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenReturn(List.of(new QPortalVehicle()));
        assertThat(qPortalRestClient.listVehicleTypes("organizationId")).isNotNull();
    }

    @Test
    void listVehicleTypes_whenParseException_thenShouldThrowApiCallException() throws IOException {
        String api = "api/open_api/v1/vehicle_types.json/";
        URI uri = URI.create(baseUrl + api);
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);
        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getVehicleTypesAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.GET, request, String.class)).thenReturn(response);
        when(objectMapper.readTree(anyString())).thenReturn(node1);
        when(node1.get(anyString())).thenReturn(node2);
        when(node2.traverse()).thenReturn(mock(JsonParser.class));
        when(objectMapper.readValue(any(JsonParser.class), any(TypeReference.class))).thenThrow(new RuntimeException());
        assertThatThrownBy(() -> qPortalRestClient.listVehicleTypes("organizationId"))
                .isInstanceOf(ApiCallException.class);
    }


}
