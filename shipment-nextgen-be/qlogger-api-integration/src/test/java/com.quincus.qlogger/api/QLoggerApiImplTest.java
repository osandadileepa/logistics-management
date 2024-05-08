package com.quincus.qlogger.api;

import com.quincus.qlogger.model.QLoggerResponse;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.web.common.exception.model.ApiCallException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QLoggerApiImplTest {

    @InjectMocks
    QLoggerApiImpl qLoggerAPI;
    @Mock
    QLoggerRestClient qLoggerRestClient;
    @Mock
    Shipment shipment;
    @Mock
    PackageJourneySegment packageJourneySegment;

    @Test
    void publishShipmentCreatedEvent_shouldReturnResult() {
        ResponseEntity<QLoggerResponse> response = new ResponseEntity(mock(QLoggerResponse.class), HttpStatus.OK);
        String source = "ShipmentController#add";
        when(qLoggerRestClient.publishShipmentCreatedEvent("ShipmentController#add", shipment)).thenReturn(response);
        assertThat(qLoggerAPI.publishShipmentCreatedEvent("ShipmentController#add", shipment)).isNotNull();
    }

    @Test
    void publishShipmentCancelledEvent_shouldReturnResult() {
        ResponseEntity<QLoggerResponse> response = new ResponseEntity(mock(QLoggerResponse.class), HttpStatus.OK);
        when(qLoggerRestClient.publishShipmentCancelledEvent("ShipmentController#update", shipment)).thenReturn(response);
        assertThat(qLoggerAPI.publishShipmentCancelledEvent("ShipmentController#update", shipment)).isNotNull();
    }

    @Test
    void publishShipmentUpdatedEvent_shouldReturnResult() {
        ResponseEntity<QLoggerResponse> response = new ResponseEntity(mock(QLoggerResponse.class), HttpStatus.OK);
        when(qLoggerRestClient.publishShipmentUpdatedEvent("ShipmentController#cancel", shipment)).thenReturn(response);
        assertThat(qLoggerAPI.publishShipmentUpdatedEvent("ShipmentController#cancel", shipment)).isNotNull();
    }

    @Test
    void publishShipmentExportedEvent_shouldReturnResult() {
        ResponseEntity<QLoggerResponse> response = new ResponseEntity(mock(QLoggerResponse.class), HttpStatus.OK);
        Organization organization = mock(Organization.class);
        when(qLoggerRestClient.publishShipmentExportedEvent("ShipmentController#exportToCsv", organization, "DUMMY CONTENT")).thenReturn(response);
        assertThat(qLoggerAPI.publishShipmentExportedEvent("ShipmentController#exportToCsv", organization, "DUMMY CONTENT")).isNotNull();
    }

    @Test
    void publishShipmentExportedEventShouldReturnResultWithRetry() {
        ShipmentJourney previousShipmentJourney = new ShipmentJourney();
        ShipmentJourney newShipmentJourney = new ShipmentJourney();
        Shipment shipment = new Shipment();
        ResponseEntity<QLoggerResponse> qLoggerResponse = ResponseEntity.ok(new QLoggerResponse());

        when(qLoggerRestClient.publishShipmentJourneyUpdatedEvent(anyString(), any(ShipmentJourney.class), any(ShipmentJourney.class), any(Shipment.class)))
                .thenReturn(qLoggerResponse);

        ResponseEntity<QLoggerResponse> result = qLoggerAPI.publishShipmentJourneyUpdatedEventWithRetry("source", previousShipmentJourney, newShipmentJourney, shipment);

        assertThat(result).isEqualTo(qLoggerResponse);
        verify(qLoggerRestClient, times(1)).publishShipmentJourneyUpdatedEvent("source", previousShipmentJourney, newShipmentJourney, shipment);
    }

    @Test
    void publishShipmentJourneyUpdatedEventWithRetryApiCallFailed() {
        ShipmentJourney previousShipmentJourney = new ShipmentJourney();
        ShipmentJourney newShipmentJourney = new ShipmentJourney();
        Shipment shipment = new Shipment();
        ResponseEntity<QLoggerResponse> qLoggerResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        when(qLoggerRestClient.publishShipmentJourneyUpdatedEvent(anyString(), any(ShipmentJourney.class), any(ShipmentJourney.class), any(Shipment.class)))
                .thenReturn(qLoggerResponse);

        assertThatThrownBy(() -> qLoggerAPI.publishShipmentJourneyUpdatedEventWithRetry("source", previousShipmentJourney, newShipmentJourney, shipment))
                .isInstanceOf(ApiCallException.class)
                .hasMessage("Qlogger external API call failed");

        verify(qLoggerRestClient, times(1)).publishShipmentJourneyUpdatedEvent("source", previousShipmentJourney, newShipmentJourney, shipment);
    }

    @Test
    void publishVendorBookingUpdateEvent_shouldReturnResult() {
        ResponseEntity<QLoggerResponse> response = new ResponseEntity(mock(QLoggerResponse.class), HttpStatus.OK);
        String source = "VendorBookingPostProcessService#notifyOthersOnVendorBookingUpdate";
        when(qLoggerRestClient.publishVendorBookingUpdateEvent(anyString(), any(Shipment.class), any(PackageJourneySegment.class), any(PackageJourneySegment.class))).thenReturn(response);
        assertThat(qLoggerAPI.publishVendorBookingUpdateEvent(source, shipment, packageJourneySegment, packageJourneySegment)).isNotNull();
    }
}
