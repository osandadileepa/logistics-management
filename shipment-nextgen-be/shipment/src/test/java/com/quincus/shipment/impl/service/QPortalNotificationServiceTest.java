package com.quincus.shipment.impl.service;

import com.quincus.ext.DateTimeUtil;
import com.quincus.qportal.model.QPortalNotificationRequest;
import com.quincus.qportal.model.QPortalParam;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.domain.Consignee;
import com.quincus.shipment.api.domain.Driver;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.FlightDetails;
import com.quincus.shipment.api.domain.FlightStatus;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Partner;
import com.quincus.shipment.api.domain.Sender;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.NotificationRequest;
import com.quincus.shipment.impl.validator.NotificationRequestValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QPortalNotificationServiceTest {
    private final ArgumentCaptor<QPortalNotificationRequest> argumentCaptor = ArgumentCaptor.forClass(QPortalNotificationRequest.class);
    @InjectMocks
    private QPortalNotificationService qPortalNotificationService;
    @Mock
    private NotificationRequestValidator notificationRequestValidator;
    @Mock
    private QPortalService qPortalService;

    @Test
    void testHandleShipmentLost() {
        String milestoneSegmentId = UUID.randomUUID().toString();
        String segmentId = UUID.randomUUID().toString();
        NotificationRequest notificationRequest = createNotificationRequestForMilestoneNotification(MilestoneCode.SHP_LOST, milestoneSegmentId, segmentId);

        qPortalNotificationService.sendNotification(notificationRequest);

        verify(qPortalService, times(1)).sendNotification(any(), argumentCaptor.capture());
        verify(qPortalService, times(1)).getUserWithoutCache(any(), any());

        QPortalNotificationRequest capturedRequest = argumentCaptor.getValue();
        QPortalParam params = capturedRequest.getParams();

        assertThat(params.getShipmentId()).isEqualTo(notificationRequest.getShipment().getShipmentTrackingId());
        assertThat(params.getOrderIdLabel()).isEqualTo(notificationRequest.getShipment().getOrder().getOrderIdLabel());
        assertThat(params.getTrackingUrl()).isEqualTo(notificationRequest.getShipment().getOrder().getTrackingUrl());
        assertThat(params.getOrderId()).isEqualTo(notificationRequest.getShipment().getOrder().getOrderIdLabel());
        assertThat(params.getMilestoneDatetime()).isNull();
        assertThat(params.getFlightNumber()).isNull();
        assertThat(params.getAirline()).isNull();
        assertThat(params.getAirlineCode()).isNull();
        assertThat(params.getFacilityName()).isNull();
        assertThat(params.getDepartureDateTime()).isNull();
        assertThat(params.getArrivalDateTime()).isNull();
    }

    @Test
    void testHandleShipmentReturned() {
        String milestoneSegmentId = UUID.randomUUID().toString();
        String segmentId = UUID.randomUUID().toString();
        NotificationRequest notificationRequest = createNotificationRequestForMilestoneNotification(MilestoneCode.SHP_RETURNED, milestoneSegmentId, segmentId);

        qPortalNotificationService.sendNotification(notificationRequest);

        verify(qPortalService, times(1)).sendNotification(any(), argumentCaptor.capture());
        verify(qPortalService, times(1)).getUserWithoutCache(any(), any());

        QPortalNotificationRequest capturedRequest = argumentCaptor.getValue();
        QPortalParam params = capturedRequest.getParams();

        assertThat(params.getOrderIdLabel()).isEqualTo(notificationRequest.getShipment().getOrder().getOrderIdLabel());
        assertThat(params.getTrackingUrl()).isEqualTo(notificationRequest.getShipment().getOrder().getTrackingUrl());
        assertThat(params.getShipmentId()).isEqualTo(notificationRequest.getShipment().getShipmentTrackingId());
        assertThat(params.getMilestoneDatetime()).isEqualTo(formatDateAsReadable(notificationRequest.getMilestone().getMilestoneTime().toString()));
        assertThat(params.getOrderId()).isEqualTo(notificationRequest.getShipment().getOrder().getOrderIdLabel());
        assertThat(params.getFlightNumber()).isNull();
        assertThat(params.getAirline()).isNull();
        assertThat(params.getAirlineCode()).isNull();
        assertThat(params.getFacilityName()).isNull();
        assertThat(params.getDepartureDateTime()).isNull();
        assertThat(params.getArrivalDateTime()).isNull();
    }

    @Test
    void testHandlePickupSuccessful() {
        String milestoneSegmentId = UUID.randomUUID().toString();
        NotificationRequest notificationRequest = createNotificationRequestForMilestoneNotification(MilestoneCode.DSP_PICKUP_SUCCESSFUL, milestoneSegmentId, milestoneSegmentId);

        qPortalNotificationService.sendNotification(notificationRequest);

        verify(qPortalService, times(1)).sendNotification(any(), argumentCaptor.capture());
        verify(qPortalService, times(1)).getUserWithoutCache(any(), any());

        QPortalNotificationRequest capturedRequest = argumentCaptor.getValue();
        QPortalParam params = capturedRequest.getParams();

        assertThat(params.getShipmentId()).isEqualTo(notificationRequest.getShipment().getShipmentTrackingId());
        assertThat(params.getMilestoneDatetime()).isEqualTo(formatDateAsReadable(notificationRequest.getMilestone().getMilestoneTime().toString()));
        assertThat(params.getOrderIdLabel()).isEqualTo(notificationRequest.getShipment().getOrder().getOrderIdLabel());
        assertThat(params.getTrackingUrl()).isEqualTo(notificationRequest.getShipment().getOrder().getTrackingUrl());
        assertThat(params.getOrderId()).isEqualTo(notificationRequest.getShipment().getOrder().getOrderIdLabel());
        assertThat(params.getFlightNumber()).isNull();
        assertThat(params.getAirline()).isNull();
        assertThat(params.getAirlineCode()).isNull();
        assertThat(params.getFacilityName()).isNull();
        assertThat(params.getDepartureDateTime()).isNull();
        assertThat(params.getArrivalDateTime()).isNull();
    }

    @Test
    void shouldNotSendNotificationWhenPickupSuccessful() {
        String milestoneSegmentId = UUID.randomUUID().toString();
        String segmentId = UUID.randomUUID().toString();
        NotificationRequest notificationRequest = createNotificationRequestForMilestoneNotification(MilestoneCode.DSP_PICKUP_SUCCESSFUL, milestoneSegmentId, segmentId);

        qPortalNotificationService.sendNotification(notificationRequest);

        verify(qPortalService, times(0)).sendNotification(any(), any());
        verify(qPortalService, times(0)).getUserWithoutCache(any(), any());

    }

    @Test
    void testHandleDeliverySuccessful() {
        String milestoneSegmentId = UUID.randomUUID().toString();
        NotificationRequest notificationRequest = createNotificationRequestForMilestoneNotification(MilestoneCode.DSP_DELIVERY_SUCCESSFUL, milestoneSegmentId, milestoneSegmentId);

        qPortalNotificationService.sendNotification(notificationRequest);

        verify(qPortalService, times(1)).sendNotification(any(), argumentCaptor.capture());
        verify(qPortalService, times(1)).getUserWithoutCache(any(), any());

        QPortalNotificationRequest capturedRequest = argumentCaptor.getValue();
        QPortalParam params = capturedRequest.getParams();

        assertThat(params.getShipmentId()).isEqualTo(notificationRequest.getShipment().getShipmentTrackingId());
        assertThat(params.getMilestoneDatetime()).isEqualTo(formatDateAsReadable(notificationRequest.getMilestone().getMilestoneTime().toString()));
        assertThat(params.getOrderIdLabel()).isEqualTo(notificationRequest.getShipment().getOrder().getOrderIdLabel());
        assertThat(params.getTrackingUrl()).isEqualTo(notificationRequest.getShipment().getOrder().getTrackingUrl());
        assertThat(params.getOrderId()).isEqualTo(notificationRequest.getShipment().getOrder().getOrderIdLabel());
        assertThat(params.getFlightNumber()).isNull();
        assertThat(params.getAirline()).isNull();
        assertThat(params.getAirlineCode()).isNull();
        assertThat(params.getFacilityName()).isNull();
        assertThat(params.getDepartureDateTime()).isNull();
        assertThat(params.getArrivalDateTime()).isNull();
    }

    @Test
    void shouldNotSendNotificationWhenDeliverySuccessful() {
        String milestoneSegmentId = UUID.randomUUID().toString();
        String segmentId = UUID.randomUUID().toString();
        NotificationRequest notificationRequest = createNotificationRequestForMilestoneNotification(MilestoneCode.DSP_DELIVERY_SUCCESSFUL, milestoneSegmentId, segmentId);

        qPortalNotificationService.sendNotification(notificationRequest);

        verify(qPortalService, times(0)).sendNotification(any(), any());
        verify(qPortalService, times(0)).getUserWithoutCache(any(), any());

    }

    @Test
    void testHandleFlightDeparted() {
        String milestoneSegmentId = UUID.randomUUID().toString();
        String segmentId = UUID.randomUUID().toString();
        NotificationRequest notificationRequest = createNotificationRequestForMilestoneNotification(MilestoneCode.SHP_FLIGHT_DEPARTED, milestoneSegmentId, segmentId);
        Flight testFlight = createFlight();
        notificationRequest.setFlight(testFlight);

        qPortalNotificationService.sendNotification(notificationRequest);

        verify(qPortalService, times(1)).sendNotification(any(), argumentCaptor.capture());
        verify(qPortalService, times(1)).getUserWithoutCache(any(), any());

        QPortalNotificationRequest capturedRequest = argumentCaptor.getValue();
        QPortalParam params = capturedRequest.getParams();

        assertThat(params.getOrderIdLabel()).isEqualTo(notificationRequest.getShipment().getOrder().getOrderIdLabel());
        assertThat(params.getTrackingUrl()).isEqualTo(notificationRequest.getShipment().getOrder().getTrackingUrl());
        assertThat(params.getFlightNumber()).isEqualTo(testFlight.getFlightStatus().getAirlineCode() + testFlight.getFlightNumber());
        assertThat(params.getAirlineCode()).isEqualTo(testFlight.getFlightStatus().getAirlineCode());
        assertThat(params.getAirline()).isEqualTo(testFlight.getFlightStatus().getAirlineName());
        assertThat(params.getFacilityName()).isEqualTo(testFlight.getFlightStatus().getDeparture().getAirportName());
        assertThat(params.getDepartureDateTime()).isEqualTo(formatDateAsReadable(testFlight.getFlightStatus().getDeparture().getActualTime()));
        assertThat(params.getOrderId()).isEqualTo(notificationRequest.getShipment().getOrder().getOrderIdLabel());
        assertThat(params.getArrivalDateTime()).isNull();
        assertThat(params.getMilestoneDatetime()).isNull();
        assertThat(params.getShipmentId()).isNull();
    }

    @Test
    void testHandleFlightArrived() {
        String milestoneSegmentId = UUID.randomUUID().toString();
        String segmentId = UUID.randomUUID().toString();
        NotificationRequest notificationRequest = createNotificationRequestForMilestoneNotification(MilestoneCode.SHP_FLIGHT_ARRIVED, milestoneSegmentId, segmentId);
        Flight testFlight = createFlight();
        notificationRequest.setFlight(testFlight);

        qPortalNotificationService.sendNotification(notificationRequest);

        verify(qPortalService, times(1)).sendNotification(any(), argumentCaptor.capture());
        verify(qPortalService, times(1)).getUserWithoutCache(any(), any());

        QPortalNotificationRequest capturedRequest = argumentCaptor.getValue();
        QPortalParam params = capturedRequest.getParams();

        assertThat(params.getOrderIdLabel()).isEqualTo(notificationRequest.getShipment().getOrder().getOrderIdLabel());
        assertThat(params.getTrackingUrl()).isEqualTo(notificationRequest.getShipment().getOrder().getTrackingUrl());
        assertThat(params.getFlightNumber()).isEqualTo(testFlight.getFlightStatus().getAirlineCode() + testFlight.getFlightNumber());
        assertThat(params.getAirlineCode()).isEqualTo(testFlight.getFlightStatus().getAirlineCode());
        assertThat(params.getAirline()).isEqualTo(testFlight.getFlightStatus().getAirlineName());
        assertThat(params.getFacilityName()).isEqualTo(testFlight.getFlightStatus().getArrival().getAirportName());


        assertThat(params.getArrivalDateTime()).isEqualTo(formatDateAsReadable(testFlight.getFlightStatus().getArrival().getActualTime()));
        assertThat(params.getOrderId()).isEqualTo(notificationRequest.getShipment().getOrder().getOrderIdLabel());
        assertThat(params.getDepartureDateTime()).isNull();
        assertThat(params.getMilestoneDatetime()).isNull();
        assertThat(params.getShipmentId()).isNull();
    }

    private NotificationRequest createNotificationRequestForMilestoneNotification(MilestoneCode milestoneCode, String packageSegmentId, String milestoneSegmentId) {
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(milestoneCode);
        milestone.setMilestoneTime(OffsetDateTime.now());
        milestone.setSegmentId(milestoneSegmentId);
        milestone.setOrganizationId(UUID.randomUUID().toString());
        milestone.setDriverId(UUID.randomUUID().toString());

        Sender sender = new Sender();
        sender.setEmail("sender@example.com");
        sender.setContactCode("+1");
        sender.setContactNumber("1234567890");

        Consignee consignee = new Consignee();
        consignee.setEmail("consignee@example.com");
        consignee.setContactCode("+2");
        consignee.setContactNumber("23456677");

        Driver driver = new Driver();
        driver.setPhoneCode("+3");
        driver.setPhoneNumber("123123555");

        Order order = new Order();
        order.setOrderIdLabel("QC-12313-123123");
        order.setTrackingUrl("www.fake-tracker.com/track?id=1123124424");

        Shipment shipment = new Shipment();
        shipment.setSender(sender);
        shipment.setConsignee(consignee);
        shipment.setOrder(order);
        ShipmentJourney shipmentJourney = new ShipmentJourney();
        PackageJourneySegment segment = new PackageJourneySegment();
        segment.setSegmentId(packageSegmentId);
        Partner partner = new Partner();
        partner.setId(UUID.randomUUID().toString());
        segment.setPartner(partner);
        shipmentJourney.setPackageJourneySegments(List.of(segment));
        shipment.setShipmentJourney(shipmentJourney);

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setMilestone(milestone);
        notificationRequest.setShipment(shipment);
        notificationRequest.setOrganizationId(UUID.randomUUID().toString());
        return notificationRequest;
    }

    private Flight createFlight() {
        FlightDetails departure = new FlightDetails();
        departure.setActualTime("2022-12-27 14:27:02 +0700");
        departure.setAirportName("American Airport");

        FlightDetails arrival = new FlightDetails();
        arrival.setActualTime("2022-12-27 14:27:02 +0700");
        arrival.setAirportName("Singapore Airport");

        FlightStatus status = new FlightStatus();
        status.setDeparture(departure);
        status.setArrival(arrival);
        status.setAirlineName("Kennedy Airline");
        status.setAirlineCode("KAL");

        Flight flight = new Flight();
        flight.setFlightStatus(status);

        return flight;
    }

    private String formatDateAsReadable(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String readableDateFormat = "%s (UTC %s)";
        OffsetDateTime offsetDateTime = DateTimeUtil.parseOffsetDateTime(date);

        if(offsetDateTime!=null){
            ZoneOffset zoneOffset = offsetDateTime.getOffset();
            String formattedDateTime = dateTimeFormatter.format(offsetDateTime);
            return String.format(readableDateFormat, formattedDateTime, zoneOffset);
        }

        return date;
    }
}