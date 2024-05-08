package com.quincus.finance.costing.common.validator;

import com.quincus.finance.costing.common.web.model.RoundingLogic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class RoundingLogicValidator implements ConstraintValidator<RoundingLogicConstraint, RoundingLogic> {

    private static final String ERR_INVALID_ROUND_TO = "error: invalid roundTo value";
    private static final String ERR_INVALID_THRESHOLD = "error: invalid threshold value";
    public static final int ROUNDING_LOGIC_SCALE = 4;

    private static final Set<BigDecimal> VALID_ROUND_TO = Set.of(
            new BigDecimal("1000.0000"),
            new BigDecimal("100.0000"),
            new BigDecimal("10.0000"),
            new BigDecimal("1.0000"),
            new BigDecimal("0.1000"),
            new BigDecimal("0.0100"),
            new BigDecimal("0.0010")
    );

    public boolean isValid(RoundingLogic rule, ConstraintValidatorContext constraintValidatorContext) {

        log.debug("Validating rounding logic");

        constraintValidatorContext.disableDefaultConstraintViolation();
        List<String> errors = new ArrayList<>();

        if (rule != null) {
            BigDecimal roundTo = rule.getRoundTo().setScale(ROUNDING_LOGIC_SCALE, RoundingMode.CEILING);
            BigDecimal roundingThreshold = rule.getThreshold().setScale(ROUNDING_LOGIC_SCALE, RoundingMode.CEILING);

            log.debug("roundTo: {}", roundTo);
            log.debug("roundingThreshold: {}", roundingThreshold);

            if (!VALID_ROUND_TO.contains(roundTo)) {
                log.debug("roundTo value is not in: {}", VALID_ROUND_TO);
                errors.add(ERR_INVALID_ROUND_TO);
            } else {

                BigDecimal thresholdByPlace = roundingThreshold.divide(roundTo, 4, RoundingMode.CEILING);
                log.debug("thresholdByPlace: {}", thresholdByPlace);

                if (!(thresholdByPlace.compareTo(BigDecimal.valueOf(0.1)) >= 0 &&
                        thresholdByPlace.compareTo(BigDecimal.valueOf(0.9)) <= 0)) {

                    log.debug("invalid threshold, thresholdByPlace is not between 0.1 and 0.9");
                    errors.add(ERR_INVALID_THRESHOLD);

                }

            }
        }

        if (!errors.isEmpty()) {
            for (String error : errors) {
                constraintValidatorContext.buildConstraintViolationWithTemplate(error).addConstraintViolation();
            }
            return false;
        }

        return true;
    }
}
