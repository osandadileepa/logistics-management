package com.quincus.finance.costing.weightcalculation.impl.parser;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.quincus.finance.costing.weightcalculation.api.model.Conversion;
import com.quincus.finance.costing.weightcalculation.api.model.SpecialVolumeWeightRule;
import com.quincus.finance.costing.common.exception.CostingApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class SpecialVolumeWeightTemplateCSVParser implements SpecialVolumeWeightTemplateParser {

    private static final String ERR_CSV_PARSE_FAILED = "Failed to parse CSV file";

    @Override
    public SpecialVolumeWeightRule parseFile(MultipartFile file) {

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            CsvToBean<SpecialVolumeWeightTemplateCSVRecord> csvToBean = new CsvToBeanBuilder<SpecialVolumeWeightTemplateCSVRecord>(reader)
                    .withType(SpecialVolumeWeightTemplateCSVRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<SpecialVolumeWeightTemplateCSVRecord> records = csvToBean.parse();

            SpecialVolumeWeightRule response = new SpecialVolumeWeightRule();
            List<Conversion> conversionTable = new ArrayList<>();

            for (SpecialVolumeWeightTemplateCSVRecord row : records) {
                if (isValidRow(row)) {
                    conversionTable.add(new Conversion(
                            row.getFrom(),
                            row.getTo(),
                            row.getResult()
                    ));
                }
            }

            response.setCustomFormula(records.get(0).getFormula());
            response.setConversions(conversionTable);

            return response;

        } catch (Exception e) {
            log.warn("Error during parse: {}", e.getMessage());
            throw new CostingApiException(ERR_CSV_PARSE_FAILED, e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isValidRow(SpecialVolumeWeightTemplateCSVRecord row) {
        return !Objects.equals(row.getFrom(), BigDecimal.ZERO) && !Objects.equals(row.getTo(), BigDecimal.ZERO) && !Objects.equals(row.getResult(), BigDecimal.ZERO);
    }

    @Override
    public TemplateFormat getTemplateFormat() {
        return TemplateFormat.CSV;
    }
}
