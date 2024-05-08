package com.quincus.shipment.impl.web;

import com.quincus.qlogger.api.QLoggerAPI;
import com.quincus.shipment.api.ShipmentApi;
import com.quincus.shipment.api.constant.EtaStatus;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.domain.ShipmentResult;
import com.quincus.shipment.api.dto.ShipmentJourneyUpdateResponse;
import com.quincus.shipment.api.exception.ShipmentNotFoundException;
import com.quincus.shipment.api.filter.ExportFilter;
import com.quincus.shipment.api.filter.ShipmentFilter;
import com.quincus.shipment.api.filter.ShipmentFilterResult;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.web.common.model.Request;
import com.quincus.web.common.model.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentControllerImplTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    private ShipmentControllerImpl shipmentControllerImpl;
    @Mock
    private ShipmentApi shipmentApi;

    @Mock
    private QLoggerAPI qLoggerAPI;

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Test
    void add_ValidData_ShouldReturnSuccess() {
        Shipment domain = testUtil.createSingleShipmentData();
        Request<Shipment> request = new Request<>();
        request.setData(domain);
        when(shipmentApi.create(any(Shipment.class))).thenReturn(domain);
        Response<Shipment> response = shipmentControllerImpl.add(request);

        assertThat(response.getData()).isEqualTo(domain);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void addBulk_ValidData_ShouldReturnSuccess() {
        Request<List<Shipment>> request = new Request<>();
        List<Shipment> shipments = List.of(testUtil.createBulkShipmentData());
        request.setData(shipments);

        List<ShipmentResult> shipmentResults = new ArrayList<>();
        Shipment shipmentRs1 = shipments.get(0);
        shipmentRs1.setId("shipment-1");
        Shipment shipmentRs2 = shipments.get(1);
        shipmentRs1.setId("shipment-2");

        shipmentResults.add(new ShipmentResult(shipmentRs1, true));
        shipmentResults.add(new ShipmentResult(shipmentRs2, true));
        when(shipmentApi.createBulk(shipments)).thenReturn(shipmentResults);

        Response<List<ShipmentResult>> response = shipmentControllerImpl.addBulk(request);

        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0).getShipment()).isNotNull();
        assertThat(response.getData().get(1).getShipment()).isNotNull();
        assertThat(response.getData().get(0).isSuccess()).isTrue();
        assertThat(response.getData().get(1).isSuccess()).isTrue();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void update_ValidData_ShouldReturnSuccess() {
        Request<Shipment> request = new Request<>();
        Shipment domain = testUtil.createSingleShipmentData();
        request.setData(domain);
        when(shipmentApi.update(any(Shipment.class), anyBoolean())).thenReturn(domain);
        Response<Shipment> response = shipmentControllerImpl.update(request);
        assertThat(response.getData()).isEqualTo(request.getData());
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void cancel_ValidData_ShouldReturnSuccess() throws ShipmentNotFoundException {
        String id = "thisis-shipmentid";
        Response<Shipment> response = shipmentControllerImpl.cancel(id);
        assertThat(response.getStatus()).isEqualTo("Shipment Cancelled.");
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void find_shipmentFound_shouldReturnShipment() {
        String id = "thisis-shipmentid";
        var shipmentResponse = new Shipment();
        when(shipmentApi.findAndCheckLocationPermission(id)).thenReturn(shipmentResponse);

        Response<Shipment> response = shipmentControllerImpl.findByShipmentId(id);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isEqualTo(shipmentResponse);
    }

    @Test
    void findByShipmentTrackingId_shipmentFound_shouldReturnShipment() {
        String shipmentTrackingId = "shipmentTrackingId";
        var shipmentResponse = new Shipment();
        when(shipmentApi.findByShipmentTrackingIdAndCheckLocationPermission(shipmentTrackingId)).thenReturn(shipmentResponse);

        Response<Shipment> response = shipmentControllerImpl.findByShipmentTrackingId(shipmentTrackingId);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isEqualTo(shipmentResponse);
    }

    @Test
    void findAll_ValidData_ShouldReturnSuccess() {
        Request<ShipmentFilter> request = new Request<>();
        ShipmentFilter data = new ShipmentFilter();
        data.setOrganization(new Organization());
        data.setEtaStatus(new EtaStatus[]{EtaStatus.ON_TIME});
        data.setSize(1);
        data.setPageNumber(0);
        request.setData(data);

        long elements = 2L;
        int pages = 1;
        List<Shipment> shipments = List.of(testUtil.createBulkShipmentData());

        ShipmentFilterResult result = new ShipmentFilterResult(shipments).totalElements(elements).totalPages(pages);
        when(shipmentApi.findAll(data)).thenReturn(result);

        Response<ShipmentFilterResult> response = shipmentControllerImpl.findAll(request);
        assertThat(response.getData().totalElements()).isEqualTo(elements);
        assertThat(response.getData().totalPages()).isEqualTo(pages);
        assertThat(response.getData().getResult()).hasSize(2);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void exportToCsv_validData_shouldExecute() {
        Request<ExportFilter> request = new Request<>();
        ExportFilter data = new ExportFilter();
        data.setEtaStatus(new EtaStatus[]{EtaStatus.ON_TIME});
        request.setData(data);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        try {
            when(httpServletResponse.getWriter()).thenReturn(writer);
            when(httpServletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
            when(userDetailsProvider.getCurrentOrganization()).thenReturn(new Organization());
            shipmentControllerImpl.exportToCsv(request, httpServletResponse);

            verify(shipmentApi, times(1)).export(any(ExportFilter.class), any(PrintWriter.class));
        } catch (IOException e) {
            fail("Exception occurred in normal path testing: " + e.getMessage());
        }
    }

    @Test
    void exportToCsv_getWriterThrowsException_shouldThrowException() throws IOException {
        Request<ExportFilter> request = new Request<>();
        ExportFilter data = new ExportFilter();
        data.setOrganization(new Organization());
        data.setEtaStatus(new EtaStatus[]{EtaStatus.ON_TIME});
        request.setData(data);

        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        doThrow(new IOException("Not an exception. Test only."))
                .when(httpServletResponse).getWriter();

        assertThatThrownBy(() -> shipmentControllerImpl.exportToCsv(request, httpServletResponse))
                .isInstanceOf(IOException.class);

        verify(shipmentApi, never()).export(any(ExportFilter.class), any(PrintWriter.class));
    }

    @Test
    void exportToCsv_exportToCsvThrowsException_shouldThrowException() throws IOException {
        Request<ExportFilter> request = new Request<>();
        ExportFilter data = new ExportFilter();
        data.setOrganization(new Organization());
        data.setEtaStatus(new EtaStatus[]{EtaStatus.ON_TIME});
        request.setData(data);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletResponse.getWriter()).thenReturn(writer);

        doThrow(new IOException("Not an exception. Test only.")).when(shipmentApi).export(data, writer);

        assertThatThrownBy(() -> shipmentControllerImpl.exportToCsv(request, httpServletResponse))
                .isInstanceOf(IOException.class);

        verify(shipmentApi, times(1)).export(any(ExportFilter.class), any(PrintWriter.class));
    }

    @Test
    void getShipmentJourney_validData_shouldNotThrowException() {
        String shipmentId = "SHP-ID";
        Shipment shipment = mock(Shipment.class);
        when(shipmentApi.findAndCheckLocationPermission(shipmentId)).thenReturn(shipment);
        when(shipment.getShipmentJourney()).thenReturn(any(ShipmentJourney.class));

        Response<ShipmentJourney> response = shipmentControllerImpl.getShipmentJourney(shipmentId);

        assertThat(response).isNotNull();

        verify(shipmentApi, times(1)).findAndCheckLocationPermission(anyString());
    }

    @Test
    void updateShipmentJourney_validData_shouldNotThrowException() {
        Request<ShipmentJourney> request = new Request<>();
        ShipmentJourney data = mock(ShipmentJourney.class);
        request.setData(data);

        when(shipmentApi.updateShipmentJourney(data))
                .thenReturn(new ShipmentJourneyUpdateResponse().shipmentJourney(data));

        Response<ShipmentJourneyUpdateResponse> response = shipmentControllerImpl.updateShipmentJourney(request);

        assertThat(response).isNotNull();

        verify(shipmentApi, times(1)).updateShipmentJourney(any(ShipmentJourney.class));
    }

    @Test
    @DisplayName("GIVEN existing shipment tracking id and organization id WHEN getShipmentPackageInfo THEN return package info")
    void shouldGetPackageInfo() {
        String shipmentTrackingId = UUID.randomUUID().toString();
        Shipment shipment = mock(Shipment.class);

        when(shipmentApi.findShipmentPackageInfoByShipmentTrackingId(anyString())).thenReturn(shipment);
        Response<Shipment> response = shipmentControllerImpl.getShipmentPackageDimension(shipmentTrackingId);

        assertThat(response).isNotNull();
        verify(shipmentApi, times(1)).findShipmentPackageInfoByShipmentTrackingId(anyString());
    }

    @Test
    @DisplayName("GIVEN in-existing shipment tracking id and organization id WHEN getShipmentPackageInfo THEN return package info")
    void shouldThrowShipmentNotFoundExceptionWhenGetPackageInfo() {
        String shipmentTrackingId = UUID.randomUUID().toString();
        doThrow(new ShipmentNotFoundException("Not an exception. Test only.")).when(shipmentApi).findShipmentPackageInfoByShipmentTrackingId(anyString());
        assertThatThrownBy(() -> shipmentControllerImpl.getShipmentPackageDimension(shipmentTrackingId))
                .isInstanceOf(ShipmentNotFoundException.class);
        verify(shipmentApi, times(1)).findShipmentPackageInfoByShipmentTrackingId(anyString());
    }
}
