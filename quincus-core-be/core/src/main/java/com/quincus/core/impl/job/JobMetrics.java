package com.quincus.core.impl.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Data
public class JobMetrics<V> {
    private static final float RECORD_PERCENTAGE_BEFORE_UPDATE = .10f;
    private final long countPercentageBeforeUpdate;
    private final long totalRecords;
    private final String organizationId;
    private final String executedById;
    private final String executedByName;
    private String id;
    @JsonIgnore
    private Instant timeStarted;
    private long timeElapsed;
    private long processedRecords;
    private long successfulRecords;
    private long failedRecords;
    private List<V> data;
    private List<V> dataWithError;
    private JobState status = JobState.QUEUE;
    private String failReason;
    private String recordClassType;
    @JsonIgnore
    private int mostRecordSubDataCount;

    public JobMetrics(List<V> data, String organizationId, String executedById, String executedByName) {
        this.data = data;
        this.totalRecords = data.size();
        this.organizationId = organizationId;
        this.countPercentageBeforeUpdate = computeCountPercentageBeforeUpdate(totalRecords);
        this.executedById = executedById;
        this.executedByName = executedByName;
    }

    public JobMetrics(long totalRecords, String organizationId, String executedById, String executedByName) {
        this.totalRecords = totalRecords;
        this.organizationId = organizationId;
        countPercentageBeforeUpdate = computeCountPercentageBeforeUpdate(totalRecords);
        this.executedById = executedById;
        this.executedByName = executedByName;
    }

    private long computeCountPercentageBeforeUpdate(long totalRecords) {
        long result = (long) (totalRecords * RECORD_PERCENTAGE_BEFORE_UPDATE);
        return result == 0 ? totalRecords : result;
    }

    public void incrementProcessRecords() {
        processedRecords++;
    }

    public void incrementFailedRecords() {
        failedRecords++;
    }

    public void incrementSuccessRecords() {
        successfulRecords++;
    }

    private boolean hasReachUpdatableRecords() {
        return processedRecords == totalRecords
                || processedRecords % countPercentageBeforeUpdate == 0;
    }

    public boolean isJobMetricsUpdatable() {
        return hasReachUpdatableRecords() || JobState.FAILED == this.status
                || JobState.COMPLETED == this.status;
    }

    public void startTime() {
        timeStarted = Instant.now(Clock.systemUTC());
    }

    public void calculateElapsedTime() {
        timeElapsed = Duration.between(timeStarted, Instant.now(Clock.systemUTC())).toMillis();
    }
}
