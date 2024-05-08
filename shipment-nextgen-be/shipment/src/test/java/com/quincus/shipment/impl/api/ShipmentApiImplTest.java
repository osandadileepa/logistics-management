package com.quincus.shipment.impl.api;

import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.ShipmentJourneyContext;
import com.quincus.shipment.api.exception.ShipmentNotFoundException;
import com.quincus.shipment.api.filter.ExportFilter;
import com.quincus.shipment.api.filter.ShipmentExportFilterResult;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.impl.helper.UpdateShipmentHelper;
import com.quincus.shipment.impl.helper.journey.PackageJourneySegmentRefIdsAndSequenceEnricher;
import com.quincus.shipment.impl.service.PackageDimensionService;
import com.quincus.shipment.impl.service.ShipmentFetchService;
import com.quincus.shipment.impl.service.ShipmentJourneyService;
import com.quincus.shipment.impl.service.ShipmentListingCsvExportService;
import com.quincus.shipment.impl.service.ShipmentService;
import com.quincus.shipment.impl.validator.PackageJourneySegmentAlertGenerator;
import com.quincus.shipment.impl.validator.PackageJourneySegmentValidator;
import com.quincus.shipment.impl.validator.ShipmentValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentApiImplTest {
    @InjectMocks
    private ShipmentApiImpl shipmentApi;

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private ShipmentListingCsvExportService shipmentListingCsvExportService;

    @Mock
    private ShipmentValidator shipmentValidator;

    @Mock
    private ShipmentJourneyService shipmentJourneyService;

    @Mock
    private PackageDimensionService packageDimensionService;

    @Mock
    private PackageJourneySegmentAlertGenerator packageJourneySegmentAlertGenerator;
    @Mock
    private UpdateShipmentHelper updateShipmentHelper;
    @Mock
    private ShipmentFetchService shipmentFetchService;
    @Mock
    private PackageJourneySegmentRefIdsAndSequenceEnricher packageJourneySegmentRefIdsAndSequenceEnricher;

    @Mock
    private PackageJourneySegmentValidator packageJourneySegmentValidator;

    @Test
    void create_hasShipmentAPIImpl_shouldCallCreateOnce() {
        shipmentApi.create(new Shipment());
        verify(shipmentValidator, atMostOnce()).validateShipment(any(Shipment.class));
        verify(shipmentService, atMostOnce()).createShipmentJourneyEntity(any(Shipment.class), anyBoolean());
        verify(shipmentService, times(1)).createShipmentThenSendJourneyToOtherProducts(any(Shipment.class), any());
    }

    @Test
    void findAndCheckLocationPermission_hasShipmentAPIImpl_shouldCallFindByIdOnce() {
        shipmentApi.findAndCheckLocationPermission("id-to-find");
        verify(shipmentService, times(1)).findByIdAndCheckLocationPermission(any(String.class));
    }

    @Test
    void findByShipmentTrackingIdAndCheckLocationPermission_hasShipmentAPIImpl_shouldCallFindByShipmentTrackingIdAndCheckLocationPermissionOnce() {
        shipmentApi.findByShipmentTrackingIdAndCheckLocationPermission("id-to-find");
        verify(shipmentService, times(1)).findByShipmentTrackingIdAndCheckLocationPermission(any(String.class));
    }

    @Test
    void find_hasShipmentAPIImpl_shouldCallFindByIdOnce() {
        shipmentApi.find("id-to-find");
        verify(updateShipmentHelper, times(1)).getShipmentById(any(String.class));
    }

    @Test
    void update_hasShipmentAPIImpl_shouldCallUpdateOnce() {
        shipmentApi.update(new Shipment(), false);
        verify(shipmentService, times(1)).update(any(Shipment.class), anyBoolean());
    }

    @Test
    void delete_hasShipmentAPIImpl_shouldCallDeleteOnce() {
        shipmentApi.delete("id-to-delete");
        verify(shipmentService, times(1)).deleteById(any(String.class));
    }

    @Test
    void cancel_hasShipmentAPIImpl_shouldCallCancelOnce() {
        shipmentApi.cancel("id-to-cancel", TriggeredFrom.SHP);
        verify(shipmentService, times(1)).cancelById(anyString(), any(TriggeredFrom.class));
    }

    @Test
    void createBulk_hasShipmentAPIImpl_shouldCallCreateBulkOnce() {
        Shipment shipment = new Shipment();
        shipment.setShipmentPackage(new Package());
        List<Shipment> shipments = List.of(shipment);

        shipmentApi.createBulk(shipments);
        assertThat(shipments.get(0).getShipmentPackage().getSource()).isEqualTo(TriggeredFrom.SHP);
        verify(shipmentService, times(1)).createBulk(anyList());
    }

    @Test
    void findAll_hasShipmentAPIImpl_shouldCallFindAllOnce() {
        shipmentApi.findAll(new ShipmentFilter());
        verify(shipmentService, times(1)).findAll(any(ShipmentFilter.class));
    }

    @Test
    void export_hasShipmentAPIImpl_shouldCallExportOnce() {
        Shipment shipment = new Shipment();
        var exportFilterResult = new ShipmentExportFilterResult(List.of(shipment)).filter(new ExportFilter());
        when(shipmentListingCsvExportService.export(any(ExportFilter.class)))
                .thenReturn(exportFilterResult);
        try {
            shipmentApi.export(new ExportFilter(), new PrintWriter(new StringWriter()));
            verify(shipmentListingCsvExportService, times(1)).writeShipmentToCsv(anyList(), any(Writer.class));
        } catch (IOException e) {
            fail("Exception occurred in normal path testing: " + e.getMessage());
        }
    }

    @Test
    void export_serviceThrowsException_shouldThrowException() throws IOException {
        Shipment shipment = new Shipment();
        var exportFilterResult = new ShipmentExportFilterResult(List.of(shipment)).filter(new ExportFilter());
        when(shipmentListingCsvExportService.export(any(ExportFilter.class)))
                .thenReturn(exportFilterResult);

        doThrow(new IOException("Not an exception. Test only."))
                .when(shipmentListingCsvExportService).writeShipmentToCsv(anyList(), any(Writer.class));

        assertThatThrownBy(() -> shipmentApi.export(new ExportFilter(), new PrintWriter(new StringWriter())))
                .isInstanceOf(IOException.class);

        verify(shipmentListingCsvExportService, times(1)).writeShipmentToCsv(anyList(), any(Writer.class));
    }

    @Test
    void updateShipmentJourney_hasShipmentAPIImpl_shouldCallUpdateShipmentJourneyOnce() {
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        shipmentJourney.setShipmentId("shipmentId");
        shipmentJourney.setJourneyId("journeyId");
        shipmentJourney.setOrderId("orderId");

        ShipmentJourneyContext shipmentJourneyContext = new ShipmentJourneyContext();
        shipmentJourneyContext.updatedShipmentJourney(new ShipmentJourney());
        shipmentJourneyContext.previousShipmentJourney(new ShipmentJourney());
        shipmentJourneyContext.shipmentIds(List.of());
        shipmentJourneyContext.shipmentTrackingIds(List.of());

        when(shipmentJourneyService.updateShipmentJourney(any())).thenReturn(shipmentJourneyContext);

        shipmentApi.updateShipmentJourney(shipmentJourney);

        verify(packageJourneySegmentRefIdsAndSequenceEnricher, times(1)).enrichSegmentsWithTypesRefIdsAndSequence(anyList());
        verify(packageJourneySegmentValidator, times(1)).validatePackageJourneySegments(any(ShipmentJourney.class));
        verify(packageJourneySegmentAlertGenerator, times(1)).generateAlertPackageJourneySegments(any(), anyBoolean());
        verify(shipmentJourneyService, times(1)).sendShipmentJourneyUpdates(any());
    }

    @Test
    @DisplayName("GIVEN existing shipment tracking id and organization id WHEN findShipmentPackageInfoByShipmentTrackingIdAndOrganizationId THEN return package info")
    void shouldGetPackageInfo() {
        String shipmentTrackingId = UUID.randomUUID().toString();
        Shipment shipment = mock(Shipment.class);
        when(packageDimensionService.findShipmentPackageInfoByShipmentTrackingIdAndOrganizationId(anyString())).thenReturn(shipment);

        shipmentApi.findShipmentPackageInfoByShipmentTrackingId(shipmentTrackingId);

        verify(packageDimensionService, times(1)).findShipmentPackageInfoByShipmentTrackingIdAndOrganizationId(anyString());
    }

    @Test
    @DisplayName("GIVEN in-existing shipment tracking id and organization id WHEN getShipmentPackageInfo THEN return package info")
    void shouldThrowShipmentNotFoundExceptionWhenGetPackageInfo() {
        String shipmentTrackingId = UUID.randomUUID().toString();

        doThrow(new ShipmentNotFoundException("Not an exception. Test only.")).when(packageDimensionService).findShipmentPackageInfoByShipmentTrackingIdAndOrganizationId(anyString());

        assertThatThrownBy(() -> shipmentApi.findShipmentPackageInfoByShipmentTrackingId(shipmentTrackingId))
                .isInstanceOf(ShipmentNotFoundException.class);
        verify(packageDimensionService, times(1)).findShipmentPackageInfoByShipmentTrackingIdAndOrganizationId(anyString());
    }

    @Test
    void findAllByOrderIdUsingTupleShouldHandleEmptyReturn() {
        Order order = new Order();
        order.setId("testId");
        when(shipmentFetchService.findAllByOrderIdUsingTuple(order.getId())).thenReturn(Collections.emptyList());
        List<Shipment> result = shipmentApi.findAllRelatedFromOrder(order);
        assertThat(result).isNotNull().isEmpty();
    }
}