package com.quincus.networkmanagement.impl.attachment;

import com.quincus.networkmanagement.api.constant.AttachmentType;
import com.quincus.networkmanagement.api.domain.JobMetrics;
import com.quincus.networkmanagement.api.dto.JobStatusResponse;
import com.quincus.networkmanagement.impl.mapper.JobMetricsMapper;
import com.quincus.networkmanagement.impl.repository.entity.JobMetricsEntity;
import com.quincus.networkmanagement.impl.service.JobMetricsService;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
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
    protected final JobMetricsMapper<V> jobMetricsMapper;
    protected final UserDetailsContextHolder userDetailsContextHolder;
    private final JobTemplateStrategy<V> jobTemplateStrategy;

    @Transactional
    public String uploadFile(@NonNull MultipartFile file, boolean overwrite) {
        JobMetrics<V> jobMetrics = createJobMetrics(parseToDomain(file), overwrite);
        jobTemplateStrategy.process(jobMetrics);
        return jobMetrics.getId();
    }

    private JobMetrics<V> createJobMetrics(List<V> data, boolean overwrite) {
        String organizationId = userDetailsContextHolder.getCurrentOrganizationId();
        String userFullName = userDetailsContextHolder.getCurrentUserFullName();
        String userId = userDetailsContextHolder.getCurrentUserId();
        JobMetrics<V> jobMetrics = new JobMetrics<>(data, organizationId, userId, userFullName);
        jobMetrics.setId(jobMetricsService.createJob(jobMetrics));
        jobMetrics.setOverwrite(overwrite);
        return jobMetrics;
    }

    public JobStatusResponse getJobMetrics(@NonNull String jobId) {
        JobMetricsEntity jobMetricsEntity = jobMetricsService.getJobMetricsByJobId(jobId,
                userDetailsContextHolder.getCurrentOrganizationId());
        return jobMetricsMapper.toJobStatusResponse(jobMetricsEntity);
    }

    public JobStatusResponse cancelUpload(@NonNull String jobId) {
        jobMetricsService.cancelJob(jobId, userDetailsContextHolder.getCurrentOrganizationId());
        return new JobStatusResponse(jobId);
    }

    public abstract List<V> parseToDomain(MultipartFile multipartFile);

    public abstract AttachmentType getAttachmentType();

    public abstract String getUploadFileTemplate();
}
