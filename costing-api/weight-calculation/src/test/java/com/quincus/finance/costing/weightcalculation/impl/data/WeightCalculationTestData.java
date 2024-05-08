package com.quincus.finance.costing.weightcalculation.impl.data;

import com.quincus.finance.costing.common.web.model.Partner;
import com.quincus.finance.costing.common.web.model.RoundingLogic;
import com.quincus.finance.costing.weightcalculation.api.model.ChargeableWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.Conversion;
import com.quincus.finance.costing.weightcalculation.api.model.SpecialVolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.VolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationInput;
import com.quincus.finance.costing.weightcalculation.api.model.WeightCalculationRule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class WeightCalculationTestData {

    public static WeightCalculationInput dummyInput() {
        WeightCalculationInput input = new WeightCalculationInput();
        input.setLength(new BigDecimal(2));
        input.setWidth(new BigDecimal(3));
        input.setHeight(new BigDecimal(4));
        input.setActualWeight(new BigDecimal(10));
        return input;
    }

    public static WeightCalculationRule dummyRule() {
        WeightCalculationRule rule = new WeightCalculationRule();
        rule.setChargeableWeightRule(ChargeableWeightRule.HIGHER_VALUE_BETWEEN_ACTUAL_AND_VOLUME_WEIGHT);
        rule.setVolumeWeightRule(VolumeWeightRule.STANDARD);
        rule.setStandardVolumeWeightRuleDivisor(new BigDecimal(2));
        rule.setName("Dummy Rule");
        rule.setActive(true);
        rule.setOrganizationId("0");
        rule.setPartners(dummyPartners());
        rule.setRoundingLogic(dummyRoundingLogic());
        return rule;
    }

    public static RoundingLogic dummyRoundingLogic() {
        return new RoundingLogic(BigDecimal.valueOf(0.001), BigDecimal.valueOf(0.0005));
    }

    public static Set<Partner> dummyPartners() {
        Set<Partner> partners = new HashSet<>();
        partners.add(dummyPartner("0", "LBC"));
        partners.add(dummyPartner("1", "NinjaVan"));
        return partners;
    }

    public static Partner dummyPartner(String id, String name) {
        Partner partner = new Partner();
        partner.setId(id);
        partner.setName(name);
        return partner;
    }

    public static SpecialVolumeWeightRule dummySpecialVolumeWeightRule(String formula, List<Conversion> conversions) {
        SpecialVolumeWeightRule rule = new SpecialVolumeWeightRule();
        rule.setCustomFormula(formula);
        rule.setConversions(conversions);
        return rule;
    }

    public static SpecialVolumeWeightRule dummySpecialVolumeWeightRule(String formula) {
        SpecialVolumeWeightRule rule = new SpecialVolumeWeightRule();
        rule.setCustomFormula(formula);
        return rule;
    }

    public static SpecialVolumeWeightRule dummySpecialVolumeWeightRule(List<Conversion> conversions) {
        SpecialVolumeWeightRule rule = new SpecialVolumeWeightRule();
        rule.setCustomFormula("l+w+h");
        rule.setConversions(conversions);
        return rule;
    }

    public static SpecialVolumeWeightRule dummyValidSpecialVolumeWeightRule() {
        return dummySpecialVolumeWeightRule(
                "l+w+h",
                List.of(
                        new Conversion(BigDecimal.valueOf(0.0), BigDecimal.valueOf(10.0), BigDecimal.valueOf(5.0)),
                        new Conversion(BigDecimal.valueOf(10.0), BigDecimal.valueOf(20.0), BigDecimal.valueOf(8.0)),
                        new Conversion(BigDecimal.valueOf(25.0), BigDecimal.valueOf(30.0), BigDecimal.valueOf(10.0)),
                        new Conversion(BigDecimal.valueOf(30.0), BigDecimal.valueOf(40.0), BigDecimal.valueOf(12.0)),
                        new Conversion(BigDecimal.valueOf(40.0), BigDecimal.valueOf(90.0), BigDecimal.valueOf(20.0)),
                        new Conversion(BigDecimal.valueOf(90.0), BigDecimal.valueOf(120.0), BigDecimal.valueOf(30.0)),
                        new Conversion(BigDecimal.valueOf(120.0), BigDecimal.valueOf(9999.0), BigDecimal.valueOf(50.0))
                )
        );
    }

    public static MultipartFile dummyTemplate(String fileName, String contentType) {
        try {
            ClassPathResource path = new ClassPathResource("templates/" + fileName);
            FileInputStream input = new FileInputStream(path.getFile());
            return new MockMultipartFile(
                    fileName,
                    fileName,
                    contentType,
                    IOUtils.toByteArray(input)
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static MultipartFile dummyValidXLSXTemplate() {
        return dummyTemplate(
                "valid-special-volume-weight-template.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
    }

    public static MultipartFile dummyValidXLSTemplate() {
        return dummyTemplate(
                "valid-special-volume-weight-template.xls",
                "application/vnd.ms-excel"
        );
    }


    public static MultipartFile dummyValidCSVTemplate() {
        return dummyTemplate(
                "valid-special-volume-weight-template.csv",
                "text/csv"
        );
    }

    public static MultipartFile dummyInvalidTemplate() {
        return dummyTemplate(
                "invalid-template.txt",
                "text/plain"
        );
    }

}
