package com.quincus.finance.costing.common.util;

import com.quincus.finance.costing.common.web.model.RoundingLogic;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@NoArgsConstructor(access = AccessLevel.NONE)
public final class RoundingLogicUtil {

    public static BigDecimal applyRoundingLogic(BigDecimal value, RoundingLogic roundingLogic) {

        log.debug("{} to be rounded using rounding logic {}", value, roundingLogic);

        BigDecimal[] valueOverRoundTo = value.divideAndRemainder(roundingLogic.getRoundTo());

        BigDecimal nonDecimal = valueOverRoundTo[0];
        BigDecimal remainder = valueOverRoundTo[1];

        log.debug("value/roundTo nonDecimal: {}", nonDecimal);
        log.debug("value/roundTo remainder: {}", remainder);

        if (remainder.compareTo(roundingLogic.getThreshold()) >= 0) {
            log.debug("remainder is >= threshold, rounding up");
            nonDecimal = nonDecimal.add(BigDecimal.ONE);
        }

        BigDecimal result = nonDecimal.multiply(roundingLogic.getRoundTo()).setScale(roundingLogic.getRoundTo().scale(), RoundingMode.UNNECESSARY);
        log.debug("rounding result: {}", result);

        return result;
    }

}
