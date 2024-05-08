package com.quincus.shipment.kafka.consumers.interceptor;

import com.quincus.web.security.AuthenticationProvider;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaPreAuthenticationRecordInterceptorTest {
    private static final String TEST_MESSAGE = "{\"organisation_id\": \"test-org-id\", \"id\": \"test-order-id\"}";
    private static final String TEST_MESSAGE_NESTED = "{\"id\":\"123\",\"content\":{\"organisation_id\": \"test-org-id\", \"id\": \"test-order-id\"}}";
    private static final String INVALID_TEST_MESSAGE = "{\"organisation_id\": \"\", \"id\": \"test-order-id\"}";
    @Mock
    private AuthenticationProvider authenticationProvider;
    @Mock
    private Authentication authentication;
    @Mock
    private ConsumerRecord<String, String> record;
    @InjectMocks
    private KafkaPreAuthenticationRecordInterceptor interceptor;

    @Test
    void testIntercept_ValidOrgId() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("KAFKA");
        when(authenticationProvider.authenticatePreAuthenticatedUser(anyString(), anyString(), anyString())).thenReturn(authentication);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "test-key", TEST_MESSAGE);

        ConsumerRecord<String, String> result = interceptor.intercept(record);
        assertThat(result).isEqualTo(record);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void testIntercept_ValidOrgId_Nested() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("KAFKA");
        when(authenticationProvider.authenticatePreAuthenticatedUser(anyString(), anyString(), anyString())).thenReturn(authentication);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "test-key", TEST_MESSAGE_NESTED);

        ConsumerRecord<String, String> result = interceptor.intercept(record);
        assertThat(result).isEqualTo(record);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void testIntercept_InvalidOrgId() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "test-key", INVALID_TEST_MESSAGE);

        interceptor.intercept(record);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testInterceptWithInvalidMessage() {
        when(record.value()).thenReturn(null);

        interceptor.intercept(record);
        verify(authenticationProvider, never()).authenticatePreAuthenticatedUser(anyString(), anyString(), anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void intercept_invalidMessage_throwException() {
        when(record.value()).thenReturn("{}");

        interceptor.intercept(record);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testOnConsume() {
        String key = "key";
        String value = "{\"organisation_id\":\"123\",\"id\":\"456\"}";
        int offset = 0;
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, offset, key, value);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("KAFKA");
        when(authenticationProvider.authenticatePreAuthenticatedUser(anyString(), anyString(), anyString())).thenReturn(authentication);

        ConsumerRecord<String, String> result = interceptor.intercept(record);

        assertThat(result).isEqualTo(record);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void testOnConsumeWithInvalidMessage() {
        String key = "key";
        String value = "invalid-json";
        int offset = 0;
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, offset, key, value);

        interceptor.intercept(record);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testOnConsumeWithMissingOrganizationId() {
        String key = "key";
        int offset = 0;
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, offset, key, INVALID_TEST_MESSAGE);

        interceptor.intercept(record);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}