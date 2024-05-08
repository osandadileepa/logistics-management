package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.constant.FlightEventType;
import com.quincus.shipment.api.constant.FlightStatusCode;
import com.quincus.shipment.api.domain.FlightDetails;
import com.quincus.shipment.api.domain.FlightStatus;
import com.quincus.shipment.impl.mapper.FlightStatusMapper;
import com.quincus.shipment.impl.mapper.FlightStatusMapperImpl;
import com.quincus.shipment.impl.repository.FlightStatusRepository;
import com.quincus.shipment.impl.repository.entity.FlightEntity;
import com.quincus.shipment.impl.repository.entity.FlightStatusEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightStatusMessageServiceTest {
    @InjectMocks
    private FlightStatusService flightStatusService;
    @Mock
    private FlightStatusRepository flightStatusRepository;
    @Mock
    private FlightStatusMapper flightStatusMapper;

    @Test
    void testSave() {
        FlightDetails flightDetailsDeparture = new FlightDetails();
        flightDetailsDeparture.setAirportCode("MNL");
        flightDetailsDeparture.setAirportName("Ninoy Aquino International Airport");
        OffsetDateTime departureScheduledTime = OffsetDateTime.now().plus(10, ChronoUnit.DAYS);
        flightDetailsDeparture.setScheduledTime(departureScheduledTime.format(ISO_OFFSET_DATE_TIME));
        flightDetailsDeparture.setEstimatedTime(OffsetDateTime.now().plus(11, ChronoUnit.DAYS).format(ISO_OFFSET_DATE_TIME));
        flightDetailsDeparture.setActualTime(OffsetDateTime.now().plus(12, ChronoUnit.DAYS).format(ISO_OFFSET_DATE_TIME));
        flightDetailsDeparture.setTimezone("UTC" + departureScheduledTime.getOffset());
        FlightDetails flightDetailsArrival = new FlightDetails();
        flightDetailsArrival.setAirportCode("LAX");
        flightDetailsArrival.setAirportName("Los Angeles International Airport");
        OffsetDateTime arrivalScheduledTime = OffsetDateTime.now().plus(13, ChronoUnit.DAYS);
        flightDetailsArrival.setScheduledTime(arrivalScheduledTime.format(ISO_OFFSET_DATE_TIME));
        flightDetailsArrival.setEstimatedTime(OffsetDateTime.now().plus(14, ChronoUnit.DAYS).format(ISO_OFFSET_DATE_TIME));
        flightDetailsArrival.setActualTime(OffsetDateTime.now().plus(15, ChronoUnit.DAYS).format(ISO_OFFSET_DATE_TIME));
        flightDetailsArrival.setTimezone("UTC" + arrivalScheduledTime.getOffset());
        Long flightId = 1121799569L;
        FlightStatus flightStatus = new FlightStatus();
        flightStatus.setId("id");
        flightStatus.setFlightId(flightId);
        flightStatus.setStatus(FlightStatusCode.C);
        flightStatus.setDeparture(flightDetailsDeparture);
        flightStatus.setArrival(flightDetailsArrival);
        flightStatus.setAirlineCode("PR");
        flightStatus.setAirlineName("Philippine Airlines");
        flightStatus.setOperatingAirlineCode("PR");
        flightStatus.setLongitude("longitude");
        flightStatus.setLatitude("latitude");
        flightStatus.setSpeedMph("speed");
        flightStatus.setAltitudeFt("altitude");
        flightStatus.setEventDate("2022-12-09T01:11:29.513Z");
        flightStatus.setEventType(FlightEventType.FLIGHT_STATUS_RS);
        flightStatus.setEventName(FlightEventName.TIME_ADJUSTMENT);
        FlightStatusMapper mapper = new FlightStatusMapperImpl();
        FlightEntity flightEntity = new FlightEntity();
        flightEntity.setFlightId(flightId);
        FlightStatusEntity flightStatusEntity = mapper.mapDomainToEntity(flightStatus);
        flightStatusEntity.setFlight(flightEntity);
        when(flightStatusMapper.mapDomainToEntity(any(FlightStatus.class))).thenReturn(flightStatusEntity);
        when(flightStatusRepository.save(any(FlightStatusEntity.class))).thenReturn(flightStatusEntity);
        FlightStatusEntity result = flightStatusService.save(flightStatus);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version", "flight")
                .isEqualTo(flightStatus);

        assertThat(result.getFlight().getFlightId()).isEqualTo(flightStatus.getFlightId());
    }
}
