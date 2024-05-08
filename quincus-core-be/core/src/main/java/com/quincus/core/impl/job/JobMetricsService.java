package com.quincus.core.impl.job;

import com.quincus.core.impl.exception.JobCancellationNotAllowedException;
import com.quincus.core.impl.exception.JobNotFoundException;
import com.quincus.core.impl.repository.JobMetricsRepository;
import com.quincus.core.impl.repository.entity.JobMetricsEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Transactional(readOnly = true)
@AllArgsConstructor
public class JobMetricsService<V> {

    private static final List<JobState> NON_CANCELABLE_JOB_STATE = Arrays.asList(JobState.CANCELLED, JobState.FAILED, JobState.COMPLETED);
    private static final String JOB_CANNOT_BE_CANCELLED_ERROR = "Cancellation of Job id `%s` is not allowed due to its current state `%s`";
    private static final String JOB_NOT_FOUND_ERROR = "No Job found for id: %s";
    private final JobMetricsRepository jobMetricsRepository;
    private final JobMetricsMapper<V> mapper;

    @Transactional
    public String createJob(JobMetrics<V> jobMetrics) {
        JobMetricsEntity jobEntity = mapper.mapToEntity(jobMetrics);
        return jobMetricsRepository.saveAndFlush(jobEntity).getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateJobMetrics(JobMetrics<V> jobMetrics) {
        JobMetricsEntity jobMetricsEntity = findJobMetricsEntityByIdAndOrgId(jobMetrics.getId(), jobMetrics.getOrganizationId());
        mapper.toEntityForUpdate(jobMetricsEntity, jobMetrics);
        jobMetricsRepository.saveAndFlush(jobMetricsEntity);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public JobMetricsEntity getJobMetricsByJobId(String jobId, String orgId) {
        return findJobMetricsEntityByIdAndOrgId(jobId, orgId);
    }

    @Transactional
    public void cancelJob(String jobId, String orgId) {
        JobMetricsEntity entity = findJobMetricsEntityByIdAndOrgId(jobId, orgId);
        if (NON_CANCELABLE_JOB_STATE.contains(entity.getStatus())) {
            // add also job is 90 percent to be able to cancel fully
            throw new JobCancellationNotAllowedException(String.format(JOB_CANNOT_BE_CANCELLED_ERROR, jobId, entity.getStatus()));
        }
        entity.setStatus(JobState.CANCELLED);
        jobMetricsRepository.saveAndFlush(entity);
    }

    public JobMetrics<V> findJobByIdAndOrgId(String jobId, String orgId) {
        return mapper.mapToDomain(findJobMetricsEntityByIdAndOrgId(jobId, orgId));
    }

    private JobMetricsEntity findJobMetricsEntityByIdAndOrgId(String jobId, String orgId) {
        return jobMetricsRepository.findByIdAndOrganizationId(jobId, orgId).orElseThrow(() -> new JobNotFoundException(String.format(JOB_NOT_FOUND_ERROR, jobId)));
    }

}
