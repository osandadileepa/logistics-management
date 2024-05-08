package com.quincus.networkmanagement.impl.utility;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.quincus.networkmanagement.impl.attachment.connection.ConnectionRecord;
import com.quincus.networkmanagement.impl.attachment.node.NodeRecord;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public class CsvExportUtility {

    private CsvExportUtility() {
    }

    public static String buildNodesHeader() {
        return Arrays.stream(NodeRecord.class.getDeclaredFields())
                .filter(f -> f.getAnnotation(CsvBindByPosition.class) != null && f.getAnnotation(CsvBindByName.class) != null)
                .sorted(Comparator.comparing(f -> f.getAnnotation(CsvBindByPosition.class).position()))
                .map(f -> "\"" + f.getAnnotation(CsvBindByName.class).column() + "\"")
                .collect(Collectors.joining(",")) + "\n";
    }

    public static String buildConnectionsHeader() {
        return Arrays.stream(ConnectionRecord.class.getDeclaredFields())
                .filter(f -> f.getAnnotation(CsvBindByPosition.class) != null && f.getAnnotation(CsvBindByName.class) != null)
                .sorted(Comparator.comparing(f -> f.getAnnotation(CsvBindByPosition.class).position()))
                .map(f -> "\"" + f.getAnnotation(CsvBindByName.class).column() + "\"")
                .collect(Collectors.joining(",")) + "\n";
    }

}
