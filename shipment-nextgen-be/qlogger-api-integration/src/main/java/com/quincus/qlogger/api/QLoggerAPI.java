package com.quincus.qlogger.api;

import com.quincus.qlogger.model.QLoggerResponse;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface QLoggerAPI {

    ResponseEntity<QLoggerResponse> publishShipmentCreatedEvent(String source, Shipment shipment);

    ResponseEntity<QLoggerResponse> publishShipmentCancelledEvent(String source, Shipment shipment);

    ResponseEntity<QLoggerResponse> publishShipmentUpdatedEvent(String source, Shipment shipment);

    ResponseEntity<QLoggerResponse> publishShipmentExportedEvent(String source, Organization organization, String fileContent);

    ResponseEntity<QLoggerResponse> publishShipmentJourneyUpdatedEvent(String source, ShipmentJourney previousShipmentJourney, ShipmentJourney newShipmentJourney, Shipment shipment);

    ResponseEntity<QLoggerResponse> publishShipmentJourneyUpdatedEventWithRetry(String source, ShipmentJourney previousShipmentJourney, ShipmentJourney newShipmentJourney, Shipment shipment);

    ResponseEntity<QLoggerResponse> publishShipmentJourneyCreatedEvent(String source, ShipmentJourney newShipmentJourney, Shipment shipment);

    ResponseEntity<QLoggerResponse> publishPackageDimensionUpdateEvent(String source, PackageDimension oldDimension, Shipment shipment);

    ResponseEntity<QLoggerResponse> publishBulkPackageDimensionUpdateEvent(String source, List<Package> packageAttributes, List<PackageDimension> oldDimensions, List<PackageDimension> newDimensions);

    ResponseEntity<QLoggerResponse> publishCostCreatedEvent(String source, Cost cost);

    ResponseEntity<QLoggerResponse> publishCostUpdatedEvent(String source, Cost cost);

    ResponseEntity<QLoggerResponse> publishVendorBookingUpdateEvent(String source, Shipment shipment, PackageJourneySegment oldPackageJourneySegment, PackageJourneySegment newPackageJourneySegment);
}
