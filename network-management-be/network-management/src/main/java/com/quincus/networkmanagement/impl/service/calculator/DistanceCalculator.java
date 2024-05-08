package com.quincus.networkmanagement.impl.service.calculator;

import com.quincus.networkmanagement.api.constant.DistanceUnit;
import com.quincus.networkmanagement.api.domain.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DistanceCalculator {
    private static final double EARTH_RADIUS_KILOMETERS = 6371;
    private static final double EARTH_RADIUS_MILES = 3958.756;

    public synchronized BigDecimal calculateDistance(Node departureNode, Node arrivalNode, DistanceUnit distanceUnit) {
        if (ObjectUtils.anyNull(departureNode.getFacility(), arrivalNode.getFacility())) {
            return scale(BigDecimal.ZERO);
        }

        BigDecimal lat1 = departureNode.getFacility().getLat();
        BigDecimal lon1 = departureNode.getFacility().getLon();
        BigDecimal lat2 = arrivalNode.getFacility().getLat();
        BigDecimal lon2 = arrivalNode.getFacility().getLon();

        if (ObjectUtils.anyNull(lat1, lon1, lat2, lon2)) {
            return scale(BigDecimal.ZERO);
        }

        if (isSameStartAndEnd(lat1, lon1, lat2, lon2)) {
            return scale(BigDecimal.ZERO);
        }

        return calculateDistance(
                lat1.doubleValue(),
                lon1.doubleValue(),
                lat2.doubleValue(),
                lon2.doubleValue(),
                distanceUnit
        );
    }

    private BigDecimal calculateDistance(double lat1, double lon1, double lat2, double lon2, DistanceUnit unit) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double distance = getRadius(unit) * c;

        return scale(BigDecimal.valueOf(distance));
    }

    private boolean isSameStartAndEnd(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        return lat1.compareTo(lat2) == 0 && lon1.compareTo(lon2) == 0;
    }

    private double getRadius(DistanceUnit unit) {
        if (unit == DistanceUnit.MILES)
            return EARTH_RADIUS_MILES;
        return EARTH_RADIUS_KILOMETERS;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(4, RoundingMode.DOWN);
    }
}
