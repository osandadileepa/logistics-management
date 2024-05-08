package com.quincus.shipment.impl.mapper;

import com.quincus.ext.DateTimeUtil;
import com.quincus.order.api.domain.Attachment;
import com.quincus.order.api.domain.CommoditiesPackage;
import com.quincus.order.api.domain.CustomerReference;
import com.quincus.order.api.domain.Package;
import com.quincus.order.api.domain.Packaging;
import com.quincus.order.api.domain.PricingInfo;
import com.quincus.order.api.domain.Root;
import com.quincus.order.api.domain.Shipper;
import com.quincus.shipment.api.constant.MeasurementUnit;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Commodity;
import com.quincus.shipment.api.domain.Consignee;
import com.quincus.shipment.api.domain.Instruction;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.OrderAttachment;
import com.quincus.shipment.api.domain.Organization;
import com.quincus.shipment.api.domain.PackageDimension;
import com.quincus.shipment.api.domain.Sender;
import com.quincus.shipment.api.domain.ServiceType;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.helper.EnumUtil;
import com.quincus.shipment.impl.helper.ShipmentUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.quincus.shipment.api.constant.MilestoneCode.OM_BOOKED;
import static java.util.Objects.nonNull;

@NoArgsConstructor(access = AccessLevel.NONE)
@Slf4j
public class OrderToShipmentMapper {

    public static Shipment mapOrderMessageToShipmentDomain(Package shipmentPackageMessage, Root omMessage) {
        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setUserId(omMessage.getUserId());
        shipmentDomain.setShipmentTrackingId(shipmentPackageMessage.getShipmentIdLabel());
        shipmentDomain.setId(omMessage.getTransactionId());
        shipmentDomain.setPartnerId(omMessage.getPartnerId());
        shipmentDomain.setPickUpLocation(omMessage.getOrigin().getId());
        shipmentDomain.setDeliveryLocation(omMessage.getDestination().getId());
        shipmentDomain.setNotes(shipmentPackageMessage.getNote());
        Organization organization = new Organization();
        organization.setId(omMessage.getOrganisationId());
        shipmentDomain.setOrganization(organization);
        Sender sender = new Sender();
        sender.setName(omMessage.getShipper().getName());
        sender.setEmail(omMessage.getShipper().getEmail());
        sender.setContactNumber(omMessage.getShipper().getPhone());
        shipmentDomain.setServiceType(createServiceType(omMessage.getServiceType(), omMessage.getServiceTypeId()));
        shipmentDomain.setShipmentPackage(createPackage(shipmentPackageMessage, omMessage.getPricingInfo()));
        shipmentDomain.setConsignee(createConsignee(omMessage.getConsignee()));
        shipmentDomain.setOrigin(OMLocationMapper.mapToAddress(omMessage.getOrigin()));
        shipmentDomain.setDestination(OMLocationMapper.mapToAddress(omMessage.getDestination()));
        shipmentDomain.setSender(createSender(omMessage.getShipper()));
        shipmentDomain.setMilestone(createMilestone());
        shipmentDomain.setShipmentReferenceId(Collections.singletonList(shipmentPackageMessage.getAdditionalData1()));
        shipmentDomain.setDescription(shipmentPackageMessage.getDescription());
        shipmentDomain.setShipmentTags(shipmentPackageMessage.getAllTagsList());
        shipmentDomain.setInternalOrderId(omMessage.getOrderIdLabel());
        shipmentDomain.setExternalOrderId(omMessage.getExternalOrderId());
        shipmentDomain.setCustomerOrderId(omMessage.getCustomerOrderId());
        shipmentDomain.setSegmentsUpdatedFromSource(omMessage.isSegmentsUpdated());
        shipmentDomain.setDistanceUom(ShipmentUtil.convertCalculatedMileageUomStringToUom(omMessage.getDistanceUom()));
        return shipmentDomain;
    }

    public static Order mapOrderMessageToShipmentOrder(Root root) {
        Order order = new Order();
        order.setOrganizationId(root.getOrganisationId());
        order.setStatus(root.getStatus());
        order.setTimeCreated(root.getCreatedAt());
        order.setId(root.getId());
        order.setCustomerReferenceId(createCustomerReferenceIds(root.getCustomerReferences()));
        order.setOrderIdLabel(root.getOrderIdLabel());
        order.setTrackingUrl(root.getTrackingUrl());
        order.setPickupStartTime(DateTimeUtil.toIsoDateTimeFormat(root.getPickupStartTime()));
        order.setPickupCommitTime(DateTimeUtil.toIsoDateTimeFormat(root.getPickupCommitTime()));
        order.setPickupTimezone(root.getPickupTimezone());
        order.setDeliveryStartTime(DateTimeUtil.toIsoDateTimeFormat(root.getDeliveryStartTime()));
        order.setDeliveryCommitTime(DateTimeUtil.toIsoDateTimeFormat(root.getDeliveryCommitTime()));
        order.setDeliveryTimezone(root.getDeliveryTimezone());
        order.setNotes(root.getNote());
        order.setTags(root.getTagList());
        order.setOpsType(root.getOpsType());
        order.setAttachments(createAttachments(root.getAttachments()));
        order.setCancelReason(root.getCancelReason());
        order.setCode(root.getShipmentCode());
        List<Instruction> instructions = OMInstructionMapper.mapOmInstructions(root.getInstructions(),
                root.getOrganisationId(), root.getId());
        order.setInstructions(instructions);
        order.setOrderReferences(root.getOrderReferences());
        return order;
    }

    private static Milestone createMilestone() {
        Milestone milestone = new Milestone();
        milestone.setMilestoneCode(OM_BOOKED);
        milestone.setMilestoneName("DESC");
        return milestone;
    }

    private static List<OrderAttachment> createAttachments(List<Attachment> objects) {
        List<OrderAttachment> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(objects)) {
            objects.forEach(e -> {
                OrderAttachment oa = new OrderAttachment();
                oa.setId(e.getId());
                oa.setFileName(e.getFileName());
                oa.setFileSize(e.getFileSize());
                oa.setFileUrl(e.getFileUrl());
                list.add(oa);
            });
        }
        return list;
    }

    private static List<String> createCustomerReferenceIds(List<CustomerReference> customerReferences) {
        if (CollectionUtils.isEmpty(customerReferences)) {
            return Collections.emptyList();
        }
        return customerReferences.stream().map(CustomerReference::getIdLabel).toList();
    }

    private static Sender createSender(Shipper pShipper) {
        Sender sender = new Sender();
        sender.setName(pShipper.getName());
        sender.setEmail(pShipper.getEmail());
        sender.setContactNumber(pShipper.getPhone());
        if (nonNull(pShipper.getShipperPhoneCodeId())) {
            sender.setContactCode(pShipper.getShipperPhoneCodeId().getCode());
        }
        return sender;
    }

    private static Consignee createConsignee(com.quincus.order.api.domain.Consignee pConsignee) {
        Consignee consignee = new Consignee();
        consignee.setName(pConsignee.getName());
        consignee.setEmail(pConsignee.getEmail());
        consignee.setContactNumber(pConsignee.getPhone());
        if (nonNull(pConsignee.getConsigneePhoneCodeId())) {
            consignee.setContactCode(pConsignee.getConsigneePhoneCodeId().getCode());
        }
        return consignee;
    }

    private static com.quincus.shipment.api.domain.Package createPackage(Package pPackage, PricingInfo srcPricingInfo) {
        com.quincus.shipment.api.domain.Package pkg = new com.quincus.shipment.api.domain.Package();
        pkg.setId(pPackage.getId());
        pkg.setCode(pPackage.getCode());
        pkg.setRefId(pPackage.getId());
        pkg.setType(pPackage.getPackageType());
        Packaging packagingInfo = pPackage.getPackaging();
        if (nonNull(packagingInfo)) {
            pkg.setTypeRefId(packagingInfo.getId());
        }
        pkg.setTotalItemsCount((long) pPackage.getItemsCount());
        pkg.setSource(TriggeredFrom.OM);

        PackageDimension dimension = new PackageDimension();
        dimension.setMeasurementUnit(EnumUtil.toEnum(MeasurementUnit.class, pPackage.getMeasurementUnits()));
        dimension.setLength(BigDecimal.valueOf(pPackage.getLength()));
        dimension.setWidth(BigDecimal.valueOf(pPackage.getWidth()));
        dimension.setHeight(BigDecimal.valueOf(pPackage.getHeight()));
        dimension.setVolumeWeight(BigDecimal.valueOf(pPackage.getVolumeWeight()));
        dimension.setGrossWeight(BigDecimal.valueOf(pPackage.getGrossWeight()));
        dimension.setChargeableWeight(BigDecimal.valueOf(pPackage.getChargeableWeight()));
        pkg.setDimension(dimension);

        List<Commodity> commodityList = new ArrayList<>();
        if (nonNull(pPackage.getCommoditiesPackages())) {
            for (CommoditiesPackage srcCommodity : pPackage.getCommoditiesPackages()) {
                Commodity commodity = new Commodity();
                commodity.setExternalId(srcCommodity.getCommodityId());
                commodity.setName(srcCommodity.getCommodityName());
                commodity.setQuantity((long) srcCommodity.getItemsCount());
                commodity.setValue(Optional.ofNullable(srcCommodity.getValueOfGoods())
                        .map(BigDecimal::new)
                        .orElse(null));
                commodity.setDescription(srcCommodity.getDescription());
                commodity.setCode(srcCommodity.getCode());
                commodity.setHsCode(srcCommodity.getHsCode());
                commodity.setNote(srcCommodity.getNote());
                commodity.setPackagingType(srcCommodity.getPackagingType());
                commodityList.add(commodity);
            }
        }
        pkg.setCommodities(commodityList);

        pkg.setTotalValue(BigDecimal.valueOf(pPackage.getValueOfGoods()));
        if (nonNull(srcPricingInfo)) {
            pkg.setCurrency(srcPricingInfo.getCurrencyCode());
            com.quincus.shipment.api.domain.PricingInfo pricingInfo = new com.quincus.shipment.api.domain.PricingInfo();
            pricingInfo.setExternalId(srcPricingInfo.getId());
            pricingInfo.setCurrency(srcPricingInfo.getCurrencyCode());
            pricingInfo.setBaseTariff(BigDecimal.valueOf(srcPricingInfo.getBaseTariff()));
            pricingInfo.setServiceTypeCharge(BigDecimal.valueOf(srcPricingInfo.getServiceTypeCharge()));
            pricingInfo.setSurcharge(BigDecimal.valueOf(srcPricingInfo.getSurcharge()));
            pricingInfo.setInsuranceCharge(BigDecimal.valueOf(srcPricingInfo.getInsuranceCharge()));
            pricingInfo.setExtraCareCharge(BigDecimal.valueOf(0.00));
            pricingInfo.setDiscount(BigDecimal.valueOf(srcPricingInfo.getDiscount()));
            pricingInfo.setTax(BigDecimal.valueOf(srcPricingInfo.getTax()));
            pricingInfo.setCod(BigDecimal.valueOf(srcPricingInfo.getCod()));
            pricingInfo.setTotal(BigDecimal.valueOf(srcPricingInfo.getTotal()));
            pkg.setPricingInfo(pricingInfo);
        }
        pkg.setReadyTime(null);
        pkg.setCode(pPackage.getCode());
        return pkg;
    }

    private static ServiceType createServiceType(String serviceType, String serviceTypeId) {
        ServiceType sType = new ServiceType();
        sType.setId(serviceTypeId);
        sType.setCode(serviceType);
        sType.setName(serviceType);
        return sType;
    }
}
