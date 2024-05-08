package com.quincus.shipment;

import com.quincus.ext.annotation.UUID;
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
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.List;

@Validated
@RequestMapping("/shipments")
@Tag(name = "shipments", description = "This endpoint allows to manage shipments related transactions.")
public interface ShipmentController {

    @PostMapping
    @Operation(summary = "Create Shipment API", description = "Create a new shipment.", tags = "shipments")
    Response<Shipment> add(@Valid @RequestBody final Request<Shipment> request);

    @GetMapping("/{id}")
    @Operation(summary = "Find Shipment By Shipment UUID API ", description = "Find an existing shipment using the Shipment V2 generated UUID.", tags = "shipments")
    Response<Shipment> findByShipmentId(@UUID @PathVariable("id") final String id);

    @GetMapping
    @Operation(summary = "Find Shipment By Shipment Tracking ID API", description = "Find an existing shipment using tracking ID.", tags = "shipments")
    Response<Shipment> findByShipmentTrackingId(@Size(max=128) @RequestParam("shipment_tracking_id") final String shipmentTrackingId);

    @PutMapping
    @Operation(summary = "Update Shipment API", description = "Update an existing shipment.", tags = "shipments")
    Response<Shipment> update(@Valid @RequestBody final Request<Shipment> request);

    @PostMapping("/bulk")
    @Operation(summary = "Add Bulk Shipment API", description = "Create new shipments in bulk.", tags = "shipments")
    Response<List<ShipmentResult>> addBulk(@Valid @RequestBody final Request<List<@Valid Shipment>> request);

    @PatchMapping("/cancel/{id}")
    @Operation(summary = "Cancel Shipment API", description = "Cancel an existing shipment.", tags = "shipments")
    Response<Shipment> cancel(@PathVariable("id") @UUID final String id);

    @PostMapping("/list")
    @Operation(summary = "Find Shipments API", description = "Return a list of shipments based on a filter request.", tags = "shipments")
    Response<ShipmentFilterResult> findAll(@Valid @RequestBody final Request<ShipmentFilter> request);

    @PostMapping("/export")
    @Operation(summary = "Export Shipment API", description = "Export a CSV list of shipments based on a filter request.", tags = "shipments")
    void exportToCsv(@Valid @RequestBody final Request<ExportFilter> request, final HttpServletResponse servletResponse) throws IOException;

    @GetMapping("/{shipmentId}/shipment_journey")
    @Operation(summary = "Find Shipment Journey API", description = "Find an existing shipment journey.", tags = "shipments")
    Response<ShipmentJourney> getShipmentJourney(@UUID @PathVariable("shipmentId") final String shipmentId);

    @PutMapping("/shipment_journey")
    @Operation(summary = "Update Shipment Journey API", description = "Update an existing shipment journey.", tags = "shipments")
    Response<ShipmentJourneyUpdateResponse> updateShipmentJourney(@Valid @RequestBody final Request<ShipmentJourney> request);

    @GetMapping("/get-by-tracking-id/{shipment_tracking_id}/package-dimension")
    @Operation(summary = "Find Shipment Package Dimension API", description = "Return a shipment’s package dimension based on shipment tracking id.", tags = "shipments")
    Response<Shipment> getShipmentPackageDimension(@Size(max=128) @PathVariable("shipment_tracking_id") final String shipmentTrackingId);

    @PutMapping("/get-by-tracking-id/{shipment_tracking_id}/package-dimension")
    @Operation(summary = "Update Shipment Package Dimension API", description = "Update a shipment’s package dimension based on shipment tracking id.", tags = "shipments")
    Response<PackageDimensionUpdateResponse> updateShipmentPackageDimension(
            @PathVariable("shipment_tracking_id") @Size(min = 1, max = 48) final String shipmentTrackingId,
            @Valid @RequestBody final Request<PackageDimensionUpdateRequest> request);

    @PatchMapping("/get-by-tracking-id/{shipment_tracking_id}/milestone-and-additional-info")
    @Operation(summary = "Update Shipment Milestone Status, Notes and Attachments API", description = "Update a shipment’s milestone status, notes, and attachments based on shipment tracking id.", tags = "shipments")
    Response<ShipmentMilestoneOpsUpdateResponse> updateShipmentMilestoneOpsUpdate(
            @PathVariable("shipment_tracking_id") @Size(min = 1, max = 48) final String shipmentTrackingId,
            @Valid @RequestBody final Request<ShipmentMilestoneOpsUpdateRequest> additionalDetailsRequest);
}
