package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.domain.HostedFile;
import com.quincus.shipment.api.domain.Milestone;
import com.quincus.shipment.api.domain.MilestoneAdditionalInfo;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateRequest;
import com.quincus.shipment.api.dto.ShipmentMilestoneOpsUpdateResponse;
import com.quincus.shipment.impl.repository.entity.AddressEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import com.quincus.shipment.impl.repository.entity.OrderEntity_;
import com.quincus.shipment.impl.repository.entity.OrganizationEntity;
import com.quincus.shipment.impl.repository.entity.PackageEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity;
import com.quincus.shipment.impl.repository.entity.ShipmentEntity_;
import com.quincus.shipment.impl.repository.entity.ShipmentJourneyEntity;
import com.quincus.shipment.impl.repository.entity.component.BaseEntity_;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mapstruct.factory.Mappers;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.quincus.shipment.impl.mapper.LocalDateMapper.toLocalDateTime;

@NoArgsConstructor(access = AccessLevel.NONE)
public class ShipmentMapper {

    private static final MilestoneMapper MILESTONE_MAPPER = Mappers.getMapper(MilestoneMapper.class);

    public static ShipmentEntity mapDomainToEntity(Shipment shipmentDomain,
                                                   ObjectMapper mapper) {
        if ((shipmentDomain == null) || (mapper == null)) {
            return null;
        }

        ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setShipmentTrackingId(shipmentDomain.getShipmentTrackingId());
        shipmentEntity.setPickUpLocation(shipmentDomain.getPickUpLocation());
        shipmentEntity.setDeliveryLocation(shipmentDomain.getDeliveryLocation());
        shipmentEntity.setReturnLocation(shipmentDomain.getReturnLocation());
        shipmentEntity.setExtraCareInfo(shipmentDomain.getExtraCareInfo());
        shipmentEntity.setInsuranceInfo(shipmentDomain.getInsuranceInfo());
        shipmentEntity.setUserId(shipmentDomain.getUserId());
        shipmentEntity.setShipmentPackage(PackageMapper.toEntity(shipmentDomain.getShipmentPackage()));
        shipmentEntity.setOrganization(OrganizationMapper.mapDomainToEntity(shipmentDomain.getOrganization()));
        shipmentEntity.setOrder(OrderMapper.mapDomainToEntity(shipmentDomain.getOrder(), mapper));
        shipmentEntity.setStatus(shipmentDomain.getStatus());
        shipmentEntity.setEtaStatus(shipmentDomain.getEtaStatus());
        shipmentEntity.setSender(shipmentDomain.getSender());
        shipmentEntity.setConsignee(shipmentDomain.getConsignee());
        shipmentEntity.setOrigin(AddressMapper.mapDomainToEntity(shipmentDomain.getOrigin()));
        shipmentEntity.setDestination(AddressMapper.mapDomainToEntity(shipmentDomain.getDestination()));
        shipmentEntity.setCustomer(CustomerMapper.mapDomainToEntity(shipmentDomain.getCustomer()));
        shipmentEntity.setServiceType(ServiceTypeMapper.mapDomainToEntity(shipmentDomain.getServiceType(), shipmentEntity.getOrganization().getId()));
        shipmentEntity.setPartnerId(shipmentDomain.getPartnerId());
        shipmentEntity.setInstructions(shipmentDomain.getInstructions());
        shipmentEntity.setNotes(shipmentDomain.getNotes());
        shipmentEntity.setShipmentReferenceId(shipmentDomain.getShipmentReferenceId());
        shipmentEntity.setShipmentTags(shipmentDomain.getShipmentTags());
        shipmentEntity.setShipmentAttachments(shipmentDomain.getShipmentAttachments());
        shipmentEntity.setExternalOrderId(shipmentDomain.getExternalOrderId());
        shipmentEntity.setInternalOrderId(shipmentDomain.getInternalOrderId());
        shipmentEntity.setCustomerOrderId(shipmentDomain.getCustomerOrderId());
        shipmentEntity.setDeleted(shipmentDomain.isDeleted());
        shipmentEntity.setDescription(shipmentDomain.getDescription());
        shipmentEntity.setDistanceUom(shipmentDomain.getDistanceUom());
        return shipmentEntity;
    }

    public static Shipment mapEntityToDomain(ShipmentEntity shipmentEntity,
                                             ObjectMapper mapper) {
        if ((shipmentEntity == null) || (mapper == null)) {
            return null;
        }

        Shipment shipmentDomain = new Shipment();
        shipmentDomain.setPartnerId(shipmentEntity.getPartnerId());
        shipmentDomain.setId(shipmentEntity.getId());
        shipmentDomain.setPickUpLocation(shipmentEntity.getPickUpLocation());
        shipmentDomain.setDeliveryLocation(shipmentEntity.getDeliveryLocation());
        shipmentDomain.setUserId(shipmentEntity.getUserId());
        shipmentDomain.setOrganization(OrganizationMapper.mapEntityToDomain(shipmentEntity.getOrganization()));
        shipmentDomain.setShipmentPackage(PackageMapper.toDomain(shipmentEntity.getShipmentPackage()));
        shipmentDomain.setReturnLocation(shipmentEntity.getReturnLocation());
        shipmentDomain.setExtraCareInfo(shipmentEntity.getExtraCareInfo());
        shipmentDomain.setInsuranceInfo(shipmentEntity.getInsuranceInfo());

        shipmentDomain.setStatus(shipmentEntity.getStatus());
        shipmentDomain.setEtaStatus(shipmentEntity.getEtaStatus());

        shipmentDomain.setShipmentTrackingId(shipmentEntity.getShipmentTrackingId());
        shipmentDomain.setOrder(OrderMapper.mapEntityToDomain(shipmentEntity.getOrder()));
        shipmentDomain.setOrderId(shipmentEntity.getOrderId());
        shipmentDomain.setSender(shipmentEntity.getSender());
        shipmentDomain.setConsignee(shipmentEntity.getConsignee());
        shipmentDomain.setOrigin(AddressMapper.mapEntityToDomain(shipmentEntity.getOrigin()));
        shipmentDomain.setDestination(AddressMapper.mapEntityToDomain(shipmentEntity.getDestination()));
        shipmentDomain.setCustomer(CustomerMapper.mapEntityToDomain(shipmentEntity.getCustomer()));
        shipmentDomain.setServiceType(ServiceTypeMapper.mapEntityToDomain(shipmentEntity.getServiceType()));

        Optional.ofNullable(ShipmentJourneyMapper.mapEntityToDomain(shipmentEntity.getShipmentJourney()))
                .ifPresent(shipmentJourney -> {
                    shipmentJourney.setOrderId(shipmentEntity.getOrderId());
                    shipmentDomain.setShipmentJourney(shipmentJourney);
                });

        shipmentDomain.setInstructions(shipmentEntity.getInstructions());
        shipmentDomain.setNotes(shipmentEntity.getNotes());
        shipmentDomain.setShipmentReferenceId(shipmentEntity.getShipmentReferenceId());
        shipmentDomain.setShipmentTags(shipmentEntity.getShipmentTags());
        shipmentDomain.setCreatedTime(toLocalDateTime(shipmentEntity.getCreateTime()));
        shipmentDomain.setLastUpdatedTime(toLocalDateTime(shipmentEntity.getModifyTime()));
        shipmentDomain.setShipmentAttachments(shipmentEntity.getShipmentAttachments());
        if (shipmentEntity.getMilestoneEvents() != null) {
            shipmentDomain.setMilestoneEvents(
                    shipmentEntity.getMilestoneEvents().stream()
                            .map(MILESTONE_MAPPER::toDomain)
                            .toList());
        }
        shipmentDomain.setExternalOrderId(shipmentEntity.getExternalOrderId());
        shipmentDomain.setInternalOrderId(shipmentEntity.getInternalOrderId());
        shipmentDomain.setCustomerOrderId(shipmentEntity.getCustomerOrderId());
        shipmentDomain.setDeleted(shipmentEntity.isDeleted());
        shipmentDomain.setDescription(shipmentEntity.getDescription());
        shipmentDomain.setDistanceUom(shipmentEntity.getDistanceUom());
        return shipmentDomain;
    }

    public static List<ShipmentEntity> mapDomainListToEntityListShipment(List<Shipment> shipmentDomainList,
                                                                         ObjectMapper mapper) {
        if ((shipmentDomainList == null) || (mapper == null)) {
            return Collections.emptyList();
        }

        List<ShipmentEntity> shipmentEntityList = new ArrayList<>(shipmentDomainList.size());
        for (Shipment shipment : shipmentDomainList) {
            shipmentEntityList.add(mapDomainToEntity(shipment, mapper));
        }

        return shipmentEntityList;
    }

    public static List<Shipment> mapEntityListToDomainListShipment(List<ShipmentEntity> shipmentEntityEntityList,
                                                                   ObjectMapper mapper) {
        if ((shipmentEntityEntityList == null) || (mapper == null)) {
            return Collections.emptyList();
        }

        List<Shipment> shipmentDomainList = new ArrayList<>(shipmentEntityEntityList.size());
        for (ShipmentEntity shipmentEntity : shipmentEntityEntityList) {
            shipmentDomainList.add(mapEntityToDomain(shipmentEntity, mapper));
        }

        return shipmentDomainList;
    }

    public static List<Shipment> mapEntitiesForListing(List<ShipmentEntity> entities) {
        List<Shipment> shipments = new ArrayList<>();
        for (ShipmentEntity shipmentEntity : entities) {
            Shipment shipment = new Shipment();
            shipment.setDeleted(shipmentEntity.isDeleted());
            shipment.setDescription(shipmentEntity.getDescription());
            shipment.setEtaStatus(shipmentEntity.getEtaStatus());
            shipment.setId(shipmentEntity.getId());
            shipment.setOrder(OrderMapper.mapEntityToDomain(shipmentEntity.getOrder()));
            shipment.setOrigin(AddressMapper.mapEntityToDomain(shipmentEntity.getOrigin()));
            shipment.setDestination(AddressMapper.mapEntityToDomain(shipmentEntity.getDestination()));
            shipment.setShipmentTrackingId(shipmentEntity.getShipmentTrackingId());
            shipment.setShipmentJourney(ShipmentJourneyMapper.mapEntityToDomainForListing(shipmentEntity.getShipmentJourney()));
            shipment.setDescription(shipmentEntity.getDescription());
            if (shipmentEntity.getMilestoneEvents() != null) {
                shipment.setMilestoneEvents(
                        shipmentEntity.getMilestoneEvents().stream()
                                .map(MILESTONE_MAPPER::toDomain)
                                .toList());
            }
            shipments.add(shipment);
        }
        return shipments;
    }

    public static ShipmentEntity toShipmentEntity(Tuple tuple) {
        final ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(tuple.get(BaseEntity_.ID, String.class));
        shipmentEntity.setShipmentJourney(tuple.get(ShipmentEntity_.SHIPMENT_JOURNEY, ShipmentJourneyEntity.class));
        shipmentEntity.setShipmentTrackingId(tuple.get(ShipmentEntity_.SHIPMENT_TRACKING_ID, String.class));
        shipmentEntity.setStatus(tuple.get(ShipmentEntity_.STATUS, ShipmentStatus.class));
        shipmentEntity.setPartnerId(tuple.get(ShipmentEntity_.PARTNER_ID, String.class));
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(tuple.get(ShipmentEntity_.ORDER_ID, String.class));
        orderEntity.setPickupTimezone(tuple.get(OrderEntity_.PICKUP_TIMEZONE, String.class));
        orderEntity.setDeliveryTimezone(tuple.get(OrderEntity_.DELIVERY_TIMEZONE, String.class));
        shipmentEntity.setOrder(orderEntity);
        return shipmentEntity;
    }

    public static ShipmentEntity toShipmentEntityForShipmentJourneyUpdate(ObjectMapper objectMapper, Tuple tuple) {
        final ShipmentEntity shipmentEntity = new ShipmentEntity();
        shipmentEntity.setId(tuple.get(BaseEntity_.ID, String.class));
        shipmentEntity.setPartnerId(tuple.get(ShipmentEntity_.PARTNER_ID, String.class));
        shipmentEntity.setOrder(tuple.get(ShipmentEntity_.ORDER, OrderEntity.class));
        shipmentEntity.setShipmentJourney(tuple.get(ShipmentEntity_.SHIPMENT_JOURNEY, ShipmentJourneyEntity.class));
        shipmentEntity.setShipmentTrackingId(tuple.get(ShipmentEntity_.SHIPMENT_TRACKING_ID, String.class));
        shipmentEntity.setOrganization(tuple.get(ShipmentEntity_.ORGANIZATION, OrganizationEntity.class));
        shipmentEntity.setShipmentPackage(tuple.get(ShipmentEntity_.SHIPMENT_PACKAGE, PackageEntity.class));
        shipmentEntity.setShipmentReferenceId(readJsonArray(objectMapper, tuple.get(ShipmentEntity_.SHIPMENT_REFERENCE_ID, List.class)));
        shipmentEntity.setShipmentTags(readJsonArray(objectMapper, tuple.get(ShipmentEntity_.SHIPMENT_TAGS, List.class)));
        shipmentEntity.setExternalOrderId(tuple.get(ShipmentEntity_.EXTERNAL_ORDER_ID, String.class));
        shipmentEntity.setInternalOrderId(tuple.get(ShipmentEntity_.INTERNAL_ORDER_ID, String.class));
        shipmentEntity.setCustomerOrderId(tuple.get(ShipmentEntity_.CUSTOMER_ORDER_ID, String.class));
        shipmentEntity.setOrigin(tuple.get(ShipmentEntity_.ORIGIN, AddressEntity.class));
        shipmentEntity.setDestination(tuple.get(ShipmentEntity_.DESTINATION, AddressEntity.class));
        shipmentEntity.setStatus(tuple.get(ShipmentEntity_.STATUS, ShipmentStatus.class));
        shipmentEntity.setDeleted(tuple.get(ShipmentEntity_.DELETED, Boolean.class));
        shipmentEntity.setDescription(tuple.get(ShipmentEntity_.DESCRIPTION, String.class));
        return shipmentEntity;
    }

    public static Shipment toShipmentForShipmentJourneyUpdate(ShipmentEntity entity) {
        Shipment domain = new Shipment();
        domain.setId(entity.getId());
        domain.setUserId(entity.getUserId());
        domain.setShipmentTrackingId(entity.getShipmentTrackingId());
        domain.setPartnerId(entity.getPartnerId());
        domain.setOrder(OrderMapper.toOrderForShipmentJourneyUpdate(entity.getOrder()));
        domain.setOrganization(OrganizationMapper.mapEntityToDomain(entity.getOrganization()));
        domain.setShipmentPackage(PackageMapper.toDomain(entity.getShipmentPackage()));
        domain.setShipmentReferenceId(entity.getShipmentReferenceId());
        domain.setShipmentTags(entity.getShipmentTags());
        domain.setOrigin(AddressMapper.mapEntityToDomain(entity.getOrigin()));
        domain.setDestination(AddressMapper.mapEntityToDomain(entity.getDestination()));
        domain.setSender(entity.getSender());
        domain.setConsignee(entity.getConsignee());
        domain.setStatus(entity.getStatus());
        domain.setServiceType(ServiceTypeMapper.mapEntityToDomain(entity.getServiceType()));
        domain.setNotes(entity.getNotes());
        domain.setDescription(entity.getDescription());

        domain.setExternalOrderId(entity.getExternalOrderId());
        domain.setInternalOrderId(entity.getInternalOrderId());
        domain.setCustomerOrderId(entity.getCustomerOrderId());
        domain.setDeleted(entity.isDeleted());
        domain.setDescription(entity.getDescription());
        domain.setDistanceUom(entity.getDistanceUom());
        return domain;
    }

    public static ShipmentMilestoneOpsUpdateResponse toShipmentMilestoneOpsUpdateResponse(ShipmentMilestoneOpsUpdateRequest infoRequest,
                                                                                          Milestone previousMilestone,
                                                                                          Milestone currentMilestone,
                                                                                          Shipment updatedShipment) {
        String previousMilestoneName = Optional.ofNullable(previousMilestone).map(Milestone::getMilestoneName).orElse(null);
        String previousMilestoneCode = Optional.ofNullable(previousMilestone).map(Milestone::getMilestoneCode).map(Object::toString).orElse(null);
        String currentMilestoneCode = Optional.ofNullable(currentMilestone.getMilestoneCode()).map(Object::toString).orElse(infoRequest.getMilestoneCode());
        String notes = Optional.ofNullable(currentMilestone.getAdditionalInfo()).map(MilestoneAdditionalInfo::getRemarks).orElse(infoRequest.getNotes());
        List<HostedFile> attachments = Optional.ofNullable(currentMilestone.getAdditionalInfo())
                .map(ShipmentMapper::combineAttachments).orElse(null);
        return new ShipmentMilestoneOpsUpdateResponse()
                .shipmentId(updatedShipment.getId())
                .previousMilestoneName(previousMilestoneName)
                .previousMilestoneCode(previousMilestoneCode)
                .currentMilestoneName(currentMilestone.getMilestoneName())
                .currentMilestoneId(currentMilestone.getId())
                .currentMilestoneCode(currentMilestoneCode)
                .shipmentTrackingId(updatedShipment.getShipmentTrackingId())
                .organizationId(updatedShipment.getOrganization().getId())
                .notes(notes)
                .attachments(attachments)
                .updatedBy(currentMilestone.getUserName())
                .milestoneTime(currentMilestone.getMilestoneTime())
                .usersLocation(infoRequest.getUsersLocation());
    }

    private static List<HostedFile> combineAttachments(MilestoneAdditionalInfo additionalInfo) {
        List<HostedFile> imageAttachments = Optional.ofNullable(additionalInfo.getImages()).orElse(Collections.emptyList());
        List<HostedFile> signatureAttachments = Optional.ofNullable(additionalInfo.getSignature()).orElse(Collections.emptyList());
        List<HostedFile> genericAttachments = Optional.ofNullable(additionalInfo.getAttachments()).orElse(Collections.emptyList());

        return Stream.concat(Stream.concat(imageAttachments.stream(), signatureAttachments.stream()), genericAttachments.stream()).toList();
    }

    private static List<String> readJsonArray(ObjectMapper objectMapper, List<? extends Object> values) {
        List<String> stringList = Collections.emptyList();
        if (values != null) {
            stringList = objectMapper.convertValue(values, new TypeReference<>() {});
        }
        return stringList;
    }

}
