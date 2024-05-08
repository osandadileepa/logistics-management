package com.quincus.shipment.api.validator;

import com.quincus.shipment.api.constant.CostCategory;
import com.quincus.shipment.api.domain.Cost;
import com.quincus.shipment.api.domain.CostType;
import com.quincus.shipment.api.domain.Currency;
import com.quincus.shipment.api.validator.constraint.ValidCostCategory;
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
class CostCategoryValidatorTest {

    private final CostCategoryValidator validator = new CostCategoryValidator();
    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    @Mock
    private ValidCostCategory validCostCategoryAnnotation;

    @BeforeEach
    public void setUp() {
        lenient().when(context.buildConstraintViolationWithTemplate(org.mockito.Mockito.anyString())).thenReturn(violationBuilder);
    }

    @ParameterizedTest
    @MethodSource("provideCostsForValidation")
    void testCostCategoryValidation(Cost input, boolean expected) {
        assertThat(validator.isValid(input, context)).isEqualTo(expected);
    }

    private static Stream<Arguments> provideCostsForValidation() {
        Currency currencyWithId = new Currency();
        currencyWithId.setId("c28ddc77-d089-470a-8d09-34d23d8c1396");

        Currency currencyWithoutId = new Currency();

        CostType timeBasedCostType = new CostType();
        timeBasedCostType.setCategory(CostCategory.TIME_BASED);

        CostType nonTimeBasedCostType = new CostType();
        nonTimeBasedCostType.setCategory(CostCategory.NON_TIME_BASED);

        Cost costTimeBasedWithNullCurrency = new Cost();
        costTimeBasedWithNullCurrency.setCostType(timeBasedCostType);

        Cost costTimeBasedWithoutId = new Cost();
        costTimeBasedWithoutId.setCostType(timeBasedCostType);
        costTimeBasedWithoutId.setCurrency(currencyWithoutId);

        Cost costTimeBasedWithId = new Cost();
        costTimeBasedWithId.setCostType(timeBasedCostType);
        costTimeBasedWithId.setCurrency(currencyWithId);

        Cost costNonTimeBasedWithoutCurrency = new Cost();
        costNonTimeBasedWithoutCurrency.setCostType(nonTimeBasedCostType);

        Cost costNonTimeBasedWithoutId = new Cost();
        costNonTimeBasedWithoutId.setCostType(nonTimeBasedCostType);
        costNonTimeBasedWithoutId.setCurrency(currencyWithoutId);

        Cost costNonTimeBasedWithId = new Cost();
        costNonTimeBasedWithId.setCostType(nonTimeBasedCostType);
        costNonTimeBasedWithId.setCurrency(currencyWithId);

        return Stream.of(
                // Time based cost types
                Arguments.of(costTimeBasedWithNullCurrency, true),
                Arguments.of(costTimeBasedWithoutId, true),
                Arguments.of(costTimeBasedWithId, true),

                // Non time based cost types
                Arguments.of(costNonTimeBasedWithId, true)
        );
    }
}
