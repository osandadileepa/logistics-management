package com.quincus.shipment.kafka.producers.mapper;

import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.MeasuredValue;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.api.dto.ShipmentMessageDto;
import com.quincus.shipment.kafka.producers.message.qship.PackageMsgPart;
import com.quincus.shipment.kafka.producers.message.qship.QshipSegmentMessage;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Component
public class ShipmentToQshipMapperImpl implements ShipmentToQshipMapper {

    @Override
    public List<QshipSegmentMessage> mapShipmentDomainToQshipSegmentMessageList(Shipment shipmentDomain) {
        return mapShipmentDomainToQshipSegmentMessageList(shipmentDomain, shipmentDomain.getShipmentJourney());
    }

    @Override
    public List<QshipSegmentMessage> mapShipmentDomainToQshipSegmentMessageList(Shipment shipmentDomain,
                                                                                ShipmentJourney journeyDomain) {
        List<PackageJourneySegment> segmentDomainList = journeyDomain.getPackageJourneySegments();
        if (CollectionUtils.isEmpty(segmentDomainList)) {
            return Collections.emptyList();
        }

        List<QshipSegmentMessage> qshipSegmentList = new ArrayList<>();
        for (PackageJourneySegment segmentDomain : segmentDomainList) {
            qshipSegmentList.add(mapSegmentDomainToQshipSegmentMessage(segmentDomain, shipmentDomain));
        }

        return qshipSegmentList;
    }

    private QshipSegmentMessage mapSegmentDomainToQshipSegmentMessage(@NonNull PackageJourneySegment segmentDomain) {
        QshipSegmentMessage qshipSegmentMessage = new QshipSegmentMessage();

        qshipSegmentMessage.setId(segmentDomain.getSegmentId());
        qshipSegmentMessage.setRefId(segmentDomain.getRefId());
        qshipSegmentMessage.setJourneyId(segmentDomain.getJourneyId());
        qshipSegmentMessage.setType(segmentDomain.getType().getLabel());
        qshipSegmentMessage.setStatus(segmentDomain.getStatus().getLabel());
        qshipSegmentMessage.setSequenceNo(segmentDomain.getSequence());
        qshipSegmentMessage.setTransportCategory(MapperUtil.getValueFromEnum(segmentDomain.getTransportType()));
        qshipSegmentMessage.setVehicleInfo(segmentDomain.getVehicleInfo());
        qshipSegmentMessage.setFlightNumber(segmentDomain.getFlightNumber());
        qshipSegmentMessage.setAirline(segmentDomain.getAirline());
        qshipSegmentMessage.setAirlineCode(segmentDomain.getAirlineCode());
        qshipSegmentMessage.setMasterWaybill(segmentDomain.getMasterWaybill());
        qshipSegmentMessage.setPickUpFacilityId(getFacilityExternalId(segmentDomain.getStartFacility()));
        qshipSegmentMessage.setDropOffFacilityId(getFacilityExternalId(segmentDomain.getEndFacility()));
        qshipSegmentMessage.setPickUpTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getPickUpTime()));
        qshipSegmentMessage.setPickUpTimezone(segmentDomain.getPickUpTimezone());
        qshipSegmentMessage.setLockoutTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getLockOutTime()));
        qshipSegmentMessage.setLockoutTimezone(segmentDomain.getLockOutTimezone());
        qshipSegmentMessage.setDepartureTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getDepartureTime()));
        qshipSegmentMessage.setDepartureTimezone(segmentDomain.getDepartureTimezone());
        qshipSegmentMessage.setArrivalTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getArrivalTime()));
        qshipSegmentMessage.setArrivalTimezone(segmentDomain.getArrivalTimezone());
        qshipSegmentMessage.setDropOffTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getDropOffTime()));
        qshipSegmentMessage.setDropOffTimezone(segmentDomain.getDropOffTimezone());
        qshipSegmentMessage.setDropOffOnSiteTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getDropOffOnSiteTime()));
        qshipSegmentMessage.setDropOffOnSiteTimezone(segmentDomain.getDropOffOnSiteTimezone());
        qshipSegmentMessage.setPickUpOnSiteTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getPickUpOnSiteTime()));
        qshipSegmentMessage.setPickUpOnSiteTimezone(segmentDomain.getPickUpOnSiteTimezone());
        qshipSegmentMessage.setPickUpActualTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getPickUpActualTime()));
        qshipSegmentMessage.setPickUpActualTimezone(segmentDomain.getPickUpActualTimezone());
        qshipSegmentMessage.setDropOffActualTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getDropOffActualTime()));
        qshipSegmentMessage.setDropOffActualTimezone(segmentDomain.getDropOffActualTimezone());
        qshipSegmentMessage.setRecoveryTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getRecoveryTime()));
        qshipSegmentMessage.setRecoveryTimezone(segmentDomain.getRecoveryTimezone());
        qshipSegmentMessage.setCalculatedMileage(mapValueUomPairToMeasuredValue(segmentDomain.getCalculatedMileage(), segmentDomain.getCalculatedMileageUnit()));
        qshipSegmentMessage.setDuration(mapValueUomPairToMeasuredValue(segmentDomain.getDuration(), segmentDomain.getDurationUnit()));
        qshipSegmentMessage.setInternalBookingReference(segmentDomain.getInternalBookingReference());
        qshipSegmentMessage.setExternalBookingReference(segmentDomain.getExternalBookingReference());
        qshipSegmentMessage.setAssignmentStatus(segmentDomain.getAssignmentStatus());
        qshipSegmentMessage.setRejectionReason(segmentDomain.getRejectionReason());
        mapDriverDetailToQshipSegment(qshipSegmentMessage, segmentDomain);
        mapVehicleDetailsToQshipSegment(qshipSegmentMessage, segmentDomain);

        qshipSegmentMessage.setFlightId(
                Optional.ofNullable(segmentDomain.getFlight())
                        .map(flight -> String.valueOf(flight.getFlightId()))
                        .orElse(null)
        );

        if (nonNull(segmentDomain.getPartner())) {
            qshipSegmentMessage.setPartnerId(segmentDomain.getPartner().getId());
        }
        qshipSegmentMessage.setInstructions(segmentDomain.getInstructions());

        Optional.ofNullable(segmentDomain.getFlight())
                .map(Flight::getFlightStatuses)
                .ifPresent(statuses -> {
                    qshipSegmentMessage.setDepartureActualTime(DateTimeUtil.parseZonedDateTime(MapperUtil.getDepartureActualTime(statuses)));
                    qshipSegmentMessage.setDepartureActualTimezone(segmentDomain.getDepartureTimezone());
                    qshipSegmentMessage.setArrivalActualTime(DateTimeUtil.parseZonedDateTime(MapperUtil.getArrivalActualTime(statuses)));
                    qshipSegmentMessage.setArrivalActualTimezone(segmentDomain.getArrivalTimezone());
                    qshipSegmentMessage.setLat(MapperUtil.getLatitude(statuses));
                    qshipSegmentMessage.setLon(MapperUtil.getLongitude(statuses));
                });
        mapModifyTime(segmentDomain, qshipSegmentMessage);
        return qshipSegmentMessage;
    }

    @Override
    public QshipSegmentMessage mapSegmentDomainToQshipSegmentMessage(@NonNull PackageJourneySegment segmentDomain,
                                                                     @NonNull Shipment shipmentDomain) {
        QshipSegmentMessage qshipSegmentMessage = mapSegmentDomainToQshipSegmentMessage(segmentDomain);

        qshipSegmentMessage.setOrderId(shipmentDomain.getOrder().getId());
        qshipSegmentMessage.setOrganisationId(shipmentDomain.getOrganization().getId());
        PackageMsgPart qShipSegmentPackage = new PackageMsgPart();
        //Only 1 entry since SHPv2 is 1 shipment = 1 package
        qShipSegmentPackage.setId(shipmentDomain.getShipmentPackage().getId());
        qShipSegmentPackage.setRefId(shipmentDomain.getShipmentPackage().getRefId());
        //using first entry of shipment reference ID for now (limitation of message structure)
        List<String> shipmentReferenceId = shipmentDomain.getShipmentReferenceId();
        if (nonNull(shipmentReferenceId)) {
            qShipSegmentPackage.setAdditionalData1(shipmentReferenceId.get(0));
        }

        qshipSegmentMessage.setPackages(List.of(qShipSegmentPackage));
        qshipSegmentMessage.setExternalOrderId(shipmentDomain.getExternalOrderId());
        qshipSegmentMessage.setInternalOrderId(shipmentDomain.getInternalOrderId());
        qshipSegmentMessage.setCustomerOrderId(shipmentDomain.getCustomerOrderId());
        qshipSegmentMessage.setOrderReferences(shipmentDomain.getOrder().getOrderReferences());

        return qshipSegmentMessage;
    }

    @Override
    public QshipSegmentMessage mapSegmentDomainToQshipSegmentMessage(@NonNull PackageJourneySegment segmentDomain,
                                                                     @NonNull ShipmentMessageDto shipmentMessageDto) {
        QshipSegmentMessage qshipSegmentMessage = mapSegmentDomainToQshipSegmentMessage(segmentDomain);

        qshipSegmentMessage.setOrderId(shipmentMessageDto.getOrderId());
        qshipSegmentMessage.setOrganisationId(shipmentMessageDto.getOrganizationId());
        PackageMsgPart qShipSegmentPackage = new PackageMsgPart();
        //Only 1 entry since SHPv2 is 1 shipment = 1 package
        qShipSegmentPackage.setId(shipmentMessageDto.getPackageId());
        qShipSegmentPackage.setRefId(shipmentMessageDto.getPackageRefId());
        //using first entry of shipment reference ID for now (limitation of message structure)
        List<String> shipmentReferenceId = shipmentMessageDto.getShipmentReferenceId();
        if (nonNull(shipmentReferenceId)) {
            qShipSegmentPackage.setAdditionalData1(shipmentReferenceId.get(0));
        }

        qshipSegmentMessage.setPackages(List.of(qShipSegmentPackage));
        qshipSegmentMessage.setExternalOrderId(shipmentMessageDto.getExternalOrderId());
        qshipSegmentMessage.setInternalOrderId(shipmentMessageDto.getInternalOrderId());
        qshipSegmentMessage.setCustomerOrderId(shipmentMessageDto.getCustomerOrderId());
        qshipSegmentMessage.setOrderReferences(shipmentMessageDto.getOrderReferences());

        return qshipSegmentMessage;
    }

    void mapModifyTime(PackageJourneySegment segmentDomain, QshipSegmentMessage qshipSegmentMessage) {
        if (segmentDomain.getModifyTime() == null) return;
        ZonedDateTime modifyTime = ZonedDateTime.ofInstant(segmentDomain.getModifyTime(), ZoneId.systemDefault());
        qshipSegmentMessage.setUpdatedAt(modifyTime);
        if (segmentDomain.isDeleted()) {
            qshipSegmentMessage.setDeletedAt(modifyTime);
        }
    }

    private void mapDriverDetailToQshipSegment(QshipSegmentMessage qshipSegmentMessage, PackageJourneySegment segmentDomain) {
        if (segmentDomain.getDriver() == null) {
            return;
        }
        qshipSegmentMessage.setDriverId(segmentDomain.getDriver().getId());
        qshipSegmentMessage.setDriverName(segmentDomain.getDriver().getName());
        qshipSegmentMessage.setDriverPhoneCode(segmentDomain.getDriver().getPhoneCode());
        qshipSegmentMessage.setDriverPhoneNumber(segmentDomain.getDriver().getPhoneNumber());
    }

    private void mapVehicleDetailsToQshipSegment(QshipSegmentMessage qshipSegmentMessage, PackageJourneySegment segmentDomain) {
        if (segmentDomain.getVehicle() == null) {
            return;
        }
        qshipSegmentMessage.setVehicleId(segmentDomain.getVehicle().getId());
        qshipSegmentMessage.setVehicleName(segmentDomain.getVehicle().getName());
        qshipSegmentMessage.setVehicleNumber(segmentDomain.getVehicle().getNumber());
        qshipSegmentMessage.setVehicleType(segmentDomain.getVehicle().getType());
    }

    private static String getFacilityExternalId(Facility facility) {
        return facility != null ? facility.getExternalId() : null;
    }

    private MeasuredValue mapValueUomPairToMeasuredValue(Number value, UnitOfMeasure uom) {
        if (value == null) {
            return null;
        }

        MeasuredValue measuredValue = new MeasuredValue();
        measuredValue.setValue(value);
        measuredValue.setUom(uom);
        return measuredValue;
    }
}
