package com.quincus.shipment.impl.attachment.packagejourneyairsegment;

import com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv;
import com.quincus.shipment.impl.attachment.JobTemplateStrategy;
import com.quincus.shipment.impl.mapper.JobMetricsMapper;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.JobMetricsService;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PackageJourneyAirSegmentAttachmentServiceTest {

    private static final String INSTRUCTION = "\"Test1, New segment instructions here, wrapped in quotes.\"";
    private static final String NOTES = "Test2, New segment notes here, without quotes";
    private PackageJourneyAirSegmentAttachmentService attachmentService;

    @Mock
    private JobMetricsService<PackageJourneyAirSegmentCsv> jobMetricsService;

    @Mock
    private JobTemplateStrategy<PackageJourneyAirSegmentCsv> jobTemplateStrategy;

    @Mock
    private JobMetricsMapper<PackageJourneyAirSegmentCsv> jobMetricsMapper;

    @Mock
    private PackageJourneyAirSegmentCsvExportService packageJourneyAirSegmentCsvExportService;

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @BeforeEach
    void setup() {
        attachmentService = new PackageJourneyAirSegmentAttachmentService(jobMetricsService, jobTemplateStrategy, jobMetricsMapper, userDetailsProvider, packageJourneyAirSegmentCsvExportService);
    }

    @Test
    @DisplayName("Given a valid CSV file, when parsing to domain, then return a list of PackageJourneyAirSegmentCsv")
    void givenValidCsvFileWhenParsingToDomainThenReturnListOfPackageJourneyAirSegmentCsv() {
        String csvData = "Shipment ID,Unit (Do not edit),Volume Weight (Do not edit),Gross Weight (Do not edit),Airline Code,Flight Number,Departure DateTime,Departure Timezone,Origin Facility,Origin Country,Origin State/Province,Origin City,Airway Bill,Vendor,Lockout DateTime,Lockout Timezone,Arrival Datetime,Arrival Timezone,Recovery DateTime,Recovery Timezone,Segment Note/Instruction Content\n" +
                "123,Metric,5,20,AA,1234,2023-05-10T08:00:00,UTC+08:00,SINGAPORE_AIRPORT,SINGAPORE,Central Singapore Community Development Council,SINGAPORE,50712345675,Singapore Airlines,2023-05-10T08:00:00,UTC+08:00,2023-09-19T11:00:00,UTC+08:00,2023-09-19T15:00:00,UTC+08:00," + INSTRUCTION + "\n" +
                "456,Imperial,11,44,BB,5678,2023-05-11T09:00:00,UTC+08:00,NINOY AQUINO INTERNATIONAL AIRPORT,PHILIPPINES,METRO MANILA,MANILA,12345678912,Partner Freight Forwarder,2023-05-11T09:00:00,UTC+08:00,2023-09-19T14:00:00,UTC+08:00,2023-09-19T18:00:00,UTC+08:00," + INSTRUCTION;
        MockMultipartFile multipartFile = new MockMultipartFile("file.csv", csvData.getBytes());

        List<PackageJourneyAirSegmentCsv> result = attachmentService.parseToDomain(multipartFile);

        assertThat(result).hasSize(2);

        PackageJourneyAirSegmentCsv firstCsv = result.get(0);
        assertThat(firstCsv.getShipmentId()).isEqualTo("123");
        assertThat(firstCsv.getAirlineCode()).isEqualTo("AA");
        assertThat(firstCsv.getFlightNumber()).isEqualTo("1234");
        assertThat(firstCsv.getDepartureDatetime()).isEqualTo("2023-05-10T08:00:00");
        assertThat(firstCsv.getDepartureTimezone()).isEqualTo("UTC+08:00");
        assertThat(firstCsv.getAirWayBill()).isEqualTo("50712345675");
        assertThat(firstCsv.getInstructionContent()).isEqualTo(StringUtils.remove(INSTRUCTION, "\""));
        assertThat(firstCsv.getVendor()).isEqualTo("Singapore Airlines");

        PackageJourneyAirSegmentCsv secondCsv = result.get(1);
        assertThat(secondCsv.getShipmentId()).isEqualTo("456");
        assertThat(secondCsv.getAirlineCode()).isEqualTo("BB");
        assertThat(secondCsv.getFlightNumber()).isEqualTo("5678");
        assertThat(secondCsv.getDepartureDatetime()).isEqualTo("2023-05-11T09:00:00");
    }

    @Test
    @DisplayName("Given a CSV file with a header record, when parsing to domain, then skip the header record")
    void givenCsvFileWithHeaderRecordWhenParsingToDomainThenSkipHeaderRecord() {
        String csvData = "Shipment ID,Unit (Do not edit),Volume Weight (Do not edit),Gross Weight (Do not edit),Airline Code,Flight Number,Departure DateTime,Departure Timezone,Origin Facility,Origin Country,Origin State/Province,Origin City,Airway Bill,Vendor,Lockout DateTime,Lockout Timezone,Arrival Datetime,Arrival Timezone,Recovery DateTime,Recovery Timezone,Segment Note/Instruction Content\n" +
                "123,Metric,5,20,SG,1923,2023-09-19T07:00:00,UTC+08:00,SINGAPORE_AIRPORT,SINGAPORE,Central Singapore Community Development Council,SINGAPORE,50712345675,Singapore Airlines,2023-09-19T05:00:00,UTC+08:00,2023-09-19T11:00:00,UTC+08:00,2023-09-19T15:00:00,UTC+08:00," + INSTRUCTION + "\n" +
                "456,Imperial,11,44,PH,3012,2023-09-19T09:00:00,UTC+08:00,NINOY AQUINO INTERNATIONAL AIRPORT,PHILIPPINES,METRO MANILA,MANILA,12345678912,Partner Freight Forwarder,2023-09-19T07:00:00,UTC+08:00,2023-09-19T14:00:00,UTC+08:00,2023-09-19T18:00:00,UTC+08:00," + NOTES;
        MockMultipartFile multipartFile = new MockMultipartFile("file.csv", csvData.getBytes());

        List<PackageJourneyAirSegmentCsv> result = attachmentService.parseToDomain(multipartFile);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getShipmentId()).isEqualTo("123");
        assertThat(result.get(1).getShipmentId()).isEqualTo("456");
    }

    @Test
    @DisplayName("Given CsvFile with Instruction with comma and No double quote was set, when ParseToDomain, then should merge value to instruction")
    void givenCsvFileWithNoteHaveCommaAndNoDoubleQuote_whenParseToDomain_shouldMergeValueToInstruction() {
        String csvData = "Shipment ID,Unit (Do not edit),Volume Weight (Do not edit),Gross Weight (Do not edit),Airline Code,Flight Number,Departure DateTime,Departure Timezone,Origin Facility,Origin Country,Origin State/Province,Origin City,Airway Bill,Vendor,Lockout DateTime,Lockout Timezone,Arrival Datetime,Arrival Timezone,Recovery DateTime,Recovery Timezone,Segment Note/Instruction Content\n" +
                "123,Metric,5,20,SG,1923,2023-09-19T07:00:00,UTC+08:00,SINGAPORE_AIRPORT,SINGAPORE,Central Singapore Community Development Council,SINGAPORE,50712345675,Singapore Airlines,2023-09-19T05:00:00,UTC+08:00,2023-09-19T11:00:00,UTC+08:00,2023-09-19T15:00:00,UTC+08:00,instruction1, instruction2\n" +
                "456,Imperial,11,44,PH,3012,2023-09-19T09:00:00,UTC+08:00,NINOY AQUINO INTERNATIONAL AIRPORT,PHILIPPINES,METRO MANILA,MANILA,12345678912,Partner Freight Forwarder,2023-09-19T07:00:00,UTC+08:00,2023-09-19T14:00:00,UTC+08:00,2023-09-19T18:00:00,UTC+08:00,instruction1, instruction2, instruction3";
        MockMultipartFile multipartFile = new MockMultipartFile("file.csv", csvData.getBytes());

        List<PackageJourneyAirSegmentCsv> result = attachmentService.parseToDomain(multipartFile);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getShipmentId()).isEqualTo("123");
        assertThat(result.get(0).getInstructionContent()).isEqualTo("instruction1, instruction2");
        assertThat(result.get(1).getInstructionContent()).isEqualTo("instruction1, instruction2, instruction3");
        assertThat(result.get(1).getShipmentId()).isEqualTo("456");
    }

    @Test
    @DisplayName("Given csv file with error when ParseToDomain should not consider error message as instruction")
    void givenCsvFileWithError_whenParseToDomain_shouldNotConsiderErrorMessageAsInstruction() {
        String csvData = "Shipment ID,Unit (Do not edit),Volume Weight (Do not edit),Gross Weight (Do not edit),Airline Code,Flight Number,Departure DateTime,Departure Timezone,Origin Facility,Origin Country,Origin State/Province,Origin City,Airway Bill,Vendor,Lockout DateTime,Lockout Timezone,Arrival Datetime,Arrival Timezone,Recovery DateTime,Recovery Timezone,Segment Note/Instruction Content,Failed Reason\n" +
                "123,Metric,5,20,SG,1923,2023-09-19T07:00:00,UTC+08:00,SINGAPORE_AIRPORT,SINGAPORE,Central Singapore Community Development Council,SINGAPORE,50712345675,Singapore Airlines,2023-09-19T05:00:00,UTC+08:00,2023-09-19T11:00:00,UTC+08:00,2023-09-19T15:00:00,UTC+08:00,instruction1,Validation Error:failedReason2\n" +
                "456,Imperial,11,44,PH,3012,2023-09-19T09:00:00,UTC+08:00,NINOY AQUINO INTERNATIONAL AIRPORT,PHILIPPINES,METRO MANILA,MANILA,12345678912,Partner Freight Forwarder,2023-09-19T07:00:00,UTC+08:00,2023-09-19T14:00:00,UTC+08:00,2023-09-19T18:00:00,UTC+08:00,instruction2,Validation Error:failedReason1";
        MockMultipartFile multipartFile = new MockMultipartFile("file.csv", csvData.getBytes());

        List<PackageJourneyAirSegmentCsv> result = attachmentService.parseToDomain(multipartFile);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getShipmentId()).isEqualTo("123");
        assertThat(result.get(0).getInstructionContent()).isEqualTo("instruction1");
        assertThat(result.get(1).getInstructionContent()).isEqualTo("instruction2");
        assertThat(result.get(1).getShipmentId()).isEqualTo("456");
    }

    @Test
    @DisplayName("Given csv file with failed reason column and instructions with comma value and not double quote when ParseToDomain should not consider error message as instruction")
    void givenCsvFileWithErrorAndInstructionsWithCommaAndNoDoubleQuote_whenParseToDomain_shouldNotConsiderErrorMessageAsInstrcution() {
        String csvData = "Shipment ID,Unit (Do not edit),Volume Weight (Do not edit),Gross Weight (Do not edit),Airline Code,Flight Number,Departure DateTime,Departure Timezone,Origin Facility,Origin Country,Origin State/Province,Origin City,Airway Bill,Vendor,Lockout DateTime,Lockout Timezone,Arrival Datetime,Arrival Timezone,Recovery DateTime,Recovery Timezone,Segment Note/Instruction Content,Failed Reason\n" +
                "123,Metric,5,20,SG,1923,2023-09-19T07:00:00,UTC+08:00,SINGAPORE_AIRPORT,SINGAPORE,Central Singapore Community Development Council,SINGAPORE,50712345675,Singapore Airlines,2023-09-19T05:00:00,UTC+08:00,2023-09-19T11:00:00,UTC+08:00,2023-09-19T15:00:00,UTC+08:00,instruction1, instruction2,Validation Error:failed reason 1\n" +
                "456,Imperial,11,44,PH,3012,2023-09-19T09:00:00,UTC+08:00,NINOY AQUINO INTERNATIONAL AIRPORT,PHILIPPINES,METRO MANILA,MANILA,12345678912,Partner Freight Forwarder,2023-09-19T07:00:00,UTC+08:00,2023-09-19T14:00:00,UTC+08:00,2023-09-19T18:00:00,UTC+08:00,instruction1, instruction2, instruction3,Validation Error:failed reason 2\n" +
                "456,Imperial,11,44,PH,3012,2023-09-19T09:00:00,UTC+08:00,NINOY AQUINO INTERNATIONAL AIRPORT,PHILIPPINES,METRO MANILA,MANILA,12345678912,Partner Freight Forwarder,2023-09-19T07:00:00,UTC+08:00,2023-09-19T14:00:00,UTC+08:00,2023-09-19T18:00:00,UTC+08:00,instruction1, instruction2, instruction3, instruction4";

        MockMultipartFile multipartFile = new MockMultipartFile("file.csv", csvData.getBytes());

        List<PackageJourneyAirSegmentCsv> result = attachmentService.parseToDomain(multipartFile);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getShipmentId()).isEqualTo("123");
        assertThat(result.get(0).getInstructionContent()).isEqualTo("instruction1, instruction2");
        assertThat(result.get(1).getInstructionContent()).isEqualTo("instruction1, instruction2, instruction3");
        assertThat(result.get(2).getInstructionContent()).isEqualTo("instruction1, instruction2, instruction3, instruction4");
        assertThat(result.get(1).getShipmentId()).isEqualTo("456");
    }

    @Test
    @DisplayName("Given a CSV file with EXAMPLE entry, when parsing to domain, then skip the EXAMPLE record")
    void givenCsvFileWithExampleRecordWhenParsingToDomainThenSkipExampleRecord() {
        String csvData = "Shipment ID,Unit (Do not edit),Volume Weight (Do not edit),Gross Weight (Do not edit),Airline Code,Flight Number,Departure DateTime,Departure Timezone,Origin Facility,Origin Country,Origin State/Province,Origin City,Airway Bill,Vendor,Lockout DateTime,Lockout Timezone,Arrival Datetime,Arrival Timezone,Recovery DateTime,Recovery Timezone,Segment Note/Instruction Content\n" +
                "EXAMPLE1,Metric,5,20,SG,1923,2023-09-19T07:00:00,UTC+08:00,SINGAPORE_AIRPORT,SINGAPORE,Central Singapore Community Development Council,SINGAPORE,50712345675,Singapore Airlines,2023-09-19T05:00:00,UTC+08:00,2023-09-19T11:00:00,UTC+08:00,2023-09-19T15:00:00,UTC+08:00," + INSTRUCTION + "\n" +
                "EXAMPLE2,Metric,5,20,US,1923,2023-09-19T07:00:00,UTC+08:00,SINGAPORE_AIRPORT,SINGAPORE,Central Singapore Community Development Council,SINGAPORE,50712345675,Singapore Airlines,2023-09-19T05:00:00,UTC+08:00,2023-09-19T11:00:00,UTC+08:00,2023-09-19T15:00:00,UTC+08:00," + INSTRUCTION + "\n" +
                "123,Imperial,11,44,SG,1923,2023-09-19T07:00:00,UTC+08:00,SINGAPORE_AIRPORT,SINGAPORE,Central Singapore Community Development Council,SINGAPORE,50712345675,Singapore Airlines,2023-09-19T05:00:00,UTC+08:00,2023-09-19T11:00:00,UTC+08:00,2023-09-19T15:00:00,UTC+08:00," + INSTRUCTION + "\n" +
                "456,Imperial,11,44,PH,3012,2023-09-19T09:00:00,UTC+08:00,NINOY AQUINO INTERNATIONAL AIRPORT,PHILIPPINES,METRO MANILA,MANILA,12345678912,Partner Freight Forwarder,2023-09-19T07:00:00,UTC+08:00,2023-09-19T14:00:00,UTC+08:00,2023-09-19T18:00:00,UTC+08:00," + INSTRUCTION;
        MockMultipartFile multipartFile = new MockMultipartFile("file.csv", csvData.getBytes());

        List<PackageJourneyAirSegmentCsv> result = attachmentService.parseToDomain(multipartFile);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getShipmentId()).isEqualTo("123");
        assertThat(result.get(1).getShipmentId()).isEqualTo("456");
    }

    @Test
    @DisplayName("Given an invalid CSV file, when parsing to domain, then throw a QuincusValidationException")
    void givenInvalidCsvFileWhenParsingToDomainThenThrowQuincusValidationException() {
        MockMultipartFile multipartFile = new MockMultipartFile("file.csv", "invalid,csv,data".getBytes());

        assertThatThrownBy(() -> attachmentService.parseToDomain(multipartFile))
                .isInstanceOf(QuincusValidationException.class);
    }
}