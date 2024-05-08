package com.quincus.shipment.kafka.producers.message;

import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.kafka.producers.message.constant.MilestoneFromAction;
import lombok.Data;

@Data
public class MilestoneMessage {
    private String id;
    private String packageId;
    private String milestoneId;
    private String milestoneTime;
    private String milestoneTimezone;
    private String userId;
    private String fromLocationId;
    private String partnerId;
    private String toLocationId;
    private String eta;
    private String etaTimezone;
    private String toCountryId;
    private String toStateId;
    private String toCityId;
    private String fromCountryId;
    private String fromStateId;
    private String fromCityId;
    private String latitude;
    private String longitude;
    private MilestoneFromAction fromAction;
    private String milestoneCode;
    private String milestoneName;
    private String refId;
    private String organizationId;
    private boolean active;
    private String shipmentId;
    private String segmentId;
    private String segmentRefId;
    private String journeyId;
    private String hubId;
    private String countryId;
    private String stateId;
    private String cityId;
    private String driverId;
    private String driverName;
    private String driverPhoneCode;
    private String driverPhoneNumber;
    private String driverEmail;
    private String vehicleId;
    private String vehicleType;
    private String vehicleName;
    private String vehicleNumber;
    private String senderName;
    private String senderCompany;
    private String senderDepartment;
    private String receiverName;
    private String receiverCompany;
    private String receiverDepartment;
    private MilestoneAdditionalInfoMsgPart additionalInfo;
    private TriggeredFrom triggeredFrom;
}
