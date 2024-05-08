package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.constant.ShipmentStatus;
import com.quincus.shipment.api.domain.Shipment;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.Set;

import static com.quincus.shipment.impl.test_utils.ShipmentJourneyTestDataFactory.createShipmentJourney;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentValidatorTest {

    @InjectMocks
    private ShipmentValidator shipmentValidator;
    @Mock
    private Validator validator;
    @Mock
    private PackageJourneySegmentValidator packageJourneySegmentValidator;

    @Test
    void validateShipment_validShipment_shouldDoNothing() {
        Shipment domain = new Shipment();
        domain.setStatus(ShipmentStatus.CREATED);
        domain.setShipmentJourney(createShipmentJourney());
        when(validator.validate(domain)).thenReturn(Collections.emptySet());

        shipmentValidator.validateShipment(domain);

        verify(validator, times(1)).validate(domain);
    }

    @Test
    void validateShipment_invalidShipment_shouldThrowException() {
        Shipment domain = new Shipment();
        domain.setStatus(ShipmentStatus.CREATED);

        ConstraintViolation<Shipment> constraintViolation = mock(ConstraintViolation.class);

        when(constraintViolation.getLeafBean()).thenReturn(new Object());
        when(validator.validate(domain)).thenReturn(Set.of(constraintViolation));

        assertThatThrownBy(() -> shipmentValidator.validateShipment(domain))
                .isInstanceOf(QuincusValidationException.class);

        verify(validator, times(1)).validate(domain);
    }
    
}
