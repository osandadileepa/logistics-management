package com.quincus.shipment.api.validator;

import com.quincus.shipment.api.validator.constraint.FieldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Annotation;
import java.time.temporal.Temporal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FieldTypeValidatorTest {
    private static final String MUST_BE_OF_TYPE = "must be of type ";
    private FieldTypeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new FieldTypeValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testTemporalType_ValidValue_ReturnsTrue() {
        FieldType annotation = createFieldTypeAnnotation(Temporal.class);
        validator.initialize(annotation);
        assertThat(validator.isValid("2023-05-19T12:34:56Z", context)).isTrue();
    }

    @Test
    void testTemporalType_InvalidValue_ReturnsFalse() {
        FieldType annotation = createFieldTypeAnnotation(Temporal.class);
        validator.initialize(annotation);
        assertThat(validator.isValid("InvalidDate", context)).isFalse();
    }

    @Test
    void testNumberType_ValidValue_ReturnsTrue() {
        FieldType annotation = createFieldTypeAnnotation(Number.class);
        validator.initialize(annotation);
        assertThat(validator.isValid("12345", context)).isTrue();
    }

    @Test
    void testNumberType_InvalidValue_ReturnsFalse() {
        FieldType annotation = createFieldTypeAnnotation(Number.class);
        validator.initialize(annotation);
        assertThat(validator.isValid("InvalidNumber", context)).isFalse();
    }

    @Test
    void testCustomType_ValidValue_ReturnsTrue() {
        FieldType annotation = createFieldTypeAnnotation(String.class);
        validator.initialize(annotation);
        assertThat(validator.isValid("ValidString", context)).isTrue();
    }

    @Test
    void testNonMandatory_NullValue_ReturnsTrue() {
        FieldType annotation = createFieldTypeAnnotation(String.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(null, context)).isTrue();
    }

    private FieldType createFieldTypeAnnotation(Class<?> type) {
        return new FieldType() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public Class<?> type() {
                return type;
            }

            @Override
            public String message() {
                return MUST_BE_OF_TYPE + type;
            }

            @Override
            public Class<?>[] groups() {
                return new Class<?>[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }
        };
    }
}
