package com.quincus.apigateway.mapper;

import com.quincus.apigateway.api.dto.APIGFlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class FlightScheduleMapperImplTest {

    public static final String CARRIER_MISMATCH = "Carrier mismatch.";
    public static final String CARRIER_NAME_MISMATCH = "Carrier Name mismatch.";
    public static final String FLIGHT_NUMBER_MISMATCH = "Flight Number mismatch.";
    public static final String DEPARTURE_TIME_MISMATCH = "Departure Time mismatch.";
    public static final String ARRIVAL_TIME_MISMATCH = "Arrival Time mismatch.";
    public static final String CARRIER_MISMATCH1 = CARRIER_MISMATCH;
    private final FlightScheduleMapper mapper = Mappers.getMapper(FlightScheduleMapper.class);

    private static void assertLocalDateFromText(LocalDate expectedDate, String actualDateText) {
        LocalDate actualDate = LocalDate.parse(actualDateText, DateTimeFormatter.ISO_DATE);
        assertThat(actualDate).isNotNull().withFailMessage("Departure Date mismatch.").isEqualTo(expectedDate);
    }

    private static void assertOffsetDateTimeFromText(String expectedDateTimeText, OffsetDateTime actualDateTime,
                                                     String assertFailMessage) {
        OffsetDateTime expectedDateTime = OffsetDateTime.parse(expectedDateTimeText,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        assertThat(actualDateTime).isNotNull().withFailMessage(assertFailMessage).isEqualTo(expectedDateTime);
    }

    @Test
    void mapDomainToDtoFlightScheduleSearchParameterDomainShouldReturnDto() {
        var domain = new FlightScheduleSearchParameter();
        domain.setOrigin("MNL");
        domain.setDestination("SIN");
        domain.setDepartureDate(LocalDate.now());
        domain.setCarrier("PR");

        final var dto = mapper.mapDomainToDto(domain);

        assertThat(dto.getOrigin()).isEqualTo(domain.getOrigin());
        assertThat(dto.getDestination()).isEqualTo(domain.getDestination());
        assertLocalDateFromText(domain.getDepartureDate(), dto.getDepartureDate());
        assertThat(dto.getCarrier()).withFailMessage(CARRIER_MISMATCH).isEqualTo(domain.getCarrier());
    }

    @Test
    void mapDtoToDomainFlightScheduleDtoShouldReturnDomain() {
        var dto = new APIGFlightSchedule();
        dto.setCarrier("PR");
        dto.setCarrierName("Philippine Airlines");
        dto.setFlightNumber("509");
        dto.setOrigin("MNL");
        dto.setDepartureTime("2023-01-05T19:40:00.000+08:00");
        dto.setDestination("SIN");
        dto.setArrivalTime("2023-01-05T23:20:00.000+08:00");
        dto.setEquipment("321");
        dto.setDepartureTerminal("2");
        dto.setServiceType("J");

        final var domain = mapper.mapDtoToDomain(dto);

        assertThat(domain.getCarrier()).withFailMessage(CARRIER_MISMATCH1).isEqualTo(dto.getCarrier());
        assertThat(domain.getCarrierName()).withFailMessage(CARRIER_NAME_MISMATCH).isEqualTo(dto.getCarrierName());
        assertThat(domain.getFlightNumber()).withFailMessage(FLIGHT_NUMBER_MISMATCH).isEqualTo(dto.getFlightNumber());

        assertOffsetDateTimeFromText(dto.getDepartureTime(), domain.getDepartureTime(), DEPARTURE_TIME_MISMATCH);
        assertOffsetDateTimeFromText(dto.getArrivalTime(), domain.getArrivalTime(), ARRIVAL_TIME_MISMATCH);
    }

    @Test
    void mapDtoListToDomainListFlightScheduleDtoListShouldReturnDomainList() {
        var dto1 = new APIGFlightSchedule();
        dto1.setCarrier("PR");
        dto1.setCarrierName("Philippine Airlines");
        dto1.setFlightNumber("509");
        dto1.setOrigin("MNL");
        dto1.setDepartureTime("2023-01-05T19:40:00.000+08:00");
        dto1.setDestination("SIN");
        dto1.setArrivalTime("2023-01-05T23:20:00.000+08:00");
        dto1.setEquipment("321");
        dto1.setDepartureTerminal("2");
        dto1.setServiceType("J");

        var dto2 = new APIGFlightSchedule();
        dto2.setCarrier("NZ");
        dto2.setCarrierName("Air New Zealand");
        dto2.setFlightNumber("3441");
        dto2.setOrigin("SIN");
        dto2.setDepartureTime("2023-01-05T19:00:00+08:00");
        dto2.setDestination("MNL");
        dto2.setArrivalTime("2023-01-05T22:45:00+08:00");
        dto2.setEquipment("123");
        dto2.setDepartureTerminal("1");
        dto2.setServiceType("K");

        final var domainList = mapper.mapDtoListToDomainList(List.of(dto1, dto2));

        assertThat(domainList).hasSize(2);

        var domain = domainList.get(0);
        assertThat(domain.getCarrier()).withFailMessage(CARRIER_MISMATCH1).isEqualTo(dto1.getCarrier());
        assertThat(domain.getCarrierName()).withFailMessage(CARRIER_NAME_MISMATCH).isEqualTo(dto1.getCarrierName());
        assertThat(domain.getFlightNumber()).withFailMessage(FLIGHT_NUMBER_MISMATCH).isEqualTo(dto1.getFlightNumber());


        assertOffsetDateTimeFromText(dto1.getDepartureTime(), domain.getDepartureTime(), DEPARTURE_TIME_MISMATCH);
        assertOffsetDateTimeFromText(dto1.getArrivalTime(), domain.getArrivalTime(), ARRIVAL_TIME_MISMATCH);

        domain = domainList.get(1);
        assertThat(domain.getCarrier()).withFailMessage(CARRIER_MISMATCH1).isEqualTo(dto2.getCarrier());
        assertThat(domain.getCarrierName()).withFailMessage(CARRIER_NAME_MISMATCH).isEqualTo(dto2.getCarrierName());
        assertThat(domain.getFlightNumber()).withFailMessage(FLIGHT_NUMBER_MISMATCH).isEqualTo(dto2.getFlightNumber());
        assertOffsetDateTimeFromText(dto2.getDepartureTime(), domain.getDepartureTime(), DEPARTURE_TIME_MISMATCH);
        assertOffsetDateTimeFromText(dto2.getArrivalTime(), domain.getArrivalTime(), ARRIVAL_TIME_MISMATCH);
    }
}
