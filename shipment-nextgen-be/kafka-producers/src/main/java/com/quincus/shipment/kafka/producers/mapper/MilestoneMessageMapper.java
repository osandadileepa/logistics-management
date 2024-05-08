package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.shipment.api.domain.Coordinate;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneAdditionalInfo;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.api.helper.MilestoneCodeUtil;
import com.quincus.shipment.kafka.producers.message.MilestoneAdditionalInfoMsgPart;
import com.quincus.shipment.kafka.producers.message.MilestoneMessage;
import com.quincus.shipment.kafka.producers.message.constant.MilestoneFromAction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import static java.util.Objects.nonNull;

@NoArgsConstructor(access = AccessLevel.NONE)
@Slf4j
public class MilestoneMessageMapper {

    private static void populateStartFacility(PackageJourneySegment segment, MilestoneMessage milestone) {
        if (nonNull(segment.getStartFacility())) {
            milestone.setFromLocationId(segment.getStartFacility().getExternalId());
            if (nonNull(segment.getStartFacility().getLocation())) {
                milestone.setFromCountryId(segment.getStartFacility().getLocation().getCountryId());
                milestone.setFromStateId(segment.getStartFacility().getLocation().getStateId());
                milestone.setFromCityId(segment.getStartFacility().getLocation().getCityId());
            }
        }
    }

    private static void populateEndFacility(PackageJourneySegment segment, MilestoneMessage milestone) {
        if (nonNull(segment.getEndFacility())) {
            milestone.setToLocationId(segment.getEndFacility().getExternalId());
            if (nonNull(segment.getEndFacility().getLocation())) {
                milestone.setToCountryId(segment.getEndFacility().getLocation().getCountryId());
                milestone.setToStateId(segment.getEndFacility().getLocation().getStateId());
                milestone.setToCityId(segment.getEndFacility().getLocation().getCityId());
            }
        }
    }

    private static void populateMilestoneInfo(MilestoneMessage milestoneMessage, Milestone shipmentMilestone) {
        Coordinate coordinate = shipmentMilestone.getMilestoneCoordinates();
        if (coordinate != null) {
            milestoneMessage.setLatitude(String.valueOf(coordinate.getLat()));
            milestoneMessage.setLongitude(String.valueOf(coordinate.getLon()));
        }
        if (nonNull(shipmentMilestone.getMilestoneCode())) {
            milestoneMessage.setMilestoneCode(shipmentMilestone.getMilestoneCode().toString());
        }
        milestoneMessage.setMilestoneName(shipmentMilestone.getMilestoneName());
        milestoneMessage.setHubId(shipmentMilestone.getHubId());
        milestoneMessage.setCountryId(shipmentMilestone.getHubCountryId());
        milestoneMessage.setStateId(shipmentMilestone.getHubStateId());
        milestoneMessage.setCityId(shipmentMilestone.getHubCityId());
        milestoneMessage.setId(shipmentMilestone.getId());
        milestoneMessage.setSenderName(shipmentMilestone.getSenderName());
        milestoneMessage.setSenderCompany(shipmentMilestone.getSenderCompany());
        milestoneMessage.setSenderDepartment(shipmentMilestone.getSenderDepartment());
        milestoneMessage.setReceiverName(shipmentMilestone.getReceiverName());
        milestoneMessage.setReceiverCompany(shipmentMilestone.getReceiverCompany());
        milestoneMessage.setReceiverDepartment(shipmentMilestone.getReceiverDepartment());
    }

    public static MilestoneMessage createMilestoneMessage(@NonNull Shipment shipmentDomain,
                                                          @NonNull PackageJourneySegment segment,
                                                          @NonNull Milestone shipmentMilestone) {
        MilestoneMessage milestoneMessage = new MilestoneMessage();
        milestoneMessage.setPackageId(shipmentDomain.getShipmentPackage().getId());
        milestoneMessage.setRefId(shipmentDomain.getShipmentPackage().getRefId());
        milestoneMessage.setUserId(shipmentMilestone.getUserId());
        milestoneMessage.setShipmentId(shipmentDomain.getId());
        milestoneMessage.setOrganizationId(shipmentDomain.getOrganization().getId());
        populateStartFacility(segment, milestoneMessage);
        populateEndFacility(segment, milestoneMessage);
        milestoneMessage.setMilestoneId(shipmentMilestone.getMilestoneRefId());
        milestoneMessage.setMilestoneTime(shipmentMilestone.getMilestoneTime().toString());
        milestoneMessage.setMilestoneTimezone(shipmentMilestone.getMilestoneTimezone());
        populateMilestoneInfo(milestoneMessage, shipmentMilestone);
        milestoneMessage.setDriverId(shipmentMilestone.getDriverId());
        milestoneMessage.setDriverName(shipmentMilestone.getDriverName());
        milestoneMessage.setDriverPhoneCode(shipmentMilestone.getDriverPhoneCode());
        milestoneMessage.setDriverPhoneNumber(shipmentMilestone.getDriverPhoneNumber());
        milestoneMessage.setDriverEmail(shipmentMilestone.getDriverEmail());
        milestoneMessage.setVehicleId(shipmentMilestone.getVehicleId());
        milestoneMessage.setVehicleType(shipmentMilestone.getVehicleType());
        milestoneMessage.setVehicleName(shipmentMilestone.getVehicleName());
        milestoneMessage.setVehicleNumber(shipmentMilestone.getVehicleNumber());
        milestoneMessage.setAdditionalInfo(additionalInfoToMsgPart(shipmentMilestone.getAdditionalInfo()));
        if (nonNull(segment.getPartner())) {
            milestoneMessage.setPartnerId(segment.getPartner().getId());
        }
        milestoneMessage.setEta(segment.getArrivalTime());
        milestoneMessage.setEtaTimezone(shipmentMilestone.getEtaTimezone());
        milestoneMessage.setActive(true);
        if (MilestoneCodeUtil.isSegmentRelated(shipmentMilestone.getMilestoneCode())) {
            milestoneMessage.setJourneyId(segment.getJourneyId());
            milestoneMessage.setSegmentId(segment.getSegmentId());
            milestoneMessage.setSegmentRefId(segment.getRefId());
        }
        milestoneMessage.setFromAction(MilestoneFromAction.OTHERS);
        return milestoneMessage;
    }

    public static MilestoneMessage createFlightMilestoneMessage(@NonNull ShipmentMessageDto shipmentMessageDto,
                                                                @NonNull PackageJourneySegment segment,
                                                                @NonNull Milestone shipmentMilestone) {
        MilestoneMessage milestone = new MilestoneMessage();
        milestone.setPackageId(shipmentMessageDto.getPackageId());
        milestone.setRefId(shipmentMessageDto.getPackageRefId());
        milestone.setShipmentId(shipmentMessageDto.getId());
        milestone.setOrganizationId(shipmentMessageDto.getOrganizationId());
        populateStartFacility(segment, milestone);
        populateEndFacility(segment, milestone);
        milestone.setMilestoneId(shipmentMilestone.getMilestoneRefId());
        milestone.setMilestoneTime(shipmentMilestone.getMilestoneTime().toString());
        milestone.setMilestoneTimezone(shipmentMilestone.getMilestoneTimezone());
        populateMilestoneInfo(milestone, shipmentMilestone);
        milestone.setAdditionalInfo(additionalInfoToMsgPart(shipmentMilestone.getAdditionalInfo()));
        if (nonNull(segment.getPartner())) {
            milestone.setPartnerId(segment.getPartner().getId());
        }
        milestone.setEta(segment.getArrivalTime());
        milestone.setEtaTimezone(shipmentMilestone.getEtaTimezone());
        milestone.setActive(true);
        milestone.setSegmentId(segment.getSegmentId());
        milestone.setFromAction(MilestoneFromAction.OTHERS);
        return milestone;
    }

    private static MilestoneAdditionalInfoMsgPart additionalInfoToMsgPart(MilestoneAdditionalInfo additionalInfo) {
        if (additionalInfo == null) {
            return null;
        }
        MilestoneAdditionalInfoMsgPart additionalInfoMsgPart = new MilestoneAdditionalInfoMsgPart();
        additionalInfoMsgPart.setImages(additionalInfo.getImages());
        additionalInfoMsgPart.setSignature(additionalInfo.getSignature());
        additionalInfoMsgPart.setAttachments(additionalInfo.getAttachments());
        additionalInfoMsgPart.setRemarks(additionalInfo.getRemarks());
        additionalInfoMsgPart.setCod(additionalInfo.getCod());
        return additionalInfoMsgPart;
    }
}
