package com.quincus.qlogger.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.quincus.qlogger.api.utils.QLoggerPayloadUtil;
import com.quincus.qlogger.config.QLoggerProperties;
import com.quincus.qlogger.model.QLoggerRequest;
import com.quincus.qlogger.model.QLoggerResponse;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QLoggerRestClientTest {
    @InjectMocks
    QLoggerRestClient qLoggerRestClient;
    @Mock
    QLoggerProperties qLoggerProperties;
    @Mock
    RestTemplate restTemplate;

    @Mock
    ObjectMapper objectMapper;
    @Mock
    QLoggerPayloadUtil qLoggerPayloadUtil;

    @Mock
    ObjectWriter writer;
    Shipment shipment;

    ShipmentJourney shipmentJourney;
    PackageJourneySegment packageJourneySegment;
    String baseUrl;
    String api;
    String url;
    String auth;
    HttpHeaders headers;
    HttpEntity request;
    String body;

    @BeforeEach
    void before() {
        baseUrl = "https://api.dev.quincus.com/";
        api = "api/v1/q_logger/events.json";
        url = baseUrl + api;

        body = "ANY-BODY";
        auth = "DUMMY-AUTH";

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-API-AUTHORIZATION", auth);
        headers.add("X-ORGANISATION-ID", "ORG-ID");
        headers.setContentLength(body.length());

        request = new HttpEntity<>(body, headers);

        shipment = new Shipment();
        Organization organization = new Organization();
        organization.setId("ORG-ID");
        shipment.setOrganization(organization);

        mockData();
    }

    @Test
    void publishShipmentCreatedEvent_shouldReturnResult() {
        ResponseEntity<QLoggerResponse> result = qLoggerRestClient.publishShipmentCreatedEvent("ShipmentController#create", shipment);
        assertThat(result).isNotNull();
    }

    @Test
    void publishShipmentUpdatedEvent_shouldReturnResult() {
        ResponseEntity<QLoggerResponse> result = qLoggerRestClient.publishShipmentUpdatedEvent("ShipmentController#update", shipment);
        assertThat(result).isNotNull();
    }

    @Test
    void publishShipmentCancelledEvent_shouldReturnResult() {
        ResponseEntity<QLoggerResponse> result = qLoggerRestClient.publishShipmentCancelledEvent("ShipmentController#exportToCsv", shipment);
        assertThat(result).isNotNull();
    }

    @Test
    void publishShipmentExportedEvent_shouldReturnResult() {
        ResponseEntity<QLoggerResponse> result = qLoggerRestClient.publishShipmentExportedEvent("ShipmentController#exportToCsv", shipment.getOrganization(), "ANY-CONTENT");
        assertThat(result).isNotNull();
    }

    @Test
    void publishShipmentJourneyUpdatedEvent_shouldReturnResult() {
        ResponseEntity<QLoggerResponse> result = qLoggerRestClient.publishShipmentJourneyUpdatedEvent("DummyClass#dummymethod", shipmentJourney, shipmentJourney, shipment);
        assertThat(result).isNotNull();
    }

    @Test
    void publishShipmentJourneyCreateddEvent_shouldReturnResult() {
        ResponseEntity<QLoggerResponse> result = qLoggerRestClient.publishShipmentJourneyCreatedEvent("DummyClass#dummymethod", shipmentJourney, shipment);
        assertThat(result).isNotNull();
    }

    @Test
    void publishVendorBookingUpdateEvent_shouldReturnResult() {
        ResponseEntity<QLoggerResponse> result = qLoggerRestClient.publishVendorBookingUpdateEvent("DummyClass#dummymethod", shipment, packageJourneySegment, packageJourneySegment);
        assertThat(result).isNotNull();
    }

    private void mockData() {
        when(qLoggerProperties.getBaseUrl()).thenReturn(baseUrl);
        when(qLoggerProperties.getPublishEventAPI()).thenReturn(api);
        when(objectMapper.writer()).thenReturn(writer);
        when(qLoggerPayloadUtil.createQLoggerPayloadWithMandatoryFields(any(), any(), any(Shipment.class))).thenReturn(new QLoggerRequest());
    }
}
