package com.quincus.order.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.order.api.OrderApi;
import com.quincus.shipment.api.ShipmentApi;
import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.constant.TriggeredFrom;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.OrderReference;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.dto.OrderShipmentResponse;
import com.quincus.web.common.exception.model.QuincusException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class OrderApiImpl implements OrderApi {
    private static final String ERROR_PARSING_ORDER_PAYLOAD = "Error parsing Order payload.";
    private static final String ERROR_DRAFT_ORDER_STATUS = "The requested operation cannot be performed on a draft entity: Order Id: %s";
    private static final String DRAFT_STATUS = "draft";
    private final ShipmentApi shipmentApi;
    private final ObjectMapper objectMapper;

    @Override
    public List<Shipment> createOrUpdateShipments(Order order, String orderPayload, String uuid) {
        validateOrderStatus(order);
        List<Shipment> shipments = shipmentApi.convertOrderMessageToShipments(orderPayload, uuid);
        if (CollectionUtils.isEmpty(shipments)) return Collections.emptyList();
        List<Shipment> result = new ArrayList<>(shipmentApi.createOrUpdate(shipments, order.isSegmentsUpdated()));

        cancelRemovedShipments(order, shipments);
        return result;
    }

    @Override
    public OrderShipmentResponse asyncCreateOrUpdateShipments(Order order, String orderPayload, String uuid) {
        validateOrderStatus(order);
        List<Shipment> shipments = shipmentApi.convertOrderMessageToShipments(orderPayload, uuid);
        List<String> shipmentTrackingIdsFromPayload = shipments.stream().map(Shipment::getShipmentTrackingId).toList();
        shipmentApi.asyncCreateOrUpdate(shipments, order.isSegmentsUpdated());
        cancelRemovedShipments(order, shipments);
        return new OrderShipmentResponse(order.getId(), order.getOrderIdLabel(), shipmentTrackingIdsFromPayload);
    }

    @Async("threadPoolTaskExecutor")
    protected void cancelRemovedShipments(Order order, List<Shipment> shipments) {
        List<String> shipmentTackingIdsFromPayload = new ArrayList<>();
        shipments.forEach(e -> shipmentTackingIdsFromPayload.add(e.getShipmentTrackingId()));
        List<Shipment> allShipmentsInOrder = shipmentApi.findAllRelatedFromOrder(order);
        List<String> removedShipmentIds = allShipmentsInOrder.stream()
                .filter(shp -> shp.getStatus() != ShipmentStatus.CANCELLED
                        && !shipmentTackingIdsFromPayload.contains(shp.getShipmentTrackingId()))
                .map(Shipment::getId)
                .toList();
        removedShipmentIds.forEach(id -> shipmentApi.cancel(id, TriggeredFrom.OM));
    }

    @Override
    public List<Shipment> createOrUpdateShipmentsLocal(String orderPayload, boolean segmentsUpdated) {
        validateOrderStatus(createOrderFromPayload(orderPayload));
        List<Shipment> shipments = shipmentApi.convertOrderMessageToShipments(orderPayload, UUID.randomUUID().toString());
        List<Shipment> result = new ArrayList<>();
        shipments.forEach(e -> result.add(shipmentApi.createOrUpdateLocal(e, segmentsUpdated)));
        return result;
    }

    @Override
    public Order createOrderFromPayload(String orderPayload) {
        try {
            final JsonNode orderNode = objectMapper.readTree(orderPayload);
            Order order = new Order();
            order.setId(asText(orderNode.get("id")));
            order.setTrackingUrl(asText(orderNode.get("tracking_url")));
            order.setStatus(asText(orderNode.get("status")));
            order.setOrderIdLabel(asText(orderNode.get("order_id_label")));
            order.setSegmentsUpdated(asBoolean(orderNode.get("segments_updated")));
            order.setUsedOpenApi(asBoolean(orderNode.get("used_open_api")));
            order.setOrderReferences(mapJsonArrayToList( orderNode.get("order_references"), OrderReference.class));
            String createdAt = Optional.ofNullable(asText(orderNode.get("created_at"))).orElse(OffsetDateTime.now(Clock.systemUTC()).toString());
            order.setTimeCreated(createdAt);
            return order;
        } catch (JsonProcessingException e) {
            throw new QuincusException(String.format(ERROR_PARSING_ORDER_PAYLOAD));
        }
    }

    @Override
    public boolean isOrderNotCancelled(final Order order) {
        return !StringUtils.equalsIgnoreCase("CANCELLED", order.getStatus());
    }

    private boolean asBoolean(JsonNode payload) {
        return Optional.ofNullable(payload)
                .map(JsonNode::asBoolean)
                .orElse(false);
    }

    private String asText(JsonNode payload) {
        return Optional.ofNullable(payload)
                .map(JsonNode::asText)
                .orElse(null);
    }

    private void validateOrderStatus(final Order order) {
        if (DRAFT_STATUS.equalsIgnoreCase(order.getStatus())) {
            throw new QuincusValidationException(String.format(ERROR_DRAFT_ORDER_STATUS, order.getOrderIdLabel()));
        }
    }

    private <T> List<T> mapJsonArrayToList(JsonNode jsonNode, Class<T> valueType) {
        List<T> resultList = new ArrayList<>();
        try {
            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    T mappedObject = objectMapper.treeToValue(node, valueType);
                    resultList.add(mappedObject);
                }
            }
        } catch (JsonProcessingException e) {
            log.warn("Error mapping json array to list", e);
        }
        return resultList;
    }
}
