package com.quincus.networkmanagement.impl.attachment;

import com.quincus.networkmanagement.api.constant.JobState;
import com.quincus.networkmanagement.api.domain.JobMetrics;
import com.quincus.networkmanagement.api.exception.JobCanceledException;
import com.quincus.networkmanagement.api.exception.JobRecordExecutionException;
import com.quincus.networkmanagement.impl.service.JobMetricsService;
import com.quincus.web.common.exception.model.QuincusException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public abstract class JobTemplateStrategy<V> {

    private static final String JOB_PROCESSING_ERROR_MESSAGE = "An unexpected error occurred while processing the Job id `%s` ";
    private static final String JOB_CANCELLED_ERROR_MESSAGE = "Job id `%s` was cancelled.";
    private static final String JOB_CANCELLED_WARNING_MESSAGE = "Job cancelled for Job id `{}`.";
    private static final String JOB_STARTED_PROCESSING_MESSAGE = "Started processing for Job id `{}`.";
    private static final String JOB_FINISHED_PROCESSING_MESSAGE = "Finished processing for Job id `{}`.";
    private static final String JOB_EXECUTION_EXCEPTION_MESSAGE = "Data issue found from jobId: `%s` on `%d` record with message `%s`";

    private final JobMetricsService<V> jobMetricsService;

    @Transactional
    @Async("threadPoolTaskExecutor")
    public void process(JobMetrics<V> jobMetrics) {
        if (CollectionUtils.isEmpty(jobMetrics.getData()) || StringUtils.isBlank(jobMetrics.getId())) {
            return;
        }
        List<V> dataWithError = new ArrayList<>();
        String jobId = jobMetrics.getId();
        try {
            log.info(JOB_STARTED_PROCESSING_MESSAGE, jobId);
            processRecords(jobMetrics, dataWithError);
        } catch (JobCanceledException e) {
            handleCancelledJob(jobMetrics, e);
        } catch (Exception e) {
            handleFailedJob(jobMetrics, jobId, e);
        } finally {
            log.info(JOB_FINISHED_PROCESSING_MESSAGE, jobId);
        }
    }

    protected void preRecordProcess(JobMetrics<V> jobMetrics) {
        jobMetrics.startTime();
        jobMetrics.setStatus(JobState.IN_PROGRESS);
    }

    private void processRecords(JobMetrics<V> jobMetrics, List<V> dataWithError) {
        preRecordProcess(jobMetrics);
        jobMetrics.getData().forEach(data -> processSingleRecord(jobMetrics, data, dataWithError));
        postRecordProcess(jobMetrics, dataWithError);
        updateJobMetricsOnBatchProcessing(jobMetrics);
    }

    private void handleCancelledJob(JobMetrics<V> jobMetrics, JobCanceledException e) {
        log.info(e.getMessage(), e);
        jobMetrics.calculateElapsedTime();
        throw e;
    }

    private void handleFailedJob(JobMetrics<V> jobMetrics, String jobId, Exception e) {
        jobMetrics.incrementFailedRecords();
        jobMetrics.setStatus(JobState.FAILED);
        jobMetrics.setFailReason(e.getMessage());
        jobMetrics.calculateElapsedTime();
        updateJobMetricsOnBatchProcessing(jobMetrics);
        String errorMessage = String.format(JOB_PROCESSING_ERROR_MESSAGE, jobId);
        log.warn(errorMessage + "with error message : `{}`", e.getMessage(), e);
        throw new QuincusException(errorMessage, e);
    }

    private void processSingleRecord(JobMetrics<V> jobMetrics, V dataToProcess, List<V> dataWithError) {
        try {
            updateJobMetricsOnBatchProcessing(jobMetrics);
            execute(dataToProcess, jobMetrics.isOverwrite());
            jobMetrics.incrementSuccessRecords();
        } catch (JobRecordExecutionException jobRecordExecutionException) {
            handleJobRecordExecutionException(jobRecordExecutionException, jobMetrics, dataToProcess, dataWithError);
        } finally {
            jobMetrics.incrementProcessRecords();
        }
    }

    protected void handleJobRecordExecutionException(JobRecordExecutionException jobRecordExecutionException, JobMetrics<V> jobMetrics, V dataToProcess, List<V> dataWithError) {
        jobMetrics.incrementFailedRecords();
        dataWithError.add(dataToProcess);
        if (log.isDebugEnabled()) {
            log.debug(String.format(JOB_EXECUTION_EXCEPTION_MESSAGE, jobMetrics.getId(), jobMetrics.getProcessedRecords(), jobRecordExecutionException.getMessage()));
        }
    }

    private void updateJobMetricsOnBatchProcessing(JobMetrics<V> jobMetrics) {
        if (!jobMetrics.isJobMetricsUpdatable()) {
            return;
        }
        if (JobState.CANCELLED == jobMetricsService.getJobMetricsByJobId(jobMetrics.getId(), jobMetrics.getOrganizationId()).getStatus()) {
            log.warn(JOB_CANCELLED_WARNING_MESSAGE, jobMetrics.getId());
            throw new JobCanceledException(String.format(JOB_CANCELLED_ERROR_MESSAGE, jobMetrics.getId()));
        }
        jobMetricsService.updateJobMetrics(jobMetrics);
    }

    protected void postRecordProcess(JobMetrics<V> jobMetrics, List<V> dataWithError) {
        jobMetrics.setStatus(JobState.COMPLETED);
        jobMetrics.setDataWithError(dataWithError);
        jobMetrics.setRecordClassType(jobMetrics.getData().get(0).getClass().getName());
        jobMetrics.calculateElapsedTime();
    }

    public abstract void execute(V data, boolean overwrite) throws JobRecordExecutionException;
}
