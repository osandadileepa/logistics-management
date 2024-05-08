package com.quincus.networkmanagement.impl.attachment.connection;

import com.quincus.networkmanagement.api.constant.AttachmentType;
import com.quincus.networkmanagement.api.constant.TemplateFormat;
import com.quincus.networkmanagement.impl.attachment.AbstractAttachmentService;
import com.quincus.networkmanagement.impl.attachment.JobTemplateStrategy;
import com.quincus.networkmanagement.impl.attachment.connection.parser.ConnectionTemplateParser;
import com.quincus.networkmanagement.impl.attachment.connection.parser.ConnectionTemplateParserFactory;
import com.quincus.networkmanagement.impl.mapper.JobMetricsMapper;
import com.quincus.networkmanagement.impl.service.JobMetricsService;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
public class ConnectionAttachmentService extends AbstractAttachmentService<ConnectionRecord> {
    private static final String TEMPLATE_FILE = "template/connections-template.csv";

    private final ConnectionTemplateParserFactory connectionTemplateParserFactory;

    public ConnectionAttachmentService(JobMetricsService<ConnectionRecord> jobMetricsService,
                                       JobTemplateStrategy<ConnectionRecord> jobTemplateStrategy,
                                       JobMetricsMapper<ConnectionRecord> milestoneJobMetricsMapper,
                                       UserDetailsContextHolder userDetailsContextHolder,
                                       ConnectionTemplateParserFactory connectionTemplateParserFactory) {
        super(jobMetricsService, milestoneJobMetricsMapper, userDetailsContextHolder, jobTemplateStrategy);
        this.connectionTemplateParserFactory = connectionTemplateParserFactory;
    }

    @Override
    public List<ConnectionRecord> parseToDomain(MultipartFile file) {
        TemplateFormat format = TemplateFormat.getFormatByContentType(file.getContentType());
        ConnectionTemplateParser parser = connectionTemplateParserFactory.getParser(format);
        return parser.parseFile(file);
    }

    @Override
    public AttachmentType getAttachmentType() {
        return AttachmentType.CONNECTIONS;
    }

    @Override
    public String getUploadFileTemplate() {
        return TEMPLATE_FILE;
    }
}
