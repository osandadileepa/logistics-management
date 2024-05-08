package com.quincus.shipment.impl.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.impl.repository.entity.OrderEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.NONE)
public class OrderMapper {
    private static final InstructionMapper INSTRUCTION_MAPPER = Mappers.getMapper(InstructionMapper.class);

    public static OrderEntity mapDomainToEntity(Order orderDomain, ObjectMapper mapper) {
        if ((orderDomain == null) || (mapper == null)) {
            return null;
        }

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderDomain.getId());
        mapDomainToExistingEntity(orderEntity, orderDomain, mapper);

        if (!CollectionUtils.isEmpty(orderDomain.getInstructions())) {
            orderEntity.setInstructions(
                    orderDomain.getInstructions().stream()
                            .map(INSTRUCTION_MAPPER::toEntity)
                            .collect(Collectors.toCollection(ArrayList::new))
            );
        }
        return orderEntity;
    }

    public static void mapDomainToExistingEntity(@NotNull OrderEntity orderEntity, @NotNull Order orderDomain,
                                                 @NotNull ObjectMapper mapper) {
        orderEntity.setGroup(orderDomain.getGroup());
        orderEntity.setPickupStartTime(orderDomain.getPickupStartTime());
        orderEntity.setPickupCommitTime(orderDomain.getPickupCommitTime());
        orderEntity.setPickupTimezone(orderDomain.getPickupTimezone());
        orderEntity.setDeliveryStartTime(orderDomain.getDeliveryStartTime());
        orderEntity.setDeliveryCommitTime(orderDomain.getDeliveryCommitTime());
        orderEntity.setDeliveryTimezone(orderDomain.getDeliveryTimezone());
        orderEntity.setCustomerReferenceId(orderDomain.getCustomerReferenceId());
        orderEntity.setOrderIdLabel(orderDomain.getOrderIdLabel());
        orderEntity.setTrackingUrl(orderDomain.getTrackingUrl());
        orderEntity.setNotes(orderDomain.getNotes());
        orderEntity.setTags(orderDomain.getTags());
        orderEntity.setAttachments(orderDomain.getAttachments());
        orderEntity.setOpsType(orderDomain.getOpsType());
        orderEntity.setData(mapper.convertValue(orderDomain.getData(), JsonNode.class));
        orderEntity.setStatus(orderDomain.getStatus());
        orderEntity.setCancelReason(orderDomain.getCancelReason());
        orderEntity.setOrderReferences(orderDomain.getOrderReferences());
    }

    public static Order mapEntityToDomain(OrderEntity orderEntity) {
        if (orderEntity == null) {
            return null;
        }

        Order orderDomain = new Order();
        orderDomain.setId(orderEntity.getId());
        orderDomain.setGroup(orderEntity.getGroup());
        orderDomain.setPickupStartTime(orderEntity.getPickupStartTime());
        orderDomain.setPickupCommitTime(orderEntity.getPickupCommitTime());
        orderDomain.setPickupTimezone(orderEntity.getPickupTimezone());
        orderDomain.setDeliveryStartTime(orderEntity.getDeliveryStartTime());
        orderDomain.setDeliveryCommitTime(orderEntity.getDeliveryCommitTime());
        orderDomain.setDeliveryTimezone(orderEntity.getDeliveryTimezone());
        orderDomain.setCustomerReferenceId(orderEntity.getCustomerReferenceId());
        orderDomain.setOrderIdLabel(orderEntity.getOrderIdLabel());
        orderDomain.setTrackingUrl(orderEntity.getTrackingUrl());
        orderDomain.setNotes(orderEntity.getNotes());
        orderDomain.setTags(orderEntity.getTags());
        orderDomain.setAttachments(orderEntity.getAttachments());
        orderDomain.setOpsType(orderEntity.getOpsType());
        orderDomain.setStatus(orderEntity.getStatus());
        orderDomain.setCancelReason(orderEntity.getCancelReason());
        orderDomain.setOrderReferences(orderEntity.getOrderReferences());
        if (!CollectionUtils.isEmpty(orderEntity.getInstructions())) {
            orderDomain.setInstructions(
                    orderEntity.getInstructions().stream()
                            .map(INSTRUCTION_MAPPER::toDomain)
                            .collect(Collectors.toCollection(ArrayList::new))
            );
        }
        JsonNode data = orderEntity.getData();
        if (data != null) {
            orderDomain.setData(data.asText());
        }

        return orderDomain;
    }

    public static Order toOrderForShipmentJourneyUpdate(OrderEntity orderEntity) {
        if (orderEntity == null) {
            return null;
        }

        Order orderDomain = new Order();
        orderDomain.setOrganizationId(orderEntity.getOrganizationId());
        orderDomain.setId(orderEntity.getId());
        orderDomain.setGroup(orderEntity.getGroup());
        orderDomain.setPickupStartTime(orderEntity.getPickupStartTime());
        orderDomain.setPickupCommitTime(orderEntity.getPickupCommitTime());
        orderDomain.setPickupTimezone(orderEntity.getPickupTimezone());
        orderDomain.setDeliveryStartTime(orderEntity.getDeliveryStartTime());
        orderDomain.setDeliveryCommitTime(orderEntity.getDeliveryCommitTime());
        orderDomain.setDeliveryTimezone(orderEntity.getDeliveryTimezone());
        orderDomain.setCustomerReferenceId(orderEntity.getCustomerReferenceId());
        orderDomain.setOrderIdLabel(orderEntity.getOrderIdLabel());
        orderDomain.setTrackingUrl(orderEntity.getTrackingUrl());
        orderDomain.setNotes(orderEntity.getNotes());
        orderDomain.setTags(orderEntity.getTags());
        orderDomain.setAttachments(orderEntity.getAttachments());
        orderDomain.setOpsType(orderEntity.getOpsType());
        orderDomain.setStatus(orderEntity.getStatus());
        orderDomain.setCancelReason(orderEntity.getCancelReason());
        orderDomain.setOrderReferences(orderEntity.getOrderReferences());

        JsonNode data = orderEntity.getData();
        if (data != null) {
            orderDomain.setData(data.asText());
        }

        return orderDomain;
    }
}
