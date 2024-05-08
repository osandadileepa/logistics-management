package com.quincus.shipment.impl.attachment.packagejourneyairsegment;

import com.quincus.apigateway.api.ApiGatewayApi;
import com.quincus.apigateway.domain.FlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import com.quincus.shipment.api.constant.InstructionApplyToType;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.dto.csv.PackageJourneyAirSegmentCsv;
import com.quincus.shipment.api.exception.JobRecordExecutionException;
import com.quincus.shipment.impl.repository.entity.InstructionEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.service.FlightStatsEventService;
import com.quincus.shipment.impl.service.JobMetricsService;
import com.quincus.shipment.impl.service.PackageJourneySegmentService;
import com.quincus.shipment.impl.service.PartnerService;
import com.quincus.shipment.impl.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvValidator.AIRLINE_CODE_ERROR_MESSAGE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvValidator.DATE_FORMAT_ERROR_MESSAGE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentCsvValidator.FLIGHT_NUMBER_ERROR_MESSAGE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentJobStrategy.SEGMENT_UPDATE_ERROR_MESSAGE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentJobStrategy.SHIPMENT_SEGMENTS_NOT_FOUND_ERROR_MESSAGE;
import static com.quincus.shipment.impl.attachment.packagejourneyairsegment.PackageJourneyAirSegmentJobStrategy.UNEXPECTED_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageJourneyAirSegmentJobStrategyTest {

    private static final String ORGANIZATION_ID = UUID.randomUUID().toString();
    private static final String DELIMITER = " | ";
    private PackageJourneyAirSegmentJobStrategy strategy;
    @Mock
    private JobMetricsService<PackageJourneyAirSegmentCsv> jobMetricsService;
    @Mock
    private PackageJourneySegmentService packageJourneySegmentService;
    @Mock
    private ShipmentService shipmentService;
    @Mock
    private PartnerService partnerService;
    @Mock
    private PackageJourneyAirSegmentCsvValidator packageJourneyAirSegmentCsvValidator;
    @Mock
    private FlightStatsEventService flightStatsEventService;
    @Mock
    private ApiGatewayApi gatewayApi;
    @Captor
    private ArgumentCaptor<FlightScheduleSearchParameter> flightScheduleSearchCaptor;
    @Captor
    private ArgumentCaptor<PackageJourneySegmentEntity> segmentEntityArgumentCaptor;

    @BeforeEach
    void setup() {
        strategy = new PackageJourneyAirSegmentJobStrategy(jobMetricsService, packageJourneyAirSegmentCsvValidator,
                packageJourneySegmentService, shipmentService, partnerService, flightStatsEventService, gatewayApi);
    }

    @Test
    @DisplayName("Given valid data, when executing the strategy, then no exception should be thrown")
    void givenValidDataWhenExecutingStrategyThenNoExceptionThrown() {
        PackageJourneyAirSegmentCsv validData = new PackageJourneyAirSegmentCsv();

        validData.setShipmentId("12345");
        validData.setAirlineCode("AA");
        validData.setFlightNumber("123");
        validData.setOrganizationId(ORGANIZATION_ID);
        validData.setDepartureDatetime("2023-05-10T08:00:00");
        validData.setLockoutDatetime("2023-05-10T10:00:00");
        validData.setArrivalDatetime("2023-05-10T20:00:00");
        validData.setArrivalTimezone("UTC+08:00");
        validData.setRecoveryDatetime("2023-05-10T21:00:00");

        validData.setInstructionContent("Test Note");
        validData.setVendor("Test Valid Vendor");

        PackageJourneySegment foundSegment = new PackageJourneySegment();
        foundSegment.setSegmentId(UUID.randomUUID().toString());
        foundSegment.setSequence("1");
        foundSegment.setStatus(SegmentStatus.PLANNED);
        foundSegment.setDepartureTimezone("UTC+08:00");
        foundSegment.setArrivalTimezone("UTC+08:00");
        foundSegment.setStartFacility(createFacility("fac1", "city", "state", "country"));
        foundSegment.setEndFacility(createFacility("fac2", "city", "state", "country"));
        when(shipmentService.findActiveAirSegmentsMap(validData.getShipmentId(), ORGANIZATION_ID)).thenReturn(Map.of(foundSegment.getSegmentId(), List.of(foundSegment)));
        when(packageJourneyAirSegmentCsvValidator.isValid(validData)).thenReturn(true);

        PackageJourneySegmentEntity mockedReturnedEntity = new PackageJourneySegmentEntity();
        mockedReturnedEntity.setFlightOrigin("SG");
        mockedReturnedEntity.setFlightDestination("MNL");
        when(packageJourneySegmentService.findBySegmentId(foundSegment.getSegmentId())).thenReturn(Optional.of(mockedReturnedEntity));
        FlightSchedule flightSchedule = new FlightSchedule();
        flightSchedule.setCarrierName("new airline");
        when(gatewayApi.searchFlights(any())).thenReturn(List.of(flightSchedule));

        assertThatCode(() -> strategy.execute(validData)).doesNotThrowAnyException();
        assertThat(validData.getFailedReason()).isNull();
        assertThat(mockedReturnedEntity.getRecoveryTime()).isEqualTo("2023-05-10T21:00:00+08:00");
        assertThat(mockedReturnedEntity.getRecoveryTimezone()).isEqualTo(foundSegment.getArrivalTimezone());
        assertThat(mockedReturnedEntity.getArrivalTime()).isEqualTo("2023-05-10T20:00:00+08:00");
        assertThat(mockedReturnedEntity.getArrivalTimezone()).isEqualTo(validData.getArrivalTimezone());
        assertThat(mockedReturnedEntity.getLockOutTime()).isEqualTo("2023-05-10T10:00:00+08:00");
        assertThat(mockedReturnedEntity.getLockOutTimezone()).isEqualTo(foundSegment.getDepartureTimezone());
        assertThat(mockedReturnedEntity.getDepartureTime()).isEqualTo("2023-05-10T08:00:00+08:00");
        assertThat(mockedReturnedEntity.getDepartureTimezone()).isEqualTo(foundSegment.getDepartureTimezone());
        assertThat(mockedReturnedEntity.getAirline()).isEqualTo(flightSchedule.getCarrierName());
        verify(gatewayApi, times(1)).searchFlights(flightScheduleSearchCaptor.capture());
        assertThat(flightScheduleSearchCaptor.getValue().getOrigin()).isEqualTo(mockedReturnedEntity.getFlightOrigin());
        assertThat(flightScheduleSearchCaptor.getValue().getDestination()).isEqualTo(mockedReturnedEntity.getFlightDestination());
        assertThat(flightScheduleSearchCaptor.getValue().getCarrier()).isEqualTo(mockedReturnedEntity.getAirlineCode());
    }

    @Test
    @DisplayName("Given valid instruction, when executing the strategy, then no exception should be thrown")
    void givenValidInstructionWhenExecutingStrategyThenNoExceptionThrown() {
        PackageJourneyAirSegmentCsv validData = new PackageJourneyAirSegmentCsv();

        validData.setShipmentId("12345");
        validData.setAirlineCode("AA");
        validData.setFlightNumber("123");
        validData.setOrganizationId(ORGANIZATION_ID);
        validData.setDepartureDatetime("2023-05-10T08:00:00");
        validData.setLockoutDatetime("2023-05-10T10:00:00");
        validData.setArrivalDatetime("2023-05-10T20:00:00");
        validData.setArrivalTimezone("UTC+08:00");
        validData.setRecoveryDatetime("2023-05-10T21:00:00");

        String instruction = "THE SEGMENT INSTRUCTION";
        validData.setInstructionContent(instruction);
        validData.setVendor("Test Valid Vendor");

        PackageJourneySegment foundSegment = new PackageJourneySegment();
        foundSegment.setSegmentId(UUID.randomUUID().toString());
        foundSegment.setRefId("1");
        foundSegment.setSequence("1");
        foundSegment.setStatus(SegmentStatus.PLANNED);
        foundSegment.setDepartureTimezone("UTC+08:00");
        foundSegment.setArrivalTimezone("UTC+08:00");
        foundSegment.setStartFacility(createFacility("fac1", "city", "state", "country"));
        foundSegment.setEndFacility(createFacility("fac2", "city", "state", "country"));
        when(shipmentService.findActiveAirSegmentsMap(validData.getShipmentId(), ORGANIZATION_ID)).thenReturn(Map.of(foundSegment.getSegmentId(), List.of(foundSegment)));
        when(packageJourneyAirSegmentCsvValidator.isValid(validData)).thenReturn(true);

        PackageJourneySegmentEntity mockedReturnedEntity = new PackageJourneySegmentEntity();
        mockedReturnedEntity.setId(UUID.randomUUID().toString());
        mockedReturnedEntity.setRefId("1");
        mockedReturnedEntity.setFlightOrigin("SG");
        mockedReturnedEntity.setFlightDestination("MNL");
        when(packageJourneySegmentService.findBySegmentId(foundSegment.getSegmentId())).thenReturn(Optional.of(mockedReturnedEntity));
        FlightSchedule flightSchedule = new FlightSchedule();
        flightSchedule.setCarrierName("new airline");
        when(gatewayApi.searchFlights(any())).thenReturn(List.of(flightSchedule));

        assertThatCode(() -> strategy.execute(validData)).doesNotThrowAnyException();
        assertThat(validData.getFailedReason()).isNull();
        verify(packageJourneySegmentService, times(1)).update(segmentEntityArgumentCaptor.capture());

        PackageJourneySegmentEntity savedSegment = segmentEntityArgumentCaptor.getValue();
        assertThat(savedSegment).isNotNull();
        assertThat(savedSegment.getInstructions()).hasSize(1);

        InstructionEntity segmentInstruction = savedSegment.getInstructions().get(0);
        assertThat(segmentInstruction.getExternalId()).isEqualTo(mockedReturnedEntity.getId());
        assertThat(segmentInstruction.getOrganizationId()).isEqualTo(ORGANIZATION_ID);
        assertThat(segmentInstruction.getValue()).isEqualTo(instruction);
        assertThat(segmentInstruction.getLabel()).isEqualTo("Segment 1 instruction");
        assertThat(segmentInstruction.getSource()).isEqualTo(Instruction.SOURCE_SEGMENT);
        assertThat(segmentInstruction.getApplyTo()).isEqualTo(InstructionApplyToType.SEGMENT);
        assertThat(segmentInstruction.getCreatedAt()).isNotNull();
        assertThat(segmentInstruction.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Given valid instruction(update), when executing the strategy, then no exception should be thrown")
    void givenValidInstructionUpdateWhenExecutingStrategyThenNoExceptionThrown() {
        PackageJourneyAirSegmentCsv validData = new PackageJourneyAirSegmentCsv();

        validData.setShipmentId("12345");
        validData.setAirlineCode("AA");
        validData.setFlightNumber("123");
        validData.setOrganizationId(ORGANIZATION_ID);
        validData.setDepartureDatetime("2023-05-10T08:00:00");
        validData.setLockoutDatetime("2023-05-10T10:00:00");
        validData.setArrivalDatetime("2023-05-10T20:00:00");
        validData.setArrivalTimezone("UTC+08:00");
        validData.setRecoveryDatetime("2023-05-10T21:00:00");

        String instruction = "THE SEGMENT INSTRUCTION";
        validData.setInstructionContent(instruction);
        validData.setVendor("Test Valid Vendor");

        PackageJourneySegment foundSegment = new PackageJourneySegment();
        foundSegment.setSegmentId(UUID.randomUUID().toString());
        foundSegment.setRefId("1");
        foundSegment.setSequence("1");
        foundSegment.setStatus(SegmentStatus.PLANNED);
        foundSegment.setDepartureTimezone("UTC+08:00");
        foundSegment.setArrivalTimezone("UTC+08:00");
        foundSegment.setStartFacility(createFacility("fac1", "city", "state", "country"));
        foundSegment.setEndFacility(createFacility("fac2", "city", "state", "country"));
        when(shipmentService.findActiveAirSegmentsMap(validData.getShipmentId(), ORGANIZATION_ID)).thenReturn(Map.of(foundSegment.getSegmentId(), List.of(foundSegment)));
        when(packageJourneyAirSegmentCsvValidator.isValid(validData)).thenReturn(true);

        PackageJourneySegmentEntity mockedReturnedEntity = new PackageJourneySegmentEntity();
        mockedReturnedEntity.setId(UUID.randomUUID().toString());
        mockedReturnedEntity.setRefId("1");
        mockedReturnedEntity.setFlightOrigin("SG");
        mockedReturnedEntity.setFlightDestination("MNL");
        InstructionEntity existingSegmentInstruction = new InstructionEntity();
        existingSegmentInstruction.setId(UUID.randomUUID().toString());
        existingSegmentInstruction.setExternalId(UUID.randomUUID().toString());
        existingSegmentInstruction.setLabel("Segment X instruction");
        existingSegmentInstruction.setSource(Instruction.SOURCE_SEGMENT);
        existingSegmentInstruction.setApplyTo(InstructionApplyToType.SEGMENT);
        existingSegmentInstruction.setValue("OLD INSTRUCTION");
        existingSegmentInstruction.setCreatedAt("dummy1");
        existingSegmentInstruction.setUpdatedAt("dummy1");
        InstructionEntity existingOtherInstruction = new InstructionEntity();
        existingOtherInstruction.setId(UUID.randomUUID().toString());
        existingOtherInstruction.setValue("OTHER INSTRUCTION");

        mockedReturnedEntity.setInstructions(new ArrayList<>());
        mockedReturnedEntity.getInstructions().addAll(List.of(existingOtherInstruction, existingSegmentInstruction));
        when(packageJourneySegmentService.findBySegmentId(foundSegment.getSegmentId())).thenReturn(Optional.of(mockedReturnedEntity));
        FlightSchedule flightSchedule = new FlightSchedule();
        flightSchedule.setCarrierName("new airline");
        when(gatewayApi.searchFlights(any())).thenReturn(List.of(flightSchedule));

        assertThatCode(() -> strategy.execute(validData)).doesNotThrowAnyException();
        assertThat(validData.getFailedReason()).isNull();
        verify(packageJourneySegmentService, times(1)).update(segmentEntityArgumentCaptor.capture());

        PackageJourneySegmentEntity savedSegment = segmentEntityArgumentCaptor.getValue();
        assertThat(savedSegment).isNotNull();
        assertThat(savedSegment.getInstructions()).hasSize(2);

        InstructionEntity segmentInstruction = savedSegment.getInstructions().stream()
                .filter(i -> InstructionApplyToType.SEGMENT == i.getApplyTo())
                .findFirst().orElse(null);
        assertThat(segmentInstruction).isNotNull();
        assertThat(segmentInstruction.getExternalId()).isEqualTo(existingSegmentInstruction.getExternalId());
        assertThat(segmentInstruction.getValue()).isEqualTo(instruction);
        assertThat(segmentInstruction.getLabel()).isEqualTo("Segment 1 instruction");
        assertThat(segmentInstruction.getSource()).isEqualTo(Instruction.SOURCE_SEGMENT);
        assertThat(segmentInstruction.getApplyTo()).isEqualTo(InstructionApplyToType.SEGMENT);
        assertThat(segmentInstruction.getUpdatedAt()).isNotEqualTo(segmentInstruction.getCreatedAt());
    }

    @Test
    @DisplayName("Given invalid data, when executing the strategy, then JobRecordExecutionException should be thrown with the correct error message")
    void givenInvalidDataWhenExecutingStrategyThenJobRecordExecutionExceptionThrown() {
        String expectedErrorMessage = AIRLINE_CODE_ERROR_MESSAGE + DELIMITER + FLIGHT_NUMBER_ERROR_MESSAGE + DELIMITER + DATE_FORMAT_ERROR_MESSAGE;

        PackageJourneyAirSegmentCsv invalidData = new PackageJourneyAirSegmentCsv();
        String shipmentId = UUID.randomUUID().toString();
        invalidData.setShipmentId(shipmentId);
        invalidData.setAirlineCode("");
        invalidData.setFlightNumber("");
        invalidData.setDepartureDatetime("2022-01-01");
        invalidData.setOrganizationId(ORGANIZATION_ID);
        invalidData.addErrorMessage(AIRLINE_CODE_ERROR_MESSAGE);
        invalidData.addErrorMessage(FLIGHT_NUMBER_ERROR_MESSAGE);
        invalidData.addErrorMessage(DATE_FORMAT_ERROR_MESSAGE);

        when(packageJourneyAirSegmentCsvValidator.isValid(invalidData)).thenReturn(false);
        assertThatThrownBy(() -> strategy.execute(invalidData))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining(expectedErrorMessage);

        assertThat(invalidData.getFailedReason()).isNotNull();
    }

    @Test
    @DisplayName("Given data with invalid departure date format and non-existing order id, when executing the strategy, then JobRecordExecutionException should be thrown with the correct error message")
    void givenDataWithInvalidDepartureDateAndNonExistingOrderIWhenExecutingStrategyThenJobRecordExecutionExceptionThrown() {
        PackageJourneyAirSegmentCsv invalidData = new PackageJourneyAirSegmentCsv();
        String expectedErrorMessage = DATE_FORMAT_ERROR_MESSAGE;
        invalidData.setShipmentId("123");
        invalidData.setAirlineCode("AA");
        invalidData.setFlightNumber("1234");
        invalidData.setDepartureDatetime("2022-01-asdas:00:00");
        invalidData.setOrganizationId(ORGANIZATION_ID);
        invalidData.addErrorMessage(expectedErrorMessage);

        when(packageJourneyAirSegmentCsvValidator.isValid(invalidData)).thenReturn(false);

        assertThatThrownBy(() -> strategy.execute(invalidData))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessage("Validation Error:" + expectedErrorMessage);

        assertThat(invalidData.getFailedReason()).isEqualTo("Validation Error:" + expectedErrorMessage);
    }

    @Test
    @DisplayName("Given valid data but no active air segments, when executing the strategy, then JobRecordExecutionException should be thrown with the correct error message")
    void givenValidDataButNoActiveAirSegmentsWhenExecutingStrategyThenJobRecordExecutionExceptionThrown() {
        PackageJourneyAirSegmentCsv validData = new PackageJourneyAirSegmentCsv();
        validData.setShipmentId("123");
        validData.setAirlineCode("AA");
        validData.setFlightNumber("1234");
        validData.setDepartureDatetime("2023-05-10T08:00:00+08:00");
        validData.setOrganizationId(ORGANIZATION_ID);
        when(shipmentService.findActiveAirSegmentsMap(validData.getShipmentId(), ORGANIZATION_ID)).thenReturn(Map.of());
        when(packageJourneyAirSegmentCsvValidator.isValid(validData)).thenReturn(true);
        String expectedErrorMessage = "Validation Error:" + SHIPMENT_SEGMENTS_NOT_FOUND_ERROR_MESSAGE;

        assertThatThrownBy(() -> strategy.execute(validData))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessage(expectedErrorMessage);

        assertThat(validData.getFailedReason()).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("Given unexpected error during execution, when executing the strategy, then JobRecordExecutionException should be thrown with the correct error message")
    void givenUnexpectedErrorDuringExecutionWhenExecutingStrategyThenJobRecordExecutionExceptionThrown() {
        PackageJourneyAirSegmentCsv validData = new PackageJourneyAirSegmentCsv();
        validData.setShipmentId("123");
        validData.setAirlineCode("AA");
        validData.setFlightNumber("1234");
        validData.setDepartureDatetime("2023-05-10T08:00:00+08:00");
        validData.setOrganizationId(ORGANIZATION_ID);
        when(shipmentService.findActiveAirSegmentsMap(validData.getShipmentId(), ORGANIZATION_ID)).thenThrow(new RuntimeException("Unexpected error"));
        when(packageJourneyAirSegmentCsvValidator.isValid(validData)).thenReturn(true);

        String expectedErrorMessage = String.format(UNEXPECTED_ERROR_MESSAGE, validData.getShipmentId());

        assertThatThrownBy(() -> strategy.execute(validData))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessageContaining(expectedErrorMessage);

        assertThat(validData.getFailedReason()).isNotNull();
        assertThat(validData.getFailedReason()).contains(expectedErrorMessage);
    }

    @Test
    @DisplayName("Given an error during update of earliest active air segment, when executing the strategy, then JobRecordExecutionException should be thrown with the correct error message")
    void givenErrorDuringUpdateOfEarliestActiveAirSegmentWhenExecutingStrategyThenJobRecordExecutionExceptionThrown() {
        PackageJourneyAirSegmentCsv validData = new PackageJourneyAirSegmentCsv();
        validData.setShipmentId("123");
        validData.setAirlineCode("AA");
        validData.setFlightNumber("1234");
        validData.setDepartureDatetime("2023-09-19T07:00:00");
        validData.setDepartureTimezone("UTC+08:00");
        validData.setOrganizationId(ORGANIZATION_ID);

        PackageJourneySegment earliestActiveAirSegment = new PackageJourneySegment();
        earliestActiveAirSegment.setSegmentId("segment-id-123");
        earliestActiveAirSegment.setSequence("1");
        earliestActiveAirSegment.setStatus(SegmentStatus.IN_PROGRESS);
        earliestActiveAirSegment.setStartFacility(createFacility("fac1", "city", "state", "country"));
        earliestActiveAirSegment.setEndFacility(createFacility("fac2", "city", "state", "country"));

        when(shipmentService.findActiveAirSegmentsMap(validData.getShipmentId(), ORGANIZATION_ID)).thenReturn(Map.of(UUID.randomUUID().toString(), List.of(earliestActiveAirSegment)));

        PackageJourneySegmentEntity mockedSegmentEntity = new PackageJourneySegmentEntity();
        mockedSegmentEntity.setId("segment-id-123");
        mockedSegmentEntity.setSequence("1");
        when(packageJourneySegmentService.findBySegmentId(earliestActiveAirSegment.getSegmentId())).thenReturn(Optional.of(mockedSegmentEntity));
        when(packageJourneyAirSegmentCsvValidator.isValid(validData)).thenReturn(true);
        when(gatewayApi.searchFlights(any())).thenReturn(List.of(new FlightSchedule()));

        RuntimeException updateError = new RuntimeException("Update error");
        doThrow(updateError).when(packageJourneySegmentService).update(any());

        String expectedErrorMessage = "Validation Error:" + String.format(SEGMENT_UPDATE_ERROR_MESSAGE, earliestActiveAirSegment.getSegmentId(), validData.getShipmentId());

        assertThatThrownBy(() -> strategy.execute(validData))
                .isInstanceOf(JobRecordExecutionException.class)
                .hasMessage(expectedErrorMessage);

        assertThat(validData.getFailedReason()).isEqualTo(expectedErrorMessage);
    }

    private static Facility createFacility(String facilityName, String cityName, String stateName, String countryName) {
        Facility facility = new Facility();
        facility.setName(facilityName);
        facility.setTimezone("Asia/Hong_Kong UTC+08:00");
        Address address = new Address();
        address.setCityName(cityName);
        address.setStateName(stateName);
        address.setCountryName(countryName);
        facility.setLocation(address);
        return facility;
    }
}
