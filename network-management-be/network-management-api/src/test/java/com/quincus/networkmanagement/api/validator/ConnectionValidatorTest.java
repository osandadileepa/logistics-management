package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.data.NetworkManagementTestData;
import com.quincus.networkmanagement.api.domain.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;

import static com.quincus.networkmanagement.api.data.NetworkManagementTestData.dummyAirConnection;
import static com.quincus.networkmanagement.api.data.NetworkManagementTestData.dummyGroundConnection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConnectionValidatorTest {

    private final ConnectionValidator validator = new ConnectionValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @BeforeEach
    public void setUp() {
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        doNothing().when(context).disableDefaultConstraintViolation();
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class));
    }

    @Test
    @DisplayName("GIVEN valid data WHEN validate THEN return true")
    void returnTrueWhenValid() {
        assertThat(validator.isValid(dummyGroundConnection(), context)).isTrue();
        assertThat(validator.isValid(dummyAirConnection(), context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN same departureNodeId and arrivalNodeId WHEN validate THEN return false")
    void returnFalseWhenSameDepartureAndArrivalNode() {
        Connection connection = dummyAirConnection();
        connection.setDepartureNode(NetworkManagementTestData.dummyNode("00001"));
        connection.setArrivalNode(NetworkManagementTestData.dummyNode("00001"));
        assertThat(validator.isValid(connection, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN missing airLockoutDuration on AIR connection WHEN validate THEN return false")
    void returnFalseWhenMissingAirLockoutDuration() {
        Connection connection = dummyAirConnection();
        connection.setAirLockoutDuration(null);
        assertThat(validator.isValid(connection, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN missing airRecoveryDuration on AIR connection WHEN validate THEN return false")
    void returnFalseWhenMissingAirRecoveryDuration() {
        Connection connection = dummyAirConnection();
        connection.setAirRecoveryDuration(null);
        assertThat(validator.isValid(connection, context)).isFalse();
    }

}
