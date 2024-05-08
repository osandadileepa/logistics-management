package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.FlightDetails;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.NotificationRequest;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class NotificationRequestValidator {

    private static final int LAST_COMMA_AND_SPACE_LENGTH = 2;

    public void validateShipmentReturnedParams(NotificationRequest notificationRequest) {
        StringBuilder missingFields = new StringBuilder();
        missingFields.append(validatePartnerIds(notificationRequest.getMilestone().getPartnerId(), notificationRequest.getShipment().getPartnerId()));
        missingFields.append(validateShipmentTrackingId(notificationRequest.getShipment()));
        missingFields.append(validateMilestoneDateTime(notificationRequest.getMilestone()));

        checkMissingFields(missingFields);
    }

    public void validateShipmentLostParams(NotificationRequest notificationRequest) {
        StringBuilder missingFields = new StringBuilder();
        missingFields.append(validatePartnerIds(notificationRequest.getMilestone().getPartnerId(), notificationRequest.getShipment().getPartnerId()));
        missingFields.append(validateShipmentTrackingId(notificationRequest.getShipment()));

        checkMissingFields(missingFields);
    }

    public void validateDeliverySuccessfulParams(NotificationRequest notificationRequest) {
        StringBuilder missingFields = new StringBuilder();
        missingFields.append(validatePartnerIds(notificationRequest.getMilestone().getPartnerId(), notificationRequest.getShipment().getPartnerId()));
        missingFields.append(validateShipmentTrackingId(notificationRequest.getShipment()));
        missingFields.append(validateOrderIdLabel(notificationRequest.getShipment()));
        missingFields.append(validateTrackingUrl(notificationRequest.getShipment()));
        missingFields.append(validateMilestoneDateTime(notificationRequest.getMilestone()));

        checkMissingFields(missingFields);
    }

    public void validatePickupSuccessfulParams(NotificationRequest notificationRequest) {
        validateDeliverySuccessfulParams(notificationRequest);
    }

    public void validateFlightDepartedParams(NotificationRequest notificationRequest) {
        StringBuilder missingFields = new StringBuilder();
        missingFields.append(validatePartnerIds(notificationRequest.getMilestone().getPartnerId(), notificationRequest.getShipment().getPartnerId()));
        missingFields.append(validateFlightDetails(notificationRequest.getFlight(), false));

        checkMissingFields(missingFields);
    }

    public void validateFlightArrivedParams(NotificationRequest notificationRequest) {
        StringBuilder missingFields = new StringBuilder();
        missingFields.append(validatePartnerIds(notificationRequest.getMilestone().getPartnerId(), notificationRequest.getShipment().getPartnerId()));
        missingFields.append(validateFlightDetails(notificationRequest.getFlight(), true));

        checkMissingFields(missingFields);
    }

    private String validateShipmentTrackingId(Shipment shipment) {
        return StringUtils.isEmpty(shipment.getShipmentTrackingId()) ? "Shipment tracking ID, " : StringUtils.EMPTY;
    }

    private String validateMilestoneDateTime(Milestone milestone) {
        return milestone.getMilestoneTime() == null ? "Milestone datetime, " : StringUtils.EMPTY;
    }

    private String validateOrderIdLabel(Shipment shipment) {
        return StringUtils.isEmpty(shipment.getOrder().getOrderIdLabel()) ? "Order ID, " : StringUtils.EMPTY;
    }

    private String validateTrackingUrl(Shipment shipment) {
        return StringUtils.isEmpty(shipment.getOrder().getTrackingUrl()) ? "Tracking URL, " : StringUtils.EMPTY;
    }

    private String validateFlightDetails(Flight flight, boolean isArrival) {
        StringBuilder missingFields = new StringBuilder();
        if (StringUtils.isEmpty(flight.getFlightNumber())) {
            missingFields.append("Flight number, ");
        }

        if (StringUtils.isEmpty(flight.getFlightStatus().getAirlineName())) {
            missingFields.append("Airline name, ");
        }

        FlightDetails detail = isArrival ? flight.getFlightStatus().getArrival() : flight.getFlightStatus().getDeparture();

        if (StringUtils.isEmpty(detail.getAirportName())) {
            missingFields.append(isArrival ? "Arrival airport name, " : "Departure airport name, ");
        }

        if (StringUtils.isEmpty(detail.getActualTime())) {
            missingFields.append(isArrival ? "Arrival time, " : "Departure time, ");
        }

        return missingFields.toString();
    }

    private void checkMissingFields(StringBuilder missingFields) {
        if (missingFields.length() > 0) {
            // Remove the last comma and space
            missingFields.setLength(missingFields.length() - LAST_COMMA_AND_SPACE_LENGTH);
            throw new QuincusValidationException("Missing parameters: " + missingFields);
        }
    }

    private String validatePartnerIds(String... partnerIds) {
        return Arrays.stream(partnerIds).noneMatch(StringUtils::isNotEmpty) ? "Partner Ids, " : StringUtils.EMPTY;
    }

}
