package com.quincus.shipment.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.shipment.api.constant.KafkaModuleErrorCode;

import java.time.LocalDateTime;

public record FailedMessage(
        String transactionId,
        LocalDateTime timestamp,
        JsonNode data,
        KafkaModuleErrorCode kafkaModuleErrorCode,
        String errorMessage) {
}
