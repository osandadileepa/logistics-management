package com.quincus.shipment.impl.attachment;

import com.quincus.shipment.api.constant.AttachmentType;
import com.quincus.shipment.api.domain.JobMetrics;
import com.quincus.shipment.api.dto.JobStatusResponse;
import com.quincus.shipment.impl.mapper.JobMetricsMapper;
import com.quincus.shipment.impl.repository.entity.JobMetricsEntity;
import com.quincus.shipment.impl.resolver.UserDetailsProvider;
import com.quincus.shipment.impl.service.JobMetricsService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public abstract class AbstractAttachmentService<V> {

    protected final JobMetricsService<V> jobMetricsService;
    private final JobTemplateStrategy<V> jobTemplateStrategy;
    protected final JobMetricsMapper<V> jobMetricsMapper;
    protected final UserDetailsProvider userDetailsProvider;

    @Transactional
    public String uploadCsv(@NonNull MultipartFile file) {
        JobMetrics<V> jobMetrics = createJobMetrics(parseToDomain(file));
        jobTemplateStrategy.process(jobMetrics);
        return jobMetrics.getId();
    }

    private JobMetrics<V> createJobMetrics(List<V> data) {
        String organizationId = userDetailsProvider.getCurrentOrganizationId();
        String userFullName = userDetailsProvider.getCurrentUserFullName();
        String userId = userDetailsProvider.getCurrentUserId();
        JobMetrics<V> jobMetrics = new JobMetrics<>(data, organizationId, userId, userFullName);
        jobMetrics.setId(jobMetricsService.createJob(jobMetrics));
        return jobMetrics;
    }

    public JobStatusResponse checkUploadStatus(@NonNull String jobId) {
        JobMetricsEntity jobMetricsEntity = jobMetricsService.getJobMetricsByJobId(jobId, userDetailsProvider.getCurrentOrganizationId());
        return jobMetricsMapper.toJobStatusResponse(jobMetricsEntity);
    }

    public JobStatusResponse cancelUpload(@NonNull String jobId) {
        jobMetricsService.cancelJob(jobId, userDetailsProvider.getCurrentOrganizationId());
        return new JobStatusResponse(jobId);
    }

    public abstract List<V> parseToDomain(MultipartFile multipartFile);

    public abstract AttachmentType getAttachmentType();

    public abstract String getCsvTemplate();
}