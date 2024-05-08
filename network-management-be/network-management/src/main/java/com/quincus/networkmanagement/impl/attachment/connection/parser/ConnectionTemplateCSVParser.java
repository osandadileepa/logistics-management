package com.quincus.networkmanagement.impl.attachment.connection.parser;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.quincus.networkmanagement.api.constant.TemplateFormat;
import com.quincus.networkmanagement.api.exception.InvalidTemplateException;
import com.quincus.networkmanagement.impl.attachment.connection.ConnectionRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Component
@Slf4j
public class ConnectionTemplateCSVParser implements ConnectionTemplateParser {

    @Override
    public List<ConnectionRecord> parseFile(MultipartFile file) {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<ConnectionRecord> csvToBean = new CsvToBeanBuilder<ConnectionRecord>(reader)
                    .withType(ConnectionRecord.class)
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
