package com.quincus.shipment.impl.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.JourneyStatus;
import com.quincus.shipment.api.constant.MilestoneCode;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.domain.Address;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Package;
import com.quincus.shipment.api.domain.PackageJourneySegment;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.impl.mapper.AddressMapper;
import com.quincus.shipment.impl.mapper.CommodityMapper;
import com.quincus.shipment.impl.mapper.CustomerMapper;
import com.quincus.shipment.impl.mapper.OrderMapper;
import com.quincus.shipment.impl.mapper.PackageDimensionMapper;
import com.quincus.shipment.impl.mapper.ServiceTypeMapper;
import com.quincus.shipment.impl.mapper.ShipmentMapper;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.CommodityEntity;
import com.quincus.shipment.impl.repository.entity.MilestoneEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.PackageEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.service.AddressService;
import com.quincus.shipment.impl.service.CustomerService;
import com.quincus.shipment.impl.service.LocationHierarchyService;
import com.quincus.shipment.impl.service.MilestoneService;
import com.quincus.shipment.impl.service.OrderService;
import com.quincus.shipment.impl.service.OrganizationService;
import com.quincus.shipment.impl.service.ServiceTypeService;
import com.quincus.shipment.impl.service.ShipmentFetchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Slf4j
@AllArgsConstructor
public class UpdateShipmentHelper {
    private static final String UPDATING_SHIPMENT = "Updating Shipment with ID '%s'";
    private final OrderService orderService;
    private final OrganizationService organizationService;
    private final CustomerService customerService;
    private final ServiceTypeService serviceTypeService;
    private final LocationHierarchyService locationHierarchyService;
    private final AddressService addressService;
    private final ObjectMapper objectMapper;
    private final MilestoneService milestoneService;
    private final ShipmentFetchService shipmentFetchService;

    public AddressEntity getUpdatedAddressEntityFromDomain(final Address addressDomain,
                                                           final AddressEntity addressEntity) {
        if (isNull(addressDomain)) {
            return addressEntity;
        }
        AddressEntity updatedAddressEntity = AddressMapper.mapDomainToEntity(addressDomain, addressEntity);
        updatedAddressEntity.setLocationHierarchy(locationHierarchyService.setUpLocationHierarchy(addressDomain, null));
        return updatedAddressEntity;
    }

    public void updatePackageEntityFromDomain(final Package packageDomain,
                                              final PackageEntity packageEntity) {
        if (isNull(packageDomain)) {
            return;
        }
        packageEntity.setRefId(packageDomain.getRefId());
        packageEntity.setTotalValue(packageDomain.getTotalValue());
        packageEntity.setCurrency(packageDomain.getCurrency());
        packageEntity.setType(packageDomain.getType());
        packageEntity.setTypeRefId(packageDomain.getTypeRefId());

        if (nonNull(packageDomain.getValue())) {
            packageEntity.setValue(packageDomain.getValue());
        }
        if (nonNull(packageDomain.getReadyTime())) {
            packageEntity.setReadyTime(packageDomain.getReadyTime());
        }
        packageEntity.setDimension(PackageDimensionMapper.mapDomainToEntity(packageDomain.getDimension()));

        List<CommodityEntity> commodityEntityList = CommodityMapper
                .mapDomainListToEntityListCommodity(packageDomain.getCommodities());
        if (CollectionUtils.isNotEmpty(packageEntity.getCommodities())) {
            Long existingVersion = null;
            Instant existingCreateTime = null;
            List<CommodityEntity> existingCommodityEntityList = packageEntity.getCommodities();
            if (!existingCommodityEntityList.isEmpty()) {
                existingVersion = existingCommodityEntityList.get(0).getVersion();
                existingCreateTime = existingCommodityEntityList.get(0).getCreateTime();
            }
            packageEntity.getCommodities().clear();
            packageEntity.getCommodities().addAll(commodityEntityList);
            for (CommodityEntity commodity : packageEntity.getCommodities()) {
                commodity.setVersion(existingVersion);
                commodity.setCreateTime(existingCreateTime);
            }
        } else {
            packageEntity.setCommodities(commodityEntityList);
        }
        packageEntity.setPricingInfo(packageDomain.getPricingInfo());
        packageEntity.setCode(packageDomain.getCode());
        packageEntity.setTotalItemsCount(packageDomain.getTotalItemsCount());
        packageEntity.setSource(packageDomain.getSource());
    }

    public void updateShipmentEntityFromDomain(
            final Shipment shipmentDomain,
            final ShipmentEntity shipmentEntity) {
        if ((isNull(shipmentDomain)) || (isNull(shipmentEntity)) || (isNull(objectMapper))) {
            return;
        }
        log.debug(String.format(UPDATING_SHIPMENT, shipmentEntity.getId()));
        shipmentEntity.setPartnerId(shipmentDomain.getPartnerId());
        shipmentEntity.setOrganization(organizationService.findOrCreateOrganization());
        shipmentEntity.setPickUpLocation(shipmentDomain.getPickUpLocation());
        shipmentEntity.setDeliveryLocation(shipmentDomain.getDeliveryLocation());
        shipmentEntity.setReturnLocation(shipmentDomain.getReturnLocation());
        shipmentEntity.setExtraCareInfo(shipmentDomain.getExtraCareInfo());
        shipmentEntity.setInsuranceInfo(shipmentDomain.getInsuranceInfo());
        shipmentEntity.setServiceType(serviceTypeService
                .findOrCreateServiceType(ServiceTypeMapper.mapDomainToEntity(shipmentDomain.getServiceType(), shipmentEntity.getOrganization().getId())));
        shipmentEntity.setUserId(shipmentDomain.getUserId());
        shipmentEntity.setSender(shipmentDomain.getSender());
        shipmentEntity.setOrigin(getUpdatedAddressEntityFromDomain(shipmentDomain.getOrigin(),
                shipmentEntity.getOrigin()));
        shipmentEntity.setConsignee(shipmentDomain.getConsignee());
        shipmentEntity.setDestination(getUpdatedAddressEntityFromDomain(shipmentDomain.getDestination(),
                shipmentEntity.getDestination()));

        updatePackageEntityFromDomain(shipmentDomain.getShipmentPackage(),
                shipmentEntity.getShipmentPackage());
        orderService.updateStatus(shipmentDomain.getOrder());

        OrderEntity orderEntity = updateOrderEntityFromDomain(shipmentDomain.getOrder(), shipmentEntity.getOrder());
        if (orderEntity != null) {
            shipmentEntity.setOrder(orderEntity);
        }

        shipmentEntity.setCustomer(customerService
                .findOrCreateCustomer(CustomerMapper.mapDomainToEntity(shipmentDomain.getCustomer())));
        ShipmentStatus shipmentStatus = shipmentDomain.getStatus();
        if (nonNull(shipmentStatus)) {
            shipmentEntity.setStatus(shipmentDomain.getStatus());
        }
        shipmentEntity.setEtaStatus(shipmentDomain.getEtaStatus());
        shipmentEntity.setInstructions(shipmentDomain.getInstructions());
        shipmentEntity.setNotes(shipmentDomain.getNotes());
        shipmentEntity.setShipmentTags(shipmentDomain.getShipmentTags());
        ShipmentUtil.convertOrderTimezonesToUtc(shipmentDomain.getOrder());
    }

    public OrderEntity updateOrderEntityFromDomain(final Order orderDomain,
                                                   final OrderEntity existingOrderEntity) {
        if (orderDomain == null) {
            return null;
        }

        if (existingOrderEntity != null) {
            String orderEntityId = existingOrderEntity.getId();
            if (orderEntityId != null && orderEntityId.equalsIgnoreCase(orderDomain.getId())) {
                OrderMapper.mapDomainToExistingEntity(existingOrderEntity, orderDomain, objectMapper);
                InstructionUtil.updateInstructionList(existingOrderEntity.getInstructions(), orderDomain.getInstructions());
                return existingOrderEntity;
            }
        }

        String orderId = orderDomain.getId();
        OrderEntity orderLookup = orderId != null ? orderService.findById(orderId) : null;
        if (orderLookup != null) {
            return orderLookup;
        }

        return OrderMapper.mapDomainToEntity(orderDomain, objectMapper);
    }

    public Shipment updateEtaStatus(Shipment shipmentDomain) {
        if (ShipmentStatus.COMPLETED.equals(shipmentDomain.getStatus())
                || ShipmentStatus.CANCELLED.equals(shipmentDomain.getStatus())) {
            ShipmentUtil.clearEtaStatus(shipmentDomain);
            return shipmentDomain;
        }
        return ShipmentUtil.updateEtaStatusFromSegmentStatuses(shipmentDomain);
    }

    public void setupAddress(List<PackageJourneySegment> packageJourneySegments) {
        addressService.setFacilityAddress(packageJourneySegments);
    }

    public boolean isSegmentsUpdated(String orderPayload) throws JsonProcessingException {
        return Optional.ofNullable(objectMapper.readTree(orderPayload))
                .map(jsonNode -> jsonNode.get("segments_updated"))
                .map(JsonNode::asBoolean)
                .orElse(Boolean.FALSE);
    }

    public boolean isShipmentCancelled(ShipmentEntity shipment) {
        return shipment.getStatus() == ShipmentStatus.CANCELLED
                || (shipment.getShipmentJourney() != null && shipment.getShipmentJourney().getStatus() == JourneyStatus.CANCELLED)
                || hasCancelledMilestone(shipment.getMilestoneEvents());
    }

    private boolean hasCancelledMilestone(Set<MilestoneEntity> milestoneEntities) {
        return CollectionUtils.isNotEmpty(milestoneEntities) &&
                milestoneEntities.stream().anyMatch(m -> MilestoneCode.OM_ORDER_CANCELED.equals(m.getMilestoneCode()));
    }

    @Transactional(readOnly = true)
    public Shipment getShipmentById(String id) {
        ShipmentEntity entity = shipmentFetchService.findByIdOrThrowException(id);
        Shipment shipment = ShipmentMapper.mapEntityToDomain(entity, objectMapper);
        setupAddress(shipment.getShipmentJourney().getPackageJourneySegments());
        milestoneService.setMilestoneEventsForShipment(entity, shipment);
        return shipment;
    }
}
