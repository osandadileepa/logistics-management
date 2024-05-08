package com.quincus.networkmanagement.impl.attachment.connection.parser;

import com.quincus.networkmanagement.api.constant.TemplateFormat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ConnectionTemplateParserFactory {

    private final Map<TemplateFormat, ConnectionTemplateParser> templateParserMap;

    public ConnectionTemplateParserFactory(List<ConnectionTemplateParser> connectionTemplateParsers) {
        this.templateParserMap = connectionTemplateParsers.stream().collect(Collectors.toUnmodifiableMap(
                ConnectionTemplateParser::getTemplateFormat, Function.identity()
        ));
    }

    public ConnectionTemplateParser getParser(TemplateFormat format) {
        return templateParserMap.get(format);
    }
}
