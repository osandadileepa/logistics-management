package com.quincus.shipment.kafka.producers.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quincus.ext.DateTimeUtil;
import com.quincus.shipment.api.domain.FlightStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.NONE)
public class MapperUtil {
    public static List<String> convertStringList(List<String> fromList) {
        if (CollectionUtils.isEmpty(fromList)) {
            return Collections.emptyList();
        }
        List<String> toList = new ArrayList<>(fromList.size());
        toList.addAll(fromList);
        return toList;
    }

    public static String parseTextFromJson(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        return jsonNode.asText();
    }

    public static BigDecimal parseBigDecimalFromJson(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        return jsonNode.decimalValue();
    }

    public static Boolean parseBooleanFromJson(JsonNode jsonNode) {
        if (jsonNode == null) {
            return false;
        }
        return jsonNode.asBoolean();
    }

    public static JsonNode readRawJson(String orderJsonText, ObjectMapper objectMapper) {
        JsonNode rawJson = null;
        try {
            rawJson = objectMapper.readTree(orderJsonText);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return rawJson;
    }

    public static JsonNode extractPartFromRawJson(String orderJsonText, String fieldName, ObjectMapper objectMapper) {
        JsonNode rawJson = readRawJson(orderJsonText, objectMapper);
        if (rawJson == null) {
            return null;
        }
        return rawJson.get(fieldName);
    }

    public static String getValueFromEnum(Enum<?> enumObj) {
        if (enumObj == null) {
            return null;
        }
        return enumObj.toString();
    }

    public static String getTimezoneFromScheduledTime(String scheduledTime) {
        if (StringUtils.isBlank(scheduledTime)) return null;
        return "UTC" + DateTimeUtil.parseZonedDateTime(scheduledTime).getOffset();
    }

    public static String getDepartureActualTime(List<FlightStatus> flightStatuses) {
        Optional<FlightStatus> status = flightStatuses.stream()
                .filter(flightStatus -> flightStatus != null && flightStatus.getDeparture() != null)
                .findAny();

        return status.map(flight -> flight.getDeparture().getActualTime()).orElse(null);
    }

    public static String getArrivalActualTime(List<FlightStatus> flightStatuses) {
        Optional<FlightStatus> status = flightStatuses.stream()
                .filter(flightStatus -> flightStatus != null && flightStatus.getArrival() != null)
                .findAny();

        return status.map(flight -> flight.getArrival().getActualTime()).orElse(null);
    }

    public static String getLatitude(List<FlightStatus> flightStatuses) {
        Optional<FlightStatus> status = flightStatuses.stream()
                .findAny();

        return status.map(FlightStatus::getLatitude).orElse(null);
    }

    public static String getLongitude(List<FlightStatus> flightStatuses) {
        Optional<FlightStatus> status = flightStatuses.stream()
                .findAny();

        return status.map(FlightStatus::getLongitude).orElse(null);
    }
}
