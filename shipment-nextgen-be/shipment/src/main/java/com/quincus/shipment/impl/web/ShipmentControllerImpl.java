package com.quincus.shipment.impl.web;

import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.ShipmentController;
import com.quincus.shipment.api.ShipmentApi;
import com.quincus.shipment.api.constant.TriggeredFrom;
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
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@RestController
public class ShipmentControllerImpl implements ShipmentController {

    private ShipmentApi shipmentApi;

    private QLoggerAPI qLoggerAPI;

    private UserDetailsProvider userDetailsProvider;

    @Override
    @PreAuthorize("hasAuthority('KARATE_USER')")
    @LogExecutionTime
    public Response<Shipment> add(final Request<Shipment> request) {
        final Shipment shipment = request.getData();
        shipment.getShipmentPackage().setSource(TriggeredFrom.SHP);
        return new Response<>(shipmentApi.create(shipment));
    }

    @Override
    @PreAuthorize("hasAuthority('SHIPMENTS_VIEW')")
    @LogExecutionTime
    public Response<Shipment> findByShipmentId(final String id) {
        return new Response<>(shipmentApi.findAndCheckLocationPermission(id));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_VIEW','S2S')")
    @LogExecutionTime
    public Response<Shipment> findByShipmentTrackingId(String shipmentTrackingId) {
        return new Response<>(shipmentApi.findByShipmentTrackingIdAndCheckLocationPermission(shipmentTrackingId));
    }

    @Override
    @PreAuthorize("hasAuthority('SHIPMENTS_EDIT')")
    @LogExecutionTime
    public Response<Shipment> update(final Request<Shipment> request) {
        final Shipment shipment = request.getData();
        shipment.getShipmentPackage().setSource(TriggeredFrom.SHP);
        return new Response<>(shipmentApi.update(shipment, false));
    }

    @Override
    @PreAuthorize("hasAuthority('KARATE_USER')")
    @LogExecutionTime
    public Response<List<ShipmentResult>> addBulk(final Request<List<Shipment>> request) {
        return new Response<>(shipmentApi.createBulk(request.getData()));
    }

    @Override
    @PreAuthorize("hasAuthority('SHIPMENTS_EDIT')")
    @LogExecutionTime
    public Response<Shipment> cancel(final String id) {
        Response<Shipment> response = new Response<>(shipmentApi.cancel(id, TriggeredFrom.SHP));
        response.setStatus("Shipment Cancelled.");
        return response;
    }

    @Override
    @PreAuthorize("hasAuthority('SHIPMENTS_VIEW')")
    @LogExecutionTime
    public Response<ShipmentFilterResult> findAll(final Request<ShipmentFilter> request) {
        return new Response<>(shipmentApi.findAll(request.getData()));
    }

    @Override
    @PreAuthorize("hasAuthority('SHIPMENTS_EXPORT')")
    @LogExecutionTime
    public void exportToCsv(final Request<ExportFilter> request, final HttpServletResponse servletResponse) throws IOException {
        servletResponse.setContentType("text/csv");
        servletResponse.addHeader("Content-Disposition", "attachment; filename=\"shipments.csv\"");
        shipmentApi.export(request.getData(), servletResponse.getWriter());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.writeTo(servletResponse.getOutputStream());
        qLoggerAPI.publishShipmentExportedEvent("ShipmentController#exportToCsv", userDetailsProvider.getCurrentOrganization(), Arrays.toString(byteArrayOutputStream.toByteArray()));
    }

    @Override
    @PreAuthorize("hasAuthority('SHIPMENTS_VIEW')")
    @LogExecutionTime
    public Response<ShipmentJourney> getShipmentJourney(final String shipmentId) {
        return new Response<>(shipmentApi.findAndCheckLocationPermission(shipmentId).getShipmentJourney());
    }

    @Override
    @PreAuthorize("hasAuthority('SHIPMENTS_EDIT')")
    @LogExecutionTime
    public Response<ShipmentJourneyUpdateResponse> updateShipmentJourney(final Request<ShipmentJourney> request) {
        final ShipmentJourney shipmentJourney = request.getData();
        return new Response<>(shipmentApi.updateShipmentJourney(shipmentJourney));
    }

    @Override
    @PreAuthorize("hasAuthority('DIMS_AND_WEIGHT_VIEW')")
    @LogExecutionTime
    public Response<Shipment> getShipmentPackageDimension(final String shipmentTrackingId) {
        return new Response<>(shipmentApi.findShipmentPackageInfoByShipmentTrackingId(shipmentTrackingId));
    }

    @Override
    @PreAuthorize("hasAuthority('DIMS_AND_WEIGHT_EDIT')")
    @LogExecutionTime
    public Response<PackageDimensionUpdateResponse> updateShipmentPackageDimension(final String shipmentTrackingId,
                                                                                   final Request<PackageDimensionUpdateRequest> request) {
        request.getData().setShipmentTrackingId(shipmentTrackingId);
        request.getData().setSource(TriggeredFrom.SHP);
        return new Response<>(shipmentApi.updateShipmentPackageDimension(request.getData()));
    }

    @Override
    @PreAuthorize("hasAuthority('SHIPMENT_STATUS_EDIT')")
    @LogExecutionTime
    public Response<ShipmentMilestoneOpsUpdateResponse> updateShipmentMilestoneOpsUpdate(final String shipmentTrackingId,
                                                                                         final Request<ShipmentMilestoneOpsUpdateRequest> milestoneOpsUpdateRequestRequest) {
        milestoneOpsUpdateRequestRequest.getData().setShipmentTrackingId(shipmentTrackingId);
        return new Response<>(shipmentApi.updateShipmentMilestoneOpsUpdate(milestoneOpsUpdateRequestRequest.getData()));
    }
}