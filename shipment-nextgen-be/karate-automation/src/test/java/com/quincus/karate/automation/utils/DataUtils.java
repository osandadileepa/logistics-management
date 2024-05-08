package com.quincus.karate.automation.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.armeria.internal.shaded.guava.net.UrlEscapers;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class DataUtils {

    public static final String URL_SPACE_ENCODING = "%20";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DataUtils() {
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static long randomNumber() {
        long leftLimit = 10000000L;
        long rightLimit = 99999999L;
        return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
    }

    public static String getFormattedDateNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }

    public static String getFormattedDateTimeNow() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return now.format(formatter);
    }

    public static String getFormattedOffsetDateTimeNow() {
        OffsetDateTime now = OffsetDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");
        return now.format(formatter);
    }

    public static String getOffsetDateTimeMinusDays(int days) {
        return OffsetDateTime.now().minusDays(days).toString();
    }

    public static String getOffsetDateTimePlusDays(int days) {
        return OffsetDateTime.now().plusDays(days).toString();
    }

    public static String getDateBefore() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -2);
        return sdf.format(cal.getTime());
    }

    public static String toUpperCase(String value) {
        return value.toUpperCase();
    }

    public static String toLowerCase(String value) {
        return value.toLowerCase();
    }

    @SuppressWarnings("java:S2925")
    public static void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    public static String decodeUrl(String url) {
        return UrlEscapers.urlFragmentEscaper().escape(url);
    }

    public static List<String> toList(String... values) {
        return List.of(values);
    }

    public static List<String> getShipmentIdsFromCsv(String csvData) {
        return Arrays.stream(csvData.split("\\n"))
                .map(row -> row.split(",")[0])
                .skip(1).toList();
    }

    public static String modifyShipmentIdLabels(String orderMessageStr) {
        try {
            JsonNode orderMessage = OBJECT_MAPPER.readTree(orderMessageStr);
            JsonNode shipmentsNode = orderMessage.get("shipments");
            if (shipmentsNode.isArray()) {
                ArrayNode shipmentsArray = (ArrayNode) shipmentsNode;
                for (int i = 0; i < shipmentsArray.size(); i++) {
                    ObjectNode shipment = (ObjectNode) shipmentsArray.get(i);
                    shipment.put("shipment_id_label", "SHP" + uuid().substring(0, 12));
                }
            }
            return OBJECT_MAPPER.writeValueAsString(orderMessage);
        } catch (Exception e) {
            throw new RuntimeException("Error modifying shipments", e);
        }
    }
}
