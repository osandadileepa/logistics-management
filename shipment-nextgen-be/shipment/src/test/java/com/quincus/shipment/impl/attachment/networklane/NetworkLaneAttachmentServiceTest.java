package com.quincus.shipment.impl.attachment.networklane;

import com.quincus.shipment.api.dto.csv.NetworkLaneCsv;
import com.quincus.shipment.api.dto.csv.NetworkLaneSegmentCsv;
import com.quincus.shipment.impl.mapper.JobMetricsMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.JobMetricsService;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkLaneAttachmentServiceTest {

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Mock
    private JobMetricsService<NetworkLaneCsv> jobMetricsService;

    @Mock
    private JobMetricsMapper<NetworkLaneCsv> jobMetricsMapper;

    @InjectMocks
    private NetworkLaneAttachmentService networkLaneAttachmentService;

    @Test
    void givenBlankCsvWhenParseToDomainThenThrowsQuincusValidationException() throws IOException {
        // GIVEN:
        String csvData = "";
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(csvData.getBytes()));

        // WHEN:
        assertThatThrownBy(() -> networkLaneAttachmentService.parseToDomain(mockMultipartFile))
                .hasMessage("The CSV file does not contain any valid records.")
                .isInstanceOf(QuincusValidationException.class);
    }

    @Test
    void givenValidCsvMultipartWhenParseToDomainThenCorrectlyMapToNetworkLaneCsvData() throws IOException {
        // GIVEN:
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.getInputStream()).thenReturn(getMockInputStream());
        when(userDetailsProvider.getCurrentOrganizationId()).thenReturn("mockOrganizationId");

        // WHEN:
        List<NetworkLaneCsv> networkLanes = networkLaneAttachmentService.parseToDomain(mockMultipartFile);

        // THEN:
        assertThat(networkLanes).hasSize(1);
        NetworkLaneCsv networkLane = networkLanes.get(0);
        assertThat(networkLane.getLaneId()).isBlank();
        assertThat(networkLane.getServiceType()).contains("Express");
        assertThat(networkLane.getOriginLocationTreeLevel1()).contains("AUSTRALIA");
        assertThat(networkLane.getOriginLocationTreeLevel2()).contains("NEW SOUTH WALES");
        assertThat(networkLane.getOriginLocationTreeLevel3()).contains("SYDNEY");
        assertThat(networkLane.getOriginFacilityId()).isBlank();
        assertThat(networkLane.getDestinationLocationTreeLevel1()).contains("PHILIPPINES");
        assertThat(networkLane.getDestinationLocationTreeLevel2()).contains("METRO MANILA");
        assertThat(networkLane.getDestinationLocationTreeLevel3()).contains("PASAY");
        assertThat(networkLane.getDestinationLocationTreeLevel4()).contains("NINOY AQUINO INTERNATIONAL AIRPORT");

        assertThat(networkLane.getNetworkLaneSegments()).hasSize(3);
        assertSegmentLane1(networkLane);
        assertSegmentLane2(networkLane);
        assertSegmentLane3(networkLane);
    }

    private void assertSegmentLane1(NetworkLaneCsv networkLane) {
        NetworkLaneSegmentCsv networkLaneSegment1 = networkLane.getNetworkLaneSegments().get(0);
        assertThat(networkLaneSegment1.getSequenceNumber()).contains("1");
        assertThat(networkLaneSegment1.getTransportCategory()).contains("GROUND");
        assertThat(networkLaneSegment1.getPartnerName()).contains("Partner Airline");
        assertThat(networkLaneSegment1.getVehicleInfo()).contains("VT1324");
        assertThat(networkLaneSegment1.getFlightNumber()).isBlank();
        assertThat(networkLaneSegment1.getAirline()).isBlank();
        assertThat(networkLaneSegment1.getAirlineCode()).isBlank();
        assertThat(networkLaneSegment1.getMasterWaybill()).contains("123-12345675");
        assertThat(networkLaneSegment1.getPickupFacilityName()).contains("AUSTRALIA_NEWSOUTHWALES_SYDNEY_FAC1");
        assertThat(networkLaneSegment1.getDropOffFacilityName()).contains("SG FACILITY 1");
        assertThat(networkLaneSegment1.getPickupInstruction()).contains("pickup instruction test 1");
        assertThat(networkLaneSegment1.getDropOffInstruction()).contains("drop off instruction test 1");
        assertThat(networkLaneSegment1.getDuration()).contains("0");
        assertThat(networkLaneSegment1.getDurationUnit()).contains("MINUTE");
        assertThat(networkLaneSegment1.getLockOutTime()).isBlank();
        assertThat(networkLaneSegment1.getDepartureTime()).isBlank();
        assertThat(networkLaneSegment1.getArrivalTime()).isBlank();
        assertThat(networkLaneSegment1.getRecoveryTime()).isBlank();
        assertThat(networkLaneSegment1.getCalculatedMileage()).contains("0");
        assertThat(networkLaneSegment1.getCalculatedMileageUnit()).contains("MILE");
    }

    private void assertSegmentLane2(NetworkLaneCsv networkLane) {
        NetworkLaneSegmentCsv networkLaneSegment2 = networkLane.getNetworkLaneSegments().get(1);
        assertThat(networkLaneSegment2.getSequenceNumber()).contains("2");
        assertThat(networkLaneSegment2.getTransportCategory()).contains("AIR");
        assertThat(networkLaneSegment2.getPartnerName()).contains("Partner Airport");
        assertThat(networkLaneSegment2.getVehicleInfo()).isBlank();
        assertThat(networkLaneSegment2.getFlightNumber()).contains("AR1324");
        assertThat(networkLaneSegment2.getAirline()).contains("Singapore Airlines");
        assertThat(networkLaneSegment2.getAirlineCode()).contains("SQ");
        assertThat(networkLaneSegment2.getMasterWaybill()).contains("123-12345675");
        assertThat(networkLaneSegment2.getPickupFacilityName()).contains("SG FACILITY 1");
        assertThat(networkLaneSegment2.getDropOffFacilityName()).contains("CHANGI INTERNATIONAL AIRPORT");
        assertThat(networkLaneSegment2.getPickupInstruction()).contains("pickup instruction test 2");
        assertThat(networkLaneSegment2.getDropOffInstruction()).contains("drop off instruction test 2");
        assertThat(networkLaneSegment2.getDuration()).contains("0");
        assertThat(networkLaneSegment2.getDurationUnit()).contains("MINUTE");
        assertThat(networkLaneSegment2.getLockOutTime()).contains("2022-12-13T16:27:02+07:00");
        assertThat(networkLaneSegment2.getDepartureTime()).contains("2022-12-14T16:27:02+07:00");
        assertThat(networkLaneSegment2.getArrivalTime()).contains("2022-12-15T16:27:02+07:00");
        assertThat(networkLaneSegment2.getRecoveryTime()).contains("2022-12-17T16:27:02+07:00");
        assertThat(networkLaneSegment2.getCalculatedMileage()).contains("0");
        assertThat(networkLaneSegment2.getCalculatedMileageUnit()).contains("MILE");
    }

    private void assertSegmentLane3(NetworkLaneCsv networkLane) {
        NetworkLaneSegmentCsv networkLaneSegment3 = networkLane.getNetworkLaneSegments().get(2);
        assertThat(networkLaneSegment3.getSequenceNumber()).contains("3");
        assertThat(networkLaneSegment3.getTransportCategory()).contains("AIR");
        assertThat(networkLaneSegment3.getPartnerName()).contains("Partner Airport");
        assertThat(networkLaneSegment3.getVehicleInfo()).isBlank();
        assertThat(networkLaneSegment3.getFlightNumber()).contains("AR1324");
        assertThat(networkLaneSegment3.getAirline()).contains("Singapore Airlines");
        assertThat(networkLaneSegment3.getAirlineCode()).contains("SQ");
        assertThat(networkLaneSegment3.getMasterWaybill()).contains("123-12345675");
        assertThat(networkLaneSegment3.getPickupFacilityName()).contains("CHANGI INTERNATIONAL AIRPORT");
        assertThat(networkLaneSegment3.getDropOffFacilityName()).contains("NINOY AQUINO INTERNATIONAL AIRPORT");
        assertThat(networkLaneSegment3.getPickupInstruction()).contains("pickup instruction test 3");
        assertThat(networkLaneSegment3.getDropOffInstruction()).contains("drop off instruction test 3");
        assertThat(networkLaneSegment3.getDuration()).contains("0");
        assertThat(networkLaneSegment3.getDurationUnit()).contains("MINUTE");
        assertThat(networkLaneSegment3.getLockOutTime()).contains("2022-12-19T16:27:02+07:00");
        assertThat(networkLaneSegment3.getDepartureTime()).contains("2022-12-20T16:27:02+07:00");
        assertThat(networkLaneSegment3.getArrivalTime()).contains("2022-12-21T16:27:02+07:00");
        assertThat(networkLaneSegment3.getRecoveryTime()).contains("2022-12-23T16:27:02+07:00");
        assertThat(networkLaneSegment3.getCalculatedMileage()).contains("1");
        assertThat(networkLaneSegment3.getCalculatedMileageUnit()).contains("MILE");
    }

    // Helper method to create a mock InputStream
    private InputStream getMockInputStream() {
        String csvData = "Lane Id,Service Type,Origin Location Tree Level 1,Origin Location Tree Level 2,Origin Location Tree Level 3,Origin Location Tree Level 4,Origin Location Tree Level 5,Origin Facility Id,Destination Location Tree Level 1,Destination Location Tree Level 2,Destination Location Tree Level 3,Destination Location Tree Level 4,Destination Location Tree Level 5,Destination Facility Id,Sequence Number,Transport Category,Partner Name,Vehicle Info,Flight Number,Airline,Airline Code,Master Waybill,Pickup Facility Id,Drop Off Facility Id,Pickup Instruction,Drop Off Instruction,Duration,Duarion Unit,Lockout Time,Departure Time,Arrival Time,Recovery Time,Calculated Milage,Calculated Milage Unit,Sequence Number,Transport Category,Partner Name,Vehicle Info,Flight Number,Airline,Airline Code,Master Waybill,Pickup Facility Id,Drop Off Facility Id,Pickup Instruction,Drop Off Instruction,Duration,Duarion Unit,Lockout Time,Departure Time,Arrival Time,Recovery Time,Calculated Milage,Calculated Milage Unit,Sequence Number,Transport Category,Partner Name,Vehicle Info,Flight Number,Airline,Airline Code,Master Waybill,Pickup Facility Id,Drop Off Facility Id,Pickup Instruction,Drop Off Instruction,Duration,Duarion Unit,Lockout Time,Departure Time,Arrival Time,Recovery Time,Calculated Milage,Calculated Milage Unit\n" +
                ",Express,AUSTRALIA,NEW SOUTH WALES,SYDNEY,,,,PHILIPPINES,METRO MANILA,PASAY,NINOY AQUINO INTERNATIONAL AIRPORT,,,1,GROUND,Partner Airline,VT1324,,,,123-12345675,AUSTRALIA_NEWSOUTHWALES_SYDNEY_FAC1,SG FACILITY 1,pickup instruction test 1,drop off instruction test 1,0,MINUTE,,,,,0,MILE,2,AIR,Partner Airport,,AR1324,Singapore Airlines,SQ,123-12345675,SG FACILITY 1,CHANGI INTERNATIONAL AIRPORT,pickup instruction test 2,drop off instruction test 2,0,MINUTE,2022-12-13T16:27:02+07:00,2022-12-14T16:27:02+07:00,2022-12-15T16:27:02+07:00,2022-12-17T16:27:02+07:00,0,MILE,3,AIR,Partner Airport,,AR1324,Singapore Airlines,SQ,123-12345675,CHANGI INTERNATIONAL AIRPORT,NINOY AQUINO INTERNATIONAL AIRPORT,pickup instruction test 3,drop off instruction test 3,0,MINUTE,2022-12-19T16:27:02+07:00,2022-12-20T16:27:02+07:00,2022-12-21T16:27:02+07:00,2022-12-23T16:27:02+07:00,1,MILE";
        return new ByteArrayInputStream(csvData.getBytes());
    }
}