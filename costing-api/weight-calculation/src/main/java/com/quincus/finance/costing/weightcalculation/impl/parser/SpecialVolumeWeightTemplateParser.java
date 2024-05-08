package com.quincus.finance.costing.weightcalculation.impl.parser;

import com.quincus.finance.costing.weightcalculation.api.model.SpecialVolumeWeightRule;
import org.springframework.web.multipart.MultipartFile;

public interface SpecialVolumeWeightTemplateParser {
    SpecialVolumeWeightRule parseFile(MultipartFile multipartFile);

    TemplateFormat getTemplateFormat();
}
