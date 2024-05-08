package com.quincus.finance.costing.weightcalculation.impl.validator;

import com.quincus.finance.costing.weightcalculation.api.model.Conversion;
import com.quincus.finance.costing.weightcalculation.api.model.SpecialVolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.impl.evaluator.SpecialVolumeWeightEvaluator;
import com.quincus.finance.costing.common.exception.CostingApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@Component
public class SpecialVolumeWeightRuleValidator {

    private static final String ERR_INVALID_TEMPLATE = "There is a problem in the attached template";
    private static final String ERR_INVALID_FORMULA = "The custom formula specified in the template is invalid";
    private static final String ERR_INVALID_CONVERSION_TABLE = "There is a problem with the conversion table specified in the template";

    private final SpecialVolumeWeightEvaluator specialVolumeWeightEvaluator;

    public SpecialVolumeWeightRuleValidator(SpecialVolumeWeightEvaluator specialVolumeWeightEvaluator) {
        this.specialVolumeWeightEvaluator = specialVolumeWeightEvaluator;
    }

    public void validate(SpecialVolumeWeightRule rule) {

        if(!specialVolumeWeightEvaluator.isValidFormula(rule.getCustomFormula())) {
            throw new CostingApiException(ERR_INVALID_TEMPLATE, ERR_INVALID_FORMULA, HttpStatus.BAD_REQUEST);
        }

        Iterator<Conversion> i = rule.getConversions().iterator();
        Conversion prev = null;
        while(i.hasNext()) {
            Conversion c = i.next();
            if(c.getFrom().compareTo(c.getTo()) >= 0 || (prev != null && prev.getTo().compareTo(c.getFrom()) > 0)) {
                throw new CostingApiException(ERR_INVALID_TEMPLATE, ERR_INVALID_CONVERSION_TABLE, HttpStatus.BAD_REQUEST);
            }
            prev = c;
        }
    }

}
