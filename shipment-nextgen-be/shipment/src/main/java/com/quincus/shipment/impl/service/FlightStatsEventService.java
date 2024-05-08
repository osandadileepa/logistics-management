package com.quincus.shipment.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.MessageApi;
import com.quincus.shipment.api.constant.FlightEventName;
import com.quincus.shipment.api.constant.FlightEventType;
import com.quincus.shipment.api.constant.FlightStatusResult;
import com.quincus.shipment.api.constant.FlightSubscriptionStatus;
import com.quincus.shipment.api.constant.SegmentStatus;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.FlightStatus;
import com.quincus.shipment.api.exception.FlightStatsMessageException;
import com.quincus.shipment.impl.mapper.FlightEventMapper;
import com.quincus.shipment.impl.mapper.FlightStatusMapper;
import com.quincus.shipment.impl.repository.PackageJourneySegmentRepository;
import com.quincus.shipment.impl.repository.entity.FlightEntity;
import com.quincus.shipment.impl.repository.entity.FlightStatusEntity;
import com.quincus.shipment.impl.repository.entity.PackageJourneySegmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.kafka.producers.message.flightstats.FlightStatsMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.StaleStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class FlightStatsEventService {
    private static final String FLIGHT_NOT_FOUND = "Flight not found";
    private final PackageJourneySegmentRepository packageJourneySegmentRepository;
    private final ObjectMapper objectMapper;
    private final PackageJourneySegmentService packageJourneySegmentService;
    private final FlightService flightService;
    private final FlightEventMapper flightEventMapper;
    private final AlertService alertService;
    private final FlightStatusMapper flightStatusMapper;
    private final ShipmentFetchService shipmentFetchService;
    private final FlightStatsEventPostProcessService flightStatsEventPostProcessService;
    private final MessageApi messageApi;

    public void subscribeFlight(List<PackageJourneySegmentEntity> segments) {
        segments.stream().filter(this::hasCompleteFlightDetails).forEach(this::subscribeAndUpdate);
    }

    @Transactional
    @Retryable(value = {StaleStateException.class, ObjectOptimisticLockingFailureException.class}, maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.maxDelayStaleException}"))
    public FlightStatusResult processFlightStatsMessage(String message, String transactionId) {
        return processSegmentsWithFlight(toFlight(message, transactionId));
    }

    private FlightStatusResult processSegmentsWithFlight(Flight flight) {
        FlightStatusResult flightStatusResult = new FlightStatusResult();
        FlightEntity flightEntity = flightService.createOrUpdate(flight);
        List<PackageJourneySegmentEntity> affectedAirSegments = getAffectedAirSegmentsByFlightDetails(flight);
        log.debug("Processing `{}` segments for flight ID: `{}`", affectedAirSegments.size(), flight.getFlightId());
        List<PackageJourneySegmentEntity> toUpdateAirSegments = new ArrayList<>();
        for (PackageJourneySegmentEntity airSegment : affectedAirSegments) {
            log.debug("Updating flight information for segment with ID: `{}`", airSegment.getId());
            updateAirSegmentWithFlightDetails(airSegment, flightEntity, flight, flightStatusResult);
            if (flight.getFlightStatus().getEventName() == FlightEventName.CANCELLED) {
                log.debug("Flight with ID: `{}` has been cancelled. Generating cancellation alert for segment with ID: `{}`", flight.getFlightId(), airSegment.getId());
                flightStatusResult.addAlert(alertService.createFlightCancellationAlert(airSegment));
                flightStatusResult.addAlert(alertService.createFlightCancellationShipmentJourneyAlert(airSegment));

            }
            toUpdateAirSegments.add(airSegment);
        }
        packageJourneySegmentRepository.saveAllAndFlush(toUpdateAirSegments);
        log.debug("Finished processing `{}` segments for flight ID: `{}`", affectedAirSegments.size(), flight.getFlightId());
        return flightStatusResult;
    }

    private List<PackageJourneySegmentEntity> getAffectedAirSegmentsByFlightDetails(Flight flight) {
        if (flight.getEventType() == FlightEventType.FLIGHT_SUBSCRIBE_RS || flight.getEventType() == FlightEventType.FLIGHT_SUBSCRIBE_RQ) {
            return findSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull(flight);
        }
        return findSegmentsWithFlightDetails(flight);
    }

    private List<PackageJourneySegmentEntity> findSegmentsWithFlightDetails(Flight flight) {
        return packageJourneySegmentService.findSegmentsWithFlightDetails(flight.getCarrier(),
                flight.getFlightNumber(), flight.getDepartureDate(), flight.getOrigin(),
                flight.getDestination(), flight.getFlightId());
    }

    private List<PackageJourneySegmentEntity> findSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull(Flight flight) {
        return packageJourneySegmentService.findSegmentsWithFlightDetailsAndSubscriptionStatusIsRequestSentOrNull(flight.getCarrier(),
                flight.getFlightNumber(), flight.getDepartureDate(), flight.getOrigin(),
                flight.getDestination());
    }

    public Flight toFlight(String message, String messageTransactionId) {
        try {
            FlightStatsMessage flightStatsMessage = objectMapper.readValue(message, FlightStatsMessage.class);
            Flight flight = flightEventMapper.mapFlightEventPayloadMessageToFlight(flightStatsMessage.getEventPayload());
            flight.setEventType(flightStatsMessage.getEventType() != null ? FlightEventType.valueOf(flightStatsMessage.getEventType()) : null);
            flight.setEventDate(flightStatsMessage.getEventDateUtc());
            FlightStatus flightStatus = isNull(flight.getFlightStatus()) ? flightStatusMapper.mapFlightToFlightStatus(flight) : flight.getFlightStatus();
            if (flightStatus != null && flightStatus.getFlightId() != null) {
                flightStatus.setEventType(flight.getEventType());
                flightStatus.setEventDate(flight.getEventDate());
                flightStatus.setEventName(flight.getEventName());
                flightStatus.setSuccess(flight.isSuccess());
                flightStatus.setError(flight.getError());
            }
            flight.setFlightStatus(flightStatus);
            return flight;
        } catch (JsonProcessingException e) {
            throw new FlightStatsMessageException(String.format("Failed to convert flight stats message. Error: %s", message), messageTransactionId);
        }
    }

    private void subscribeAndUpdate(PackageJourneySegmentEntity segmentEntity) {
        FlightEntity flightEntity = flightService.findByFlightDetails(segmentEntity.getAirlineCode(), segmentEntity.getFlightNumber(),
                segmentEntity.getDepartureTime(), segmentEntity.getFlightOrigin(), segmentEntity.getFlightDestination());
        if (flightEntity == null) {
            messageApi.subscribeFlight(flightEventMapper.mapPackageJourneySegmentToFlightStatsRequest(segmentEntity));
            segmentEntity.setFlightSubscriptionStatus(FlightSubscriptionStatus.REQUEST_SENT);
            return;
        }
        FlightStatusEntity flightStatus = CollectionUtils.isEmpty(flightEntity.getFlightStatuses()) ? null : flightEntity.getFlightStatuses().get(0);
        if (nonNull(flightStatus)) {
            segmentEntity.setAirlineCode(flightStatus.getAirlineCode());
            segmentEntity.setAirline(flightStatus.getAirlineName());
        }
        segmentEntity.setFlight(flightEntity);
    }

    private boolean hasCompleteFlightDetails(PackageJourneySegmentEntity segment) {
        return segment.getTransportType() == TransportType.AIR && segment.getFlight() == null
                && StringUtils.isNotBlank(segment.getAirlineCode())
                && StringUtils.isNotBlank(segment.getFlightNumber())
                && StringUtils.isNotBlank(segment.getDepartureTime())
                && StringUtils.isNotBlank(segment.getFlightOrigin())
                && StringUtils.isNotBlank(segment.getFlightDestination());
    }

    private void updateAirSegmentWithFlightDetails(PackageJourneySegmentEntity segmentEntity, FlightEntity flightEntity, Flight flight, FlightStatusResult flightStatusResult) {
        if (flight.getEventType() == FlightEventType.FLIGHT_SUBSCRIBE_RS) {
            if (!flight.isSuccess() || StringUtils.isNotBlank(flight.getError()) && StringUtils.equalsIgnoreCase(flight.getError(), FLIGHT_NOT_FOUND)) {
                segmentEntity.setFlightSubscriptionStatus(FlightSubscriptionStatus.FLIGHT_NOT_FOUND);
                flightStatusResult.addAlert(alertService.createFlightNotFoundAlert(segmentEntity.getId(), segmentEntity.getSequence()));
                flightStatusResult.addAlert(alertService.createFlightNotFoundJourneyAlert(segmentEntity.getShipmentJourneyId(), segmentEntity.getSequence()));
                return;
            }
            if (flight.isSuccess() && StringUtils.isBlank(flight.getError()) && flight.getFlightId() != null) {
                segmentEntity.setFlightSubscriptionStatus(FlightSubscriptionStatus.SUBSCRIBED);
                segmentEntity.setFlight(flightEntity);
                return;
            }
        }

        List<ShipmentEntity> shipmentEntities = getRelatedShipmentsForSegment(segmentEntity);
        subscribeAirSegment(segmentEntity, flightEntity, flight);
        flightStatsEventPostProcessService.processFlightStats(shipmentEntities, segmentEntity, flight);
    }

    private void subscribeAirSegment(PackageJourneySegmentEntity segmentEntity, FlightEntity flightEntity, Flight flight) {
        FlightStatus flightStatus = flight.getFlightStatus();
        segmentEntity.setAirline(flightStatus.getAirlineName());
        segmentEntity.setAirlineCode(flightStatus.getAirlineCode());
        segmentEntity.setFlightNumber(flightEntity.getFlightNumber());
        segmentEntity.setFlight(flightEntity);
        segmentEntity.setFlightSubscriptionStatus(FlightSubscriptionStatus.SUBSCRIBED);
        if (FlightEventName.FLIGHT_DEPARTED == flightStatus.getEventName()) {
            segmentEntity.setStatus(SegmentStatus.IN_PROGRESS);
        } else if (FlightEventName.FLIGHT_LANDED == flightStatus.getEventName()) {
            segmentEntity.setStatus(SegmentStatus.COMPLETED);
        }
    }

    private List<ShipmentEntity> getRelatedShipmentsForSegment(PackageJourneySegmentEntity segmentEntity) {
        return shipmentFetchService.findByJourneyIdOrThrowException(segmentEntity.getShipmentJourneyId());
    }
}
