package com.quincus.qportal.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.qportal.config.QPortalProperties;
import com.quincus.qportal.model.QPortalCostType;
import com.quincus.qportal.model.QPortalDriver;
import com.quincus.qportal.model.QPortalLocation;
import com.quincus.qportal.model.QPortalNotification;
import com.quincus.qportal.model.QPortalNotificationResponse;
import com.quincus.qportal.model.QPortalPackageType;
import com.quincus.qportal.model.QPortalPartner;
import com.quincus.qportal.model.QPortalVehicle;
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
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    void getPartners_ShouldReturnResult() throws IOException {
        String api = "api/open_api/v1/partners";
        URI uri = URI.create(baseUrl + api);
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
    void triggerNotificationQcomm() {
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<QPortalNotification> requestPost = new HttpEntity<>(new QPortalNotification(), headers);

        String api = "api/open_api/v1/notification";
        URI uri = URI.create(baseUrl + api);
        QPortalNotificationResponse responseBody = mock(QPortalNotificationResponse.class);
        ResponseEntity<QPortalNotificationResponse> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(qPortalProperties.getS2sToken()).thenReturn(s2sToken);
        when(qPortalProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qPortalProperties.getNotificationAPI()).thenReturn(api);
        when(restTemplate.exchange(uri, HttpMethod.POST, requestPost, QPortalNotificationResponse.class)).thenReturn(response);

        assertThat(qPortalRestClient.sendNotification("organizationId", new QPortalNotification())).isNotNull();
    }
}
