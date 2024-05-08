package com.quincus.shipment.impl.service;

import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.FlightStatus;
import com.quincus.shipment.impl.mapper.FlightMapper;
import com.quincus.shipment.impl.mapper.FlightStatusMapper;
import com.quincus.shipment.impl.repository.FlightRepository;
import com.quincus.shipment.impl.repository.entity.FlightEntity;
import com.quincus.shipment.impl.repository.entity.FlightStatusEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {
    @InjectMocks
    private FlightService flightService;
    @Mock
    private FlightRepository flightRepository;
    @Mock
    private FlightMapper flightMapper;
    @Mock
    private FlightStatusMapper flightStatusMapper;

    @Test
    @DisplayName("given valid flight domain then save successfully")
    void testSave() {
        Flight flight = new Flight();
        flight.setId("id");
        flight.setFlightId(1121799569L);
        flight.setCarrier("PR");
        flight.setFlightNumber("112");
        flight.setDepartureDate("2023-03-03");
        flight.setOrigin("MNL");
        flight.setDestination("LAX");
        FlightMapper mapper = Mappers.getMapper(FlightMapper.class);
        FlightEntity flightEntity = mapper.mapDomainToEntity(flight);
        when(flightMapper.mapDomainToEntity(any(Flight.class))).thenReturn(flightEntity);
        when(flightRepository.saveAndFlush(any(FlightEntity.class))).thenReturn(flightEntity);
        FlightEntity result = flightService.save(flight);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version", "flightStatuses")
                .isEqualTo(flight);
    }

    @Test
    @DisplayName("given valid flight domain then update successfully")
    void testCreateOrUpdate() {
        long flightId = 1121799569L;
        Flight flight = new Flight();
        flight.setId("id");
        flight.setFlightId(flightId);
        flight.setCarrier("PR");
        flight.setFlightNumber("112");
        flight.setDepartureDate("2023-03-03");
        flight.setOrigin("MNL");
        flight.setDestination("LAX");
        FlightStatus flightStatus = new FlightStatus();
        flightStatus.setFlightId(flightId);
        flight.setFlightStatus(flightStatus);
        FlightMapper mapper = Mappers.getMapper(FlightMapper.class);
        FlightEntity flightEntity = mapper.mapDomainToEntity(flight);
        when(flightRepository.findByFlightId(anyLong())).thenReturn(null);
        when(flightService.save(any(Flight.class))).thenReturn(flightEntity);
        when(flightStatusMapper.mapDomainToEntity(any(FlightStatus.class))).thenReturn(createFlightStatusEntity(flightId, "2023-03-18T10:47:56.202331500Z"));

        FlightEntity result = flightService.createOrUpdate(flight);
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("modifyTime", "createTime", "version", "flightStatuses")
                .isEqualTo(flight);
    }

    @Test
    @DisplayName("given invalid flight details then return null")
    void testFindByInvalidFlightDetails() {
        when(flightRepository.findByCarrierAndFlightNumberAndDepartureDateAndOriginAndDestination(any(), any(), any(), any(), any()))
                .thenReturn(null);
        assertThat(flightService.findByFlightDetails("PR", "123", "2023-03-18T10:47:56.202331500Z", "LAX", "PH"))
                .isNull();
    }

    @Test
    @DisplayName("given valid flight details then return flight entity")
    void testFindByFlightDetails() {
        when(flightRepository.findByCarrierAndFlightNumberAndDepartureDateAndOriginAndDestination(any(), any(), any(), any(), any()))
                .thenReturn(new FlightEntity());
        assertThat(flightService.findByFlightDetails("PR", "123", "2023-03-18T10:47:56.202331500Z", "LAX", "PH"))
                .isNotNull();
    }

    @Test
    @DisplayName("given list of FlightStatus, when retrieved, then should return sorted list according to eventDate")
    void testReturnSortedListOfFlightStatus() {
        Long flightId = new Random().nextLong();
        FlightEntity flightEntity = new FlightEntity();
        flightEntity.setFlightId(flightId);
        List<FlightStatusEntity> flightStatusEntityList = new ArrayList<>();
        flightStatusEntityList.add(createFlightStatusEntity(flightId, "2023-03-18T10:47:56.202331500Z"));
        flightStatusEntityList.add(createFlightStatusEntity(flightId, "2023-03-18T16:47:56.202331500Z"));
        flightStatusEntityList.add(createFlightStatusEntity(flightId, "2023-02-25T16:47:56.202331500Z"));
        flightStatusEntityList.add(createFlightStatusEntity(flightId, "2023-03-30T16:47:56.202331500Z"));
        flightStatusEntityList.add(createFlightStatusEntity(flightId, "2023-03-08T16:47:56.202331500Z"));
        flightStatusEntityList.add(createFlightStatusEntity(flightId, "2023-01-31T16:47:56.202331500Z"));
        flightEntity.addAllFlightStatus(flightStatusEntityList);

        List<FlightStatusEntity> result = flightEntity.getFlightStatuses();
        assertThat(result).hasSameSizeAs(flightStatusEntityList);

        assertThat(result.get(0).getEventDate()).isEqualTo("2023-03-30T16:47:56.202331500Z");
        assertThat(result.get(1).getEventDate()).isEqualTo("2023-03-18T16:47:56.202331500Z");
        assertThat(result.get(2).getEventDate()).isEqualTo("2023-03-18T10:47:56.202331500Z");
        assertThat(result.get(3).getEventDate()).isEqualTo("2023-03-08T16:47:56.202331500Z");
        assertThat(result.get(4).getEventDate()).isEqualTo("2023-02-25T16:47:56.202331500Z");
        assertThat(result.get(5).getEventDate()).isEqualTo("2023-01-31T16:47:56.202331500Z");

    }

    private FlightStatusEntity createFlightStatusEntity(Long flightId, String evenDate) {
        FlightStatusEntity flightStatusEntity = new FlightStatusEntity();
        flightStatusEntity.setFlightId(flightId);
        flightStatusEntity.setEventDate(evenDate);
        return flightStatusEntity;
    }
}
