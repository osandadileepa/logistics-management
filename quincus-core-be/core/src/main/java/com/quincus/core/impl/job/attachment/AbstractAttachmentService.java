package com.quincus.core.impl.job.attachment;

import com.quincus.core.impl.job.JobMetrics;
import com.quincus.core.impl.job.JobMetricsMapper;
import com.quincus.core.impl.job.JobMetricsService;
import com.quincus.core.impl.job.JobStatusResponse;
import com.quincus.core.impl.job.JobTemplateStrategy;
import com.quincus.core.impl.repository.entity.JobMetricsEntity;
import com.quincus.core.impl.security.UserDetailsContextHolder;
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
public abstract class AbstractAttachmentService<V, T> {
    protected final JobMetricsService<V> jobMetricsService;
    protected final JobMetricsMapper<V> jobMetricsMapper;
    protected final UserDetailsContextHolder userDetailsContextHolder;
    private final JobTemplateStrategy<V> jobTemplateStrategy;

    @Transactional
    public String upload(@NonNull MultipartFile file) {
        JobMetrics<V> jobMetrics = createJobMetrics(parseToDomain(file));
        jobTemplateStrategy.process(jobMetrics);
        return jobMetrics.getId();
    }

    private JobMetrics<V> createJobMetrics(List<V> data) {
        String organizationId = userDetailsContextHolder.getCurrentOrganizationId();
        String userFullName = userDetailsContextHolder.getCurrentUserFullName();
        String userId = userDetailsContextHolder.getCurrentUserId();
        JobMetrics<V> jobMetrics = new JobMetrics<>(data, organizationId, userId, userFullName);
        jobMetrics.setId(jobMetricsService.createJob(jobMetrics));
        return jobMetrics;
    }

    public JobStatusResponse checkUploadStatus(@NonNull String jobId) {
        JobMetricsEntity jobMetricsEntity = jobMetricsService.getJobMetricsByJobId(jobId, userDetailsContextHolder.getCurrentOrganizationId());
        return jobMetricsMapper.toJobStatusResponse(jobMetricsEntity);
    }

    public JobStatusResponse cancelUpload(@NonNull String jobId) {
        jobMetricsService.cancelJob(jobId, userDetailsContextHolder.getCurrentOrganizationId());
        return new JobStatusResponse(jobId);
    }

    public abstract List<V> parseToDomain(MultipartFile multipartFile);

    public abstract T getAttachmentType();

    public abstract String getTemplate();
}