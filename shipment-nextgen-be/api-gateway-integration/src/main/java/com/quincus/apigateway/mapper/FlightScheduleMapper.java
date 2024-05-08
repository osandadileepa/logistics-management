package com.quincus.apigateway.mapper;

import com.quincus.apigateway.api.dto.APIGFlightSchedule;
import com.quincus.apigateway.api.dto.APIGFlightScheduleSearchParameter;
import com.quincus.apigateway.domain.FlightSchedule;
import com.quincus.apigateway.domain.FlightScheduleSearchParameter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface FlightScheduleMapper {

    APIGFlightScheduleSearchParameter mapDomainToDto(FlightScheduleSearchParameter domain);

    @Mapping(target = "departureTime", qualifiedByName = "offsetDateTime")
    @Mapping(target = "arrivalTime", qualifiedByName = "offsetDateTime")
    FlightSchedule mapDtoToDomain(APIGFlightSchedule dto);

    List<FlightSchedule> mapDtoListToDomainList(List<APIGFlightSchedule> dtoList);

    @Named("offsetDateTime")
    default OffsetDateTime offsetDateTimeStringToOffsetDateTime(String offsetDateTimeText) {
        return OffsetDateTime.parse(offsetDateTimeText, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
