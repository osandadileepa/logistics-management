package com.quincus.shipment.impl.helper;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

@UtilityClass
public final class ShipmentOrderDateUtil {

    private static final String GMT = "GMT";
    private static final String COLON = ":";

    public static String modifyOrderDateToSegmentDate(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        String modified = removeLastColon(s);
        return modified.replace(GMT, Strings.EMPTY);
    }

    private static String removeLastColon(String s) {
        int pos = s.lastIndexOf(COLON);
        if (pos > -1) {
            return s.substring(0, pos) + s.substring(pos + COLON.length());
        }
        return s;
    }
}
