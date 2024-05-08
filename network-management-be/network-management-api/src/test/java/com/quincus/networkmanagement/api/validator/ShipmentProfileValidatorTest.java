package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.domain.ShipmentProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

import static com.quincus.networkmanagement.api.data.NetworkManagementTestData.dummyShipmentProfile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShipmentProfileValidatorTest {

    private final ShipmentProfileValidator validator = new ShipmentProfileValidator();
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
        ShipmentProfile shipmentProfile = dummyShipmentProfile();
        assertThat(validator.isValid(shipmentProfile, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN minLength > maxLength WHEN validate THEN return false")
    void returnFalseWhenMinLengthAboveMaxLength() {
        ShipmentProfile shipmentProfile = dummyShipmentProfile();
        shipmentProfile.setMinLength(BigDecimal.TEN);
        shipmentProfile.setMaxLength(BigDecimal.ONE);
        assertThat(validator.isValid(shipmentProfile, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN minWidth > maxWidth WHEN validate THEN return false")
    void returnFalseWhenMinWidthAboveMaxWidth() {
        ShipmentProfile shipmentProfile = dummyShipmentProfile();
        shipmentProfile.setMinWidth(BigDecimal.TEN);
        shipmentProfile.setMaxWidth(BigDecimal.ONE);
        assertThat(validator.isValid(shipmentProfile, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN minHeight > maxHeight WHEN validate THEN return false")
    void returnFalseWhenMinHeightAboveMaxHeight() {
        ShipmentProfile shipmentProfile = dummyShipmentProfile();
        shipmentProfile.setMinHeight(BigDecimal.TEN);
        shipmentProfile.setMaxHeight(BigDecimal.ONE);
        assertThat(validator.isValid(shipmentProfile, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN minWeight > maxWeight WHEN validate THEN return false")
    void returnFalseWhenMinWeightAboveMaxWeight() {
        ShipmentProfile shipmentProfile = dummyShipmentProfile();
        shipmentProfile.setMinWeight(BigDecimal.TEN);
        shipmentProfile.setMaxWeight(BigDecimal.ONE);
        assertThat(validator.isValid(shipmentProfile, context)).isFalse();
    }
}
