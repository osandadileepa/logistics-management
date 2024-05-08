package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.constant.FlightStatsEventType;
import com.quincus.shipment.api.dto.FlightStatsRequest;
import com.quincus.shipment.kafka.producers.MessageService;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightEventPayloadMessage;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightStatsMessage;
import com.quincus.shipment.kafka.producers.utility.ProducerUtil;
import org.springframework.stereotype.Component;

@Component
public class FlightStatsMessageMapperImpl implements FlightStatsMessageMapper {

    @Override
    public FlightStatsMessage mapToFlightStatsMessage(FlightEventPayloadMessage eventPayload, String uuid) {
        if (eventPayload == null) {
            return null;
        }

        FlightStatsMessage message = new FlightStatsMessage();
        message.setEventId(uuid);
        message.setCorrelationId(uuid);
        message.setEventDateUtc(ProducerUtil.localToUTC());
        message.setModule(MessageService.DEFAULT_MDC_KEY);
        message.setEventType(FlightStatsEventType.FLIGHT_SUBSCRIBE_RQ.toString());
        message.setEventPayload(eventPayload);

        return message;
    }

    @Override
    public FlightEventPayloadMessage mapFlightToEventPayloadMessage(FlightStatsRequest flight) {

        FlightEventPayloadMessage message = new FlightEventPayloadMessage();
        message.setCarrier(flight.getCarrier());
        message.setFlightNumber(flight.getFlightNumber());
        message.setDepartureDate(flight.getDepartureDate().toString());
        message.setOrigin(flight.getOrigin());
        message.setDestination(flight.getDestination());

        return message;
    }
}
