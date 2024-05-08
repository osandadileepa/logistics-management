package com.quincus.shipment.api.dto;

import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Shipment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {
    private Shipment shipment;
    private Milestone milestone;
    private Flight flight;
    private String organizationId;

    public static NotificationRequest ofMilestoneNotification(Shipment shipment,
                                                              Milestone milestone,
                                                              String organizationId) {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setShipment(shipment);
        notificationRequest.setMilestone(milestone);
        notificationRequest.setOrganizationId(organizationId);
        return notificationRequest;
    }

    public static NotificationRequest ofFlightNotification(Shipment shipment,
                                                           Milestone milestone,
                                                           Flight flight) {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setShipment(shipment);
        notificationRequest.setMilestone(milestone);
        notificationRequest.setFlight(flight);
        notificationRequest.setOrganizationId(shipment.getOrganization().getId());
        return notificationRequest;
    }
}
