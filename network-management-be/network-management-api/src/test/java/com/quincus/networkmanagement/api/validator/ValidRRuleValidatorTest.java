package com.quincus.networkmanagement.api.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidRRuleValidatorTest {

    private final RRuleValidator validator = new RRuleValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    private static Stream<Arguments> provideValidRRULEs() {
        return Stream.of(
                Arguments.of("RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=MO,TU,WE,TH,FR;BYHOUR=7;BYMINUTE=30"),
                Arguments.of("RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=SA,SU;BYHOUR=10;BYMINUTE=15")
        );
    }

    private static Stream<Arguments> provideInvalidRRULEs() {
        return Stream.of(
                // missing frequency
                Arguments.of("RRULE:INTERVAL=1;WKST=MO;BYDAY=MO,TU,WE,TH,FR;BYHOUR=7;BYMINUTE=30"),
                // missing BYHOUR
                Arguments.of("RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=MO,TU,WE,TH,FR;BYMINUTE=30"),
                // missing BYMINUTE
                Arguments.of("RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;BYDAY=MO,TU,WE,TH,FR;BYHOUR=7"),
                // completely invalid string
                Arguments.of("INVALID_STRING")
        );
    }

    @BeforeEach
    public void setUp() {
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        doNothing().when(context).disableDefaultConstraintViolation();
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class));
    }

    @ParameterizedTest
    @MethodSource("provideValidRRULEs")
    @DisplayName("GIVEN valid RRULE WHEN validate THEN return true")
    void returnTrueWhenValid(String rrule) {
        assertThat(validator.isValid(rrule, context)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRRULEs")
    @DisplayName("GIVEN valid RRULE WHEN validate THEN return true")
    void returnFalseWhenInvalid(String rrule) {
        assertThat(validator.isValid(rrule, context)).isFalse();
    }


}
