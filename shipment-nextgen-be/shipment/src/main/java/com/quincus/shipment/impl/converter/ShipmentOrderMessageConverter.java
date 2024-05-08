package com.quincus.shipment.impl.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.order.api.domain.Package;
import com.quincus.order.api.domain.Root;
import com.quincus.order.api.domain.SegmentsPayload;
import com.quincus.shipment.api.domain.Order;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.shipment.api.domain.ShipmentJourney;
import com.quincus.shipment.impl.helper.journey.ShipmentJourneyProvider;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.validator.RootOrderValidator;
import com.quincus.web.common.exception.model.OrganizationDetailsNotFoundException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.quincus.shipment.impl.mapper.OrderToShipmentMapper.mapOrderMessageToShipmentDomain;
import static com.quincus.shipment.impl.mapper.OrderToShipmentMapper.mapOrderMessageToShipmentOrder;

@Slf4j
@Component
@AllArgsConstructor
public class ShipmentOrderMessageConverter {

    private static final String PARSING_OM_PAYLOAD_ERROR_MESSAGE = "Failed to parse order message to Root object";
    private static final String PROCESSING_OM_MESSAGE = "Processing OM Message with Order ID %s and Transaction ID %s";
    private static final String ERROR_IN_MAPPING_ORDER_TO_SHIPMENT = "Error in mapping Order to Shipment: ";
    private static final String SHIPMENT_DOMAIN_JSON = "Shipment Domain Payload: %s, Transaction Id: %s";
    private static final String SEGMENTS_PAYLOAD = "segments_payload";
    private ObjectMapper objectMapper;
    private UserDetailsProvider userDetailsProvider;
    private ShipmentJourneyProvider shipmentJourneyProvider;
    private RootOrderValidator rootOrderValidator;

    public List<Shipment> convertOrderMessageToShipments(String orderMessagePayload, String transactionId) {
        return Optional.of(parseOrderMessageToRoot(orderMessagePayload))
                .map(root -> createShipmentListFromRoot(root, transactionId, orderMessagePayload))
                .orElseGet(List::of);
    }

    private Root parseOrderMessageToRoot(String orderMessage) {
        try {
            Root orderRoot = objectMapper.readValue(orderMessage, Root.class);
            orderRoot.setOrganisationId(userDetailsProvider.getCurrentOrganizationId());
            orderRoot.setSegmentsPayloads(createSegmentsFromOrderMessage(orderMessage, orderRoot.getIsSegment()));
            rootOrderValidator.validate(orderRoot);
            return orderRoot;
        } catch (OrganizationDetailsNotFoundException | QuincusValidationException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(PARSING_OM_PAYLOAD_ERROR_MESSAGE, e);
            throw new QuincusValidationException(PARSING_OM_PAYLOAD_ERROR_MESSAGE, e);
        }
    }


    private List<Shipment> createShipmentListFromRoot(Root orderRoot, String transactionId, String orderMessage) {
        List<Shipment> shipmentList = new ArrayList<>();
        try {
            orderRoot.setTransactionId(transactionId);

            log.info(String.format(PROCESSING_OM_MESSAGE, orderRoot.getId(), orderRoot.getTransactionId()));
            ShipmentJourney shipmentJourney = shipmentJourneyProvider.generateShipmentJourney(orderRoot);
            Order order = mapOrderMessageToShipmentOrder(orderRoot);
            order.setData(orderMessage);
            for (Package orderPackage : orderRoot.getShipments()) {
                Shipment shipment = mapOrderMessageToShipmentDomain(orderPackage, orderRoot);
                shipment.setShipmentJourney(shipmentJourney);
                shipment.setOrder(order);
                shipment.setOrderId(order.getId());
                shipment.setInstructions(order.getInstructions());
                if (log.isDebugEnabled()) {
                    JsonNode json = objectMapper.convertValue(shipment, JsonNode.class);
                    log.debug(String.format(SHIPMENT_DOMAIN_JSON, json.toPrettyString(), orderRoot.getTransactionId()));
                }
                shipmentList.add(shipment);
            }
        } catch (final Exception e) {
            log.error(ERROR_IN_MAPPING_ORDER_TO_SHIPMENT + e.getMessage(), e);
            throw e;
        }

        return shipmentList;
    }

    private List<SegmentsPayload> createSegmentsFromOrderMessage(String orderMessage, String isSegment) throws JsonProcessingException {
        List<SegmentsPayload> segmentsPayloadList = new ArrayList<>();
        if (Boolean.TRUE.equals(Boolean.valueOf(isSegment))) {
            JsonNode json = objectMapper.readValue(orderMessage, JsonNode.class);
            JsonNode segments = json.get(SEGMENTS_PAYLOAD);
            if (segments == null) {
                return Collections.emptyList();
            }
            List<JsonNode> segmentList = objectMapper.convertValue(segments, new TypeReference<>() {
            });
            int size = segmentList.size();
            IntStream.range(0, size)
                    .forEach(index -> {
                        JsonNode segmentNode = segmentList.get(index);
                        SegmentsPayload segmentPayload = createSegmentPayload(segmentNode, index);
                        segmentsPayloadList.add(segmentPayload);
                    });
        }
        return segmentsPayloadList;
    }

    private SegmentsPayload createSegmentPayload(JsonNode segmentNode, int index) {
        SegmentsPayload segmentPayload = objectMapper.convertValue(segmentNode, SegmentsPayload.class);
        String sequence = String.valueOf(index);
        segmentPayload.setSequence(sequence);
        if (StringUtils.isBlank(segmentPayload.getRefId())) {
            segmentPayload.setRefId(sequence);
        }
        return segmentPayload;
    }
}
