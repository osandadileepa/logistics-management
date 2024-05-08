package com.quincus.shipment.kafka.consumers.exception;

import com.quincus.shipment.api.constant.KafkaModuleErrorCode;
import com.quincus.web.common.exception.model.QuincusException;
import lombok.Getter;


public class KafkaConsumerException extends QuincusException {

    @Getter
    private final KafkaModuleErrorCode moduleErrorCode;

    public KafkaConsumerException(KafkaModuleErrorCode moduleErrorCode,
                                  String errorMessage,
                                  String uuid) {
        super(errorMessage, uuid);
        this.moduleErrorCode = moduleErrorCode;
    }
}
