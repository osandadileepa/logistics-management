package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.constant.FlightEventType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.FlightDetails;
import com.quincus.shipment.api.domain.FlightStatus;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.mapper.FlightEventMapper;
import com.quincus.shipment.impl.mapper.FlightMapperImpl;
import com.quincus.shipment.impl.mapper.FlightStatusMapper;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.entity.FlightEntity;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.test_utils.TestUtil;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightEventPayloadMessage;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightStatsMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightStatsEventServiceTest {
    private static final String FLIGHT_NOT_FOUND = "Flight not found";
    private final TestUtil testUtil = TestUtil.getInstance();
    @InjectMocks
    FlightStatsEventService flightStatsEventService;
    @Mock
    MessageApi messageApi;
    @Mock
    PackageJourneySegmentService packageJourneySegmentService;
    @Mock
    FlightEventMapper flightEventMapper;
    @Mock
    FlightStatusMapper flightStatusMapper;
    @Mock
    FlightService flightService;
    @Mock
    AlertService alertService;
    @Mock
    PackageJourneySegmentRepository packageJourneySegmentRepository;
    @Mock
    ShipmentFetchService shipmentFetchService;
    @Spy
    private ObjectMapper objectMapper = testUtil.getObjectMapper();
    @Mock
    private FlightStatsEventPostProcessService flightStatsEventPostProcessService;

    @Test
    void subscribeFlight_withValidPJS_shouldHaveNoErrors() {
        PackageJourneySegmentEntity airSegment = new PackageJourneySegmentEntity();
        airSegment.setTransportType(TransportType.AIR);
        airSegment.setAirlineCode("QF");
        airSegment.setFlightNumber("19");
        airSegment.setDepartureTime("2023-02-18 16:27:02 +0700");
        FlightEntity flight = new FlightEntity();
        airSegment.setFlight(flight);
        flight.setOrigin("SYD");
        flight.setDestination("MNL");

        assertThatNoException().isThrownBy(() -> flightStatsEventService.subscribeFlight(List.of(airSegment)));
    }

    @Test
    void receiveFlightStatsMessage_withValidMessage_shouldHaveNoErrors() throws JsonProcessingException {
        JsonNode message = testUtil.getDataFromFile("samplepayload/flight-subscribe-rs.json");
        String uuid = randomUUID().toString();
        Flight flight = new Flight();
        flight.setCarrier("PR");
        flight.setFlightNumber("118");
        flight.setDepartureDate("2023-03-03");
        flight.setOrigin("MNL");
        flight.setDestination("LAX");
        FlightEntity flightEntity = new FlightMapperImpl().mapDomainToEntity(flight);
        when(flightService.createOrUpdate(any(Flight.class))).thenReturn(flightEntity);
        when(flightEventMapper.mapFlightEventPayloadMessageToFlight(any(FlightEventPayloadMessage.class))).thenReturn(flight);

        assertThatNoException().isThrownBy(() -> flightStatsEventService.processFlightStatsMessage(message.toString(), uuid));
        verify(packageJourneySegmentService, times(1)).findSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull(anyString(), anyString(),
                anyString(), anyString(), anyString());
        verify(objectMapper, times(1)).readValue(message.toString(), FlightStatsMessage.class);
    }

    @Test
    void receiveFlightStatsMessage_withValidMessageAndFlightStatus_shouldHaveNoErrors() throws JsonProcessingException {
        JsonNode message = testUtil.getDataFromFile("samplepayload/flight-status-rs.json");
        String uuid = randomUUID().toString();
        Flight flightEvent = new Flight();
        flightEvent.setFlightId(118123L);
        flightEvent.setCarrier("PR");
        flightEvent.setFlightNumber("118");
        flightEvent.setDepartureDate("2023-03-03");
        flightEvent.setOrigin("MNL");
        flightEvent.setDestination("LAX");
        flightEvent.setEventName(FlightEventName.FLIGHT_LANDED);
        FlightStatus flightStatus = new FlightStatus();
        FlightDetails departure = new FlightDetails();
        departure.setScheduledTime("2022-12-09T11:20:00.000+08:00");
        departure.setActualTime("2022-12-09T11:20:00.000+08:00");
        FlightDetails arrival = new FlightDetails();
        arrival.setScheduledTime("2022-12-09T08:15:00.000-08:00");
        arrival.setActualTime("2022-12-09T11:20:00.000+08:00");
        flightStatus.setDeparture(departure);
        flightStatus.setArrival(arrival);
        flightStatus.setAirlineName("AIRLINE_NAME");
        flightStatus.setEventName(FlightEventName.FLIGHT_LANDED);
        flightEvent.setFlightStatus(flightStatus);
        ShipmentEntity dummyShipment = createDummyShipment();

        FlightEntity flightEntity = new FlightMapperImpl().mapDomainToEntity(flightEvent);
        when(flightEventMapper.mapFlightEventPayloadMessageToFlight(any(FlightEventPayloadMessage.class))).thenReturn(flightEvent);
        when(packageJourneySegmentService.findSegmentsWithFlightDetails(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(List.of(new PackageJourneySegmentEntity()));
        when(flightService.createOrUpdate(any(Flight.class))).thenReturn(flightEntity);
        when(shipmentFetchService.findByJourneyIdOrThrowException(any())).thenReturn(List.of(dummyShipment));

        when(
                packageJourneySegmentService.findSegmentsWithFlightDetails(
                        flightEvent.getCarrier(),
                        flightEvent.getFlightNumber(),
                        flightEvent.getDepartureDate(),
                        flightEvent.getOrigin(),
                        flightEvent.getDestination(),
                        flightEvent.getFlightId()
                )
        ).thenReturn(List.of(new PackageJourneySegmentEntity()));

        assertThatNoException().isThrownBy(() -> flightStatsEventService.processFlightStatsMessage(message.toString(), uuid));

        verify(packageJourneySegmentService, times(1)).findSegmentsWithFlightDetails(
                flightEvent.getCarrier(),
                flightEvent.getFlightNumber(),
                flightEvent.getDepartureDate(),
                flightEvent.getOrigin(),
                flightEvent.getDestination(),
                flightEvent.getFlightId()
        );
        verify(flightStatsEventPostProcessService, times(1)).processFlightStats(anyList(), any(), any());
        verify(packageJourneySegmentService, times(1)).findSegmentsWithFlightDetails(anyString(), anyString(), anyString(), anyString(), anyString(), any());
        verify(objectMapper, times(1)).readValue(message.toString(), FlightStatsMessage.class);
    }

    @Test
    void receiveFlightStatsMessage_shouldCreateAlertAndSendCancellationMessage_whenFlightCancelled() throws JsonProcessingException {
        JsonNode message = testUtil.getDataFromFile("samplepayload/flight-status-rs-cancelled.json");
        String uuid = randomUUID().toString();
        Flight flightEvent = new Flight();
        flightEvent.setFlightId(118123L);
        flightEvent.setCarrier("PR");
        flightEvent.setFlightNumber("118");
        flightEvent.setDepartureDate("2023-03-03");
        flightEvent.setOrigin("MNL");
        flightEvent.setDestination("LAX");
        flightEvent.setEventName(FlightEventName.CANCELLED);
        FlightStatus flightStatus = new FlightStatus();
        FlightDetails departure = new FlightDetails();
        departure.setScheduledTime("2022-12-09T11:20:00.000+08:00");
        departure.setActualTime("2022-12-09T11:20:00.000+08:00");
        FlightDetails arrival = new FlightDetails();
        arrival.setScheduledTime("2022-12-09T08:15:00.000-08:00");
        arrival.setActualTime("2022-12-09T11:20:00.000+08:00");
        flightStatus.setDeparture(departure);
        flightStatus.setArrival(arrival);
        flightStatus.setAirlineName("AIRLINE_NAME");
        flightStatus.setEventName(FlightEventName.CANCELLED);
        flightEvent.setFlightStatus(flightStatus);
        ShipmentEntity dummyShipment = createDummyShipment();

        PackageJourneySegmentEntity segmentWithFlightRelatedEntity = new PackageJourneySegmentEntity();
        segmentWithFlightRelatedEntity.setId("segment1");
        segmentWithFlightRelatedEntity.setRefId("1");
        segmentWithFlightRelatedEntity.setSequence("1");

        FlightEntity flightEntity = new FlightMapperImpl().mapDomainToEntity(flightEvent);
        when(flightEventMapper.mapFlightEventPayloadMessageToFlight(any(FlightEventPayloadMessage.class))).thenReturn(flightEvent);
        when(shipmentFetchService.findByJourneyIdOrThrowException(any())).thenReturn(List.of(dummyShipment));
        when(packageJourneySegmentService.findSegmentsWithFlightDetails(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(List.of(segmentWithFlightRelatedEntity));
        when(flightService.createOrUpdate(any(Flight.class))).thenReturn(flightEntity);
        when(packageJourneySegmentService.findSegmentsWithFlightDetails(flightEvent.getCarrier(), flightEvent.getFlightNumber(),
                flightEvent.getDepartureDate(), flightEvent.getOrigin(), flightEvent.getDestination(), flightEvent.getFlightId()))
                .thenReturn(List.of(new PackageJourneySegmentEntity()));

        assertThatNoException().isThrownBy(() -> flightStatsEventService.processFlightStatsMessage(message.toString(), uuid));

        verify(packageJourneySegmentService, times(1)).findSegmentsWithFlightDetails(flightEvent.getCarrier(),
                flightEvent.getFlightNumber(), flightEvent.getDepartureDate(), flightEvent.getOrigin(), flightEvent.getDestination(), flightEvent.getFlightId());
        verify(alertService, times(1)).createFlightCancellationAlert(any());
        verify(alertService, times(1)).createFlightCancellationShipmentJourneyAlert(any());
        verify(packageJourneySegmentService, times(1)).findSegmentsWithFlightDetails(anyString(), anyString(), anyString(), anyString(), anyString(), any());
        verify(packageJourneySegmentRepository, times(1)).saveAllAndFlush(any());
        verify(objectMapper, times(1)).readValue(message.toString(), FlightStatsMessage.class);
    }

    @Test
    void receiveFlightStatsMessage_shouldCreateAlert_whenFlightNotFound() throws JsonProcessingException {
        JsonNode message = testUtil.getDataFromFile("samplepayload/flight-status-rs-flight-not-found.json");
        String uuid = randomUUID().toString();
        Flight flight = new Flight();
        flight.setFlightId(118123L);
        flight.setCarrier("PR");
        flight.setFlightNumber("118");
        flight.setDepartureDate("2023-03-03");
        flight.setOrigin("MNL");
        flight.setDestination("LAX");
        flight.setSuccess(false);
        flight.setError(FLIGHT_NOT_FOUND);
        flight.setEventType(FlightEventType.FLIGHT_SUBSCRIBE_RS);

        FlightStatus flightStatus = new FlightStatus();
        FlightDetails departure = new FlightDetails();
        departure.setScheduledTime("2022-12-09T11:20:00.000+08:00");
        departure.setActualTime("2022-12-09T11:20:00.000+08:00");
        FlightDetails arrival = new FlightDetails();
        arrival.setScheduledTime("2022-12-09T08:15:00.000-08:00");
        arrival.setActualTime("2022-12-09T11:20:00.000+08:00");
        flightStatus.setDeparture(departure);
        flightStatus.setArrival(arrival);
        flightStatus.setAirlineName("AIRLINE_NAME");
        flightStatus.setEventType(FlightEventType.FLIGHT_SUBSCRIBE_RS);
        flight.setFlightStatus(flightStatus);

        ShipmentEntity dummyShipment = createDummyShipment();

        PackageJourneySegmentEntity segmentWithFlightRelatedEntity = new PackageJourneySegmentEntity();
        segmentWithFlightRelatedEntity.setId("segment1");
        segmentWithFlightRelatedEntity.setRefId("1");
        segmentWithFlightRelatedEntity.setSequence("1");


        FlightEntity flightEntity = new FlightMapperImpl().mapDomainToEntity(flight);
        when(flightEventMapper.mapFlightEventPayloadMessageToFlight(any(FlightEventPayloadMessage.class))).thenReturn(flight);

        when(flightService.createOrUpdate(any(Flight.class))).thenReturn(flightEntity);

        assertThatNoException().isThrownBy(() -> flightStatsEventService.processFlightStatsMessage(message.toString(), uuid));

        verify(packageJourneySegmentService, times(1)).findSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull(flight.getCarrier(),
                flight.getFlightNumber(), flight.getDepartureDate(), flight.getOrigin(), flight.getDestination());
        verify(packageJourneySegmentService, times(1)).findSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(alertService, never()).createFlightCancellationAlert(any());
        verify(messageApi, never()).sendDispatchCanceledFlight(any(Shipment.class), anyString(), anyString());
        verify(objectMapper, times(1)).readValue(message.toString(), FlightStatsMessage.class);
        //verify(flightStatsEventPostProcessService, times(1)).sendUpdatedSegmentFromShipment(List.of(dummyShipment),segmentWithFlightRelatedEntity.getId());
        // check that to updated data is sent to entity to be updated
        assertThat(dummyShipment.getShipmentJourney().getPackageJourneySegments().get(0).getFlightNumber()).isEqualTo("123-123");

    }

    private ShipmentEntity createDummyShipment() {
        PackageJourneySegmentEntity segment1 = new PackageJourneySegmentEntity();
        segment1.setRefId("1");
        segment1.setSequence("1");
        segment1.setId("segment1");
        segment1.setFlightNumber("123-123");

        ShipmentJourneyEntity shipmentJourney = new ShipmentJourneyEntity();
        shipmentJourney.addPackageJourneySegment(segment1);

        ShipmentEntity dummyShipment = new ShipmentEntity();
        dummyShipment.setId("shipment_id_001");
        dummyShipment.setUserId("user_id_001");

        OrganizationEntity organization = new OrganizationEntity();
        organization.setId(randomUUID().toString());

        dummyShipment.setOrganization(organization);
        dummyShipment.setShipmentJourney(shipmentJourney);
        return dummyShipment;
    }
}
