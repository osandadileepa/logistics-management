package com.quincus.shipment.api;

import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.domain.ShipmentResult;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.PackageDimensionUpdateResponse;
import com.quincus.shipment.api.dto.ShipmentJourneyUpdateResponse;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateResponse;
import com.quincus.shipment.api.filter.ExportFilter;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.api.filter.ShipmentFilterResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public interface ShipmentApi {

    List<Shipment> createOrUpdate(List<Shipment> shipments, boolean segmentsUpdated);

    Shipment create(Shipment shipment);

    Shipment findAndCheckLocationPermission(String id);

    Shipment findByShipmentTrackingIdAndCheckLocationPermission(String shipmentTrackingId);

    Shipment find(String id);

    void asyncCreateOrUpdate(List<Shipment> shipment, boolean segmentsUpdated);

    Shipment createOrUpdateLocal(Shipment shipment, boolean segmentsUpdated);

    Shipment update(Shipment shipment, boolean segmentsUpdated);

    void delete(String id);

    Shipment cancel(String id, TriggeredFrom triggeredFrom);

    List<ShipmentResult> createBulk(List<Shipment> shipments);

    ShipmentFilterResult findAll(ShipmentFilter filter);

    List<Shipment> findAllRelatedFromOrder(Order order);

    void export(ExportFilter filter, PrintWriter writer) throws IOException;

    List<Shipment> convertOrderMessageToShipments(String omPayload, String transactionId);

    ShipmentJourneyUpdateResponse updateShipmentJourney(ShipmentJourney shipmentJourney);

    Milestone receiveMilestoneMessageFromDispatch(String dspPayload, String uuid);

    boolean isRetryableToDispatch(String shipmentId, String uuid);

    Shipment findShipmentPackageInfoByShipmentTrackingId(String shipmentTrackingId);

    PackageDimensionUpdateResponse updateShipmentPackageDimension(PackageDimensionUpdateRequest updatePackageDimensionRequest);

    ShipmentMilestoneOpsUpdateResponse updateShipmentMilestoneOpsUpdate(ShipmentMilestoneOpsUpdateRequest milestoneOpsUpdateRequest);
}
