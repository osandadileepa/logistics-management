package com.quincus.shipment.impl.service;

import com.quincus.ext.DateTimeUtil;
import com.quincus.qportal.model.QPortalNotificationRequest;
import com.quincus.qportal.model.QPortalOperator;
import com.quincus.qportal.model.QPortalOperatorUser;
import com.quincus.qportal.model.QPortalParam;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.domain.Consignee;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.Sender;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.domain.User;
import com.quincus.shipment.api.dto.NotificationRequest;
import com.quincus.shipment.impl.validator.NotificationRequestValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.quincus.shipment.api.constant.MilestoneCode.DSP_DELIVERY_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.DSP_PICKUP_SUCCESSFUL;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_FLIGHT_ARRIVED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_FLIGHT_DEPARTED;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_LOST;
import static com.quincus.shipment.api.constant.MilestoneCode.SHP_RETURNED;

/**
 * This service class encapsulates the communication with the QPortal API.
 * It handles various types of shipment notifications such as order cancellation,
 * shipment loss, return, delivery success, etc.
 * <p>
 * Each type of notification is processed by a specific handler,
 * which is selected based on the milestone code of the shipment.
 * <p>
 * After processing, the service sends a request to the QPortal API.
 * QPortal in turn internally communicates with the <b>QComms API</b> to complete the notification process.
 */
@Service
@Slf4j
public class QPortalNotificationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String READABLE_DATE_FORMAT = "%s (UTC %s)";

    private final Map<MilestoneCode, Consumer<NotificationRequest>> notificationHandlers;

    private final QPortalService qPortalService;

    private final NotificationRequestValidator validator;

    public QPortalNotificationService(QPortalService qPortalService, NotificationRequestValidator validator) {
        this.qPortalService = qPortalService;
        this.notificationHandlers = initNotificationHandlers();
        this.validator = validator;
    }

    private Map<MilestoneCode, Consumer<NotificationRequest>> initNotificationHandlers() {
        Map<MilestoneCode, Consumer<NotificationRequest>> handlers = new EnumMap<>(MilestoneCode.class);
        handlers.put(SHP_LOST, this::handleShipmentLost);
        handlers.put(SHP_RETURNED, this::handleShipmentReturned);
        handlers.put(DSP_PICKUP_SUCCESSFUL, this::handlePickupSuccessful);
        handlers.put(DSP_DELIVERY_SUCCESSFUL, this::handleDeliverySuccessful);
        handlers.put(SHP_FLIGHT_DEPARTED, this::handleFlightDeparted);
        handlers.put(SHP_FLIGHT_ARRIVED, this::handleFlightArrived);
        return handlers;
    }

    public void sendNotification(@NonNull NotificationRequest notificationRequest) {
        MilestoneCode milestoneCode = notificationRequest.getMilestone().getMilestoneCode();
        try {
            Consumer<NotificationRequest> handler = notificationHandlers.get(milestoneCode);
            if (handler == null) {
                log.debug("Notification trigger for QPortal Milestone Notification is not required for milestone code: `{}`", milestoneCode);
                return;
            }
            handler.accept(notificationRequest);
        } catch (Exception e) {
            log.error("Failed to send milestone notification for milestone code: `{}` due to exception: {}", milestoneCode, e.getMessage());
        }
    }

    private void handleShipmentLost(NotificationRequest notificationRequest) {
        validator.validateShipmentLostParams(notificationRequest);
        Milestone milestone = notificationRequest.getMilestone();
        Shipment shipment = notificationRequest.getShipment();
        QPortalNotificationRequest qPortalNotificationRequest = buildBaseRequest(milestone, shipment);
        qPortalNotificationRequest.setParams(QPortalParam.builder()
                .shipmentId(shipment.getShipmentTrackingId())
                .orderId(shipment.getOrder().getOrderIdLabel())
                .orderIdLabel(shipment.getOrder().getOrderIdLabel())
                .trackingUrl(shipment.getOrder().getTrackingUrl())
                .build());
        log.info("Sending notification for Shipment Lost with request `{}` ", qPortalNotificationRequest);
        qPortalService.sendNotification(notificationRequest.getOrganizationId(), qPortalNotificationRequest);
    }

    private void handleShipmentReturned(NotificationRequest notificationRequest) {
        validator.validateShipmentReturnedParams(notificationRequest);
        Milestone milestone = notificationRequest.getMilestone();
        Shipment shipment = notificationRequest.getShipment();
        QPortalNotificationRequest qPortalNotificationRequest = buildBaseRequest(milestone, shipment);
        qPortalNotificationRequest.setParams(QPortalParam.builder()
                .orderId(shipment.getOrder().getOrderIdLabel())
                .orderIdLabel(shipment.getOrder().getOrderIdLabel())
                .trackingUrl(shipment.getOrder().getTrackingUrl())
                .shipmentId(shipment.getShipmentTrackingId())
                .milestoneDatetime(formatDateAsReadable(milestone.getMilestoneTime().toString()))
                .build());
        log.info("Sending notification for Shipment Returned with request `{}` ", qPortalNotificationRequest);
        qPortalService.sendNotification(notificationRequest.getOrganizationId(), qPortalNotificationRequest);
    }

    private void handleDeliverySuccessful(NotificationRequest notificationRequest) {
        validator.validateDeliverySuccessfulParams(notificationRequest);
        Milestone milestone = notificationRequest.getMilestone();
        Shipment shipment = notificationRequest.getShipment();
        List<PackageJourneySegment> packageJourneySegments = shipment.getShipmentJourney().getPackageJourneySegments();
        if (!packageJourneySegments.isEmpty()) {
            PackageJourneySegment lastSegment = packageJourneySegments.get(packageJourneySegments.size() - 1);
            if (!StringUtils.equals(lastSegment.getSegmentId(), milestone.getSegmentId())) {
                log.warn("Last segment id `{}` is not related to the received milestone's segment id `{}`", lastSegment.getSegmentId(), milestone.getSegmentId());
                return;
            }
            QPortalNotificationRequest qPortalNotificationRequest = buildBaseRequest(milestone, shipment);
            qPortalNotificationRequest.setParams(QPortalParam.builder()
                    .orderId(shipment.getOrder().getOrderIdLabel())
                    .orderIdLabel(shipment.getOrder().getOrderIdLabel())
                    .trackingUrl(shipment.getOrder().getTrackingUrl())
                    .shipmentId(shipment.getShipmentTrackingId())
                    .milestoneDatetime(formatDateAsReadable(milestone.getMilestoneTime().toString()))
                    .build());
            log.info("Sending notification for Delivery Successful with request `{}`", qPortalNotificationRequest);
            qPortalService.sendNotification(notificationRequest.getOrganizationId(), qPortalNotificationRequest);
        }
    }

    private void handlePickupSuccessful(NotificationRequest notificationRequest) {
        validator.validatePickupSuccessfulParams(notificationRequest);
        Milestone milestone = notificationRequest.getMilestone();
        Shipment shipment = notificationRequest.getShipment();
        List<PackageJourneySegment> packageJourneySegments = shipment.getShipmentJourney().getPackageJourneySegments();
        if (!packageJourneySegments.isEmpty()) {
            PackageJourneySegment firstSegment = packageJourneySegments.get(0);
            if (!StringUtils.equals(firstSegment.getSegmentId(), milestone.getSegmentId())) {
                log.warn("First segment id `{}` is not related to the received milestone's segment id `{}`", firstSegment.getSegmentId(), milestone.getSegmentId());
                return;
            }
            QPortalNotificationRequest qPortalNotificationRequest = buildBaseRequest(milestone, shipment);
            qPortalNotificationRequest.setParams(QPortalParam.builder()
                    .orderId(shipment.getOrder().getOrderIdLabel())
                    .orderIdLabel(shipment.getOrder().getOrderIdLabel())
                    .trackingUrl(shipment.getOrder().getTrackingUrl())
                    .shipmentId(shipment.getShipmentTrackingId())
                    .milestoneDatetime(formatDateAsReadable(milestone.getMilestoneTime().toString()))
                    .build());
            log.info("Sending notification for Pickup Successful with request `{}`", qPortalNotificationRequest);
            qPortalService.sendNotification(notificationRequest.getOrganizationId(), qPortalNotificationRequest);
        }
    }

    private void handleFlightDeparted(NotificationRequest notificationRequest) {
        validator.validateFlightDepartedParams(notificationRequest);
        Milestone milestone = notificationRequest.getMilestone();
        Shipment shipment = notificationRequest.getShipment();
        Flight flight = notificationRequest.getFlight();
        QPortalNotificationRequest qPortalNotificationRequest = buildBaseRequest(milestone, shipment);
        qPortalNotificationRequest.setParams(QPortalParam.builder()
                .orderId(shipment.getOrder().getOrderIdLabel())
                .orderIdLabel(shipment.getOrder().getOrderIdLabel())
                .trackingUrl(shipment.getOrder().getTrackingUrl())
                .airlineCode(flight.getFlightStatus().getAirlineCode())
                .flightNumber(flight.getFlightStatus().getAirlineCode() + flight.getFlightNumber())
                .airline(flight.getFlightStatus().getAirlineName())
                .facilityName(flight.getFlightStatus().getDeparture().getAirportName())
                .departureDateTime(formatDateAsReadable(flight.getFlightStatus().getDeparture().getActualTime()))
                .build());
        log.info("Sending notification for Flight Departed with request `{}`", qPortalNotificationRequest);
        qPortalService.sendNotification(notificationRequest.getOrganizationId(), qPortalNotificationRequest);
    }

    private void handleFlightArrived(NotificationRequest notificationRequest) {
        validator.validateFlightArrivedParams(notificationRequest);
        Milestone milestone = notificationRequest.getMilestone();
        Shipment shipment = notificationRequest.getShipment();
        Flight flight = notificationRequest.getFlight();
        QPortalNotificationRequest qPortalNotificationRequest = buildBaseRequest(milestone, shipment);
        qPortalNotificationRequest.setParams(QPortalParam.builder()
                .orderId(shipment.getOrder().getOrderIdLabel())
                .orderIdLabel(shipment.getOrder().getOrderIdLabel())
                .trackingUrl(shipment.getOrder().getTrackingUrl())
                .airlineCode(flight.getFlightStatus().getAirlineCode())
                .flightNumber(flight.getFlightStatus().getAirlineCode() + flight.getFlightNumber())
                .airline(flight.getFlightStatus().getAirlineName())
                .facilityName(flight.getFlightStatus().getArrival().getAirportName())
                .arrivalDateTime(formatDateAsReadable(flight.getFlightStatus().getArrival().getActualTime()))
                .build());
        log.info("Sending notification for Flight Arrived with request `{}`", qPortalNotificationRequest);
        qPortalService.sendNotification(notificationRequest.getOrganizationId(), qPortalNotificationRequest);
    }

    private QPortalNotificationRequest buildBaseRequest(Milestone milestone, Shipment shipment) {
        Set<String> partnerIds = getAllPartnerIds(milestone, shipment);

        QPortalOperatorUser senderUser = Optional.ofNullable(shipment.getSender())
                .map(sender -> createQPortalOperatorUser(sender.getEmail(), formatSenderPhoneNumber(sender)))
                .orElse(null);

        QPortalOperatorUser consigneeUser = Optional.ofNullable(shipment.getConsignee())
                .map(consignee -> createQPortalOperatorUser(consignee.getEmail(), formatConsigneePhoneNumber(consignee)))
                .orElse(null);

        QPortalOperatorUser drivers = createUserDriver(milestone, shipment);

        QPortalOperator operator = new QPortalOperator();
        operator.setSenders(senderUser);
        operator.setConsignees(consigneeUser);
        operator.setDrivers(drivers);

        return new QPortalNotificationRequest(milestone.getMilestoneCode().toString(), partnerIds, operator);
    }

    private QPortalOperatorUser createUserDriver(Milestone milestone, Shipment shipment) {
        String driverEmail = milestone.getDriverEmail();
        String driverPhoneNumber = milestone.getDriverPhoneNumber();
        String driverPhoneCode = milestone.getDriverPhoneCode();
        String driverFullPhoneNumber = formatDriverPhoneNumber(milestone);
        boolean isInvalidDriverFullPhoneNumber = StringUtils.isBlank(driverPhoneCode) || StringUtils.isBlank(driverPhoneNumber);

        if (StringUtils.isBlank(driverEmail) || isInvalidDriverFullPhoneNumber) {
            String organizationId = Optional.ofNullable(shipment.getOrganization()).map(Organization::getId).orElse(milestone.getOrganizationId());
            User userDriver = getUserWithoutCache(milestone.getId(), organizationId, milestone.getDriverId());
            driverEmail = StringUtils.defaultIfBlank(driverEmail, userDriver.getEmail());
            if (isInvalidDriverFullPhoneNumber) {
                driverFullPhoneNumber = userDriver.getFullPhoneNumber();
            }
        }
        
        return createQPortalOperatorUser(driverEmail, driverFullPhoneNumber);
    }

    private User getUserWithoutCache(String milestoneId, String organizationId, String driverId) {
        if (StringUtils.isBlank(driverId)) {
            log.warn("No driver id for milestone id: `{}` with organization :`{}`. Skipping the driver details enrichment", milestoneId, organizationId);
            return new User();
        }
        return Optional.ofNullable(qPortalService.getUserWithoutCache(organizationId, driverId)).orElse(new User());
    }

    private Set<String> getAllPartnerIds(Milestone milestone, Shipment shipment) {
        Set<String> allUniquePartnerIds = new HashSet<>();

        Optional.ofNullable(shipment.getShipmentJourney())
                .map(ShipmentJourney::getPackageJourneySegments)
                .orElse(Collections.emptyList())
                .stream()
                .map(PackageJourneySegment::getPartner)
                .filter(Objects::nonNull)
                .map(Partner::getId)
                .filter(Objects::nonNull)
                .forEach(allUniquePartnerIds::add);

        Stream.of(Optional.ofNullable(milestone.getPartnerId()), Optional.ofNullable(shipment.getPartnerId()))
                .flatMap(Optional::stream)
                .forEach(allUniquePartnerIds::add);

        return allUniquePartnerIds;
    }


    private String formatDriverPhoneNumber(Milestone milestone) {
        return formatContactNumber(milestone.getDriverPhoneCode(), milestone.getDriverPhoneNumber());
    }

    private String formatSenderPhoneNumber(Sender sender) {
        return formatContactNumber(sender.getContactCode(), sender.getContactNumber());
    }

    private String formatConsigneePhoneNumber(Consignee consignee) {
        return formatContactNumber(consignee.getContactCode(), consignee.getContactNumber());
    }

    private String formatContactNumber(String code, String number) {
        return Optional.ofNullable(code).orElse(StringUtils.EMPTY) + Optional.ofNullable(number).orElse(StringUtils.EMPTY);
    }

    private QPortalOperatorUser createQPortalOperatorUser(String email, String mobileNumber) {
        List<String> emailList = Optional.ofNullable(email)
                .map(Collections::singletonList)
                .orElse(null);

        List<String> mobileList = Optional.ofNullable(mobileNumber)
                .map(Collections::singletonList)
                .orElse(null);

        return new QPortalOperatorUser(emailList, mobileList);
    }

    private String formatDateAsReadable(String date) {
        OffsetDateTime offsetDateTime = DateTimeUtil.parseOffsetDateTime(date);

        if (offsetDateTime != null) {
            ZoneOffset zoneOffset = offsetDateTime.getOffset();
            String formattedDateTime = DATE_TIME_FORMATTER.format(offsetDateTime);
            return String.format(READABLE_DATE_FORMAT, formattedDateTime, zoneOffset);
        }

        return date;
    }
}
