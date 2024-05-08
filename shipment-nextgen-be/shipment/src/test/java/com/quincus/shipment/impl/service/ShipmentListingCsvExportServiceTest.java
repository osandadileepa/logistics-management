package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.test_utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static com.quincus.shipment.impl.service.ShipmentListingCsvExportService.CSV_HEADERS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentListingCsvExportServiceTest {

    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    private ShipmentListingCsvExportService shipmentListingCsvExportService;

    @Test
    void writeShipmentToCsv_printerAvailable_shouldWriteData() {
        Shipment shipmentDomain = testUtil.createSingleShipmentData();
        fillShipmentAirSegmentFields(shipmentDomain.getShipmentJourney());

        PrintWriter writer = mock(PrintWriter.class);
        try {
            shipmentListingCsvExportService.writeShipmentToCsv(List.of(shipmentDomain), writer);
        } catch (IOException e) {
            fail("Exception occurred in normal path testing: " + e.getMessage());
        }

        int allItemsCount = CSV_HEADERS.length * 2;
        verify(writer, times(allItemsCount)).append(anyString(), anyInt(), anyInt());
    }

    @Test
    void writeShipmentToCsv_multipleSegments_shouldWriteMultipleRows() {
        List<Shipment> shipmentNSegments = testUtil.createShipmentsFromOrder("samplepayload/ordermodule-orders-1-package-n-segments.json");
        Shipment refShipment = shipmentNSegments.get(0);

        ShipmentJourney refJourney = refShipment.getShipmentJourney();
        int segmentsCount = refJourney.getPackageJourneySegments().size();

        fillShipmentAirSegmentFields(refJourney);


        PrintWriter writer = mock(PrintWriter.class);
        try {
            shipmentListingCsvExportService.writeShipmentToCsv(List.of(refShipment), writer);
        } catch (IOException e) {
            fail("Exception occurred in normal path testing: " + e.getMessage());
        }

        int expectedBlanks = 12;
        int allItemsCount = CSV_HEADERS.length * (segmentsCount + 1) - expectedBlanks;
        verify(writer, times(allItemsCount)).append(anyString(), anyInt(), anyInt());
    }

    @Test
    void writeShipmentToCsv_throwsException_shouldThrowException() {
        Shipment shipmentDomain = testUtil.createSingleShipmentData();
        PrintWriter writer = mock(PrintWriter.class);

        when(writer.append(anyString(), anyInt(), anyInt())).thenThrow(new IOException("Not an exception. Test only."));

        assertThatThrownBy(() -> shipmentListingCsvExportService.writeShipmentToCsv(List.of(shipmentDomain), writer))
                .isInstanceOf(IOException.class);
    }

    private void fillShipmentAirSegmentFields(@NonNull ShipmentJourney journey) {
        fillShipmentAirSegmentFields(journey.getPackageJourneySegments());
    }

    private void fillShipmentAirSegmentFields(@NonNull List<PackageJourneySegment> segments) {
        for (PackageJourneySegment segment : segments) {
            segment.setFlightNumber("123");
            segment.setAirline("Dummy Airline");
            segment.setAirlineCode("DD");
            segment.setLockOutTime("2023-05-25 17:27:02 +0800");
            segment.setDepartureTime("2023-05-25 17:27:02 +0800");
            segment.setArrivalTime("2023-05-25 17:27:02 +0800");
            segment.setRecoveryTime("2023-05-25 17:27:02 +0800");

            segment.setInstruction("Discovery 1");
        }
    }

}
