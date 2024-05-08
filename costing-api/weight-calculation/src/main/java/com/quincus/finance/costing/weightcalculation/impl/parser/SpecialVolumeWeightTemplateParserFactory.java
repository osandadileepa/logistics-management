package com.quincus.finance.costing.weightcalculation.impl.parser;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SpecialVolumeWeightTemplateParserFactory {

    private final Map<TemplateFormat, SpecialVolumeWeightTemplateParser> templateParserMap;

    public SpecialVolumeWeightTemplateParserFactory(List<SpecialVolumeWeightTemplateParser> specialVolumeWeightTemplateParsers) {
        this.templateParserMap = specialVolumeWeightTemplateParsers.stream().collect(Collectors.toUnmodifiableMap(
                SpecialVolumeWeightTemplateParser::getTemplateFormat, Function.identity()
        ));
    }

    public SpecialVolumeWeightTemplateParser getParser(TemplateFormat format) {
        return templateParserMap.get(format);
    }
}
