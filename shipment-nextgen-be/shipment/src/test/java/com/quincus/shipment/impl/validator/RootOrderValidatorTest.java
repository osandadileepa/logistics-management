package com.quincus.shipment.impl.validator;

import com.quincus.order.api.domain.Root;
import com.quincus.web.common.exception.model.QuincusValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RootOrderValidatorTest {

    @InjectMocks
    private RootOrderValidator rootOrderValidator;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;

    @Test
    void testValidate_NoConstraintsViolation() {
        Root rootOrder = new Root(); // Create an instance of Root

        when(validatorFactory.getValidator()).thenReturn(validator);
        Set<ConstraintViolation<Root>> constraints = Set.of();
        when(validator.validate(rootOrder)).thenReturn(constraints);


        // Calling the validate method should not throw an exception
        assertThatNoException().isThrownBy(() -> rootOrderValidator.validate(rootOrder));

        // Verify that validatorFactory and validator methods were called
        verify(validatorFactory, times(1)).getValidator();
        verify(validator, times(1)).validate(rootOrder);
    }

    @Test
    void testValidate_ConstraintsViolation() {
        Root rootOrder = new Root(); // Create an instance of Root

        // Simulate validation errors
        when(validatorFactory.getValidator()).thenReturn(validator);
        Set<ConstraintViolation<Root>> constraints = Set.of(
                mockConstraintViolation("property1", "Constraint violation 1"),
                mockConstraintViolation("property2", "Constraint violation 2")
        );
        when(validator.validate(rootOrder)).thenReturn(constraints);

        String expectedMessage = "property1 : Constraint violation 1";
        String expectedMessage2 = "property2 : Constraint violation 2";
        // Calling the validate method should throw a QuincusValidationException
        assertThatThrownBy(() -> rootOrderValidator.validate(rootOrder)).isInstanceOf(QuincusValidationException.class)
                .hasMessageContaining(expectedMessage).hasMessageContaining(expectedMessage2);

        // Verify that validatorFactory and validator methods were called
        verify(validatorFactory, times(1)).getValidator();
        verify(validator, times(1)).validate(rootOrder);
    }

    // Helper method to create a mock ConstraintViolation
    private ConstraintViolation<Root> mockConstraintViolation(String propertyPath, String message) {
        ConstraintViolation<Root> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(javax.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }
}
