package com.quincus.networkmanagement.impl.attachment.connection.parser;

import com.quincus.networkmanagement.api.constant.TemplateFormat;
import com.quincus.networkmanagement.impl.attachment.connection.ConnectionRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ConnectionTemplateParser {
    List<ConnectionRecord> parseFile(MultipartFile multipartFile);

    TemplateFormat getTemplateFormat();
}
