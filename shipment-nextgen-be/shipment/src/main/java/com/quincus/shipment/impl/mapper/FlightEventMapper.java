package com.quincus.shipment.impl.mapper;

import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.constant.FlightEventType;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.dto.FlightStatsRequest;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightEventPayloadMessage;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", imports = {DateTimeUtil.class, FlightEventName.class, FlightEventType.class}, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface FlightEventMapper {
    @Mapping(target = "eventName", expression = "java(eventPayload.getEventName() != null ? FlightEventName.valueOf(eventPayload.getEventName()) : null)")
    @Mapping(target = "eventType", expression = "java(eventPayload.getEventType() != null ? FlightEventType.valueOf(eventPayload.getEventType()) : null)")
    @Mapping(target = "flightId", expression = "java(eventPayload.getFlightId() != null ? Long.parseLong(eventPayload.getFlightId()) : null)")
    @Mapping(source = "flightStatus.departureAirportCode", target = "flightStatus.departure.airportCode")
    @Mapping(source = "flightStatus.departureAirportName", target = "flightStatus.departure.airportName")
    @Mapping(source = "flightStatus.scheduledDeparture", target = "flightStatus.departure.scheduledTime", qualifiedByName = "toCustomDateTimeFormat")
    @Mapping(source = "flightStatus.estimatedDeparture", target = "flightStatus.departure.estimatedTime", qualifiedByName = "toCustomDateTimeFormat")
    @Mapping(source = "flightStatus.actualDeparture", target = "flightStatus.departure.actualTime", qualifiedByName = "toCustomDateTimeFormat")
    @Mapping(source = "flightStatus.arrivalAirportCode", target = "flightStatus.arrival.airportCode")
    @Mapping(source = "flightStatus.arrivalAirportName", target = "flightStatus.arrival.airportName")
    @Mapping(source = "flightStatus.scheduledArrival", target = "flightStatus.arrival.scheduledTime", qualifiedByName = "toCustomDateTimeFormat")
    @Mapping(source = "flightStatus.estimatedArrival", target = "flightStatus.arrival.estimatedTime", qualifiedByName = "toCustomDateTimeFormat")
    @Mapping(source = "flightStatus.actualArrival", target = "flightStatus.arrival.actualTime", qualifiedByName = "toCustomDateTimeFormat")
    Flight mapFlightEventPayloadMessageToFlight(FlightEventPayloadMessage eventPayload);

    @Mapping(source = "airlineCode", target = "carrier")
    @Mapping(target = "departureDate", expression = "java(DateTimeUtil.convertToTargetZoneDateTime(segmentEntity.getDepartureTime(), segmentEntity.getDepartureTimezone()).toLocalDate())")
    @Mapping(source = "flightOrigin", target = "origin")
    @Mapping(source = "flightDestination", target = "destination")
    FlightStatsRequest mapPackageJourneySegmentToFlightStatsRequest(PackageJourneySegmentEntity segmentEntity);

    @Named("toCustomDateTimeFormat")
    default String toCustomDateTimeFormat(String datetimeString) {
        if (StringUtils.isBlank(datetimeString)) {
            return null;
        }
        return DateTimeUtil.toIsoDateTimeFormat(datetimeString);
    }
}