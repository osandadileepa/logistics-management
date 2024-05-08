package com.quincus.ext;

import com.quincus.ext.annotation.ISODateTime;
import com.quincus.ext.annotation.validator.ISODatetimeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ISODatetimeValidatorTest {

    private final ISODatetimeValidator validator = new ISODatetimeValidator();
    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    @Mock
    private ISODateTime isoDateTimeAnnotation;

    @BeforeEach
    public void setUp() {
        lenient().when(context.buildConstraintViolationWithTemplate(org.mockito.Mockito.anyString())).thenReturn(violationBuilder);
    }

    @ParameterizedTest
    @MethodSource("provideSampleDatetimeAndExpectedResult")
    void testISODateTimeValidation(String input, boolean expected) {
        validator.initialize(isoDateTimeAnnotation);
        assertThat(validator.isValid(input, context)).isEqualTo(expected);
    }

    private static Stream<Arguments> provideSampleDatetimeAndExpectedResult() {
        return Stream.of(
                Arguments.of("", true),
                Arguments.of(null, true),
                Arguments.of("    ", true),
//                Arguments.of("2022-12-20 16:27:02 +0700", false), remove comment when FE is already done with datetime format change
                Arguments.of("2022-12-20T16:27:02+07:00", true)
        );
    }

}
