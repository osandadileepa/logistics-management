package com.quincus.shipment.kafka.consumers.interceptor;

import com.quincus.shipment.kafka.consumers.utility.KakfaPayloadFieldExtractor;
import com.quincus.web.common.exception.model.OrganizationDetailsNotFoundException;
import com.quincus.web.common.exception.model.QuincusValidationException;
import com.quincus.web.common.exception.model.UserDetailsNotFoundException;
import com.quincus.web.security.AuthenticationProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.ERR_AUTHENTICATION_NOT_FOUND;
import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.ERR_UNABLE_TO_RESOLVE_ORGANIZATION_DETAILS;
import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.ERR_UNABLE_TO_RESOLVE_USER_DETAILS;
import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.ORGANISATION_ID_IS_EMPTY;
import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.ORGANIZATION_ID;
import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.PRE_AUTHENTICATED_SOURCE;
import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.RECEIVED_KAFKA_CONSUMER_RECORD;
import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.SUCCESSFULLY_ADDED_ORGANIZATION_DETAILS;
import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.UNABLE_TO_PARSE_KAFKA_MESSAGE;
import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.USER_ID;
import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.USER_ID_IS_EMPTY;
import static com.quincus.shipment.kafka.consumers.interceptor.constant.KafkaInterceptorMessage.WITH_KEY_VALUE_AND_OFFSET;

@AllArgsConstructor
@Slf4j
public class KafkaPreAuthenticationWithUserDetailRecordInterceptor extends AbstractKafkaPreAuthenticationRecordInterceptor {

    private final AuthenticationProvider authenticationProvider;

    @Override
    @KafkaPreAuthentication
    public ConsumerRecord<String, String> intercept(ConsumerRecord<String, String> kafkaConsumerRecord) {
        log.info(RECEIVED_KAFKA_CONSUMER_RECORD, kafkaConsumerRecord.key(), kafkaConsumerRecord.value(), kafkaConsumerRecord.offset());

        try {
            final String organizationId = KakfaPayloadFieldExtractor.extractField(kafkaConsumerRecord.value(), ORGANIZATION_ID);
            final String userId = KakfaPayloadFieldExtractor.extractField(kafkaConsumerRecord.value(), USER_ID);
            if (StringUtils.isBlank(organizationId)) {
                throw new OrganizationDetailsNotFoundException(ERR_UNABLE_TO_RESOLVE_ORGANIZATION_DETAILS + ORGANISATION_ID_IS_EMPTY);
            }
            if (StringUtils.isBlank(userId)) {
                throw new UserDetailsNotFoundException(ERR_UNABLE_TO_RESOLVE_USER_DETAILS + USER_ID_IS_EMPTY);
            }

            final Authentication authentication = authenticationProvider.authenticatePreAuthenticatedUserWithUserId(organizationId, userId, PRE_AUTHENTICATED_SOURCE);
            if (!authentication.isAuthenticated() || !StringUtils.equalsIgnoreCase(PRE_AUTHENTICATED_SOURCE, String.valueOf(authentication.getCredentials()))) {
                throw new PreAuthenticatedCredentialsNotFoundException(ERR_AUTHENTICATION_NOT_FOUND);
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info(SUCCESSFULLY_ADDED_ORGANIZATION_DETAILS, organizationId);
            return kafkaConsumerRecord;
        } catch (PreAuthenticatedCredentialsNotFoundException e) {
            SecurityContextHolder.clearContext();
            log.error(ERR_AUTHENTICATION_NOT_FOUND + WITH_KEY_VALUE_AND_OFFSET, kafkaConsumerRecord.key(), kafkaConsumerRecord.value(), kafkaConsumerRecord.offset(), e);
            return null;
        } catch (OrganizationDetailsNotFoundException | QuincusValidationException e) {
            SecurityContextHolder.clearContext();
            log.error(e.getMessage() + WITH_KEY_VALUE_AND_OFFSET, kafkaConsumerRecord.key(), kafkaConsumerRecord.value(), kafkaConsumerRecord.offset(), e);
            return null;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error(UNABLE_TO_PARSE_KAFKA_MESSAGE + WITH_KEY_VALUE_AND_OFFSET, kafkaConsumerRecord.key(), kafkaConsumerRecord.value(), kafkaConsumerRecord.offset(), e);
            return null;
        }
    }
}
