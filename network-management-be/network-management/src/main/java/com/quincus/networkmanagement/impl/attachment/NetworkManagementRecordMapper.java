package com.quincus.networkmanagement.impl.attachment;

import com.quincus.networkmanagement.api.constant.CapacityUnit;
import com.quincus.networkmanagement.api.constant.DimensionUnit;
import com.quincus.networkmanagement.api.constant.NodeType;
import com.quincus.networkmanagement.api.constant.TimeUnit;
import com.quincus.networkmanagement.api.constant.TransportType;
import com.quincus.networkmanagement.api.constant.VolumeUnit;
import com.quincus.networkmanagement.api.constant.WeightUnit;
import com.quincus.networkmanagement.api.exception.RecordMappingException;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.MapperConfig;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@MapperConfig
public interface NetworkManagementRecordMapper {

    Set<String> TRUE_VALUES = Set.of("true", "yes", "y");
    Set<String> FALSE_VALUES = Set.of("false", "no", "n");

    List<DateTimeFormatter> SUPPORTED_TIME_FORMATS = List.of(
            DateTimeFormatter.ofPattern("h:mm:ss a", Locale.US),
            DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US),
            DateTimeFormatter.ofPattern("h:mm a", Locale.US),
            DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
            DateTimeFormatter.ofPattern("H:mm:ss", Locale.US),
            DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US),
            DateTimeFormatter.ofPattern("H:mm", Locale.US),
            DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    );

    default String mapListToString(List<String> stringList) {
        return String.join(", ", stringList);
    }

    default String mapLocalTimeToString(LocalTime time) {
        return Optional.ofNullable(time)
                .map(t -> t.format(SUPPORTED_TIME_FORMATS.get(0)))
                .orElse(null);
    }

    default List<String> toListOfString(String value) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        String[] values = value.split("\\s*,\\s*");
        return Arrays.asList(values);
    }

    default LocalTime toLocalTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        for (DateTimeFormatter format : SUPPORTED_TIME_FORMATS) {
            try {
                return LocalTime.parse(value.toUpperCase(), format);
            } catch (DateTimeParseException ignored) {
                // try another format
            }
        }
        throw new RecordMappingException(String.format("'%s' is not a supported time format", value));
    }

    default BigDecimal toBigDecimal(String value) {
        try {
            if (StringUtils.isBlank(value)) {
                return null;
            }
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new RecordMappingException(String.format("'%s' is an invalid decimal value", value));
        }
    }

    default Integer toInteger(String value) {
        try {
            if (StringUtils.isBlank(value)) {
                return null;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RecordMappingException(String.format("'%s' is an invalid integer value", value));
        }
    }


    default boolean toBoolean(String value) {
        String lowerCasedValue = value.toLowerCase();
        if (TRUE_VALUES.contains(lowerCasedValue)) {
            return true;
        } else if (FALSE_VALUES.contains(lowerCasedValue)) {
            return false;
        }
        throw new RecordMappingException(String.format("'%s' is an invalid boolean value", value));
    }

    default TransportType toTransportType(String value) {
        return TransportType.fromValue(value);
    }

    default DimensionUnit toDimensionUnit(String value) {
        return DimensionUnit.fromValue(value);
    }

    default WeightUnit toWeightUnit(String value) {
        return WeightUnit.fromValue(value);
    }

    default VolumeUnit toVolumeUnit(String value) {
        return VolumeUnit.fromValue(value);
    }

    default NodeType toNodeType(String value) {
        return NodeType.fromValue(value);
    }

    default CapacityUnit toCapacityUnit(String value) {
        return CapacityUnit.fromValue(value);
    }

    default TimeUnit toTimeUnit(String value) {
        return TimeUnit.fromValue(value);
    }
}
