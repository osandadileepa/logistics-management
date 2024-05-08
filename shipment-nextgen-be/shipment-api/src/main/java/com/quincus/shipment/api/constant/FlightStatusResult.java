package com.quincus.shipment.api.constant;

import com.quincus.shipment.api.domain.Alert;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class FlightStatusResult {
    private final List<String> segmentIds = new ArrayList<>();
    private final List<Alert> alerts = new ArrayList<>();

    public void addAlert(Alert alert) {
        Optional.ofNullable(alert)
                .ifPresent(alerts::add);
    }

    public void addSegmentId(String segmentId) {
        Optional.ofNullable(segmentId)
                .filter(StringUtils::isNotBlank)
                .ifPresent(segmentIds::add);
    }
}
