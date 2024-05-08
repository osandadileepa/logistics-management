package com.quincus.shipment.impl.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.ext.annotation.Utility;
import com.quincus.shipment.api.ShipmentApi;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.domain.ShipmentResult;
import com.quincus.shipment.api.dto.PackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.PackageDimensionUpdateResponse;
import com.quincus.shipment.api.dto.ShipmentJourneyContext;
import com.quincus.shipment.api.dto.ShipmentJourneyUpdateResponse;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateResponse;
import com.quincus.shipment.api.exception.ShipmentJourneyException;
import com.quincus.shipment.api.filter.ExportFilter;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.api.filter.ShipmentFilterResult;
import com.quincus.shipment.impl.converter.ShipmentOrderMessageConverter;
import com.quincus.shipment.impl.helper.UpdateShipmentHelper;
import com.quincus.shipment.impl.helper.journey.PackageJourneySegmentRefIdsAndSequenceEnricher;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.service.PackageDimensionService;
import com.quincus.shipment.impl.service.ShipmentAsyncService;
import com.quincus.shipment.impl.service.ShipmentFetchService;
import com.quincus.shipment.impl.service.ShipmentJourneyService;
import com.quincus.shipment.impl.service.ShipmentListingCsvExportService;
import com.quincus.shipment.impl.service.ShipmentService;
import com.quincus.shipment.impl.validator.PackageJourneySegmentAlertGenerator;
import com.quincus.shipment.impl.validator.PackageJourneySegmentValidator;
import com.quincus.shipment.impl.validator.ShipmentValidator;
import com.quincus.web.common.utility.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Service
@AllArgsConstructor
public class ShipmentApiImpl implements ShipmentApi {
    private static final String INVALID_UPDATE_SHIPMENT_JOURNEY_REQUEST = "Invalid update ShipmentJourney request: journeyId, shipmentId, orderId must not be null or empty.";
    private final ShipmentService shipmentService;
    private final ShipmentAsyncService shipmentAsyncService;
    private final ShipmentListingCsvExportService shipmentListingCsvExportService;
    private final ShipmentValidator shipmentValidator;
    private final ShipmentOrderMessageConverter shipmentOrderMessageConverter;
    private final ShipmentJourneyService shipmentJourneyService;
    private final PackageDimensionService shipmentPackageService;
    private final PackageJourneySegmentAlertGenerator packageJourneySegmentAlertGenerator;
    private final ObjectMapper objectMapper;
    private final UpdateShipmentHelper updateShipmentHelper;
    private final PackageJourneySegmentRefIdsAndSequenceEnricher packageJourneySegmentRefIdsAndSequenceEnricher;
    private final ShipmentFetchService shipmentFetchService;
    private final PackageJourneySegmentValidator packageJourneySegmentValidator;

    @Override
    public List<Shipment> createOrUpdate(List<Shipment> shipments, boolean segmentsUpdated) {
        return shipmentService.createOrUpdate(shipments, segmentsUpdated);
    }

    @Override
    public Shipment create(Shipment shipment) {
        shipmentValidator.validateShipment(shipment);
        ShipmentJourneyEntity shipmentJourneyEntity = shipmentService.createShipmentJourneyEntity(shipment, false);
        return shipmentService.createShipmentThenSendJourneyToOtherProducts(shipment, shipmentJourneyEntity);
    }

    @Override
    public Shipment update(Shipment shipment, boolean segmentsUpdated) {
        shipmentValidator.validateShipment(shipment);
        return shipmentService.update(shipment, segmentsUpdated);
    }

    @Override
    public Shipment findAndCheckLocationPermission(String id) {
        return shipmentService.findByIdAndCheckLocationPermission(id);
    }

    @Override
    public Shipment findByShipmentTrackingIdAndCheckLocationPermission(String shipmentTrackingId) {
        return shipmentService.findByShipmentTrackingIdAndCheckLocationPermission(shipmentTrackingId);
    }

    @Override
    public Shipment find(String id) {
        return updateShipmentHelper.getShipmentById(id);
    }

    @Override
    public void asyncCreateOrUpdate(List<Shipment> shipments, boolean segmentsUpdated) {
        shipments.forEach(shipmentValidator::validateShipment);
        shipmentAsyncService.processAndDispatchShipments(shipments, segmentsUpdated);
    }

    @Utility
    @Override
    public Shipment createOrUpdateLocal(Shipment shipment, boolean segmentsUpdated) {
        shipmentValidator.validateShipment(shipment);
        return shipmentService.createOrUpdateLocal(shipment, segmentsUpdated);
    }

    @Override
    public void delete(String id) {
        shipmentService.deleteById(id);
    }

    @Override
    public Shipment cancel(String id, TriggeredFrom triggeredFrom) {
        return shipmentService.cancelById(id, triggeredFrom);
    }

    @Override
    public List<ShipmentResult> createBulk(List<Shipment> shipments) {
        shipments.forEach(shipment -> {
            shipmentValidator.validateShipment(shipment);
            shipment.getShipmentPackage().setSource(TriggeredFrom.SHP);
        });
        return shipmentService.createBulk(shipments);
    }

    @Override
    public ShipmentFilterResult findAll(ShipmentFilter filter) {
        return shipmentService.findAll(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipment> findAllRelatedFromOrder(Order order) {
        return shipmentFetchService.findAllByOrderIdUsingTuple(order.getId()).stream()
                .map(shipmentEntity -> ShipmentMapper.mapEntityToDomain(shipmentEntity, objectMapper))
                .toList();
    }

    @Override
    public void export(ExportFilter filter, PrintWriter writer) throws IOException {
        shipmentListingCsvExportService.writeShipmentToCsv(shipmentListingCsvExportService.export(filter).getResult(), writer);
    }

    @Override
    public List<Shipment> convertOrderMessageToShipments(String omPayload, String transactionId) {
        return shipmentOrderMessageConverter.convertOrderMessageToShipments(omPayload, transactionId);
    }

    @Override
    @LogExecutionTime
    public ShipmentJourneyUpdateResponse updateShipmentJourney(ShipmentJourney shipmentJourney) {

        if (shipmentJourney == null || StringUtils.isBlank(shipmentJourney.getShipmentId())
                || StringUtils.isBlank(shipmentJourney.getJourneyId()) || StringUtils.isBlank(shipmentJourney.getOrderId())) {
            throw new ShipmentJourneyException(INVALID_UPDATE_SHIPMENT_JOURNEY_REQUEST);
        }
        packageJourneySegmentRefIdsAndSequenceEnricher.enrichSegmentsWithTypesRefIdsAndSequence(shipmentJourney.getUnsortedPackageJourneySegments());
        packageJourneySegmentValidator.validatePackageJourneySegments(shipmentJourney);
        packageJourneySegmentAlertGenerator.generateAlertPackageJourneySegments(shipmentJourney, true);

        ShipmentJourneyContext shipmentJourneyContext = shipmentJourneyService.updateShipmentJourney(shipmentJourney);

        shipmentJourneyService.sendShipmentJourneyUpdates(shipmentJourneyContext);
        return new ShipmentJourneyUpdateResponse()
                .shipmentJourney(shipmentJourneyContext.updatedShipmentJourney())
                .updatedShipmentTrackingIds(shipmentJourneyContext.shipmentTrackingIds())
                .totalShipmentsUpdated(shipmentJourneyContext.shipmentTrackingIds().size());
    }

    @Override
    public Milestone receiveMilestoneMessageFromDispatch(String dspPayload, String uuid) {
        return shipmentService.receiveMilestoneMessageFromDispatch(dspPayload, uuid);
    }

    @Override
    public boolean isRetryableToDispatch(String shipmentId, String uuid) {
        return shipmentService.isRetryableToDispatch(shipmentId);
    }

    @Override
    public Shipment findShipmentPackageInfoByShipmentTrackingId(String shipmentTrackingId) {
        return shipmentPackageService.findShipmentPackageInfoByShipmentTrackingIdAndOrganizationId(shipmentTrackingId);
    }

    @Override
    public PackageDimensionUpdateResponse updateShipmentPackageDimension(PackageDimensionUpdateRequest updatePackageDimensionRequest) {
        return shipmentPackageService.updateShipmentPackageDimension(updatePackageDimensionRequest);
    }

    @Override
    public ShipmentMilestoneOpsUpdateResponse updateShipmentMilestoneOpsUpdate(ShipmentMilestoneOpsUpdateRequest milestoneOpsUpdateRequest) {
        return shipmentService.updateShipmentFromOpsUpdate(milestoneOpsUpdateRequest);
    }
}
