package com.quincus.networkmanagement.impl.utility;

import com.opencsv.bean.CsvBindByName;
import com.quincus.networkmanagement.api.exception.InvalidTemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelImportUtility {
    private ExcelImportUtility() {
    }


    /**
     * Reflection is used to access non-public fields of a corresponding Record object
     * Fields are then populated using data parsed from the Excel template
     */
    @SuppressWarnings("squid:S3011")
    public static <T> List<T> sheetToRecords(Sheet sheet, Class<T> recordClass)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        List<T> result = new ArrayList<>();
        Map<Integer, String> headers = new HashMap<>();
        DataFormatter formatter = new DataFormatter();
        Row currentRow = sheet.getRow(sheet.getFirstRowNum());
        for (Cell cell : currentRow) {
            headers.put(cell.getColumnIndex(), cell.getStringCellValue());
        }

        Map<String, Field> fieldsMap = new HashMap<>();
        for (Field field : recordClass.getDeclaredFields()) {
            CsvBindByName ec = field.getAnnotation(CsvBindByName.class);
            if (ec != null) {
                field.setAccessible(true);
                fieldsMap.put(ec.column(), field);
            }
        }

        for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
            currentRow = sheet.getRow(i);

            if (currentRow == null) {
                throw new InvalidTemplateException(String.format("Missing row at index %s", i));
            }

            T bean = recordClass.getDeclaredConstructor().newInstance();

            for (Map.Entry<Integer, String> columnName : headers.entrySet()) {
                Cell cell = currentRow.getCell(columnName.getKey());

                String cellValue = null;
                if (cell != null) {
                    cellValue = getCellValueAsString(cell, formatter);
                }

                Field field = fieldsMap.get(columnName.getValue());
                if (field != null) {
                    field.set(bean, cellValue);
                }
            }

            result.add(bean);
        }

        return result;
    }

    /**
     * Formatter tries to interpret the cell value to String,
     * Results, however, may vary depending on machine
     */
    private static String getCellValueAsString(Cell cell, DataFormatter formatter) {
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return StringUtils.isNotBlank(formatter.formatCellValue(cell)) ? formatter.formatCellValue(cell).toUpperCase() : null;
        }
        return StringUtils.isNotBlank(formatter.formatCellValue(cell)) ? formatter.formatCellValue(cell) : null;
    }

}
