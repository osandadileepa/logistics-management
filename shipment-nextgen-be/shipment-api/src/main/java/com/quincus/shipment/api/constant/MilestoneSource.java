package com.quincus.shipment.api.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.quincus.shipment.api.exception.InvalidEnumValueException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum MilestoneSource {
    VENDOR,
    ORG;

    private static final Map<String, MilestoneSource> MILESTONE_SOURCE_MAP = new HashMap<>();

    static {
        for (MilestoneSource source : MilestoneSource.values()) {
            if (!MILESTONE_SOURCE_MAP.containsKey(source.toString())) {
                MILESTONE_SOURCE_MAP.put(source.toString(), source);
            }
        }
    }

    @JsonCreator
    public static MilestoneSource fromValue(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }

        MilestoneSource milestoneSource = MILESTONE_SOURCE_MAP.get(StringUtils.upperCase(source));
        if (milestoneSource == null) {
            throw new InvalidEnumValueException(source, MilestoneSource.class);
        }

        return milestoneSource;
    }
}
