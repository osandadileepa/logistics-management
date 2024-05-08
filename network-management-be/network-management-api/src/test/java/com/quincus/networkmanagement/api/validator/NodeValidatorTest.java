package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.domain.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

import static com.quincus.networkmanagement.api.data.NetworkManagementTestData.dummyNode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NodeValidatorTest {

    private final NodeValidator validator = new NodeValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    private static Stream<Arguments> provideAddressLines() {
        return Stream.of(
                Arguments.of("", "", "", true),
                Arguments.of(null, null, null, true),
                Arguments.of("A", null, null, true),
                Arguments.of("A", "", "", true),
                Arguments.of("A", "B", null, true),
                Arguments.of("A", "B", "", true),
                Arguments.of("A", "B", "C", true),
                Arguments.of("", "B", "", false),
                Arguments.of(null, "B", null, false),
                Arguments.of("", "", "C", false),
                Arguments.of(null, null, "C", false),
                Arguments.of("", "B", "C", false),
                Arguments.of(null, "B", "C", false),
                Arguments.of("A", "", "C", false),
                Arguments.of("A", null, "C", false)
        );
    }

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
        Node node = dummyNode();
        assertThat(validator.isValid(node, context)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideAddressLines")
    @DisplayName("GIVEN addressLine WHEN validate THEN return expected")
    void returnExpectedWhenGivenAddressLines(String addressLine1, String addressLine2, String addressLine3, boolean expected) {
        Node node = dummyNode();
        node.setAddressLine1(addressLine1);
        node.setAddressLine2(addressLine2);
        node.setAddressLine3(addressLine3);
        assertThat(validator.isValid(node, context)).isEqualTo(expected);
    }

}
