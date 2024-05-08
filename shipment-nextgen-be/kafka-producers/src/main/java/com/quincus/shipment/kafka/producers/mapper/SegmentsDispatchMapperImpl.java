package com.quincus.shipment.kafka.producers.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.constant.SegmentType;
import com.quincus.shipment.api.constant.TransportType;
import com.quincus.shipment.api.constant.UnitOfMeasure;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Commodity;
import com.quincus.shipment.api.domain.Consignee;
import com.quincus.shipment.api.domain.Facility;
import com.quincus.shipment.api.domain.Flight;
import com.quincus.shipment.api.domain.MeasuredValue;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.PricingInfo;
import com.quincus.shipment.api.domain.Sender;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.kafka.producers.message.dispatch.AddressDetailsMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.CommodityMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.ConsigneeMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.CurrencyMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.OrderMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.PackageMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.PhoneCodeMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.PricingInfoMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentCancelMessage;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.SegmentsDispatchMessage;
import com.quincus.shipment.kafka.producers.message.dispatch.ShipmentMsgPart;
import com.quincus.shipment.kafka.producers.message.dispatch.ShipperMsgPart;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Component
@AllArgsConstructor
public class SegmentsDispatchMapperImpl implements SegmentsDispatchMapper {

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private final ObjectMapper objectMapper;

    @Override
    public SegmentsDispatchMessage mapJourneyAndShipmentsToSegmentsDispatchMessage(List<Shipment> shipments,
                                                                                   ShipmentJourney journey) {
        if (journey == null) {
            return null;
        }

        return mapJourneyAndShipmentsToSegmentsDispatchMessage(shipments, journey.getPackageJourneySegments());
    }

    @Override
    public SegmentsDispatchMessage mapSegmentAndShipmentsToSegmentsDispatchMessage(List<Shipment> shipments,
                                                                                   PackageJourneySegment segment) {
        return mapJourneyAndShipmentsToSegmentsDispatchMessage(shipments, List.of(segment));
    }

    private SegmentsDispatchMessage mapJourneyAndShipmentsToSegmentsDispatchMessage(List<Shipment> shipments,
                                                                                    List<PackageJourneySegment> segments) {
        if (CollectionUtils.isEmpty(segments)) {
            return null;
        }
        Shipment refShipment = shipments.get(0);

        String journeyId = Optional.ofNullable(refShipment.getShipmentJourney())
                .map(ShipmentJourney::getJourneyId)
                .orElseGet(() -> segments.get(0).getJourneyId());

        SegmentsDispatchMessage segmentsDispatch = new SegmentsDispatchMessage();
        segmentsDispatch.setShpVersion(SegmentsDispatchMessage.MSG_SHP_VERSION);
        segmentsDispatch.setOrganisationId(refShipment.getOrganization().getId());
        segmentsDispatch.setUserId(refShipment.getUserId());

        OrderMsgPart orderMsgPart = mapOrderDomainToOrderMsgPart(refShipment.getOrder());
        orderMsgPart.setShipper(mapShipmentDomainToShipperMsgPart(refShipment));
        orderMsgPart.setOrigin(mapShipmentDomainToOriginMsgPart(refShipment));
        orderMsgPart.setConsignee(mapShipmentDomainToConsigneeMsgPart(refShipment));
        orderMsgPart.setDestination(mapShipmentDomainToDestinationMsgPart(refShipment));
        orderMsgPart.setPricingInfo(mapShipmentDomainToPricingInfo(refShipment));
        orderMsgPart.setServiceTypeId(refShipment.getServiceType().getId());
        orderMsgPart.setServiceTypeName(refShipment.getServiceType().getCode());
        orderMsgPart.setNumberOfShipments(shipments.size());
        segmentsDispatch.setOrder(orderMsgPart);

        segmentsDispatch.setJourneyId(journeyId);

        List<ShipmentMsgPart> shipmentMsgParts = new ArrayList<>();
        for (Shipment shipment : shipments) {
            shipmentMsgParts.add(mapShipmentDomainToShipmentMsgPart(shipment));
        }
        segmentsDispatch.setShipments(shipmentMsgParts);

        List<SegmentMsgPart> segmentMsgParts = new ArrayList<>();
        segments.stream().filter(Predicate.not(PackageJourneySegment::isDeleted))
                .forEach(segment -> segmentMsgParts.add(mapSegmentDomainToSegmentMsgPart(segment, refShipment.getOrder())));
        segmentsDispatch.setSegments(segmentMsgParts);
        segmentsDispatch.setInternalOrderId(refShipment.getInternalOrderId());
        segmentsDispatch.setExternalOrderId(refShipment.getExternalOrderId());
        segmentsDispatch.setCustomerOrderId(refShipment.getCustomerOrderId());

        return segmentsDispatch;
    }

    @Override
    public SegmentCancelMessage mapShipmentDomainToSegmentCancelMessage(Shipment shipmentDomain) {
        if (shipmentDomain == null) {
            return null;
        }

        SegmentCancelMessage segmentCancelMessage = new SegmentCancelMessage();
        segmentCancelMessage.setShipmentId(shipmentDomain.getShipmentTrackingId());
        segmentCancelMessage.setOrganisationId(shipmentDomain.getOrganization().getId());
        segmentCancelMessage.setOrderId(shipmentDomain.getOrder().getId());

        return segmentCancelMessage;
    }

    @Override
    public OrderMsgPart mapOrderDomainToOrderMsgPart(Order orderDomain) {
        if (orderDomain == null) {
            return null;
        }

        OrderMsgPart orderMsgPart = new OrderMsgPart();

        orderMsgPart.setId(orderDomain.getId());
        orderMsgPart.setNote(orderDomain.getNotes());
        orderMsgPart.setCustomerReferences(MapperUtil.convertStringList(orderDomain.getCustomerReferenceId()));
        orderMsgPart.setTagsList(MapperUtil.convertStringList(orderDomain.getTags()));

        // This will simply forward the list of attachments from OM
        orderMsgPart.setAttachments(mapOrderDomainToAttachmentList(orderDomain));

        orderMsgPart.setOpsType(orderDomain.getOpsType());
        orderMsgPart.setPickupStartTime(DateTimeUtil.toFormattedOffsetDateTime(orderDomain.getPickupStartTime()));
        orderMsgPart.setPickupCommitTimezone(orderDomain.getPickupTimezone());
        orderMsgPart.setPickupCommitTime(DateTimeUtil.toFormattedOffsetDateTime(orderDomain.getPickupCommitTime()));
        orderMsgPart.setPickupCommitTimezone(orderDomain.getPickupTimezone());
        orderMsgPart.setDeliveryStartTime(DateTimeUtil.toFormattedOffsetDateTime(orderDomain.getDeliveryStartTime()));
        orderMsgPart.setDeliveryStartTimezone(orderDomain.getDeliveryTimezone());
        orderMsgPart.setDeliveryCommitTime(DateTimeUtil.toFormattedOffsetDateTime(orderDomain.getDeliveryCommitTime()));
        orderMsgPart.setDeliveryCommitTimezone(orderDomain.getDeliveryTimezone());
        orderMsgPart.setCode(orderDomain.getOrderIdLabel());
        orderMsgPart.setOrderReferences(orderDomain.getOrderReferences());

        return orderMsgPart;
    }

    @Override
    public ShipmentMsgPart mapShipmentDomainToShipmentMsgPart(Shipment shipmentDomain) {
        ShipmentMsgPart shipmentMsgPart = new ShipmentMsgPart();
        shipmentMsgPart.setShipmentId(shipmentDomain.getId());
        shipmentMsgPart.setShipmentTrackingId(shipmentDomain.getShipmentTrackingId());
        shipmentMsgPart.setShipmentReferenceIds(shipmentDomain.getShipmentReferenceId());
        shipmentMsgPart.setShipmentTags(shipmentDomain.getShipmentTags());
        shipmentMsgPart.setShipmentAttachments(shipmentDomain.getShipmentAttachments());
        shipmentMsgPart.setPackageVal(mapShipmentPackageToPackageMsgPart(shipmentDomain));

        return shipmentMsgPart;
    }

    @Override
    public SegmentMsgPart mapSegmentDomainToSegmentMsgPart(PackageJourneySegment segmentDomain, Order orderDomain) {
        if (segmentDomain == null) return null;
        SegmentMsgPart segment = new SegmentMsgPart();
        segment.setId(segmentDomain.getSegmentId());
        segment.setRefId(segmentDomain.getRefId());
        segment.setType(MapperUtil.getValueFromEnum(segmentDomain.getType()));
        segment.setStatus(MapperUtil.getValueFromEnum(segmentDomain.getStatus()));
        segment.setSequenceNo(segmentDomain.getSequence());
        segment.setInternalBookingReference(segmentDomain.getInternalBookingReference());
        segment.setExternalBookingReference(segmentDomain.getExternalBookingReference());
        segment.setAssignmentStatus(segmentDomain.getAssignmentStatus());
        segment.setRejectionReason(segmentDomain.getRejectionReason());

        TransportType transportCategory = segmentDomain.getTransportType();
        segment.setTransportCategory(MapperUtil.getValueFromEnum(transportCategory));

        if (segmentDomain.getPartner() != null) {
            segment.setPartnerId(segmentDomain.getPartner().getId());
        }

        segment.setInstructions(segmentDomain.getInstructions());
        segment.setMasterWaybillLabel(segmentDomain.getMasterWaybill());

        if (TransportType.GROUND == transportCategory) {
            Optional.ofNullable(segmentDomain.getDriver()).ifPresent(driver -> segment.setDriverName(driver.getName()));
            Optional.ofNullable(segmentDomain.getVehicle()).ifPresent(vehicle -> {
                segment.setVehicleType(vehicle.getType());
                segment.setVehicleNumber(vehicle.getNumber());
            });
            setupSegmentPickUpTime(segment, segmentDomain, orderDomain);
            setupSegmentDropOffTime(segment, segmentDomain, orderDomain);
        } else if (TransportType.AIR == transportCategory) {
            segment.setAirNumber(segmentDomain.getFlightNumber());
            segment.setAirLine(segmentDomain.getAirline());
            segment.setAirLineCode(segmentDomain.getAirlineCode());

            segment.setLockoutTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getLockOutTime()));
            segment.setLockoutTimezone(segmentDomain.getLockOutTimezone());
            segment.setDepartedTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getDepartureTime()));
            segment.setDepartedTimezone(segmentDomain.getDepartureTimezone());
            segment.setArrivalTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getArrivalTime()));
            segment.setArrivalTimezone(segmentDomain.getArrivalTimezone());
            segment.setRecoverTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getRecoveryTime()));
            segment.setRecoverTimezone(segmentDomain.getRecoveryTimezone());

            Optional.ofNullable(segmentDomain.getFlight())
                    .map(Flight::getFlightStatuses)
                    .ifPresent(statuses -> {
                        segment.setDepartureActualTime(DateTimeUtil.parseZonedDateTime(MapperUtil.getDepartureActualTime(statuses)));
                        segment.setArrivalActualTime(DateTimeUtil.parseZonedDateTime(MapperUtil.getArrivalActualTime(statuses)));
                    });
        }
        segment.setFromFacilityId(getFacilityExternalId(segmentDomain.getStartFacility()));
        segment.setToFacilityId(getFacilityExternalId(segmentDomain.getEndFacility()));
        segment.setHubId(segmentDomain.getHubId());
        segment.setCalculatedMileage(mapValueUomPairToMeasuredValue(segmentDomain.getCalculatedMileage(),
                segmentDomain.getCalculatedMileageUnit()));
        segment.setDuration(mapValueUomPairToMeasuredValue(segmentDomain.getDuration(),
                segmentDomain.getDurationUnit()));

        return segment;
    }

    private void setupSegmentPickUpTime(SegmentMsgPart segment, PackageJourneySegment segmentDomain, Order orderDomain) {
        segment.setPickUpActualTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getPickUpActualTime()));
        segment.setPickUpActualTimezone(segmentDomain.getPickUpActualTimezone());
        segment.setPickUpOnSiteTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getPickUpOnSiteTime()));
        segment.setPickUpOnSiteTimezone(segmentDomain.getPickUpOnSiteTimezone());

        if (SegmentType.FIRST_MILE == segmentDomain.getType()) {
            segment.setPickUpStartTime(DateTimeUtil.stringToZonedDateTime(orderDomain.getPickupStartTime(), orderDomain.getPickupTimezone()));
            segment.setPickUpStartTimezone(orderDomain.getPickupTimezone());
            segment.setPickUpCommitTime(DateTimeUtil.stringToZonedDateTime(orderDomain.getPickupCommitTime(), orderDomain.getPickupTimezone()));
            segment.setPickUpCommitTimezone(orderDomain.getPickupTimezone());
            segment.setPickupTimeZone(orderDomain.getPickupTimezone());
        } else {
            // Commit time is not populated and will never be populated by orders on the segment payload. So we will be setting pickup time for the pickup commit time
            ZonedDateTime pickupStartTime = DateTimeUtil.parseZonedDateTime(segmentDomain.getPickUpTime());

            if ((segmentDomain.getSequence() == null) || Integer.parseInt(segmentDomain.getSequence()) == 0) {
                //Single Segment (LAST_MILE)
                segment.setPickUpStartTime(DateTimeUtil.stringToZonedDateTime(orderDomain.getPickupStartTime(), orderDomain.getPickupTimezone()));
                segment.setPickUpStartTimezone(orderDomain.getPickupTimezone());
                segment.setPickUpCommitTime(DateTimeUtil.stringToZonedDateTime(orderDomain.getPickupCommitTime(), orderDomain.getPickupTimezone()));
                segment.setPickUpCommitTimezone(orderDomain.getPickupTimezone());
            } else {
                segment.setPickUpStartTime(pickupStartTime);
                segment.setPickUpStartTimezone(segmentDomain.getPickUpTimezone());
                segment.setPickUpCommitTime(pickupStartTime);
                segment.setPickUpCommitTimezone(segmentDomain.getPickUpTimezone());
            }

            segment.setPickupTimeZone(segmentDomain.getPickUpTimezone());
        }
    }

    private void setupSegmentDropOffTime(SegmentMsgPart segment, PackageJourneySegment segmentDomain, Order orderDomain) {
        segment.setDropOffActualTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getDropOffActualTime()));
        segment.setDropOffActualTimezone(segmentDomain.getDropOffActualTimezone());
        segment.setDropOffOnSiteTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getDropOffOnSiteTime()));
        segment.setDropOffOnSiteTimezone(segmentDomain.getDropOffOnSiteTimezone());

        if (SegmentType.LAST_MILE == segmentDomain.getType()) {
            segment.setDropOffStartTime(compareAndGetLatestTime(segmentDomain.getPickUpTime(), DateTimeUtil.convertStringToLocalDateTime(orderDomain.getDeliveryStartTime()), orderDomain.getDeliveryTimezone()));
            segment.setDropOffStartTimezone(orderDomain.getDeliveryTimezone());
            segment.setDropOffCommitTime(DateTimeUtil.stringToZonedDateTime(orderDomain.getDeliveryCommitTime(), orderDomain.getDeliveryTimezone()));
            segment.setDropOffCommitTimezone(orderDomain.getDeliveryTimezone());
            segment.setDropOffTimeZone(orderDomain.getDeliveryTimezone());
        } else {
            // Commit time is not populated and will never be populated by orders on the segment payload. So we will be setting drop-off time for the drop-off commit time
            ZonedDateTime dropOffStartTime = DateTimeUtil.parseZonedDateTime(segmentDomain.getDropOffTime());

            if (SegmentType.MIDDLE_MILE == segmentDomain.getType()) {
                segment.setDropOffStartTime(DateTimeUtil.parseZonedDateTime(segmentDomain.getPickUpTime()));
                segment.setDropOffStartTimezone(segmentDomain.getPickUpTimezone());
            } else {
                segment.setDropOffStartTime(dropOffStartTime);
                segment.setDropOffStartTimezone(segmentDomain.getDropOffTimezone());
            }

            segment.setDropOffCommitTime(dropOffStartTime);
            segment.setDropOffCommitTimezone(segmentDomain.getDropOffTimezone());
            segment.setDropOffTimeZone(segmentDomain.getDropOffTimezone());
        }
    }

    ZonedDateTime compareAndGetLatestTime(String segmentPickupTime, LocalDateTime orderDeliveryStartTime, String timezone) {
        ZonedDateTime deliveryStartTime = DateTimeUtil.localeDateTimeToZoneDateTime(orderDeliveryStartTime, timezone);
        if (deliveryStartTime == null || StringUtils.isBlank(segmentPickupTime)) return deliveryStartTime;
        ZonedDateTime pickupTime = DateTimeUtil.parseZonedDateTime(segmentPickupTime);
        ZonedDateTime pickupTimeInUTC = pickupTime.withZoneSameInstant(UTC_ZONE);
        ZonedDateTime deliveryStartTimeInUTC = deliveryStartTime.withZoneSameInstant(UTC_ZONE);
        return pickupTimeInUTC.isAfter(deliveryStartTimeInUTC) ? pickupTime : deliveryStartTime;
    }

    List<JsonNode> mapOrderDomainToAttachmentList(Order orderDomain) {
        JsonNode rawJson = MapperUtil.extractPartFromRawJson(orderDomain.getData(), "attachments", objectMapper);
        if (rawJson == null) {
            return Collections.emptyList();
        }

        ArrayNode attachmentsJson = (ArrayNode) rawJson;
        if (attachmentsJson.isEmpty()) {
            return Collections.emptyList();
        }

        List<JsonNode> attachmentJsonList = new ArrayList<>();
        attachmentsJson.elements().forEachRemaining(attachmentJsonList::add);

        return attachmentJsonList;
    }

    PhoneCodeMsgPart mapOrderJsonToPhoneCodeMsgPart(String rawJsonText, String fieldName, String phoneCodeFieldName) {
        JsonNode rawJson = MapperUtil.extractPartFromRawJson(rawJsonText, fieldName, objectMapper);
        if (rawJson == null) {
            return null;
        }

        JsonNode phoneCodeJson = rawJson.get(phoneCodeFieldName);

        PhoneCodeMsgPart phoneCode = new PhoneCodeMsgPart();
        phoneCode.setId(MapperUtil.parseTextFromJson(phoneCodeJson.get("id")));
        phoneCode.setCode(MapperUtil.parseTextFromJson(phoneCodeJson.get("code")));
        phoneCode.setName(MapperUtil.parseTextFromJson(phoneCodeJson.get("name")));

        return phoneCode;
    }

    ShipperMsgPart mapShipmentDomainToShipperMsgPart(Shipment shipmentDomain) {
        ShipperMsgPart shipper = new ShipperMsgPart();

        Optional<Sender> senderOpt = Optional.ofNullable(shipmentDomain.getSender());

        senderOpt.ifPresent(senderDomain -> {
            shipper.setName(senderDomain.getName());
            shipper.setEmail(senderDomain.getEmail());
            shipper.setPhone(senderDomain.getContactNumber());
            shipper.setPhoneCode(mapOrderJsonToPhoneCodeMsgPart(shipmentDomain.getOrder().getData(),
                    "shipper", "shipper_phone_code_id"));
        });
        return shipper;
    }

    ConsigneeMsgPart mapShipmentDomainToConsigneeMsgPart(Shipment shipmentDomain) {
        ConsigneeMsgPart consignee = new ConsigneeMsgPart();
        Consignee consigneeDomain = shipmentDomain.getConsignee();

        consignee.setName(consigneeDomain.getName());
        consignee.setEmail(consigneeDomain.getEmail());
        consignee.setPhone(consigneeDomain.getContactNumber());
        consignee.setPhoneCode(mapOrderJsonToPhoneCodeMsgPart(shipmentDomain.getOrder().getData(),
                "consignee", "consignee_phone_code_id"));

        return consignee;
    }

    AddressDetailsMsgPart mapShipmentDomainToOriginMsgPart(Shipment shipmentDomain) {
        return mapAddressDomainToAddressMsgPart(shipmentDomain.getOrigin());
    }

    AddressDetailsMsgPart mapShipmentDomainToDestinationMsgPart(Shipment shipmentDomain) {
        return mapAddressDomainToAddressMsgPart(shipmentDomain.getDestination());
    }

    private AddressDetailsMsgPart mapAddressDomainToAddressMsgPart(Address addressDomain) {
        AddressDetailsMsgPart addressDetails = new AddressDetailsMsgPart();

        addressDetails.setId(addressDomain.getExternalId());
        addressDetails.setCity(addressDomain.getCityName());
        addressDetails.setState(addressDomain.getStateName());
        addressDetails.setCountry(addressDomain.getCountryName());
        addressDetails.setAddress(addressDomain.getFullAddress());
        addressDetails.setCityId(addressDomain.getCityId());
        addressDetails.setStateId(addressDomain.getStateId());
        addressDetails.setCountryId(addressDomain.getCountryId());
        addressDetails.setLatitude(addressDomain.getLatitude());
        addressDetails.setLongitude(addressDomain.getLongitude());
        addressDetails.setPostalCode(addressDomain.getPostalCode());
        addressDetails.setAddressLine1(addressDomain.getLine1());
        addressDetails.setAddressLine2(addressDomain.getLine2());
        addressDetails.setAddressLine3(addressDomain.getLine3());
        addressDetails.setManualCoordinates(addressDomain.isManualCoordinates());
        addressDetails.setCompany(addressDomain.getCompany());
        addressDetails.setDepartment(addressDomain.getDepartment());

        return addressDetails;
    }

    PricingInfoMsgPart mapShipmentDomainToPricingInfo(Shipment shipmentDomain) {
        PricingInfoMsgPart pricingInfo = new PricingInfoMsgPart();
        PricingInfo pricingInfoDomain = shipmentDomain.getShipmentPackage().getPricingInfo();

        pricingInfo.setId(pricingInfoDomain.getExternalId());
        pricingInfo.setCod(pricingInfoDomain.getCod());
        pricingInfo.setTax(pricingInfoDomain.getTax());

        JsonNode pricingInfoJson = MapperUtil.extractPartFromRawJson(shipmentDomain.getOrder().getData(),
                "pricing_info", objectMapper);
        if (pricingInfoJson != null) {
            pricingInfo.setTotal(MapperUtil.parseBigDecimalFromJson(pricingInfoJson.get("total")));

            JsonNode currencyJson = pricingInfoJson.get("currency");
            CurrencyMsgPart currency = new CurrencyMsgPart();
            currency.setId(MapperUtil.parseTextFromJson(currencyJson.get("id")));
            currency.setCode(MapperUtil.parseTextFromJson(currencyJson.get("code")));
            currency.setName(MapperUtil.parseTextFromJson(currencyJson.get("name")));
            currency.setDeleted(MapperUtil.parseBooleanFromJson(currencyJson.get("deleted")));

            String createdAtStr = MapperUtil.parseTextFromJson(currencyJson.get("created_at"));
            currency.setCreatedAt(DateTimeUtil.parseInstant(createdAtStr));

            String updatedAtStr = MapperUtil.parseTextFromJson(currencyJson.get("updated_at"));
            currency.setUpdatedAt(DateTimeUtil.parseInstant(updatedAtStr));

            currency.setExchangeRate(MapperUtil.parseBigDecimalFromJson(currencyJson.get("exchange_rate")));
            currency.setOrganisationId(MapperUtil.parseTextFromJson(currencyJson.get("organisation_id")));
            currency.setIsDefaultCurrency(MapperUtil.parseBooleanFromJson(currencyJson.get("is_default_currency")));

            pricingInfo.setCurrency(currency);
        }

        pricingInfo.setDiscount(pricingInfoDomain.getDiscount());
        pricingInfo.setSurcharge(pricingInfoDomain.getSurcharge());
        pricingInfo.setBaseTariff(pricingInfoDomain.getBaseTariff());
        pricingInfo.setCurrencyCode(pricingInfoDomain.getCurrency());
        pricingInfo.setInsuranceCharge(pricingInfoDomain.getInsuranceCharge());
        pricingInfo.setServiceTypeCharge(pricingInfoDomain.getServiceTypeCharge());

        return pricingInfo;
    }

    private String getFacilityExternalId(Facility facility) {
        return facility != null ? facility.getExternalId() : null;
    }

    PackageMsgPart mapShipmentPackageToPackageMsgPart(Shipment shipmentDomain) {
        PackageMsgPart packageMsg = new PackageMsgPart();

        Package packageDomain = shipmentDomain.getShipmentPackage();
        PackageDimension dimensionDomain = packageDomain.getDimension();

        packageMsg.setId(packageDomain.getId());
        packageMsg.setCode(packageDomain.getCode());
        packageMsg.setRefId(packageDomain.getRefId());
        packageMsg.setTypeRefId(packageDomain.getTypeRefId());
        packageMsg.setType(packageDomain.getType());

        packageMsg.setNote(shipmentDomain.getNotes());
        packageMsg.setValueOfGoods(packageDomain.getTotalValue());
        packageMsg.setItemCount(packageDomain.getTotalItemsCount().intValue());
        packageMsg.setHeight(dimensionDomain.getHeight());
        packageMsg.setWidth(dimensionDomain.getWidth());
        packageMsg.setLength(dimensionDomain.getLength());
        packageMsg.setGrossWeight(dimensionDomain.getGrossWeight());
        packageMsg.setVolumeWeight(dimensionDomain.getVolumeWeight());
        packageMsg.setChargeableWeight(dimensionDomain.getChargeableWeight());
        packageMsg.setMeasurement(dimensionDomain.getMeasurementUnit());
        if (!CollectionUtils.isEmpty(shipmentDomain.getShipmentReferenceId())) {
            packageMsg.setAdditionalData1(shipmentDomain.getShipmentReferenceId().get(0));
        }

        List<CommodityMsgPart> commodityMsgList = new ArrayList<>();
        for (Commodity commodityDomain : packageDomain.getCommodities()) {
            commodityMsgList.add(mapCommodityDomainToCommodityMsgPart(commodityDomain));
        }
        packageMsg.setCommodities(commodityMsgList);
        packageMsg.setCustom(dimensionDomain.isCustom());

        return packageMsg;
    }

    CommodityMsgPart mapCommodityDomainToCommodityMsgPart(Commodity commodityDomain) {
        CommodityMsgPart commodity = new CommodityMsgPart();
        commodity.setId(commodityDomain.getExternalId());
        commodity.setName(commodityDomain.getName());
        commodity.setDescription(commodityDomain.getDescription());
        commodity.setCode(commodityDomain.getCode());
        commodity.setHsCode(commodityDomain.getHsCode());
        commodity.setNote(commodityDomain.getNote());
        commodity.setPackagingType(commodityDomain.getPackagingType());
        return commodity;
    }

    MeasuredValue mapValueUomPairToMeasuredValue(Number value, UnitOfMeasure uom) {
        if (value == null) {
            return null;
        }

        MeasuredValue measuredValue = new MeasuredValue();
        measuredValue.setValue(value);
        measuredValue.setUom(uom);
        return measuredValue;
    }
}
