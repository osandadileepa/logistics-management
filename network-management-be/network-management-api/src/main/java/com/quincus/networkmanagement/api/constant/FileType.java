package com.quincus.networkmanagement.api.constant;

import java.util.HashMap;
import java.util.Map;

public enum FileType {
    CSV(".csv", "text/csv"),
    XLS(".xls", "application/vnd.ms-excel"),
    XLSX(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final String extension;
    private final String mimeType;

    private static final Map<String, FileType> FILE_TYPE_MAP = new HashMap<>();

    static {
        for (FileType fileType : FileType.values()) {
            FILE_TYPE_MAP.put(fileType.getMimeType(), fileType);
        }
    }

    FileType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static FileType fromMimeType(String mimeType) {
        return FILE_TYPE_MAP.get(mimeType);
    }
}
