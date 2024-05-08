package com.quincus.networkmanagement.api.exception;

import com.quincus.networkmanagement.api.constant.NetworkManagementErrorCode;
import com.quincus.web.common.exception.model.QuincusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MmeApiCallException extends QuincusException {
    private final NetworkManagementErrorCode code;
    private final HttpStatus httpStatus;

    public MmeApiCallException(String message, NetworkManagementErrorCode code, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
