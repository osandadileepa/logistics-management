package com.quincus.networkmanagement.impl.attachment.node.parser;

import com.quincus.networkmanagement.api.constant.TemplateFormat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NodeTemplateParserFactory {

    private final Map<TemplateFormat, NodeTemplateParser> templateParserMap;

    public NodeTemplateParserFactory(List<NodeTemplateParser> connectionTemplateParsers) {
        this.templateParserMap = connectionTemplateParsers.stream().collect(Collectors.toUnmodifiableMap(
                NodeTemplateParser::getTemplateFormat, Function.identity()
        ));
    }

    public NodeTemplateParser getParser(TemplateFormat format) {
        return templateParserMap.get(format);
    }
}
