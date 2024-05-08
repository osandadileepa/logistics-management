package com.quincus.shipment.api.helper;

import com.quincus.shipment.api.exception.InvalidEnumValueException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.NONE)
public class EnumUtil {

    public static <T extends Enum<?>> T toEnum(Class<T> type, String enumValue) {
        if (enumValue == null) {
            return null;
        }
        for (T enumKey : type.getEnumConstants()) {
            if (StringUtils.equalsIgnoreCase(enumKey.name(), enumValue)) {
                return enumKey;
            }
        }
        throw new InvalidEnumValueException(enumValue, type);
    }
}
