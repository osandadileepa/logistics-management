package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.domain.OperatingHours;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;
import java.time.LocalTime;

import static com.quincus.networkmanagement.api.data.NetworkManagementTestData.dummyOperatingHours;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OperatingHoursValidatorTest {

    private final OperatingHoursValidator validator = new OperatingHoursValidator();
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
        OperatingHours operatingHours = dummyOperatingHours();
        assertThat(validator.isValid(operatingHours, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN missing monStartTime WHEN validate THEN return false")
    void returnFalseWhenMonStartTimeIsNull() {
        OperatingHours operatingHours = dummyOperatingHours();
        operatingHours.setMonStartTime(null);
        assertThat(validator.isValid(operatingHours, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN missing monEndTime WHEN validate THEN return false")
    void returnFalseWhenMonEndTimeIsNull() {
        OperatingHours operatingHours = dummyOperatingHours();
        operatingHours.setMonEndTime(null);
        assertThat(validator.isValid(operatingHours, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN missing monProcessingTime WHEN validate THEN return false")
    void returnFalseWhenMonProcessingTimeIsNull() {
        OperatingHours operatingHours = dummyOperatingHours();
        operatingHours.setMonProcessingTime(null);
        assertThat(validator.isValid(operatingHours, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN endTime is before startTime WHEN validate THEN return false")
    void returnFalseWhenEndTimeBeforeStartTime() {
        OperatingHours operatingHours = dummyOperatingHours();
        operatingHours.setFriStartTime(LocalTime.MAX);
        operatingHours.setFriEndTime(LocalTime.MIN);
        assertThat(validator.isValid(operatingHours, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN no operating hours WHEN validate THEN return false")
    void returnFalseWhenNoOperatingHours() {
        OperatingHours operatingHours = dummyOperatingHours();
        operatingHours.setMonStartTime(null);
        operatingHours.setMonEndTime(null);
        operatingHours.setMonProcessingTime(null);
        operatingHours.setFriStartTime(null);
        operatingHours.setFriEndTime(null);
        operatingHours.setFriProcessingTime(null);
        assertThat(validator.isValid(operatingHours, context)).isFalse();
    }

}
