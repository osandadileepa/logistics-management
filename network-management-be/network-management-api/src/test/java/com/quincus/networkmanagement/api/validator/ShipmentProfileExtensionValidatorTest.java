package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.domain.ShipmentProfileExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

import static com.quincus.networkmanagement.api.data.NetworkManagementTestData.dummyShipmentProfileExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShipmentProfileExtensionValidatorTest {

    private final ShipmentProfileExtensionValidator validator = new ShipmentProfileExtensionValidator();
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
        ShipmentProfileExtension shipmentProfile = dummyShipmentProfileExtension();
        assertThat(validator.isValid(shipmentProfile, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN minSingleSide > maxSingleSide WHEN validate THEN return false")
    void returnFalseWhenMinLengthAboveMaxLength() {
        ShipmentProfileExtension shipmentProfile = dummyShipmentProfileExtension();
        shipmentProfile.setMinSingleSide(BigDecimal.TEN);
        shipmentProfile.setMaxSingleSide(BigDecimal.ONE);
        assertThat(validator.isValid(shipmentProfile, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN minLinearDim > maxLinearDim WHEN validate THEN return false")
    void returnFalseWhenMinWidthAboveMaxWidth() {
        ShipmentProfileExtension shipmentProfile = dummyShipmentProfileExtension();
        shipmentProfile.setMinLinearDim(BigDecimal.TEN);
        shipmentProfile.setMaxLinearDim(BigDecimal.ONE);
        assertThat(validator.isValid(shipmentProfile, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN minVolume > maxVolume WHEN validate THEN return false")
    void returnFalseWhenMinHeightAboveMaxHeight() {
        ShipmentProfileExtension shipmentProfile = dummyShipmentProfileExtension();
        shipmentProfile.setMinVolume(BigDecimal.TEN);
        shipmentProfile.setMaxVolume(BigDecimal.ONE);
        assertThat(validator.isValid(shipmentProfile, context)).isFalse();
    }

}
