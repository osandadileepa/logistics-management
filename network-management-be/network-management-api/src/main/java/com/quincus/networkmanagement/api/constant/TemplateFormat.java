package com.quincus.networkmanagement.api.constant;

import java.util.HashMap;
import java.util.Map;

public enum TemplateFormat {
    CSV,
    XLS;

    private static final Map<String, TemplateFormat> CONTENT_TYPE_FORMAT = new HashMap<>();

    static {
        CONTENT_TYPE_FORMAT.put("application/vnd.ms-excel", XLS);
        CONTENT_TYPE_FORMAT.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", XLS);
        CONTENT_TYPE_FORMAT.put("text/csv", CSV);
    }

    public static TemplateFormat getFormatByContentType(String contentType) {
        return CONTENT_TYPE_FORMAT.get(contentType);
    }
}
