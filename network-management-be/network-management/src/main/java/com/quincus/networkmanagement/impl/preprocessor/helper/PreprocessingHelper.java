package com.quincus.networkmanagement.impl.preprocessor.helper;

import com.quincus.networkmanagement.api.constant.DimensionUnit;
import com.quincus.networkmanagement.api.constant.VolumeUnit;
import com.quincus.networkmanagement.api.constant.WeightUnit;
import com.quincus.networkmanagement.api.domain.Currency;
import lombok.experimental.UtilityClass;
import org.dmfs.rfc5545.DateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Contains static functions used throughout the pre-processing layer
 */
@UtilityClass
public class PreprocessingHelper {
    private static final int COST_SCALE = 2;
    private static final int DEFAULT_SCALE = 4;
    private static final long ONE_MINUTE_IN_EPOCH_TIME = 60;
    private static final long MILLISECONDS_IN_SECONDS = 1000;
    private static final BigDecimal KILOGRAMS_IN_POUNDS = BigDecimal.valueOf(0.4536);
    private static final BigDecimal KILOGRAMS_IN_OUNCES = BigDecimal.valueOf(0.0283);
    private static final BigDecimal KILOGRAMS_IN_GRAMS = BigDecimal.valueOf(0.001);
    private static final BigDecimal METERS_IN_FEET = BigDecimal.valueOf(0.3048);
    private static final BigDecimal METERS_IN_INCHES = BigDecimal.valueOf(0.0254);
    private static final BigDecimal METERS_IN_MILLIMETERS = BigDecimal.valueOf(0.001);
    private static final BigDecimal CUBIC_METERS_IN_CUBIC_FEET = BigDecimal.valueOf(0.0283);

    public static BigDecimal toUniformWeight(BigDecimal value, WeightUnit unit) {
        if (unit == WeightUnit.POUNDS) {
            value = value.multiply(KILOGRAMS_IN_POUNDS);
        } else if (unit == WeightUnit.OUNCE) {
            value = value.multiply(KILOGRAMS_IN_OUNCES);
        } else if (unit == WeightUnit.GRAMS) {
            value = value.multiply(KILOGRAMS_IN_GRAMS);
        }
        return value.setScale(DEFAULT_SCALE, RoundingMode.DOWN);
    }

    public static BigDecimal toUniformDimension(BigDecimal value, DimensionUnit unit) {
        if (unit == DimensionUnit.FEET) {
            value = value.multiply(METERS_IN_FEET);
        } else if (unit == DimensionUnit.INCHES) {
            value = value.multiply(METERS_IN_INCHES);
        } else if (unit == DimensionUnit.MILLIMETERS) {
            value = value.multiply(METERS_IN_MILLIMETERS);
        }
        return value.setScale(DEFAULT_SCALE, RoundingMode.DOWN);
    }

    public static BigDecimal toUniformVolume(BigDecimal value, VolumeUnit unit) {
        if (unit == VolumeUnit.CUBIC_FEET) {
            value = value.multiply(CUBIC_METERS_IN_CUBIC_FEET);
        }
        return value.setScale(DEFAULT_SCALE, RoundingMode.DOWN);
    }

    public static BigDecimal toUniformCost(BigDecimal cost, Currency currency) {
        if (currency != null && currency.getExchangeRate() != null) {
            cost = cost.multiply(currency.getExchangeRate());
        }
        return cost.setScale(COST_SCALE, RoundingMode.DOWN);
    }

    public static long toDepartureTime(DateTime departureTiming) {
        return departureTiming.getTimestamp() / MILLISECONDS_IN_SECONDS;
    }

    public static long toArrivalTime(DateTime departureTiming, Integer duration) {
        if (duration != null) {
            return toDepartureTime(departureTiming) + (duration * ONE_MINUTE_IN_EPOCH_TIME);
        }
        return toDepartureTime(departureTiming);
    }


}
