package com.quincus.finance.costing.weightcalculation.impl.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidatorContext;

import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyInvalidTemplate;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyValidCSVTemplate;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyValidXLSTemplate;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyValidXLSXTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpecialVolumeWeightTemplateValidatorTest {

    private final SpecialVolumeWeightTemplateValidator specialVolumeWeightTemplateValidator = new SpecialVolumeWeightTemplateValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @BeforeEach
    public void setUp() {
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        doNothing().when(context).disableDefaultConstraintViolation();
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
    }

    @Test
    @DisplayName("GIVEN no template WHEN validate THEN return true")
    void returnTrueWhenNoTemplate() {
        assertThat(specialVolumeWeightTemplateValidator.isValid(null, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN CSV WHEN validate THEN return true")
    void returnTrueWhenCSV() {
        MultipartFile file = dummyValidCSVTemplate();
        assertThat(specialVolumeWeightTemplateValidator.isValid(file, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN XLSX WHEN validate THEN return true")
    void returnTrueWhenXLSX() {
        MultipartFile file = dummyValidXLSXTemplate();
        assertThat(specialVolumeWeightTemplateValidator.isValid(file, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN XLS WHEN validate THEN return true")
    void returnTrueWhenXLS() {
        MultipartFile file = dummyValidXLSTemplate();
        assertThat(specialVolumeWeightTemplateValidator.isValid(file, context)).isTrue();
    }

    @Test
    @DisplayName("GIVEN unsupported format WHEN validate THEN return false")
    void returnFalseWhenUnsupportedFormat() {
        MultipartFile file = dummyInvalidTemplate();
        assertThat(specialVolumeWeightTemplateValidator.isValid(file, context)).isFalse();
    }

    @Test
    @DisplayName("GIVEN file too large WHEN validate THEN return false")
    void returnFalseWhenFileTooLarge() {
        byte[] bytes = new byte[1024 * 500 * 2];
        MultipartFile file = new MockMultipartFile(
                "large-template.csv",
                "large-template.csv",
                "text/csv",
                bytes);
        assertThat(specialVolumeWeightTemplateValidator.isValid(file, context)).isFalse();
    }

}
