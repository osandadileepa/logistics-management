package com.quincus.networkmanagement.impl.attachment.node.parser;

import com.quincus.networkmanagement.api.constant.TemplateFormat;
import com.quincus.networkmanagement.api.exception.InvalidTemplateException;
import com.quincus.networkmanagement.impl.attachment.node.NodeRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.util.List;

import static com.quincus.networkmanagement.impl.utility.ExcelImportUtility.sheetToRecords;

@Component
@Slf4j
public class NodeTemplateExcelParser implements NodeTemplateParser {

    @Override
    public List<NodeRecord> parseFile(MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(new BufferedInputStream(file.getInputStream()));
            Sheet sheet = workbook.getSheetAt(0);
            return sheetToRecords(sheet, NodeRecord.class);
        } catch (InvalidTemplateException e) {
            log.warn("Error during parse: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("Error during parse: {}", e.getMessage());
            throw new InvalidTemplateException("Unable to parse the uploaded document");
        }
    }

    @Override
    public TemplateFormat getTemplateFormat() {
        return TemplateFormat.XLS;
    }
}
