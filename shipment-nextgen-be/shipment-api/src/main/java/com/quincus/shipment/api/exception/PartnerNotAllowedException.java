package com.quincus.shipment.api.exception;

import com.quincus.shipment.api.constant.ShipmentErrorCode;
import com.quincus.web.common.exception.model.QuincusException;
import lombok.Getter;

public class PartnerNotAllowedException extends QuincusException {

    @Getter
    private final ShipmentErrorCode errorCode;

    public PartnerNotAllowedException(String message, ShipmentErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
