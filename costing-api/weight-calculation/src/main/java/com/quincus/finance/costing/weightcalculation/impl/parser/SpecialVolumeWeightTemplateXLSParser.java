package com.quincus.finance.costing.weightcalculation.impl.parser;

import com.quincus.finance.costing.weightcalculation.api.model.Conversion;
import com.quincus.finance.costing.weightcalculation.api.model.SpecialVolumeWeightRule;
import com.quincus.finance.costing.common.exception.CostingApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class SpecialVolumeWeightTemplateXLSParser implements SpecialVolumeWeightTemplateParser {

    private static final int CUSTOM_FORMULA_ROW = 4;
    private static final int CUSTOM_FORMULA_COL = 1;
    private static final int CONVERSION_TABLE_START_ROW = 8;
    private static final int CONVERSION_TABLE_START_COL = 1;
    private static final String ERR_XLS_PARSE_FAILED = "Failed to parse XLS file";

    public SpecialVolumeWeightRule parseFile(MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(new BufferedInputStream(file.getInputStream()));
            Sheet sheet = workbook.getSheetAt(0);

            SpecialVolumeWeightRule result = new SpecialVolumeWeightRule();

            result.setCustomFormula(getCustomFormula(sheet));
            result.setConversions(getConversionTable(sheet));

            return result;
        } catch (Exception e) {
            log.warn("Error during parse: {}", e.getMessage());
            throw new CostingApiException(ERR_XLS_PARSE_FAILED, e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private String getCustomFormula(Sheet sheet) {
        return sheet.getRow(CUSTOM_FORMULA_ROW).getCell(CUSTOM_FORMULA_COL).getStringCellValue();
    }

    private List<Conversion> getConversionTable(Sheet sheet) {
        List<Conversion> conversionTable = new ArrayList<>();

        Iterator<Row> i = sheet.rowIterator();

        while (i.hasNext()) {

            Row row = i.next();

            if (row.getRowNum() >= CONVERSION_TABLE_START_ROW) {

                double from = row.getCell(CONVERSION_TABLE_START_COL).getNumericCellValue();
                double to = row.getCell(CONVERSION_TABLE_START_COL + 1).getNumericCellValue();
                double result = row.getCell(CONVERSION_TABLE_START_COL + 2).getNumericCellValue();

                if (from == 0.0 && to == 0.0 && result == 0.0) {
                    break;
                }

                conversionTable.add(
                        new Conversion(
                                BigDecimal.valueOf(from),
                                BigDecimal.valueOf(to),
                                BigDecimal.valueOf(result)
                        )
                );
            }

        }
        return conversionTable;
    }

    @Override
    public TemplateFormat getTemplateFormat() {
        return TemplateFormat.XLS;
    }

}
