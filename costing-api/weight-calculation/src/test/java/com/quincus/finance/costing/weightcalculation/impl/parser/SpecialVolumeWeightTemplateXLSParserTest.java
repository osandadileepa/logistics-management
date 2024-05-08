package com.quincus.finance.costing.weightcalculation.impl.parser;

import com.quincus.finance.costing.weightcalculation.api.model.SpecialVolumeWeightRule;
import com.quincus.finance.costing.common.exception.CostingApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyTemplate;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyValidSpecialVolumeWeightRule;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyValidXLSXTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class SpecialVolumeWeightTemplateXLSParserTest {

    private final SpecialVolumeWeightTemplateXLSParser specialVolumeWeightTemplateXLSParser = new SpecialVolumeWeightTemplateXLSParser();

    @Test
    @DisplayName("GIVEN valid template WHEN parse XLS file THEN return expected")
    void returnExpectedWhenValidTemplate() {
        MultipartFile template = dummyValidXLSXTemplate();
        SpecialVolumeWeightRule result = specialVolumeWeightTemplateXLSParser.parseFile(template);

        assertThat(result).isEqualTo(dummyValidSpecialVolumeWeightRule());
    }

    @Test
    @DisplayName("GIVEN invalid template WHEN parse XLS file THEN throw error")
    void throwErrorWhenValidTemplate() {
        MultipartFile template = dummyTemplate(
            "invalid-special-volume-weight-template.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        assertThatThrownBy(() -> specialVolumeWeightTemplateXLSParser.parseFile(template))
            .isInstanceOfSatisfying(CostingApiException.class, exception -> {
                assertThat(exception.getErrors()).contains("Cannot get a NUMERIC value from a STRING cell");
            });
    }

}
