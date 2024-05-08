package com.quincus.finance.costing.weightcalculation.impl.service;

import com.quincus.finance.costing.weightcalculation.api.model.SpecialVolumeWeightRule;
import com.quincus.finance.costing.weightcalculation.impl.parser.SpecialVolumeWeightTemplateParser;
import com.quincus.finance.costing.weightcalculation.impl.parser.SpecialVolumeWeightTemplateParserFactory;
import com.quincus.finance.costing.weightcalculation.impl.parser.TemplateFormat;
import com.quincus.finance.costing.weightcalculation.impl.validator.SpecialVolumeWeightRuleValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Service
public class SpecialVolumeWeightRuleService {

    private final SpecialVolumeWeightRuleValidator specialVolumeWeightRuleValidator;
    private final SpecialVolumeWeightTemplateParserFactory specialVolumeWeightTemplateParserFactory;

    public SpecialVolumeWeightRule parseTemplate(MultipartFile file) {

        SpecialVolumeWeightTemplateParser parser =
                specialVolumeWeightTemplateParserFactory.getParser(getFormatByContentType(file.getContentType()));

        SpecialVolumeWeightRule result = parser.parseFile(file);
        result.setFileUploaded(file.getOriginalFilename());

        specialVolumeWeightRuleValidator.validate(result);

        return result;
    }

    private TemplateFormat getFormatByContentType(String contentType) {
        if ("application/vnd.ms-excel".equals(contentType) ||
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)) {
            return TemplateFormat.XLS;
        } else if ("text/csv".equals(contentType)) {
            return TemplateFormat.CSV;
        }
        return null;
    }

}
