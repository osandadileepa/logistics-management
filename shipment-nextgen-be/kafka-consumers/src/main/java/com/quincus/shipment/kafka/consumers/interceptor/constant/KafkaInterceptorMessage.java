package com.quincus.shipment.kafka.consumers.interceptor.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaInterceptorMessage {
    public static final String CLEARING_THE_CONTEXT = "Clearing the context.";
    public static final String ERR_UNABLE_TO_RESOLVE_ORGANIZATION_DETAILS = "Unable to resolve Organization Details from Kafka message. ";
    public static final String ERR_UNABLE_TO_RESOLVE_USER_DETAILS = "Unable to resolve User Details from Kafka message. ";
    public static final String ERR_AUTHENTICATION_NOT_FOUND = "Unable to retrieve valid Authentication details when processing Kafka message.";
    public static final String ORGANIZATION_ID = "organisation_id";
    public static final String USER_ID = "user_id";
    public static final String SUCCESSFULLY_ADDED_ORGANIZATION_DETAILS = "Successfully added organizationId {} ";
    public static final String UNABLE_TO_PARSE_KAFKA_MESSAGE = "Unable to parse Kafka message. ";
    public static final String RECEIVED_KAFKA_CONSUMER_RECORD = "Received Kafka Consumer Record with key {}, value {}, and offset {} ";
    public static final String WITH_KEY_VALUE_AND_OFFSET = "with key {}, value {}, and offset {} ";
    public static final String ORGANISATION_ID_IS_EMPTY = "organisation_id is empty. ";
    public static final String USER_ID_IS_EMPTY = "user_id is empty. ";
    public static final String PRE_AUTHENTICATED_SOURCE = "KAFKA";
}
