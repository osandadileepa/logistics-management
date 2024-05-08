package com.quincus.networkmanagement.impl.attachment.node.parser;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.quincus.networkmanagement.api.constant.TemplateFormat;
import com.quincus.networkmanagement.api.exception.InvalidTemplateException;
import com.quincus.networkmanagement.impl.attachment.node.NodeRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Component
@Slf4j
public class NodeTemplateCSVParser implements NodeTemplateParser {

    @Override
    public List<NodeRecord> parseFile(MultipartFile file) {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<NodeRecord> csvToBean = new CsvToBeanBuilder<NodeRecord>(reader)
                    .withType(NodeRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSkipLines(1)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                    .build();
            return csvToBean.parse();
        } catch (Exception e) {
            log.warn("Error during parse: {}", e.getMessage());
            throw new InvalidTemplateException("Unable to parse the uploaded document");
        }
    }

    @Override
    public TemplateFormat getTemplateFormat() {
        return TemplateFormat.CSV;
    }
}
