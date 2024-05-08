package com.quincus.finance.costing.weightcalculation.impl.parser;

import com.quincus.finance.costing.weightcalculation.api.model.SpecialVolumeWeightRule;
import com.quincus.finance.costing.common.exception.CostingApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyTemplate;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyValidCSVTemplate;
import static com.quincus.finance.costing.weightcalculation.impl.data.WeightCalculationTestData.dummyValidSpecialVolumeWeightRule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class SpecialVolumeWeightTemplateCSVParserTest {

    private final SpecialVolumeWeightTemplateCSVParser specialVolumeWeightTemplateCSVParser = new SpecialVolumeWeightTemplateCSVParser();

    @Test
    @DisplayName("GIVEN valid template WHEN parse CSV file THEN return expected")
    void returnExpectedWhenValidTemplate() {
        MultipartFile template = dummyValidCSVTemplate();
        SpecialVolumeWeightRule result = specialVolumeWeightTemplateCSVParser.parseFile(template);

        assertThat(result).isEqualTo(dummyValidSpecialVolumeWeightRule());
    }

    @Test
    @DisplayName("GIVEN invalid template WHEN parse CSV file THEN throw error")
    void throwErrorWhenValidTemplate() {
        MultipartFile template = dummyTemplate(
            "invalid-special-volume-weight-template.csv",
            "text/csv"
        );

        assertThatThrownBy(() -> specialVolumeWeightTemplateCSVParser.parseFile(template))
            .isInstanceOfSatisfying(CostingApiException.class, exception -> {
                assertThat(exception.getErrors()).contains("Error parsing CSV line: 6. [40.0,90.0,FAIL,]");
            });
    }

}
