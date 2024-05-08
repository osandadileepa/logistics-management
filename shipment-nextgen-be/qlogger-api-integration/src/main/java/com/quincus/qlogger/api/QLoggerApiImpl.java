package com.quincus.qlogger.api;

import com.quincus.qlogger.model.QLoggerResponse;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.web.common.exception.model.ApiCallException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class QLoggerApiImpl implements QLoggerAPI {
    private final QLoggerRestClient qLoggerRestClient;

    @Async("externalApiExecutor")
    @Override
    public ResponseEntity<QLoggerResponse> publishShipmentCreatedEvent(final String source, final Shipment shipment) {
        return qLoggerRestClient.publishShipmentCreatedEvent(source, shipment);
    }

    @Async("externalApiExecutor")
    @Override
    public ResponseEntity<QLoggerResponse> publishShipmentCancelledEvent(final String source, final Shipment shipment) {
        return qLoggerRestClient.publishShipmentCancelledEvent(source, shipment);
    }

    @Async("externalApiExecutor")
    @Override
    public ResponseEntity<QLoggerResponse> publishShipmentUpdatedEvent(final String source, final Shipment shipment) {
        return qLoggerRestClient.publishShipmentUpdatedEvent(source, shipment);
    }

    @Async("externalApiExecutor")
    @Override
    public ResponseEntity<QLoggerResponse> publishShipmentExportedEvent(final String source, final Organization organization, final String fileContent) {
        return qLoggerRestClient.publishShipmentExportedEvent(source, organization, fileContent);
    }

    @Async("externalApiExecutor")
    @Override
    public ResponseEntity<QLoggerResponse> publishShipmentJourneyUpdatedEvent(String source, ShipmentJourney previousShipmentJourney, ShipmentJourney newShipmentJourney, Shipment shipment) {
        return qLoggerRestClient.publishShipmentJourneyUpdatedEvent(source, previousShipmentJourney, newShipmentJourney, shipment);
    }

    @Override
    @Retryable(value = {ApiCallException.class}, maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.maxDelay}"))
    public ResponseEntity<QLoggerResponse> publishShipmentJourneyUpdatedEventWithRetry(String source, ShipmentJourney previousShipmentJourney, ShipmentJourney newShipmentJourney, Shipment shipment) {
        ResponseEntity<QLoggerResponse> qLoggerResponse = qLoggerRestClient.publishShipmentJourneyUpdatedEvent(source, previousShipmentJourney, newShipmentJourney, shipment);
        if (HttpStatus.INTERNAL_SERVER_ERROR == qLoggerResponse.getStatusCode()) {
            throw new ApiCallException("Qlogger external API call failed", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return qLoggerResponse;
    }

    @Async("externalApiExecutor")
    @Override
    public ResponseEntity<QLoggerResponse> publishShipmentJourneyCreatedEvent(String source, ShipmentJourney newShipmentJourney, Shipment shipment) {
        return qLoggerRestClient.publishShipmentJourneyCreatedEvent(source, newShipmentJourney, shipment);
    }

    @Async("externalApiExecutor")
    @Override
    public ResponseEntity<QLoggerResponse> publishPackageDimensionUpdateEvent(String source, PackageDimension oldDimension, Shipment shipment) {
        return qLoggerRestClient.publishPackageDimensionUpdateEvent(source, oldDimension, shipment);
    }

    @Async("externalApiExecutor")
    @Override
    public ResponseEntity<QLoggerResponse> publishBulkPackageDimensionUpdateEvent(String source, List<Package> packageAttributes, List<PackageDimension> oldDimensions, List<PackageDimension> newDimensions) {
        return qLoggerRestClient.publishBulkPackageDimensionUpdateEvent(source, packageAttributes, oldDimensions, newDimensions);
    }

    @Async("externalApiExecutor")
    @Override
    public ResponseEntity<QLoggerResponse> publishCostCreatedEvent(String source, Cost cost) {
        return qLoggerRestClient.publishCostCreatedEvent(source, cost);
    }

    @Async("externalApiExecutor")
    @Override
    public ResponseEntity<QLoggerResponse> publishCostUpdatedEvent(String source, Cost cost) {
        return qLoggerRestClient.publishCostUpdatedEvent(source, cost);
    }

    @Async("externalApiExecutor")
    @Override
    public ResponseEntity<QLoggerResponse> publishVendorBookingUpdateEvent(String source, Shipment shipment, PackageJourneySegment oldPackageJourneySegment, PackageJourneySegment newPackageJourneySegment) {
        return qLoggerRestClient.publishVendorBookingUpdateEvent(source, shipment, oldPackageJourneySegment, newPackageJourneySegment);
    }
}
