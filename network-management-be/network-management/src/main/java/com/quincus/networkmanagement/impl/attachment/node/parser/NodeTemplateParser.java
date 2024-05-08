package com.quincus.networkmanagement.impl.attachment.node.parser;

import com.quincus.networkmanagement.api.constant.TemplateFormat;
import com.quincus.networkmanagement.impl.attachment.node.NodeRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NodeTemplateParser {
    List<NodeRecord> parseFile(MultipartFile multipartFile);

    TemplateFormat getTemplateFormat();
}
