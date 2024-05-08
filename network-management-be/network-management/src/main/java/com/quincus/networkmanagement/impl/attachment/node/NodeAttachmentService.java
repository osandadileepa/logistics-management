package com.quincus.networkmanagement.impl.attachment.node;

import com.quincus.networkmanagement.api.constant.AttachmentType;
import com.quincus.networkmanagement.api.constant.TemplateFormat;
import com.quincus.networkmanagement.impl.attachment.AbstractAttachmentService;
import com.quincus.networkmanagement.impl.attachment.JobTemplateStrategy;
import com.quincus.networkmanagement.impl.attachment.node.parser.NodeTemplateParser;
import com.quincus.networkmanagement.impl.attachment.node.parser.NodeTemplateParserFactory;
import com.quincus.networkmanagement.impl.mapper.JobMetricsMapper;
import com.quincus.networkmanagement.impl.service.JobMetricsService;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
public class NodeAttachmentService extends AbstractAttachmentService<NodeRecord> {
    private static final String TEMPLATE_FILE = "template/nodes-template.csv";

    private final NodeTemplateParserFactory nodeTemplateParserFactory;

    public NodeAttachmentService(JobMetricsService<NodeRecord> jobMetricsService,
                                 JobTemplateStrategy<NodeRecord> jobTemplateStrategy,
                                 JobMetricsMapper<NodeRecord> milestoneJobMetricsMapper,
                                 UserDetailsContextHolder userDetailsContextHolder,
                                 NodeTemplateParserFactory nodeTemplateParserFactory) {
        super(jobMetricsService, milestoneJobMetricsMapper, userDetailsContextHolder, jobTemplateStrategy);
        this.nodeTemplateParserFactory = nodeTemplateParserFactory;
    }

    @Override
    public List<NodeRecord> parseToDomain(MultipartFile file) {
        TemplateFormat format = TemplateFormat.getFormatByContentType(file.getContentType());
        NodeTemplateParser parser = nodeTemplateParserFactory.getParser(format);
        return parser.parseFile(file);
    }

    @Override
    public AttachmentType getAttachmentType() {
        return AttachmentType.NODES;
    }

    @Override
    public String getUploadFileTemplate() {
        return TEMPLATE_FILE;
    }
}
