package com.quincus.web.common.exception.model;

public class ResourceMismatchException extends QuincusException {
    private static final String EXCEPTION_MSG_FMT = "%s field `%s` does not match with given %s field `%s`(value `%s`).";

    public ResourceMismatchException(String requestContext, String requestField, Object fieldValue, String resourceContext, String resourceField) {
        super(String.format(EXCEPTION_MSG_FMT, resourceContext, resourceField, requestContext, requestField, fieldValue));
    }
}
